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
	public final void decodeStream(InputStream is, OutputStream os) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		PrintWriter out = new PrintWriter(os);

		List<String> header = getAvailableParameters();
		header.add(0, "timestamp");
		try {
			// Print file header
			out.println(buildLineCSV(header));

			String line;
			while(null != (line = br.readLine())) {
				// Decode line
				Map<String, String> data = decodeLine(line);
				// Match up position of decoded items with proper labels
				List<String> values = new ArrayList<String>();
				for(String parameter : header) {
					String value = data.get(parameter);
					if(value == null) {
						value = "";
					}
					value.replace(',', ';');
					values.add(value);
				}
				// Print decoded line
				out.println(buildLineCSV(values));
			}
		} finally {
			try {
				br.close();
			} catch(IOException ioe) {
				// Ignore
			}
			out.close();
		}
	}

	private String buildLineCSV(List<String> values) {
		StringBuilder sb = new StringBuilder();
		for(String value : values) {
			sb.append(value).append(',');
		}
		return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
	}
}
