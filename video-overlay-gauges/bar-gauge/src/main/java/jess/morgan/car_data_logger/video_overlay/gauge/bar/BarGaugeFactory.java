package jess.morgan.car_data_logger.video_overlay.gauge.bar;

import java.awt.Color;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import jess.morgan.car_data_logger.plugin.ConfigParameter;
import jess.morgan.car_data_logger.video_overlay.gauge.Gauge;
import jess.morgan.car_data_logger.video_overlay.gauge.GaugeFactory;

@PluginImplementation
public class BarGaugeFactory implements GaugeFactory {
	@Override
	public String getPluginDisplayName() {
		return "Bar Gauge";
	}

	@Override
	public ConfigParameter[] getConfigParameters() {
		return new ConfigParameter[]{
				new ConfigParameter("parameter",       "Parameter Name",                          String.class,  true),
				new ConfigParameter("horizontal",      "True for horizontal, false for vertical", Boolean.class, false),
				new ConfigParameter("borderWidth",     "Width in pixels of border",               Float.class,   false),
				new ConfigParameter("borderColor",     "RGB Hex color of border",                 String.class,  false),
				new ConfigParameter("fillColor",       "RGB Hex color of fill",                   String.class,  false),
				new ConfigParameter("label",           "Gauge label",                             String.class,  false),
				new ConfigParameter("font",            "Gauge font",                              String.class,  false),
				new ConfigParameter("displayRawValue", "Display raw value underneath bar",        Boolean.class, false),
		};
	}

	@Override
	public Gauge getPlugin(Map<String, Object> config) throws Exception {
		String parameter         = ConfigParameter.getParameter(config, "parameter",       String.class,  true);
		Boolean horizontal       = ConfigParameter.getParameter(config, "horizontal",      Boolean.class, false);
		Float borderWidth        = ConfigParameter.getParameter(config, "borderWidth",     Float.class,   false);
		String borderColorString = ConfigParameter.getParameter(config, "borderColor",     String.class,  false);
		String fillColorString   = ConfigParameter.getParameter(config, "fillColor",       String.class,  false);
		String label             = ConfigParameter.getParameter(config, "label",           String.class,  false);
		String font              = ConfigParameter.getParameter(config, "font",            String.class,  false);
		Boolean displayRawValue  = ConfigParameter.getParameter(config, "displayRawValue", Boolean.class, false);

		Color borderColor = (borderColorString == null ? null : Color.decode(borderColorString));
		Color fillColor   = (fillColorString   == null ? null : Color.decode(fillColorString));

		return new BarGauge(parameter, horizontal, borderWidth, borderColor, fillColor, label, font, displayRawValue);
	}
}
