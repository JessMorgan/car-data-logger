package jess.morgan.car_data_logger.decode.can;

import java.io.File;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import jess.morgan.car_data_logger.decode.ConfigParameter;
import jess.morgan.car_data_logger.decode.DataDecoder;
import jess.morgan.car_data_logger.decode.DataDecoderFactory;

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
	public DataDecoder getDecoder(Map<String, Object> config) throws Exception {
		Object configFile;
		if(config == null || (configFile = config.get("configFile")) == null) {
			throw new IllegalArgumentException("configFile parameter is required");
		}
		if(!(configFile instanceof File)) {
			throw new IllegalArgumentException("configFile parameter must be a File");
		}
		return new CANDataDecoder((File)configFile);
	}
}
