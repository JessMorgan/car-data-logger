package jess.morgan.car_data_logger.decode.can.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ConfigFile {
	public static List<Config> readConfig(File f) throws IOException {
		return readConfig(new FileInputStream(f));
	}

	public static List<Config> readConfig(InputStream is) throws IOException {
		List<Config> configs = new ArrayList<Config>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			String line;
			Config config;
			while(null != (line = br.readLine())) {
				config = decodeConfigLine(line);
				if(config != null) {
					configs.add(config);
				}
			}
		} finally {
			try {
				br.close();
			} catch(IOException ioe) {
				// ignore
			}
		}

		return configs;
	}

	private static Config decodeConfigLine(String line) {
		// Skip blank lines and comments (starting with #)
		if(line.matches("^\\s*$") || line.matches("^\\s*#.*")) {
			return null;
		}

		String[] parts = line.split(",");
		if(parts.length != 6) {
			System.err.println("Bad config line - expected 6 fields, got " + parts.length + ": " + line);
			return null;
		}

		int startByte;
		int endByte;
		try {
			startByte = decodeNumber(parts[3], line);
			endByte = decodeNumber(parts[4], line);
		} catch(NumberFormatException nfe) {
			return null;
		}

		return new Config(parts[0], parts[1], parts[2], startByte, endByte, parts[5]);
	}

	private static int decodeNumber(String number, String line) {
		try {
			return Integer.parseInt(number);
		} catch(NumberFormatException nfe) {
			System.err.println("Invalid number (" + number + "): " + line);
			throw nfe;
		}
	}
}
