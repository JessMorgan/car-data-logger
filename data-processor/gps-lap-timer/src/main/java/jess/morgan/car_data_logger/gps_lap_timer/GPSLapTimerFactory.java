package jess.morgan.car_data_logger.gps_lap_timer;

import java.io.File;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import jess.morgan.car_data_logger.data_processor.DataProcessor;
import jess.morgan.car_data_logger.data_processor.DataProcessorFactory;
import jess.morgan.car_data_logger.plugin.ConfigParameter;

@PluginImplementation
public class GPSLapTimerFactory implements DataProcessorFactory {
	@Override
	public String getPluginDisplayName() {
		return "GPS Lap Timer";
	}

	@Override
	public ConfigParameter[] getConfigParameters() {
		return new ConfigParameter[]{
				new ConfigParameter("track",           "Track file",                              File.class,    true),
		};
	}

	@Override
	public DataProcessor getPlugin(Map<String, Object> config) throws Exception {
		File track               = ConfigParameter.getParameter(config, "track",           File.class,   true);

		return new GPSLapTimer(track);
	}
}
