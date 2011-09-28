package jess.morgan.car_data_logger.interpolate;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jess.morgan.car_data_logger.decode.AbstractDataDecoder;
import jess.morgan.car_data_logger.decode.DataDecoder;

public class LastValueInterpolator implements Interpolator {
	@Override
	public void interpolate(List<Map<String, String>> data, boolean interpolateEnds) {
		Map<String, String> lastValue = null;
		for(Map<String, String> line : data) {
			if(lastValue == null) {
				lastValue = line;
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

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		System.out.println("Loading decoders");
		DataDecoder decoder =
				AbstractDataDecoder.getDecoders(
						Collections.singletonList(
								Arrays.asList(
										"jess.morgan.car_data_logger.decode.can.CANDataDecoder", "../../decode/decode-can/config/2004-mazda-rx8-us.cfg")));
		System.out.println("Decoders loaded (" + (System.currentTimeMillis() - startTime) + ") - decoding file");
		startTime = System.currentTimeMillis();
		List<Map<String, String>> data = decoder.decodeStream(new FileInputStream("/home/jess/car-data-logs/autocross-2011-09-17/run6.cropped.log"));
		System.out.println("Decoding completed (" + (System.currentTimeMillis() - startTime) + ") - beginning interpolation");
		startTime = System.currentTimeMillis();
		new LastValueInterpolator().interpolate(data, true);
		System.out.println("Interpolation completed (" + (System.currentTimeMillis() - startTime) + ") - writing to file");
		startTime = System.currentTimeMillis();
		decoder.writeData(data, new FileOutputStream("/home/jess/car-data-logs/autocross-2011-09-17/run6.last-value.csv"));
		System.out.println("Writing to file complete (" + (System.currentTimeMillis() - startTime) + ")");
	}
}
