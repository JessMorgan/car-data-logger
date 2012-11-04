package jess.morgan.car_data_logger.video_overlay.gauge.rotated_image;

import java.io.File;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import jess.morgan.car_data_logger.plugin.ConfigParameter;
import jess.morgan.car_data_logger.video_overlay.gauge.Gauge;
import jess.morgan.car_data_logger.video_overlay.gauge.GaugeFactory;

@PluginImplementation
public class RotatedImageGaugeFactory implements GaugeFactory {
	@Override
	public String getPluginDisplayName() {
		return "Rotated Image Gauge";
	}

	@Override
	public ConfigParameter[] getConfigParameters() {
		return new ConfigParameter[]{
				new ConfigParameter("parameter",       "Parameter Name",                          String.class,  true),
				new ConfigParameter("image",           "Image to display",                        File.class,    false),
		};
	}

	@Override
	public Gauge getPlugin(Map<String, Object> config) throws Exception {
		String parameter         = ConfigParameter.getParameter(config, "parameter",       String.class,  true);
		File image               = ConfigParameter.getParameter(config, "image",           File.class, false);

		return new RotatedImageGauge(parameter, image);
	}
}
