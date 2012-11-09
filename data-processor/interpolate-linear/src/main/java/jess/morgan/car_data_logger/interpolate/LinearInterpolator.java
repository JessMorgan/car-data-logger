package jess.morgan.car_data_logger.interpolate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jess.morgan.car_data_logger.data_processor.DataProcessor;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class LinearInterpolator implements DataProcessor {
	@Override
	public void process(List<Map<String, String>> data) {
		Map<String, Map<Long, String>> remappedData = new HashMap<String, Map<Long, String>>();
		for(Map<String, String> line : data) {
			for(Map.Entry<String, String> entry : line.entrySet()) {
				if("Timestamp".equals(entry.getKey())) {
					continue;
				}
				if(entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
					Map<Long, String> fieldByTime = remappedData.get(entry.getKey());
					if(fieldByTime == null) {
						fieldByTime = new TreeMap<Long, String>();
						remappedData.put(entry.getKey(), fieldByTime);
					}
					Long timestamp = Long.parseLong(line.get("Timestamp"));
					fieldByTime.put(timestamp, entry.getValue());
				}
			}
		}
		for(Map.Entry<String, Map<Long, String>> entry : remappedData.entrySet()) {
			String field = entry.getKey();
			Iterator<Map.Entry<Long, String>> i = entry.getValue().entrySet().iterator();

			Map.Entry<Long, String> min = i.next();
			Map.Entry<Long, String> max = i.next();
			for(Map<String, String> line : data) {
				String value = line.get(field);
				if(value != null && !value.trim().isEmpty()) {
					continue;
				}
				Long timestamp = Long.parseLong(line.get("Timestamp"));
				if(timestamp < min.getKey()) {
					continue;
				}
				if(timestamp > max.getKey()) {
					if(i.hasNext()) {
						min = max;
						max = i.next();
					} else {
						break;
					}
				}

				double firstValue;
				double lastValue;
				try {
					firstValue = Double.parseDouble(min.getValue());
					lastValue = Double.parseDouble(max.getValue());
				} catch(NumberFormatException nfe) {
					// Guess we don't understand this data format - we'll just reuse the last value
					line.put(field,  min.getValue());
					continue;
				}
				double valueDifference = lastValue - firstValue;
				long minMaxTimeDifference = max.getKey() - min.getKey();
				long minCurrentTimeDifference = timestamp - min.getKey();
				double ratio = (double)minCurrentTimeDifference / minMaxTimeDifference;
				double newValue = firstValue + (valueDifference * ratio);
				line.put(field, Double.toString(newValue));
			}
		}
	}
}
