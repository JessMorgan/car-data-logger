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
import java.util.ArrayList;
import java.util.List;

public class Main {
	public static void usage() {
		System.out.println("USAGE: java -jar capture.jar -o outfile.log <comm port>:speed [--elm elm-init-commands] <comm port>:speed ...");
		System.out.println();
		System.out.println("Comm port should be com1, com2, etc. on Windows - or /dev/ttyUSB0, /dev/ttyS0, etc. on Linux");
		System.out.println("Arduino uses /dev/ttyACM0 on Linux - you need to create a symlink to /dev/ttyS10 or something");
		System.out.println("Speed parameter can use suffixes (i.e. 500,000 can be 500000, 500k, or .5m)");
		System.out.println("Elm initialization commands in the form 'ATZ;ATZ;ATSP6;ATCAF0;ATH1;ATAL;ATMA'.  Semicolons will be replaced with carriage returns, and a carriage return will be appended to the entire command string.  These commands will be applied to the first comm port after the elm init command string.");
	}

	public static void main(String[] args) throws Exception {
		if(args == null || args.length == 0) {
			usage();
			return;
		}

		File outfile = null;
		List<Connection> connections = new ArrayList<Connection>();
		for(int i = 0; i < args.length; i++) {
			if(args[i].equalsIgnoreCase("help")
					|| args[i].equalsIgnoreCase("-h")
					|| args[i].equalsIgnoreCase("--help")
					|| args[i].equalsIgnoreCase("-help")
					|| args[i].equalsIgnoreCase("/h")
					|| args[i].equalsIgnoreCase("/help")) {
				usage();
				return;
			} else if(args[i].equalsIgnoreCase("-o")) {
				i++;
				if(i >= args.length) {
					usage();
					return;
				}
				outfile = new File(args[i]);
			} else if(args[i].equalsIgnoreCase("--elm")) {
				i += 2;
				if(i >= args.length) {
					usage();
					return;
				}

				String initString = args[i - 1];
				try {
					Connection connection = Connection.createFromArgument(args[i], initString);
					connections.add(connection);
				} catch(IllegalArgumentException iae) {
					usage();
					return;
				}
			} else {
				try {
					Connection connection = Connection.createFromArgument(args[i]);
					connections.add(connection);
				} catch(IllegalArgumentException iae) {
					usage();
					return;
				}
			}
		}

		if(outfile == null) {
			System.out.println("You need to specify an output file:");
			usage();
			return;
		}
		if(connections.isEmpty()) {
			System.out.println("You need to specify some serial ports:");
			usage();
			return;
		}

		Logger logger = new Logger(outfile);
		List<Communicator> communicators = new ArrayList<Communicator>();
		for(Connection connection : connections) {
			communicators.add(new Communicator(connection, logger));
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		br.readLine();
		System.out.println("Stopping system...");
		for(Communicator communicator : communicators) {
			communicator.stop();
		}
		logger.stop();
	}
}
