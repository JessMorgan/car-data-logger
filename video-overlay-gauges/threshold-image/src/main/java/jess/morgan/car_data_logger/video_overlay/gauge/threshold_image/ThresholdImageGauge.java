package jess.morgan.car_data_logger.video_overlay.gauge.threshold_image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import jess.morgan.car_data_logger.video_overlay.gauge.Gauge;

public class ThresholdImageGauge implements Gauge {
	private final String parameterName;
	private final BufferedImage lowImage;
	private final BufferedImage highImage;
	private final double threshold;
	private static final String DEFAULT_IMAGE_NAME = "default.png";

	public ThresholdImageGauge(String parameterName, File lowImage, File highImage, Double threshold) throws IOException {
		this.parameterName = parameterName;
		this.lowImage = (lowImage != null ? ImageIO.read(lowImage) : null);
		this.highImage = (highImage != null ? ImageIO.read(highImage) : (lowImage != null ? null : ImageIO.read(getClass().getResourceAsStream(DEFAULT_IMAGE_NAME))));
		this.threshold = (threshold != null ? threshold : 1.0);
	}

	@Override
	public void draw(Map<String, String> data, Graphics2D graphics, int x, int y, int width, int height) {
		float value = 0;
		try {
			String sValue = data.get(parameterName);
			if(sValue != null && !sValue.isEmpty()) {
				value = Float.parseFloat(sValue);
			}
		} catch(NumberFormatException nfe) {
			// Log and ignore - use the default value
			System.err.println("Illegal float value: '" + data.get(parameterName) + "'");
		}

		BufferedImage image = (value < threshold ? lowImage : highImage);

		if(image != null) {
			graphics.drawImage(image, x, y, width, height, null);
		}
	}
}
