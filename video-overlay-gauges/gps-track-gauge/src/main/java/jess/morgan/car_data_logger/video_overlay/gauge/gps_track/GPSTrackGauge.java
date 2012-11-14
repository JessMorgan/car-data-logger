/*
 * Copyright 2012 Jess Morgan
 *
 * This file is part of car-data-logger.
 *
 * car-data-logger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * car-data-logger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with car-data-logger.  If not, see <http://www.gnu.org/licenses/>.
 */
package jess.morgan.car_data_logger.video_overlay.gauge.gps_track;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import jess.morgan.car_data_logger.video_overlay.gauge.Gauge;

public class GPSTrackGauge implements Gauge {
	private BufferedImage track;
	private Properties trackProperties;
	private final float opacity;

	public GPSTrackGauge(File track, Double opacity) throws IOException {
		ZipInputStream zis = new ZipInputStream(new FileInputStream(track));
		try {
			ZipEntry entry;
			while((entry = zis.getNextEntry()) != null) {
				if("track.png".equals(entry.getName())) {
					this.track = ImageIO.read(zis);
				} else if("track.properties".equals(entry.getName())) {
					trackProperties = new Properties();
					trackProperties.load(zis);
				}
			}
		} finally {
			zis.close();
		}
		this.opacity = (opacity == null ? 0.5f : opacity.floatValue());
	}

	@Override
	public void draw(Map<String, String> data, Graphics2D graphics, int x, int y, int width, int height) {
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

		// Keep aspect ratio
		double widthRatio  = ((double)track.getWidth())  / ((double)width);
		double heightRatio = ((double)track.getHeight()) / ((double)height);
		if(widthRatio > heightRatio) {
			height = (int)(((double)height) * heightRatio / widthRatio);
		} else {
			width  = (int)(((double)width)  * widthRatio / heightRatio);
		}

		// Display track
		graphics.drawImage(track, x, y, width, height, null);

		// Display locator
		graphics.setColor(Color.RED);

		double latitude  = getDoubleParameter(data, "Latitude");
		double longitude = getDoubleParameter(data, "Longitude");

		double left   = getDoubleParameter(trackProperties, "left");
		double right  = getDoubleParameter(trackProperties, "right");
		double top    = getDoubleParameter(trackProperties, "top");
		double bottom = getDoubleParameter(trackProperties, "bottom");
		int xPos = (int)((longitude - left) / (right - left) *  ((double)width))   + x;
		int yPos = (int)((latitude - bottom) / (top - bottom) *  ((double)height)) + y;
		graphics.drawOval(xPos - 1, yPos - 1, 3, 3);
	}

	private double getDoubleParameter(String value) {
		try {
			if(value != null && !value.isEmpty()) {
				return Double.parseDouble(value);
			}
		} catch(NumberFormatException nfe) {
			// Log and ignore - use the default value
			System.err.println("Illegal float value: '" + value + "'");
		}

		return 0d;
	}
	private double getDoubleParameter(Properties data, String parameterName) {
		return getDoubleParameter(data.getProperty(parameterName));
	}
	private double getDoubleParameter(Map<String, String> data, String parameterName) {
		return getDoubleParameter(data.get(parameterName));
	}
}
