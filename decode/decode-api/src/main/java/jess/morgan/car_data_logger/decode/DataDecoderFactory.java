package jess.morgan.car_data_logger.decode;

import java.util.Map;

import net.xeoh.plugins.base.Plugin;

public interface DataDecoderFactory extends Plugin {
	public String getPluginDisplayName();
	public ConfigParameter[] getConfigParameters();
	public DataDecoder getDecoder(Map<String, Object> config) throws Exception;
}
