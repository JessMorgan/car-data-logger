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
package jess.morgan.car_data_logger.video_overlay.gauge.text;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import jess.morgan.car_data_logger.video_overlay.gauge.Gauge;

public class TextGauge implements Gauge {
	private final String parameterName;
	private final String format;
	private final DateFormat timeFormat;
	private final String emptyValue;
	private final Color textColor;

	public TextGauge(String parameterName, String format, String timeFormat, String emptyValue, Color textColor) throws IOException {
		this.parameterName = parameterName;
		this.format = (format == null ? "%.1f" : format);
		this.timeFormat = (timeFormat == null ? null : new SimpleDateFormat(timeFormat));
		this.emptyValue = (emptyValue == null ? "" : emptyValue);
		this.textColor = (textColor == null ? Color.WHITE : textColor);
	}

	@Override
	public void draw(Map<String, String> data, Graphics2D graphics, int x, int y, int width, int height) {
		String str = data.get(parameterName);
		Object obj = str;
		try {
			if(str != null && !str.isEmpty()) {
				obj = Double.parseDouble(str);
			}
		} catch(NumberFormatException nfe) {
			// Ignore - it's not a number
		}
		if(obj instanceof Double) {
			if(timeFormat != null) {
				str = timeFormat.format(new Date(((Double)obj).longValue()));
			} else {
				str = String.format(format, obj);
			}
		}
		if(str == null || str.trim().isEmpty()) {
			str = emptyValue;
		}

		TextLayout layout = new TextLayout(str, graphics.getFont(), graphics.getFontRenderContext());
		Rectangle2D bounds = layout.getPixelBounds(null, x, y);

		Color oldColor = graphics.getColor();
		graphics.setColor(textColor);
		layout.draw(graphics, x, (float) (y + bounds.getHeight()));
		graphics.setColor(oldColor);
	}
}
