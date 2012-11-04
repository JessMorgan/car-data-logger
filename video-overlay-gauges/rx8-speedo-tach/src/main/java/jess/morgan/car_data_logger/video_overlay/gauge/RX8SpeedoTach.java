package jess.morgan.car_data_logger.video_overlay.gauge;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

public class RX8SpeedoTach implements Gauge {
	private static final Color BACKGROUND_COLOR  = Color.BLACK;
	private static final Color REDLINE_COLOR     = new Color(115, 41, 42);
	private static final Color FUEL_CUTOFF_COLOR = Color.RED;
	private static final Color NEEDLE_COLOR      = new Color(195, 22, 15);
	private static final Color TEXT_COLOR        = new Color(236, 236, 248);
	private static final Color PIP_COLOR         = TEXT_COLOR;
	private static final int   RPM_ANGLE         = 240;
	private static final double LONG_PIP_LENGTH  = 0.06;
	private static final double MID_PIP_LENGTH   = LONG_PIP_LENGTH * 2 / 3;
	private static final double SHORT_PIP_LENGTH = LONG_PIP_LENGTH / 2;
	private static final double BUFFER_WIDTH     = SHORT_PIP_LENGTH;

	private final String speedParam;
	private final String rpmParam;
	private final int redlineRpm;
	private final int fuelCutoffRpm;
	private final int maxRpm;

	public RX8SpeedoTach(String speedParam, String rpmParam, Integer redlineRpm, Integer fuelCutoffRpm) {
		this.speedParam    = (speedParam    != null) ? speedParam    : "Speed";
		this.rpmParam      = (rpmParam      != null) ? rpmParam      : "RPM";
		this.redlineRpm    = (redlineRpm    != null) ? redlineRpm    : 8500;
		this.fuelCutoffRpm = (fuelCutoffRpm != null) ? fuelCutoffRpm : 9000;
		// Next highest
		this.maxRpm = (this.fuelCutoffRpm / 1000 + 1) * 1000;
	}

