package jess.morgan.car_data_logger.video_overlay;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GaugeInfo {
	private final String factoryClass;
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final Map<String, String> config;

	public GaugeInfo(String factoryClass, int x, int y, int width, int height, Map<String, String> config) {
		this.factoryClass = factoryClass;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.config = new HashMap<String, String>(config);
	}

	public String getFactoryClass() {
		return factoryClass;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Map<String, String> getConfig() {
		return Collections.unmodifiableMap(config);
	}
}
