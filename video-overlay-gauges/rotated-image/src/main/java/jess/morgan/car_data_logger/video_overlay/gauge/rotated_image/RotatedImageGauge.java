package jess.morgan.car_data_logger.video_overlay.gauge.rotated_image;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import jess.morgan.car_data_logger.video_overlay.gauge.Gauge;

public class RotatedImageGauge implements Gauge {
	private final String parameterName;
	private final BufferedImage image;
	private static final String DEFAULT_IMAGE_NAME = "default.png";
	private double pivotX;
	private double pivotY;

	public RotatedImageGauge(String parameterName, File image) throws IOException {
		this.parameterName = parameterName;
		this.image = (image != null ? ImageIO.read(image) : ImageIO.read(getClass().getResourceAsStream(DEFAULT_IMAGE_NAME)));
		pivotX = this.image.getWidth() / 2;
		pivotY = this.image.getHeight() / 2;
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

		double rotationRequired = Math.toRadians(value);
		AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, pivotX, pivotY);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

		graphics.drawImage(op.filter(image, null), x, y, width, height, null);
	}
}
