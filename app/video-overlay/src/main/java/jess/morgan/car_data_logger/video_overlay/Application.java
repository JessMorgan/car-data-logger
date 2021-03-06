package jess.morgan.car_data_logger.video_overlay;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import jess.morgan.car_data_logger.plugin.PluginManager;
import jess.morgan.car_data_logger.video_overlay.gauge.Gauge;
import jess.morgan.car_data_logger.video_overlay.gauge.GaugeFactory;

public class Application {
	public static void main(String[] args) throws IOException {
		File inFile = new File(args[0]);

		Config config = new Config();
		PluginManager pluginManager = new PluginManager(config.getPluginDirectories());

		Map<GaugeInfo, Gauge> gauges = new LinkedHashMap<GaugeInfo, Gauge>();
		for(GaugeInfo gaugeInfo : config.getGauges()) {
			try {
				Gauge gauge = pluginManager.loadPlugin(
						gaugeInfo.getFactoryClass(),
						gaugeInfo.getConfig(),
						GaugeFactory.class
						);
				if(gauge == null) {
					System.err.println("Gauge plugin not found: " + gaugeInfo.getFactoryClass());
					return;
				}
				gauges.put(gaugeInfo, gauge);
			} catch (Exception e) {
				System.err.println("Error loading gauge plugin: " + gaugeInfo.getFactoryClass());
				e.printStackTrace();
				return;
			}
		}

		// Count frames
		String framesSrcPath  = config.getVideoFramesSrcPath();
		String framesDestPath = config.getVideoFramesDestPath();
		int frameCount = 0;
		while(new File(String.format(framesSrcPath, frameCount + 1)).isFile()) {
			frameCount++;
		}

		Thread statsThread = null;
		LinkedBlockingQueue<Long> statsQueue = null;
		if(config.getStatDisplayFrequency() > 0) {
			statsQueue = new LinkedBlockingQueue<Long>();
			statsThread = new Thread(new StatisticsThread(frameCount, config.getStatDisplayFrequency(), statsQueue));
			statsThread.start();
		}

		boolean rescale = config.isRescaleDataToVideo();
		long lastTimestamp = 0;
		if(rescale) {
			BufferedReader br = new BufferedReader(new FileReader(inFile));
			try {
				String[] paramNames = br.readLine().split(",");
				String line = br.readLine();
				Map<String, String> data = parseLine(line, paramNames);
				String lastLine = line;
				while(null != (line = br.readLine())) {
					lastLine = line;
				}
				data = parseLine(lastLine, paramNames);
				lastTimestamp = Long.parseLong(data.get("Timestamp"));
			} finally {
				br.close();
			}
		}

		// Open data file and read headers
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		String[] paramNames = br.readLine().split(",");
		String[] units = br.readLine().split(",");
		Map<String, String> data = parseLine(br.readLine(), paramNames);

		// Create thread pool
		ExecutorService executor = Executors.newFixedThreadPool(config.getThreads());

		// Loop through frames
		long firstTimestamp = Long.parseLong(data.get("Timestamp"));
		long offset = config.getVideoOffsetSeconds().movePointRight(9).longValue();
		BigDecimal fps = config.getVideoFPS();
		long videoLength = BigDecimal.valueOf(frameCount).movePointRight(9).divideToIntegralValue(fps).longValue();
		long currentTimestamp = scaleTimestamp(firstTimestamp, rescale, firstTimestamp, lastTimestamp, videoLength);
		long previousTimestamp = currentTimestamp;
		Map<String, String> lastData = data;
		Map<String, String> currentData = data;
		for(int frame = 1; frame <= frameCount; frame++) {
			long targetTimestamp = offset
					+ new BigDecimal(frame - 1).movePointRight(9).divideToIntegralValue(fps).longValue();
			// Fast forward in the data to the target timestamp
			while(currentTimestamp < targetTimestamp) {
				previousTimestamp = currentTimestamp;
				lastData = currentData;
				currentData = parseLine(br.readLine(), paramNames);
				currentTimestamp = scaleTimestamp(
						Long.parseLong(currentData.get("Timestamp")),
						rescale,
						firstTimestamp,
						lastTimestamp,
						videoLength);
			}
			// Figure out which is closer to the target: currentTimestamp or lastTimestamp.  Use whichever data is closer.
			long currentDistance = Math.abs(currentTimestamp - targetTimestamp);
			long lastDistance = Math.abs(targetTimestamp - previousTimestamp);
			data = (currentDistance <= lastDistance) ? currentData : lastData;
			// Run in the threadpool
			executor.submit(
					new DrawFrameThread(
							String.format(framesSrcPath, frame),
							String.format(framesDestPath, frame),
							config.getImageOutputFormat(),
							gauges,
							data,
							statsQueue));
		}
		executor.shutdown();
		try {
			boolean keepWaiting = true;
			do {
				keepWaiting = !executor.awaitTermination(1, TimeUnit.MINUTES);
				if(keepWaiting) {
					if(config.getStatDisplayFrequency() <= 0) {
						System.out.println("Still waiting for execution to complete...");
					}
				}
			} while(keepWaiting);
		} catch (InterruptedException e) {
			System.err.println("Interrupted - exiting");
		}
		if(statsThread != null && statsThread.isAlive()) {
			statsThread.interrupt();
		}
	}

