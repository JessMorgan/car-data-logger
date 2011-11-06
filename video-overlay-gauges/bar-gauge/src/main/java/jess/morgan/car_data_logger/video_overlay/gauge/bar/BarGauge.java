package jess.morgan.car_data_logger.video_overlay.gauge.bar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Map;

import jess.morgan.car_data_logger.video_overlay.gauge.Gauge;

public class BarGauge implements Gauge {
	private final String parameterName;
	private final boolean horizontal;
	private final Stroke stroke;
	private final Color borderColor;
	private final Color fillColor;
	// For future features:
	private final String label;
	private final String font;
	private final boolean displayRawValue;

	public BarGauge(String parameterName, Boolean horizontal, Float borderWidth, Color borderColor, Color fillColor, String label, String font, Boolean displayRawValue) {
		this.parameterName = parameterName;
		this.horizontal =  (horizontal  != null ? horizontal  : false);
		this.borderColor = (borderColor != null ? borderColor : Color.BLACK);
		this.fillColor =   (fillColor   != null ? fillColor   : Color.BLUE);
		this.stroke = new BasicStroke(borderWidth != null ? borderWidth : 1);
		this.label = label;
		this.font = font;
		this.displayRawValue = (displayRawValue != null ? displayRawValue : false);
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

		graphics.setColor(fillColor);
		if(horizontal) {
			graphics.fillRect(x, y, (int) (width * value / 100.0), height);
		} else {
			int barHeight = (int) (height * value / 100.0);
			graphics.fillRect(x, y + height - barHeight, width, barHeight);
		}

		graphics.setColor(borderColor);
		graphics.setStroke(stroke);
		graphics.drawRect(x, y, width, height);
	}
}
