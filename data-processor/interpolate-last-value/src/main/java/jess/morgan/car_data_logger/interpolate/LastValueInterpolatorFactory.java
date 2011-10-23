package jess.morgan.car_data_logger.interpolate;

import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import jess.morgan.car_data_logger.data_processor.DataProcessor;
import jess.morgan.car_data_logger.data_processor.DataProcessorFactory;
import jess.morgan.car_data_logger.plugin.ConfigParameter;

@PluginImplementation
public class LastValueInterpolatorFactory implements DataProcessorFactory {
	@Override
	public String getPluginDisplayName() {
		return "Last Value Interpolator";
	}

	@Override
	public ConfigParameter[] getConfigParameters() {
		return new ConfigParameter[]{
		};
	}

	@Override
	public DataProcessor getPlugin(Map<String, Object> config) throws Exception {
		return new LastValueInterpolator();
	}
}
