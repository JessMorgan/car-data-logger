package jess.morgan.car_data_logger.app.decode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jess.morgan.car_data_logger.data_processor.DataProcessor;
import jess.morgan.car_data_logger.decode.AbstractDataDecoder;
import jess.morgan.car_data_logger.decode.DataDecoder;

public class Application {
	public static void main(String[] args) throws IOException {
		File inFile = new File(args[0]);
		File outFile = new File(args[1]);

		Config config = new Config();
		PluginManager pluginManager = new PluginManager(config.getPluginDirectories());

		List<DataDecoder> decoders = new ArrayList<DataDecoder>();
		for(String decoderClassName : config.getDecoders()) {
			try {
				decoders.add(
						pluginManager.loadDecoder(
								decoderClassName,
								config.getDecoderConfig(decoderClassName)
								)
						);
			} catch (Exception e) {
				System.err.println("Error loading decoder plugin: " + decoderClassName);
				e.printStackTrace();
				return;
			}
		}

		List<DataProcessor> dataProcessors = new ArrayList<DataProcessor>();
		for(String dataProcessorClassName : config.getDataProcessors()) {
			try {
				dataProcessors.add(
						pluginManager.loadDataProcessor(
								dataProcessorClassName,
								config.getDecoderConfig(dataProcessorClassName)
								)
						);
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
