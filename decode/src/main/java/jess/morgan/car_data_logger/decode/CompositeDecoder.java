package jess.morgan.car_data_logger.decode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CompositeDecoder implements DataDecoder {
	private final List<DataDecoder> decoders;

	public CompositeDecoder(DataDecoder... decoders) {
		this(Arrays.asList(decoders));
	}
	public CompositeDecoder(List<DataDecoder> decoders) {
		this.decoders = decoders;
	}

	public List<String> getAvailableParameters() {
		List<String> parameters = new ArrayList<String>();
		for(DataDecoder decoder : decoders) {
			parameters.addAll(decoder.getAvailableParameters());
		}
		return parameters;
	}

	public Map<String, String> decodeData(String line) {
		Map<String, String> data = new LinkedHashMap<String, String>();
		Map<String, String> thisData;
		for(DataDecoder decoder : decoders) {
			thisData = decoder.decodeData(line);
			if(thisData != null) {
				data.putAll(thisData);
			}
		}
		return data;
	}
}
