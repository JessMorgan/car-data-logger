package jess.morgan.car_data_logger.app.decode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jess.morgan.car_data_logger.data_processor.DataProcessor;
import jess.morgan.car_data_logger.data_processor.DataProcessorFactory;
import jess.morgan.car_data_logger.decode.AbstractDataDecoder;
import jess.morgan.car_data_logger.decode.DataDecoder;
import jess.morgan.car_data_logger.decode.DataDecoderFactory;
import jess.morgan.car_data_logger.plugin.PluginManager;

public class Application {
	public static void main(String[] args) throws IOException {
		File inFile = new File(args[0]);
		File outFile = new File(args[1]);

		Config config = new Config();
		PluginManager pluginManager = new PluginManager(config.getPluginDirectories());

		List<DataDecoder> decoders = new ArrayList<DataDecoder>();
		for(String decoderClassName : config.getDecoders()) {
			try {
				DataDecoder decoder = pluginManager.loadPlugin(
						decoderClassName,
						config.getDecoderConfig(decoderClassName),
						DataDecoderFactory.class
						);
				if(decoder == null) {
					System.err.println("Decoder plugin not found: " + decoderClassName);
					return;
				}
				decoders.add(decoder);
			} catch (Exception e) {
				System.err.println("Error loading decoder plugin: " + decoderClassName);
				e.printStackTrace();
				return;
			}
		}

		List<DataProcessor> dataProcessors = new ArrayList<DataProcessor>();
		for(String dataProcessorClassName : config.getDataProcessors()) {
			try {
				DataProcessor dataProcessor = pluginManager.loadPlugin(
						dataProcessorClassName,
						config.getDecoderConfig(dataProcessorClassName),
						DataProcessorFactory.class
						);
				if(dataProcessor == null) {
					System.err.println("Data processor plugin not found: " + dataProcessorClassName);
					return;
				}
				dataProcessors.add(dataProcessor);
			} catch (Exception e) {
				System.err.println("Error loading data processor plugin: " + dataProcessorClassName);
				e.printStackTrace();
				return;
			}
		}

		DataDecoder decoder = AbstractDataDecoder.getDecoders(decoders);
		List<Map<String, String>> data = decoder.decodeStream(new FileInputStream(inFile));
		for(DataProcessor dataProcessor : dataProcessors) {
			dataProcessor.process(data);
		}
		decoder.writeData(data, new FileOutputStream(outFile));
	}
}
