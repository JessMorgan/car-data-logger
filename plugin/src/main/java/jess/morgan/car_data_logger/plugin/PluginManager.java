package jess.morgan.car_data_logger.plugin;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.AddPluginsFromOption;
import net.xeoh.plugins.base.util.PluginManagerUtil;

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
		} else if(Boolean.class.equals(type)) {
			return Boolean.parseBoolean(value);
		} else if(Integer.class.equals(type)) {
			return Integer.parseInt(value);
		} else if(Long.class.equals(type)) {
			return Long.parseLong(value);
		} else if(Float.class.equals(type)) {
			return Float.parseFloat(value);
		} else if(Double.class.equals(type)) {
			return Double.parseDouble(value);
		} else if(BigDecimal.class.equals(type)) {
			return new BigDecimal(value);
		}
		return value;
	}
}
