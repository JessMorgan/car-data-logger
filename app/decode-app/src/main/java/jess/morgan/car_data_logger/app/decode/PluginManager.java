package jess.morgan.car_data_logger.app.decode;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.AddPluginsFromOption;
import net.xeoh.plugins.base.util.PluginManagerUtil;

import jess.morgan.car_data_logger.decode.ConfigParameter;
import jess.morgan.car_data_logger.decode.DataDecoder;
import jess.morgan.car_data_logger.decode.DataDecoderFactory;
import jess.morgan.car_data_logger.interpolate.Interpolator;

public class PluginManager {
	private PluginManagerUtil pluginManager;

	public PluginManager(List<File> pluginDirectories) {
		pluginManager = new PluginManagerUtil(PluginManagerFactory.createPluginManager());
		for(File dir : pluginDirectories) {
			pluginManager.addPluginsFrom(
					dir.toURI(),
					(AddPluginsFromOption[])null);
		}
	}

	public DataDecoder loadDecoder(String className, Map<String, String> config) throws Exception {
		for(DataDecoderFactory factory : pluginManager.getPlugins(DataDecoderFactory.class)) {
			Map<String, Object> decoderConfig = new HashMap<String, Object>();
			if(factory.getClass().getName().equals(className)) {
				for(ConfigParameter param : factory.getConfigParameters()) {
					String value = config.get(param.getName());
					if(value != null) {
						decoderConfig.put(param.getName(), marshallValue(value, param.getType()));
					}
				}
				return factory.getDecoder(decoderConfig);
			}
		}
		return null;
	}

	private Object marshallValue(String value, Class<?> type) {
		if(String.class.equals(type)) {
			return value;
		} else if(File.class.equals(type)) {
			return new File(value);
		}
		return value;
	}

	public Interpolator loadInterpolator(String className) {
		for(Interpolator interpolator : pluginManager.getPlugins(Interpolator.class)) {
			if(interpolator.getClass().getName().equals(className)) {
				return interpolator;
			}
		}
		return null;
	}
}
