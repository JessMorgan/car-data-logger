/*
 * Copyright 2012 Jess Morgan
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
package jess.morgan.car_data_logger.decode.gps;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jess.morgan.car_data_logger.decode.AbstractDataDecoder;

public class GPSDataDecoder extends AbstractDataDecoder {
	private static final Pattern PATTERN = Pattern.compile("^\\[\\d+\\] \\$GP([A-Z]{3}),(.*)$");

	public Map<String, String> getAvailableParameters() {
		Map<String, String> parameters = new LinkedHashMap<String, String>();
		parameters.put("Latitude", "*");
		parameters.put("Longitude", "*");
		parameters.put("Altitude", "m");
		parameters.put("GPS Speed", "MPH");
		parameters.put("GPS Course", "*");
		parameters.put("Date", "UTC");
		parameters.put("Time", "UTC");
		parameters.put("Time Zone", "");
		return parameters;
	}

	public Map<String, String> decodeLine(String line) {
		Matcher m = PATTERN.matcher(line);
		if(!m.matches()) {
			return null;
		}

		String messageId = m.group(1);
		String[] data = m.group(2).split(",");

		Map<String, String> values = new HashMap<String, String>();
		if("GGA".equals(messageId)) {
			values.putAll(decodeGGA(data));
		} else if("RMC".equals(messageId)) {
			values.putAll(decodeRMC(data));
		} else if("ZDA".equals(messageId)) {
			values.putAll(decodeZDA(data));
		}

		return values;
	}

	private Map<String, String> decodeGGA(String[] data) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("Time",      decodeTime(      data[0]));
		map.put("Latitude",  decodeCoordinate(data[1], data[2]));
		map.put("Longitude", decodeCoordinate(data[3], data[4]));
		map.put("Altitude",                   data[8]);
		return map;
	}

	private Map<String, String> decodeRMC(String[] data) {
		Map<String, String> map = new HashMap<String, String>();
		if("V".equals(data[1])) {
			// Invalid - skip
			return map;
		}
		map.put("Time",      decodeTime(      data[0]));
		map.put("Latitude",  decodeCoordinate(data[2], data[3]));
		map.put("Longitude", decodeCoordinate(data[4], data[5]));
		try {
			double speedKnots = Double.parseDouble(data[6]);
			map.put("GPS Speed", Double.toString(speedKnots * 1.15078));
		} catch(NumberFormatException nfe) {
			// Meh - maybe speed isn't provided.
		}
		map.put("GPS Course", data[7]);
		// Can also pull date from here, but it only gives a 2 digit year and I'm too lazy to handle date windowing right now
		return map;
	}

	private Map<String, String> decodeZDA(String[] data) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("Time",      decodeTime(      data[0]));
		map.put("Date",      String.format("%s-%s-%s", data[3], data[2], data[1]));
		if(!data[4].isEmpty()) {
			map.put("Time Zone", String.format("%s:%s", data[4], data[5]));
		}
		return map;
	}

	private static final Pattern PATTERN_TIME = Pattern.compile("^(\\d{1,2})(\\d{2})(\\d{2})(?:\\.(\\d+))?$");
	private String decodeTime(String string) {
		Matcher m = PATTERN_TIME.matcher(string);
		if(!m.matches()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(m.group(1))
			.append(':').append(m.group(2))
			.append(':').append(m.group(3));
		if(m.groupCount() == 4 && m.group(4) != null) {
			sb.append('.').append(m.group(4));
		}
		return sb.toString();
	}

	private String decodeCoordinate(String string, String string2) {
		int dotPos = string.indexOf('.');
		if(dotPos < 3) {
			// Invalid - skip
			return "";
		}
		String degrees = string.substring(0, dotPos - 2);
		String minutes = string.substring(dotPos - 2);
		try {
			double degreesNum = Double.parseDouble(degrees);
			double minutesNum = Double.parseDouble(minutes);
			degreesNum += (minutesNum / 60.0);
			if("S".equals(string2) || "W".equals(string2)) {
				degreesNum = -degreesNum;
			}
			return Double.toString(degreesNum);
		} catch(NumberFormatException nfe) {
			// Invalid - skip
			return "";
		}
	}
}
