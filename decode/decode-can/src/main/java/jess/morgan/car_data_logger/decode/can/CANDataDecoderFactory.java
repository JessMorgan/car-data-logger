package jess.morgan.car_data_logger.decode.can;

import java.io.File;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import jess.morgan.car_data_logger.decode.DataDecoder;
import jess.morgan.car_data_logger.decode.DataDecoderFactory;
import jess.morgan.car_data_logger.plugin.ConfigParameter;

@PluginImplementation
public class CANDataDecoderFactory implements DataDecoderFactory {
	@Override
	public String getPluginDisplayName() {
		return "CAN Data Decoder";
	}

	@Override
	public ConfigParameter[] getConfigParameters() {
		return new ConfigParameter[]{
				new ConfigParameter("configFile", "CAN Configuration File", File.class, true),
		};
	}

	@Override
	public DataDecoder getPlugin(Map<String, Object> config) throws Exception {
		File configFile = ConfigParameter.getParameter(config, "configFile", File.class, true);

		return new CANDataDecoder(configFile);
	}
}
