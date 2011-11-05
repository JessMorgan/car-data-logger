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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CompositeDataDecoder extends AbstractDataDecoder {
	private final List<DataDecoder> decoders;

	public CompositeDataDecoder(DataDecoder... decoders) {
		this(Arrays.asList(decoders));
	}
	public CompositeDataDecoder(List<DataDecoder> decoders) {
		this.decoders = decoders;
	}

	public Map<String, String> getAvailableParameters() {
		Map<String, String> parameters = new LinkedHashMap<String, String>();
		for(DataDecoder decoder : decoders) {
			parameters.putAll(decoder.getAvailableParameters());
		}
		return parameters;
	}

	public Map<String, String> decodeLine(String line) {
		Map<String, String> data = new LinkedHashMap<String, String>();
		Map<String, String> thisData;
		for(DataDecoder decoder : decoders) {
			thisData = decoder.decodeLine(line);
			if(thisData != null) {
				data.putAll(thisData);
			}
		}
		return data;
	}
}
