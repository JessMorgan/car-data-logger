package jess.morgan.car_data_logger.video_overlay;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Config {
	private static final File configFile = new File("config.properties");
	private List<File> pluginDirectories;
	private String videoFramesSrcPath;
	private String videoFramesDestPath;
	private BigDecimal videoFPS;
	private BigDecimal videoOffsetSeconds;
	private String imageOutputFormat;
	private int threads;
	private int statDisplayFrequency;
	private List<GaugeInfo> gauges;
	private boolean rescaleDataToVideo;

	public Config() throws IOException {
		loadConfigFile();
	}

	public void loadConfigFile() throws IOException {
		Properties properties;
		InputStream is = new FileInputStream(configFile);
		try {
			properties = new Properties();
			properties.load(is);
		} finally {
			is.close();
		}

		pluginDirectories = new ArrayList<File>();
		for(int i = 1; ; i++) {
			String path = properties.getProperty("plugin.path." + i);
			if(path == null) {
				if(pluginDirectories.isEmpty()) {
					throw new IOException("Invalid properties file (property missing: 'plugin.path." + i + "')");
				}
				break;
			}
			pluginDirectories.add(new File(path));
		}

		videoFramesSrcPath  = getRequiredValue(properties, "video.frames.src.path");
		videoFramesDestPath = getRequiredValue(properties, "video.frames.dest.path");
		videoFPS            = new BigDecimal(getRequiredValue(properties, "video.fps"));
		videoOffsetSeconds  = new BigDecimal(getRequiredValue(properties, "video.offset.seconds"));
		rescaleDataToVideo  = Boolean.parseBoolean(properties.getProperty("video.rescale.data", "false"));
		imageOutputFormat   = properties.getProperty("video.frames.dest.format", "jpg");
		threads             = Integer.parseInt(properties.getProperty("threads", "2"));
		statDisplayFrequency= Integer.parseInt(properties.getProperty("stat.display.frequency", "0"));

		gauges = new ArrayList<GaugeInfo>();
		for(int i = 1; ; i++) {
			String gauge = properties.getProperty("gauge." + i);
			if(gauge == null) {
				if(gauges.isEmpty()) {
					throw new IOException("Invalid properties file (property missing: 'gauge." + i + "')");
				}
				break;
			}

			// Load position for this gauge
			String position = getRequiredValue(properties, "gauge." + i + ".position");
			String[] parts = position.split(",");
			if(parts.length != 4) {
				throw new IOException("Illegal format for gauge position (expecting x,y,width,height, received '" + position + "')");
			}
			// Parse position values
			int x      = Integer.parseInt(parts[0]);
			int y      = Integer.parseInt(parts[1]);
			int width  = Integer.parseInt(parts[2]);
			int height = Integer.parseInt(parts[3]);

			// Load config for this gauge
			Map<String, String> config = new HashMap<String, String>();
			for(int j = 1; ; j++) {
				String key   = properties.getProperty("gauge." + i + ".config." + j + ".key");
				String value = properties.getProperty("gauge." + i + ".config." + j + ".value");
				if(key == null) {
					break;
				}
				config.put(key, value);
			}
			gauges.add(new GaugeInfo(gauge, x, y, width, height, config));
		}
	}

	private String getRequiredValue(Properties properties, String key) throws IOException {
		String value = properties.getProperty(key);
		if(value == null) {
			throw new IOException("Invalid properties file (property missing: '" + key + "')");
		}
		return value;
	}

	public void saveConfigFile() {
		// TODO
	}

	public List<File> getPluginDirectories() {
		return Collections.unmodifiableList(pluginDirectories);
	}

	public void setPluginDirectories(List<File> pluginDirectories) {
		this.pluginDirectories = new ArrayList<File>(pluginDirectories);
	}

	public String getVideoFramesSrcPath() {
		return videoFramesSrcPath;
	}

	public void setVideoFramesSrcPath(String videoFramesSrcPath) {
		this.videoFramesSrcPath = videoFramesSrcPath;
	}

	public String getVideoFramesDestPath() {
		return videoFramesDestPath;
	}

	public void setVideoFramesDestPath(String videoFramesDestPath) {
		this.videoFramesDestPath = videoFramesDestPath;
	}

	public BigDecimal getVideoFPS() {
		return videoFPS;
	}

	public void setVideoFPS(BigDecimal videoFPS) {
		this.videoFPS = videoFPS;
	}

	public BigDecimal getVideoOffsetSeconds() {
		return videoOffsetSeconds;
	}

	public void setVideoOffsetSeconds(BigDecimal videoOffsetSeconds) {
		this.videoOffsetSeconds = videoOffsetSeconds;
	}

	public String getImageOutputFormat() {
		return imageOutputFormat;
	}

	public void setImageOutputFormat(String imageOutputFormat) {
		this.imageOutputFormat = imageOutputFormat;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public int getStatDisplayFrequency() {
		return statDisplayFrequency;
	}

	public void setStatDisplayFrequency(int statDisplayFrequency) {
		this.statDisplayFrequency = statDisplayFrequency;
	}

	public List<GaugeInfo> getGauges() {
		return Collections.unmodifiableList(gauges);
	}

	public void setGauges(List<GaugeInfo> gauges) {
		this.gauges = new ArrayList<GaugeInfo>(gauges);
	}

	public boolean isRescaleDataToVideo() {
		return rescaleDataToVideo;
	}

	public void setRescaleDataToVideo(boolean rescaleDataToVideo) {
		this.rescaleDataToVideo = rescaleDataToVideo;
	}
}
