package jess.morgan.car_data_logger.data_processor;

import java.util.List;
import java.util.Map;

import net.xeoh.plugins.base.Plugin;

public interface DataProcessor extends Plugin {
	public void process(List<Map<String, String>> data);
}
