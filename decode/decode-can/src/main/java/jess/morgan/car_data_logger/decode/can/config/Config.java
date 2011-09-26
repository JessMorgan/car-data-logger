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
package jess.morgan.car_data_logger.decode.can.config;

public class Config {
	private final String parameterName;
	private final String unit;
	private final String messageId;
	private final int startByte;
	private final int endByte;
	private final String algorithm;

	public Config(String parameterName, String unit, String messageId, int startByte, int endByte, String algorithm) {
		this.parameterName = parameterName;
		this.unit = unit;
		this.messageId = messageId;
		this.startByte = startByte;
		this.endByte = endByte;
		this.algorithm = algorithm;

		validate();
	}

	private void validate() {
		// Required fields
		if(parameterName == null) {
			throw new NullPointerException("parameterName");
		}
		if(messageId == null) {
			throw new NullPointerException("messageId");
		}
		if(algorithm == null) {
			throw new NullPointerException("algorithm");
		}

		// Illegal ranges
		if(startByte < 0 || startByte >= 16) {
			throw new IllegalArgumentException("startByte expected range: 0-15");
		}
		if(endByte < 0 || endByte >= 16) {
			throw new IllegalArgumentException("endByte expected range: 0-15");
		}
		if(startByte > endByte) {
			throw new IllegalArgumentException("Expected startByte < endByte");
		}

		if(!messageId.matches("^[0-9a-fA-F]+$")) {
			throw new IllegalArgumentException("Expected messageId to be valid hexadecimal number");
		}
	}

	public String getParameterName() {
		return parameterName;
	}
	public String getUnit() {
		return unit;
	}
	public String getMessageId() {
		return messageId;
	}
	public int getStartByte() {
		return startByte;
	}
	public int getEndByte() {
		return endByte;
	}
	public String getAlgorithm() {
		return algorithm;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ParameterName: ").append(parameterName)
			.append(", Unit: ").append(unit)
			.append(", MessageID: ").append(messageId)
			.append(", StartByte: ").append(startByte)
			.append(", EndByte: ").append(endByte)
			.append(", Algorithm: ").append(algorithm);

		return sb.toString();
	}
}