	@Override
	public void draw(Map<String, String> data, Graphics2D graphics, int x, int y, int width, int height) {
		double gaugeLengthMultiplier = (width + height) / 2.0;
		double gaugeBufferWidth = gaugeLengthMultiplier * BUFFER_WIDTH;
		double gaugeLongPipLength = gaugeLengthMultiplier * LONG_PIP_LENGTH;
		double gaugeMidPipLength = gaugeLengthMultiplier * MID_PIP_LENGTH;
		double gaugeShortPipLength = gaugeLengthMultiplier * SHORT_PIP_LENGTH;

		// Get RPM
		float rpmValue = 0;
		try {
			String sValue = data.get(rpmParam);
			if(sValue != null && !sValue.isEmpty()) {
				rpmValue = Float.parseFloat(sValue);
			}
		} catch(NumberFormatException nfe) {
			// Log and ignore - use the default value
			System.err.println("Illegal float value: '" + data.get(rpmParam) + "'");
		}
		// Get speed
		float speedValue = 0;
		try {
			String sValue = data.get(speedParam);
			if(sValue != null && !sValue.isEmpty()) {
				speedValue = Float.parseFloat(sValue);
			}
		} catch(NumberFormatException nfe) {
			// Log and ignore - use the default value
			System.err.println("Illegal float value: '" + data.get(speedParam) + "'");
		}

		// Draw background circle
		graphics.setColor(BACKGROUND_COLOR);
		graphics.fillOval(x, y, width, height);
		// Draw redline markers
		int redlineStartAngle = RPM_ANGLE * redlineRpm / maxRpm;
		int fuelCutoffStartAngle = RPM_ANGLE * fuelCutoffRpm / maxRpm;
		int redlineAngle = fuelCutoffStartAngle - redlineStartAngle;
		int fuelCutoffAngle = RPM_ANGLE - fuelCutoffStartAngle;
		graphics.setColor(REDLINE_COLOR);
		graphics.setStroke(new BasicStroke((float)gaugeLongPipLength, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		graphics.drawArc(
				(int)(x + gaugeBufferWidth * 2),
				(int)(y + gaugeBufferWidth * 2),
				(int)(width - (gaugeBufferWidth * 4)),
				(int)(height - (gaugeBufferWidth * 4)),
				remapAngle(redlineStartAngle, redlineAngle),
				redlineAngle);
		graphics.setColor(FUEL_CUTOFF_COLOR);
		graphics.drawArc(
				(int)(x + gaugeBufferWidth * 2),
				(int)(y + gaugeBufferWidth * 2),
				(int)(width - (gaugeBufferWidth * 4)),
				(int)(height - (gaugeBufferWidth * 4)),
				remapAngle(fuelCutoffStartAngle, fuelCutoffAngle),
				fuelCutoffAngle);
		// Draw pips
		graphics.setStroke(new BasicStroke());
		graphics.setColor(PIP_COLOR);
		// Draw main pips
		for(int rpm = 0; rpm <= maxRpm; rpm += 1000) {
			double angle = degreesToRadians(calculateRpmAngle(rpm));
			int x1 = (int)(x + width  / 2.0 + Math.cos(angle) * (width  / 2 - gaugeBufferWidth));
			int y1 = (int)(y + height / 2.0 + Math.sin(angle) * (height / 2 - gaugeBufferWidth));
			int x2 = (int)(x + width  / 2.0 + Math.cos(angle) * (width  / 2 - gaugeBufferWidth - gaugeLongPipLength));
			int y2 = (int)(y + height / 2.0 + Math.sin(angle) * (height / 2 - gaugeBufferWidth - gaugeLongPipLength));
			graphics.drawLine(x1, y1, x2, y2);
		}
		// Draw half pips
		for(int rpm = 500; rpm < maxRpm; rpm += 1000) {
			double angle = degreesToRadians(calculateRpmAngle(rpm));
			int x1 = (int)(x + width  / 2.0 + Math.cos(angle) * (width  / 2 - gaugeBufferWidth - (gaugeLongPipLength - gaugeMidPipLength) / 2));
			int y1 = (int)(y + height / 2.0 + Math.sin(angle) * (height / 2 - gaugeBufferWidth - (gaugeLongPipLength - gaugeMidPipLength) / 2));
			int x2 = (int)(x + width  / 2.0 + Math.cos(angle) * (width  / 2 - gaugeBufferWidth - (gaugeLongPipLength - gaugeMidPipLength) / 2 - gaugeMidPipLength));
			int y2 = (int)(y + height / 2.0 + Math.sin(angle) * (height / 2 - gaugeBufferWidth - (gaugeLongPipLength - gaugeMidPipLength) / 2 - gaugeMidPipLength));
			graphics.drawLine(x1, y1, x2, y2);
		}
		// Draw quarter pips
		for(int rpm = 250; rpm < maxRpm; rpm += 500) {
			double angle = degreesToRadians(calculateRpmAngle(rpm));
			int x1 = (int)(x + width  / 2.0 + Math.cos(angle) * (width  / 2 - gaugeBufferWidth - (gaugeLongPipLength - gaugeShortPipLength) / 2));
			int y1 = (int)(y + height / 2.0 + Math.sin(angle) * (height / 2 - gaugeBufferWidth - (gaugeLongPipLength - gaugeShortPipLength) / 2));
			int x2 = (int)(x + width  / 2.0 + Math.cos(angle) * (width  / 2 - gaugeBufferWidth - (gaugeLongPipLength - gaugeShortPipLength) / 2 - gaugeShortPipLength));
			int y2 = (int)(y + height / 2.0 + Math.sin(angle) * (height / 2 - gaugeBufferWidth - (gaugeLongPipLength - gaugeShortPipLength) / 2 - gaugeShortPipLength));
			graphics.drawLine(x1, y1, x2, y2);
		}
		// Draw numbers
		graphics.setColor(TEXT_COLOR);
		for(int rpm = 0; rpm <= maxRpm; rpm += 1000) {
			double angle = degreesToRadians(calculateRpmAngle(rpm));
			int x1 = (int)(x + width  / 2.0 + Math.cos(angle) * (width  / 2 - gaugeBufferWidth - gaugeLongPipLength));
			int y1 = (int)(y + height / 2.0 + Math.sin(angle) * (height / 2 - gaugeBufferWidth - gaugeLongPipLength));
			TextLayout layout = new TextLayout(Integer.toString(rpm / 1000), graphics.getFont(), graphics.getFontRenderContext());
			Rectangle2D bounds = layout.getPixelBounds(null, x1, y1);
			// Center
			x1 -= bounds.getWidth() / 2;
			y1 += bounds.getHeight() / 2;
			// Move toward the interior as needed
			x1 -= Math.cos(angle) * bounds.getWidth();
			y1 -= Math.sin(angle) * bounds.getHeight();
			layout.draw(graphics, x1, y1);
		}
		// Draw "x1000r/min" label
		// Draw "mph" unit label
		// Draw digital speedometer
		graphics.drawString(String.format("%.0f", speedValue), x + (width * 3 / 4), y + (height * 3 / 4));
		// Draw tachometer needle
		graphics.setColor(NEEDLE_COLOR);
		double angle = degreesToRadians(calculateRpmAngle(rpmValue));
		int x1 = x + width / 2;
		int y1 = y + height / 2;
		int x2 = (int)(x + width  / 2.0 + Math.cos(angle) * (width  / 2 - gaugeBufferWidth - gaugeLongPipLength));
		int y2 = (int)(y + height / 2.0 + Math.sin(angle) * (height / 2 - gaugeBufferWidth - gaugeLongPipLength));
		graphics.drawLine(x1, y1, x2, y2);

//		float value = 0;
//		try {
//			String sValue = data.get(parameterName);
//			if(sValue != null && !sValue.isEmpty()) {
//				value = Float.parseFloat(sValue);
//			}
//		} catch(NumberFormatException nfe) {
//			// Log and ignore - use the default value
//			System.err.println("Illegal float value: '" + data.get(parameterName) + "'");
//		}
//
//		graphics.setColor(fillColor);
//		if(horizontal) {
//			graphics.fillRect(x, y, (int) (width * value / 100.0), height);
//		} else {
//			int barHeight = (int) (height * value / 100.0);
//			graphics.fillRect(x, y + height - barHeight, width, barHeight);
//		}
//
//		graphics.setColor(borderColor);
//		graphics.setStroke(stroke);
//		graphics.drawRect(x, y, width, height);
	}

	private double calculateRpmAngle(float rpm) {
		return RPM_ANGLE * rpm / (double)maxRpm + 90;
	}

	private double degreesToRadians(double angle) {
		return angle * Math.PI / 180;
	}

	private int remapAngle(int angle) {
		return 270 - angle;
	}

	private int remapAngle(int startAngle, int arcWidth) {
		return remapAngle(startAngle) - arcWidth;
	}

	public static void main(String args[]) {
		final RX8SpeedoTach tach = new RX8SpeedoTach(null, null, null, null);
		JFrame frame = new JFrame();
		frame.setSize(300, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("RX-8");
		frame.setContentPane(new Panel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				tach.draw(new HashMap<String, String>(), (Graphics2D) g, 0, 0, 200, 200);
			}
		});
		frame.setVisible(true);
	}
}
