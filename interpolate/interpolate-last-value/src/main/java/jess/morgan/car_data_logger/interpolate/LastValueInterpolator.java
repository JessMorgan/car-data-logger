package jess.morgan.car_data_logger.interpolate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class LastValueInterpolator implements Interpolator {
	@Override
	public void interpolate(List<Map<String, String>> data, boolean interpolateEnds) {
		Map<String, String> lastValue = null;
		for(Map<String, String> line : data) {
			if(lastValue == null) {
				lastValue = new HashMap<String, String>(line);
			} else {
				for(Map.Entry<String, String> entry : line.entrySet()) {
					if(entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
						lastValue.put(entry.getKey(), entry.getValue());
					}
				}
				for(Map.Entry<String, String> entry : lastValue.entrySet()) {
					String value = line.get(entry.getKey());
					if(value == null || value.trim().isEmpty()) {
						line.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
	}
}
