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

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimestampDataDecoder extends AbstractDataDecoder {
	private static final Pattern PATTERN = Pattern.compile("^\\[(\\d+)\\].*");

	public Map<String, String> getAvailableParameters() {
		return Collections.singletonMap("Timestamp", "nanoseconds");
	}

	public Map<String, String> decodeLine(String line) {
		Matcher m = PATTERN.matcher(line);
		if(!m.matches()) {
			return null;
		}

		String timestamp = m.group(1);
		return Collections.singletonMap("Timestamp", timestamp);
	}
}
