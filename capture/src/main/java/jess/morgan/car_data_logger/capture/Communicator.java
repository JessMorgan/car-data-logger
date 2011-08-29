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
import java.util.concurrent.atomic.AtomicBoolean;

public class Communicator {
	private final AtomicBoolean running = new AtomicBoolean(true);
	private Thread thread;

	public Communicator(Connection connection, Logger logger) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
		SerialPort serial = connect(connection.getPortName(), connection.getSpeed());

		try {
			thread = new CommunicatorThread(logger, serial);
			thread.start();
		} catch(IOException ioe) {
			serial.close();
			stop();
		}
	}

	private SerialPort connect(String portName, int speed) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
		System.out.println("Connecting to " + portName + " at " + speed + " baud");

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

	public void stop() {
		this.running.set(false);
		if(thread != null) {
			thread.interrupt();
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
			while(running.get()) {
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
			running.set(false);
			System.out.println("Comm stopped for " + serial.getName());
		}
	}
}
