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
package jess.morgan.car_data_logger.video_overlay.gauge.text;

import java.awt.Color;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import jess.morgan.car_data_logger.plugin.ConfigParameter;
import jess.morgan.car_data_logger.video_overlay.gauge.Gauge;
import jess.morgan.car_data_logger.video_overlay.gauge.GaugeFactory;

@PluginImplementation
public class TextGaugeFactory implements GaugeFactory {
	@Override
	public String getPluginDisplayName() {
		return "GPS Track Gauge";
	}

	@Override
	public ConfigParameter[] getConfigParameters() {
		return new ConfigParameter[]{
				new ConfigParameter("parameter",        "Parameter Name",                  String.class,  true),
				new ConfigParameter("format",           "printf format",                   String.class,  false),
				new ConfigParameter("timeFormat",       "format for time value",           String.class,  false),
				new ConfigParameter("emptyValue",       "Value to display for empty/null", String.class,  false),
				new ConfigParameter("textColor",        "Text Color",                      Color.class,   false),
		};
	}

	@Override
	public Gauge getPlugin(Map<String, Object> config) throws Exception {
		String parameterName    = ConfigParameter.getParameter(config, "parameter",      String.class,   true);
		String format           = ConfigParameter.getParameter(config, "format",         String.class, false);
		String timeFormat       = ConfigParameter.getParameter(config, "timeFormat",     String.class, false);
		String emptyValue       = ConfigParameter.getParameter(config, "emptyValue",     String.class, false);
		Color textColor         = ConfigParameter.getParameter(config, "textColor",      Color.class,  false);

		return new TextGauge(parameterName, format, timeFormat, emptyValue, textColor);
	}
}
