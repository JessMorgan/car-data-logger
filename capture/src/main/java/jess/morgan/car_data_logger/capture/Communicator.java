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

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Communicator {
	public Communicator(String commPort, int speed, Logger logger) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
		SerialPort serial = connect(commPort, speed);

		try {
			new CommunicatorThread(logger, serial).start();
		} catch(IOException ioe) {
			serial.close();
		}
	}

	private SerialPort connect(String portName, int speed) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		if(portIdentifier.isCurrentlyOwned()) {
			throw new PortInUseException();
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);

			if(commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				return serialPort;
			} else {
				commPort.close();
				throw new RuntimeException("Port is not a serial port");
			}
		}
	}

	private class CommunicatorThread extends Thread {
		private final Logger logger;
		private final BufferedReader br;
		private final SerialPort serial;

		public CommunicatorThread(Logger logger, SerialPort serial) throws IOException {
			this.logger = logger;
			this.serial = serial;
			this.br = new BufferedReader(new InputStreamReader(serial.getInputStream()));

			setName("Communicator " + serial.getName());
		}

		@Override
		public void run() {
			while(logger.isRunning()) {
				String line;
				try {
					line = br.readLine();
				} catch (IOException e) {
					break;
				}
				if(line == null) {
					break;
				}
				logger.logMessage(line);
			}
			try {
				br.close();
			} catch (IOException e) {
				// Ignore
			}
			serial.close();
		}
	}
}
