package jess.morgan.car_data_logger.video_overlay.gauge.threshold_image;

import java.io.File;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import jess.morgan.car_data_logger.plugin.ConfigParameter;
import jess.morgan.car_data_logger.video_overlay.gauge.Gauge;
import jess.morgan.car_data_logger.video_overlay.gauge.GaugeFactory;

@PluginImplementation
public class ThresholdImageGaugeFactory implements GaugeFactory {
	@Override
	public String getPluginDisplayName() {
		return "Threshold Image Gauge";
	}

	@Override
	public ConfigParameter[] getConfigParameters() {
		return new ConfigParameter[]{
				new ConfigParameter("parameter",       "Parameter Name",                          String.class,  true),
				new ConfigParameter("lowImage",        "Image to display for low values",         File.class,    false),
				new ConfigParameter("highImage",       "Image to display for high values",        File.class,    false),
				new ConfigParameter("threshold",       "Cutoff value for two images",             Double.class,  false),
		};
	}

	@Override
	public Gauge getPlugin(Map<String, Object> config) throws Exception {
		String parameter         = ConfigParameter.getParameter(config, "parameter",       String.class,  true);
		File lowImage            = ConfigParameter.getParameter(config, "lowImage",        File.class,    false);
		File highImage           = ConfigParameter.getParameter(config, "highImage",       File.class,    false);
		Double threshold         = ConfigParameter.getParameter(config, "threshold",       Double.class,  false);

		return new ThresholdImageGauge(parameter, lowImage, highImage, threshold);
	}
}
