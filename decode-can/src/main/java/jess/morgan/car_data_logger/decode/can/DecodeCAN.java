package jess.morgan.car_data_logger.decode.can;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jess.morgan.car_data_logger.decode.DataDecoder;
import jess.morgan.car_data_logger.decode.can.config.Config;
import jess.morgan.car_data_logger.decode.can.config.ConfigFile;
import jess.morgan.car_data_logger.decode.can.eval.BuiltInJavaScriptEvalImpl;
import jess.morgan.car_data_logger.decode.can.eval.Eval;
import jess.morgan.car_data_logger.decode.can.eval.EvalException;

public class DecodeCAN implements DataDecoder {
	private static final Pattern PATTERN = Pattern.compile("^\\[(\\d+)\\] ([0-9a-fA-F]+) ([0-9a-fA-F ]+)$");
	private final Map<String, List<Config>> config;
	private final Eval eval;

	public DecodeCAN(File configFile) throws IOException {
		this(ConfigFile.readConfig(configFile));
	}
	public DecodeCAN(InputStream configStream) throws IOException {
		this(ConfigFile.readConfig(configStream));
	}

	public DecodeCAN(List<Config> configList) {
		eval = new BuiltInJavaScriptEvalImpl();

		config = new LinkedHashMap<String, List<Config>>();
		// Fill in map - each message id should have a list of associated configs
		for(Config c : configList) {
			List<Config> messageConfigs = config.get(c.getMessageId());
			if(messageConfigs == null) {
				messageConfigs = new ArrayList<Config>();
				config.put(c.getMessageId(), messageConfigs);
			}
			messageConfigs.add(c);
		}
	}

	public List<String> getAvailableParameters() {
		List<String> parameters = new ArrayList<String>();
		for(List<Config> configList : config.values()) {
			for(Config c : configList) {
				parameters.add(new Data(c.getParameterName(), c.getUnit(), null).getDisplayName());
			}
		}
		return parameters;
	}

	public Map<String, String> decodeData(String line) {
		Matcher m = PATTERN.matcher(line);
		if(!m.matches()) {
			return null;
		}

		String timestamp = m.group(1);
		String messageId = m.group(2);
		String data = m.group(3);
		String[] bytes = data.split("\\s+");

		List<Config> messageConfigs = config.get(messageId);
		if(messageConfigs == null || messageConfigs.isEmpty()) {
			return null;
		}

		Map<String, String> values = new HashMap<String, String>();
		values.put("timestamp", timestamp);

		long dataValue;
		for(Config messageConfig : messageConfigs) {
			dataValue = getDataValue(messageConfig, bytes, line);
			String script = scriptSubstitution(messageConfig.getAlgorithm(), dataValue);
			String value;
			try {
				value = eval.eval(script);
			} catch (EvalException e) {
				e.printStackTrace();
				continue;
			}
			Data d = new Data(messageConfig.getParameterName(), messageConfig.getUnit(), value);
			values.put(d.getDisplayName(), d.getValue());
		}

		return values;
	}

	private String scriptSubstitution(String algorithm, long dataValue) {
		return algorithm.replace("{}", Long.toString(dataValue));
	}
	private long getDataValue(Config messageConfig, String[] bytes, String line) {
		if(messageConfig.getEndByte() >= bytes.length) {
			System.err.println("Expected at least " + messageConfig.getEndByte() + " data bytes, received " + bytes.length + " (" + messageConfig + ") " + line);
			throw new IllegalArgumentException("Message data not long enough");
		}

		StringBuilder sb = new StringBuilder();
		for(int i = messageConfig.getStartByte(); i <= messageConfig.getEndByte(); i++) {
			sb.append(bytes[i]);
		}
		try {
			return Long.parseLong(sb.toString(), 16);
		} catch(NumberFormatException nfe) {
			System.err.println("Illegal value found: " + sb.toString() + " (" + messageConfig + ") " + line);
			throw new IllegalArgumentException(nfe);
		}
	}
	public static void main(String[] args) throws IOException {
		DataDecoder decoder = new DecodeCAN(new File("config/2004-mazda-rx8-us.cfg"));
		for(String param : decoder.getAvailableParameters()) {
			System.out.println(param);
		}
		System.out.println();
		Map<String, String> data = decoder.decodeData("[1724435917370] 201 8C B7 FF FF 51 FB 76 FF");
		for(Map.Entry<String, String> entry : data.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
	}
}