	private static class StatisticsThread implements Runnable {
		private final LinkedBlockingQueue<Long> queue;
		private final Statistics stats;

		public StatisticsThread(int frameCount, int statDisplayFrequency, LinkedBlockingQueue<Long> queue) {
			this.stats = new Statistics(frameCount, statDisplayFrequency);
			this.queue = queue;
		}

		@Override
		public void run() {
			while(true) {
				try {
					Long value = queue.take();
					stats.addStatistic(value);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

	private static class Statistics {
		private final int frameCount;
		private final int displayFrequency;
		private int framesReturned = 0;
		private long totalTime = 0;
		private long minTime = Long.MAX_VALUE;
		private long maxTime = Long.MIN_VALUE;

		public Statistics(int frameCount, int displayFrequency) {
			this.frameCount = frameCount;
			this.displayFrequency = displayFrequency;
		}

		public void addStatistic(long milliseconds) {
			framesReturned ++;
			totalTime += milliseconds;
			if(milliseconds > maxTime) {
				maxTime = milliseconds;
			}
			if(milliseconds < minTime) {
				minTime = milliseconds;
			}
			if(displayFrequency > 0 && (framesReturned % displayFrequency) == 0) {
				System.out.println(toString());
			}
		}

		public String toString() {
			return String.format("Frames complete: %d / %d, avg: %d, min: %d, max: %d", framesReturned, frameCount, totalTime / framesReturned, minTime, maxTime);
		}
	}

	private static class DrawFrameThread implements Callable<Void> {
		private final String inFilename;
		private final String outFilename;
		private final String imageFormat;
		private final Map<GaugeInfo, Gauge> gauges;
		private final Map<String, String> data;
		private final LinkedBlockingQueue<Long> statsQueue;

		public DrawFrameThread(String inFilename, String outFilename, String imageFormat, Map<GaugeInfo, Gauge> gauges, Map<String, String> data, LinkedBlockingQueue<Long> statsQueue) {
			this.inFilename = inFilename;
			this.outFilename = outFilename;
			this.imageFormat = imageFormat;
			this.gauges = new LinkedHashMap<GaugeInfo, Gauge>(gauges);
			this.data = new LinkedHashMap<String, String>(data);
			this.statsQueue = statsQueue;
		}

		@Override
		public Void call() throws Exception {
			long startTime = System.currentTimeMillis();

			// Load the source frame
			BufferedImage image = ImageIO.read(new File(inFilename));
			// Draw data over the source frame
			drawFrame(gauges, data, image);
			// Save the new file
			ImageIO.write(image, imageFormat, new File(outFilename));

			if(statsQueue != null) {
				statsQueue.offer(System.currentTimeMillis() - startTime);
			}
			return null;
		}
	}

	private static void drawFrame(Map<GaugeInfo, Gauge> gauges, Map<String, String> data, BufferedImage image) {
		Graphics2D graphics = (Graphics2D) image.getGraphics();

		Color           oldBackgroundColor = graphics.getBackground();
		Color           oldColor           = graphics.getColor();
		Composite       oldComposite       = graphics.getComposite();
		Font            oldFont            = graphics.getFont();
		Paint           oldPaint           = graphics.getPaint();
		Stroke          oldStroke          = graphics.getStroke();
		AffineTransform oldTransform       = graphics.getTransform();

		for(Map.Entry<GaugeInfo, Gauge> entry : gauges.entrySet()) {
			graphics.setBackground(oldBackgroundColor);
			graphics.setColor(oldColor);
			graphics.setComposite(oldComposite);
			graphics.setFont(oldFont);
			graphics.setPaint(oldPaint);
			graphics.setStroke(oldStroke);
			graphics.setTransform(oldTransform);

			GaugeInfo info = entry.getKey();
			graphics.setClip(
					info.getX(),
					info.getY(),
					info.getWidth() + 1,
					info.getHeight() + 1
					);
			entry.getValue().draw(
					data,
					graphics,
					info.getX(),
					info.getY(),
					info.getWidth(),
					info.getHeight()
					);
		}
	}

	private static long scaleTimestamp(
			long currentTimestamp,
			boolean rescaleTimestamps,
			long firstTimestamp,
			long lastTimestamp,
			long videoLength) {
		if(!rescaleTimestamps) {
			return currentTimestamp - firstTimestamp;
		}
		return (long)(((double)(currentTimestamp - firstTimestamp) * videoLength) / (lastTimestamp - firstTimestamp));
	}

	private static Map<String, String> parseLine(String line, String[] paramNames) {
		if(line == null) {
			return null;
		}
		Map<String, String> data = new HashMap<String, String>();
		String[] values = line.split(",");
		int length = Math.min(values.length, paramNames.length);
		for(int i = 0; i < length; i++) {
			data.put(paramNames[i], values[i]);
		}
		return data;
	}
}
