package jess.morgan.car_data_logger.plugin;

import java.util.Map;

import net.xeoh.plugins.base.Plugin;

public interface PluginFactory<E> extends Plugin {
	public String getPluginDisplayName();
	public ConfigParameter[] getConfigParameters();
	public E getPlugin(Map<String, Object> config) throws Exception;
}
