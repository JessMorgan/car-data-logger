package jess.morgan.car_data_logger.data_processor;

import java.util.List;
import java.util.Map;

public interface DataProcessor {
	public void process(List<Map<String, String>> data);
	/** CANNOT return null - use Collections.emptyMap() instead for a null implementation */
	public Map<String, String> getAdditionalParameters();
}
