/*
 * Copyright 2011 Jess Morgan
 *
 * This file is part of car-data-logger.
 *
 * car-data-logger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * car-data-logger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with car-data-logger.  If not, see <http://www.gnu.org/licenses/>.
 */
package jess.morgan.car_data_logger.decode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractDataDecoder implements DataDecoder {
	public static DataDecoder getDecoder(String className, String parameter) throws Exception {
		Class<?> clazz = Class.forName(className);
		DataDecoder instance;
		if(parameter == null) {
			instance = (DataDecoder) clazz.newInstance();
		} else {
			Constructor<?> constructor = clazz.getConstructor(String.class);
			instance = (DataDecoder) constructor.newInstance(parameter);
		}
		return instance;
	}
	public static DataDecoder getDecoders(List<List<String>> config) throws Exception {
		List<DataDecoder> decoders = new ArrayList<DataDecoder>();
		decoders.add(new TimestampDataDecoder());
		for(List<String> decoderConfig : config) {
			if(decoderConfig.size() == 1) {
				decoders.add(getDecoder(decoderConfig.get(0), null));
			} else {
				decoders.add(getDecoder(decoderConfig.get(0), decoderConfig.get(1)));
			}
		}
		return new CompositeDataDecoder(decoders);
	}

	public final List<Map<String, String>> decodeStream(InputStream is) throws IOException {
		final List<Map<String, String>> allData = new ArrayList<Map<String, String>>();
		streamDriver(is, new DataHandler() {
			@Override
			public void handleData(Map<String, String> data) {
				allData.add(data);
			}
		});

		return allData;
	}

	public final void decodeStream(InputStream is, OutputStream os) throws IOException {
		final PrintWriter out = new PrintWriter(os);
		final List<String> header = getAvailableParameters();

		try {
			// Print file header
			out.println(buildLineCSV(header));

			streamDriver(is, new DataHandler() {
				@Override
				public void handleData(Map<String, String> data) {
					// Match up position of decoded items with proper labels
					List<String> values = new ArrayList<String>();
					for(String parameter : header) {
						values.add(data.get(parameter));
					}
					// Print decoded line
					out.println(buildLineCSV(values));
				}
			});
		} finally {
			out.close();
		}
	}

	private String buildLineCSV(List<String> values) {
		StringBuilder sb = new StringBuilder();
		for(String value : values) {
			sb.append(value == null ? "" : value.replace(',', ';')).append(',');
		}
		return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
	}

	private interface DataHandler {
		public void handleData(Map<String, String> data);
	}
	private void streamDriver(InputStream is, DataHandler handler) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		try {
			String line;
			while(null != (line = br.readLine())) {
				// Decode line
				handler.handleData(decodeLine(line));
			}
		} finally {
			try {
				br.close();
			} catch(IOException ioe) {
				// Ignore
			}
		}
	}

	public static void main(String[] args) throws Exception {
		DataDecoder timestamp = AbstractDataDecoder.getDecoder("jess.morgan.car_data_logger.decode.timestamp.TimestampDataDecoder", null);
		DataDecoder can = AbstractDataDecoder.getDecoder("jess.morgan.car_data_logger.decode.can.CANDataDecoder", "../decode-can/config/2004-mazda-rx8-us.cfg");
		DataDecoder composite = new CompositeDataDecoder(timestamp, can);
		System.out.println(composite.decodeLine("[1724435917370] 201 8C B7 FF FF 51 FB 76 FF"));
	}
}
