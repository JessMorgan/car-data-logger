package jess.morgan.car_data_logger.video_overlay.gauge;

import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import jess.morgan.car_data_logger.plugin.ConfigParameter;

@PluginImplementation
public class RX8SpeedoTachFactory implements GaugeFactory {
	@Override
	public String getPluginDisplayName() {
		return "RX-8 Speedometer and Tachometer";
	}

	@Override
	public ConfigParameter[] getConfigParameters() {
		return new ConfigParameter[]{
				new ConfigParameter("speedParam",    "Speed Parameter Name", String.class,  false),
				new ConfigParameter("rpmParam",      "RPM Parameter Name",   String.class,  false),
				new ConfigParameter("redlineRPM",    "Redline RPM",          Integer.class, false),
				new ConfigParameter("fuelCutoffRPM", "Fuel Cutoff RPM",      Integer.class, false),
		};
	}

	@Override
	public Gauge getPlugin(Map<String, Object> config) throws Exception {
		String speedParam     = ConfigParameter.getParameter(config, "speedParam",    String.class,  false);
		String rpmParam       = ConfigParameter.getParameter(config, "rpmParam",      String.class,  false);
		Integer redlineRpm    = ConfigParameter.getParameter(config, "redlineRpm",    Integer.class,  false);
		Integer fuelCutoffRpm = ConfigParameter.getParameter(config, "fuelCutoffRpm", Integer.class,  false);

		return new RX8SpeedoTach(speedParam, rpmParam, redlineRpm, fuelCutoffRpm);
	}
}
