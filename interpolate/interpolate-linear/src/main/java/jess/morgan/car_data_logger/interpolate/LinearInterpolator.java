package jess.morgan.car_data_logger.interpolate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.AddPluginsFromOption;
import net.xeoh.plugins.base.util.PluginManagerUtil;

import jess.morgan.car_data_logger.decode.AbstractDataDecoder;
import jess.morgan.car_data_logger.decode.ConfigParameter;
import jess.morgan.car_data_logger.decode.DataDecoder;
import jess.morgan.car_data_logger.decode.DataDecoderFactory;

public class LinearInterpolator implements Interpolator {
	@Override
	public void interpolate(List<Map<String, String>> data, boolean interpolateEnds) {
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

				double firstValue = Double.parseDouble(min.getValue());
				double lastValue = Double.parseDouble(max.getValue());
				double valueDifference = lastValue - firstValue;
				long minMaxTimeDifference = max.getKey() - min.getKey();
				long minCurrentTimeDifference = timestamp - min.getKey();
				double ratio = (double)minCurrentTimeDifference / minMaxTimeDifference;
				double newValue = firstValue + (valueDifference * ratio);
				line.put(field, Double.toString(newValue));
			}
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Loading decoders");
		long startTime = System.currentTimeMillis();

		PluginManagerUtil pluginManager = new PluginManagerUtil(PluginManagerFactory.createPluginManager());
		pluginManager.addPluginsFrom(
				new File("/home/jess/.m2/repository/jess/morgan/car-data-logger/decode-can/0.1.0-SNAPSHOT/decode-can-0.1.0-SNAPSHOT.jar").toURI(),
				(AddPluginsFromOption[])null);
		Collection<DataDecoderFactory> dataDecoderFactories = pluginManager.getPlugins(DataDecoderFactory.class);
		List<DataDecoder> decoders = new ArrayList<DataDecoder>();
		File canConfigFile = new File("/home/jess/eclipse-workspace/car-data-logger/decode/decode-can/config/2004-mazda-rx8-us.cfg");
		for(DataDecoderFactory factory : dataDecoderFactories) {
			System.out.println("Plugin loaded: " + factory.getPluginDisplayName());

			Map<String, Object> config = new HashMap<String, Object>();
			ConfigParameter[] params = factory.getConfigParameters();
			for(ConfigParameter param : params) {
				if(param.isRequired()) {
					System.out.println("Required parameter: " + param);
				} else {
					System.out.println("Optional parameter: " + param);
				}
				if("CANDataDecoderFactory".equals(factory.getClass().getSimpleName())
						&& "configFile".equals(param.getName())
						&& File.class.equals(param.getType())) {
					System.out.println("Supplying value: " + canConfigFile.getPath());
					config.put(param.getName(), canConfigFile);
				}
			}

			decoders.add(factory.getDecoder(config));
		}

		DataDecoder decoder = AbstractDataDecoder.getDecoders(decoders);
		System.out.println("Decoders loaded (" + (System.currentTimeMillis() - startTime) + ") - decoding file");
		startTime = System.currentTimeMillis();
		List<Map<String, String>> data = decoder.decodeStream(new FileInputStream("/home/jess/car-data-logs/autocross-2011-09-17/run6.cropped.log"));
		System.out.println("Decoding completed (" + (System.currentTimeMillis() - startTime) + ") - beginning interpolation");
		startTime = System.currentTimeMillis();
		new LinearInterpolator().interpolate(data, true);
		System.out.println("Interpolation completed (" + (System.currentTimeMillis() - startTime) + ") - writing to file");
		startTime = System.currentTimeMillis();
		decoder.writeData(data, new FileOutputStream("/home/jess/car-data-logs/autocross-2011-09-17/run6.linear.csv"));
		System.out.println("Writing to file complete (" + (System.currentTimeMillis() - startTime) + ")");
	}
}
