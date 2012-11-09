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

import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import jess.morgan.car_data_logger.decode.DataDecoder;
import jess.morgan.car_data_logger.decode.DataDecoderFactory;
import jess.morgan.car_data_logger.plugin.ConfigParameter;

@PluginImplementation
public class GPSDataDecoderFactory implements DataDecoderFactory {
	@Override
	public String getPluginDisplayName() {
		return "GPS Data Decoder";
	}

	@Override
	public ConfigParameter[] getConfigParameters() {
		return new ConfigParameter[]{
		};
	}

	@Override
	public DataDecoder getPlugin(Map<String, Object> config) throws Exception {
		return new GPSDataDecoder();
	}
}
