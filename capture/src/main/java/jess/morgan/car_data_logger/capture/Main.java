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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class Main {
	public static void main(String[] args) throws Exception {
		Logger logger = new Logger(new File("/tmp/serial_log.out"));
		new Communicator("/dev/ttyUSB0", 9600, logger);
		new Communicator("/dev/ttyS10", 9600, logger);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		br.readLine();
		logger.stop();
	}
}
