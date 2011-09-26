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
package jess.morgan.car_data_logger.decode.can;

public class Data {
	private final String parameterName;
	private final String unit;
	private final String value;

	public Data(String parameterName, String unit, String value) {
		if(isBlank(parameterName)) {
			this.parameterName = null;
		} else {
			this.parameterName = parameterName;
		}

		if(isBlank(unit)) {
			this.unit = null;
		} else {
			this.unit = unit;
		}

		if(isBlank(value)) {
			this.value = null;
		} else {
			this.value = value;
		}

		validate();
	}

	private boolean isBlank(String string) {
		return string == null || string.matches("^\\s*$");
	}

	private void validate() {
		// Required fields
		if(parameterName == null) {
			throw new NullPointerException("parameterName");
		}
	}

	public String getDisplayName() {
		StringBuilder sb = new StringBuilder(parameterName);
		if(unit != null) {
			sb.append(" (").append(unit).append(')');
		}
		return sb.toString();
	}
	public String getParameterName() {
		return parameterName;
	}
	public String getUnit() {
		return unit;
	}
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getDisplayName());
		sb.append(": ").append(value);

		return sb.toString();
	}
}
