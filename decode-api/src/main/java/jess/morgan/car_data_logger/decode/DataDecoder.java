package jess.morgan.car_data_logger.decode;

import java.util.List;
import java.util.Map;

public interface DataDecoder {
	public List<String> getAvailableParameters();
	public Map<String, String> decodeData(String data);
}
