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
package jess.morgan.car_data_logger.capture;

import java.math.BigDecimal;

public class Connection {
	private final String portName;
	private final int speed;
	private final String initCommand;

	private Connection(String portName, int speed, String initCommand) {
		this.portName = portName;
		this.speed = speed;
		this.initCommand = initCommand;
	}

	/**
	 * Parses arguments in the format port name:speed[suffix].
	 * Examples:
	 * <ul>
	 * <li><b>COM1:9600</b> (COM1, 9600 baud)</li>
	 * <li><b>/dev/ttyS10:9.6k</b> (/dev/ttyS10, 9600 baud)</li>
	 * <li><b>/dev/ttyUSB0:.5m</b> (/dev/ttyUSB0, 500000 baud)</li>
	 * </ul>
	 * @param arg argument to be parsed
	 * @return a Connection from the argument
	 * @throws IllegalArgumentException if the argument cannot be parsed
	 */
	public static Connection createFromArgument(String arg) {
		if(arg == null) {
			throw new IllegalArgumentException();
		}

		String[] parts = arg.split(":");
		if(parts.length != 2) {
			throw new IllegalArgumentException();
		}

		String portName = parts[0];

		char lastChar = Character.toLowerCase(parts[1].charAt(parts[1].length() - 1));
		if(lastChar == 'k' || lastChar == 'm') {
			parts[1] = parts[1].substring(0, parts[1].length() - 1);
		}

		BigDecimal speed;
		try {
			speed = new BigDecimal(parts[1]);
		} catch(NumberFormatException nfe) {
			throw new IllegalArgumentException();
		}

		if(lastChar == 'k') {
			speed = speed.scaleByPowerOfTen(3);
		}
		if(lastChar == 'm') {
			speed = speed.scaleByPowerOfTen(6);
		}

		return new Connection(portName, speed.intValue(), null);
	}

	public static Connection createFromArgument(String arg, String initString) {
		Connection connection = createFromArgument(arg);
		return new Connection(connection.getPortName(), connection.getSpeed(), initString);
	}

	public String getPortName() {
		return portName;
	}

	public int getSpeed() {
		return speed;
	}

	public String getInitCommand() {
		return initCommand;
	}
}
