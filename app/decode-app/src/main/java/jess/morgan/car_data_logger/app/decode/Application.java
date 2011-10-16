package jess.morgan.car_data_logger.app.decode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jess.morgan.car_data_logger.decode.AbstractDataDecoder;
import jess.morgan.car_data_logger.decode.DataDecoder;
import jess.morgan.car_data_logger.interpolate.Interpolator;

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

		Interpolator interpolator = pluginManager.loadInterpolator(config.getInterpolator());

		DataDecoder decoder = AbstractDataDecoder.getDecoders(decoders);
		List<Map<String, String>> data = decoder.decodeStream(new FileInputStream(inFile));
		interpolator.interpolate(data, false);
		decoder.writeData(data, new FileOutputStream(outFile));
	}
}
