package jess.morgan.car_data_logger.decode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CompositeDataDecoder extends AbstractDataDecoder {
	private final List<DataDecoder> decoders;

	public CompositeDataDecoder(DataDecoder... decoders) {
		this(Arrays.asList(decoders));
	}
	public CompositeDataDecoder(List<DataDecoder> decoders) {
		this.decoders = decoders;
	}

	public List<String> getAvailableParameters() {
		List<String> parameters = new ArrayList<String>();
		for(DataDecoder decoder : decoders) {
			parameters.addAll(decoder.getAvailableParameters());
		}
		return parameters;
	}

	public Map<String, String> decodeLine(String line) {
		Map<String, String> data = new LinkedHashMap<String, String>();
		Map<String, String> thisData;
		for(DataDecoder decoder : decoders) {
			thisData = decoder.decodeLine(line);
			if(thisData != null) {
				data.putAll(thisData);
			}
		}
		return data;
	}
}
