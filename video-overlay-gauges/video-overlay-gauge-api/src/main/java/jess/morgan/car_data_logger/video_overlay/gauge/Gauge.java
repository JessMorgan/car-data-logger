package jess.morgan.car_data_logger.video_overlay.gauge;

import java.awt.Graphics2D;
import java.util.Map;

import net.xeoh.plugins.base.Plugin;

public interface Gauge extends Plugin {
	public void draw(Map<String, String> data, Graphics2D graphics, int x, int y, int width, int height);
}
