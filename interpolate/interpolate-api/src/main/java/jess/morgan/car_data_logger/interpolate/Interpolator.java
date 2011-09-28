package jess.morgan.car_data_logger.interpolate;

import java.util.List;
import java.util.Map;

public interface Interpolator {
	public void interpolate(List<Map<String, String>> data, boolean interpolateEnds);
}
