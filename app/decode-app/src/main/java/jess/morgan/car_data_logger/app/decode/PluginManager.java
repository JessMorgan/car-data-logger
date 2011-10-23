package jess.morgan.car_data_logger.app.decode;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.AddPluginsFromOption;
import net.xeoh.plugins.base.util.PluginManagerUtil;

import jess.morgan.car_data_logger.data_processor.DataProcessor;
import jess.morgan.car_data_logger.data_processor.DataProcessorFactory;
import jess.morgan.car_data_logger.decode.DataDecoder;
import jess.morgan.car_data_logger.decode.DataDecoderFactory;
import jess.morgan.car_data_logger.plugin.ConfigParameter;
import jess.morgan.car_data_logger.plugin.PluginFactory;

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
		return loadPlugin(className, config, DataDecoderFactory.class);
	}

	public DataProcessor loadDataProcessor(String className, Map<String, String> config) throws Exception {
		return loadPlugin(className, config, DataProcessorFactory.class);
	}

	public <E, T extends PluginFactory<E>> E loadPlugin(String className, Map<String, String> config, Class<T> clazz)
			throws Exception {
		for(PluginFactory<E> factory : pluginManager.getPlugins(clazz)) {
			Map<String, Object> pluginConfig = new HashMap<String, Object>();
			if(factory.getClass().getName().equals(className)) {
				for(ConfigParameter param : factory.getConfigParameters()) {
					String value = config.get(param.getName());
					if(value != null) {
						pluginConfig.put(param.getName(), marshallValue(value, param.getType()));
					}
				}
				return factory.getPlugin(pluginConfig);
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
}
