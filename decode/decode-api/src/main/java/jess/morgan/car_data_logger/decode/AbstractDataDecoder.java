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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractDataDecoder implements DataDecoder {
	public static DataDecoder getDecoders(List<DataDecoder> decoders) {
		List<DataDecoder> _decoders = new ArrayList<DataDecoder>();
		_decoders.add(new TimestampDataDecoder());
		_decoders.addAll(decoders);
		return new CompositeDataDecoder(_decoders);
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

	public final void writeData(List<Map<String, String>> data, OutputStream os) {
		final PrintWriter out = new PrintWriter(os);
		final List<String> header = getAvailableParameters();

		try {
			// Print file header
			out.println(buildLineCSV(header));

			for(Map<String, String> line : data) {
				// Match up position of decoded items with proper labels
				List<String> values = new ArrayList<String>();
				for(String parameter : header) {
					values.add(line.get(parameter));
				}
				// Print decoded line
				out.println(buildLineCSV(values));
			}
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
}
