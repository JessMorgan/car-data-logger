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
package jess.morgan.car_data_logger.video_overlay.gauge.gps_track;

import java.io.File;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import jess.morgan.car_data_logger.plugin.ConfigParameter;
import jess.morgan.car_data_logger.video_overlay.gauge.Gauge;
import jess.morgan.car_data_logger.video_overlay.gauge.GaugeFactory;

@PluginImplementation
public class GPSTrackGaugeFactory implements GaugeFactory {
	@Override
	public String getPluginDisplayName() {
		return "GPS Track Gauge";
	}

	@Override
	public ConfigParameter[] getConfigParameters() {
		return new ConfigParameter[]{
				new ConfigParameter("track",           "Track file",                              File.class,    true),
				new ConfigParameter("opacity",         "Opacity",                                 Double.class,  false),
		};
	}

	@Override
	public Gauge getPlugin(Map<String, Object> config) throws Exception {
		File track               = ConfigParameter.getParameter(config, "track",           File.class,   true);
		Double opacity           = ConfigParameter.getParameter(config, "opacity",         Double.class, false);

		return new GPSTrackGauge(track, opacity);
	}
}
