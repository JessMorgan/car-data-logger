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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface DataDecoder {
	/**
	 * @return the parameters this data decoder can return, with parameter name as key and unit as value
	 */
	public Map<String, String> getAvailableParameters();
	public void decodeStream(InputStream is, OutputStream os) throws IOException;
	public List<Map<String, String>> decodeStream(InputStream is) throws IOException;
	public void writeData(List<Map<String, String>> data, OutputStream os);
	public Map<String, String> decodeLine(String line);
}
