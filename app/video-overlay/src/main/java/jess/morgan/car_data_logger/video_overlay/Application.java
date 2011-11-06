package jess.morgan.car_data_logger.video_overlay;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
		while(new File(String.format(framesSrcPath, frameCount)).isFile()) {
			frameCount++;
		};

		// Open data file and read headers
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		String[] paramNames = br.readLine().split(",");
		String[] units = br.readLine().split(",");
		Map<String, String> data = readLine(br, paramNames);

		// Loop through frames
		long firstTimestamp = Long.parseLong(data.get("Timestamp"));
		long offset = config.getVideoOffsetSeconds().movePointRight(9).longValue();
		BigDecimal fps = config.getVideoFPS();
		long currentTimestamp = firstTimestamp;
		long lastTimestamp = currentTimestamp;
		Map<String, String> lastData = data;
		Map<String, String> currentData = data;
		for(int frame = 1; frame <= frameCount; frame++) {
			long targetTimestamp = firstTimestamp + offset
					+ new BigDecimal(frame - 1).movePointRight(9).divideToIntegralValue(fps).longValue();
			// Fast forward in the data to the target timestamp
			while(currentTimestamp < targetTimestamp) {
				lastTimestamp = currentTimestamp;
				lastData = currentData;
				currentData = readLine(br, paramNames);
				currentTimestamp = Long.parseLong(currentData.get("Timestamp"));
			}
			// Figure out which is closer to the target: currentTimestamp or lastTimestamp.  Use whichever data is closer.
			long currentDistance = Math.abs(currentTimestamp - targetTimestamp);
			long lastDistance = Math.abs(targetTimestamp - lastTimestamp);
			data = (currentDistance <= lastDistance) ? currentData : lastData;
			// Load the source frame
			BufferedImage image = ImageIO.read(new File(String.format(framesSrcPath, frame)));
			// Draw data over the source frame
			Graphics2D graphics = (Graphics2D) image.getGraphics();
			for(Map.Entry<GaugeInfo, Gauge> entry : gauges.entrySet()) {
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
			// Save the new file
			ImageIO.write(image, "png", new File(String.format(framesDestPath, frame)));
		}
	}

	private static Map<String, String> readLine(BufferedReader br, String[] paramNames) throws IOException {
		String line = br.readLine();
		if(line == null) {
			return null;
		}
		Map<String, String> data = new HashMap<String, String>();
		String[] values = line.split(",");
		for(int i = 0; i < values.length; i++) {
			data.put(paramNames[i], values[i]);
		}
		return data;
	}
}
