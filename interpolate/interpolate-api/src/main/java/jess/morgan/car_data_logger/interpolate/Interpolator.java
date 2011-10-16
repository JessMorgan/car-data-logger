package jess.morgan.car_data_logger.interpolate;

import java.util.List;
import java.util.Map;

import net.xeoh.plugins.base.Plugin;

public interface Interpolator extends Plugin {
	public void interpolate(List<Map<String, String>> data, boolean interpolateEnds);
}
