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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Logger {
	private final PrintWriter out;
	private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>();
	private final AtomicBoolean running = new AtomicBoolean(true);
	private final LoggingThread loggingThread;

	public Logger(File outFile) throws IOException {
		this.out = new PrintWriter(new BufferedWriter(new FileWriter(outFile, true)));

		loggingThread = new LoggingThread();
		loggingThread.start();
	}

	public void logMessage(String data) {
		if(data != null && data.trim().length() > 0) {
			// We will lose items if over capacity.
			// Capacity is extremely high (Integer.MAX_VALUE), so this is unlikely.
			this.messageQueue.offer(new Message(System.nanoTime(), data));
		}
	}

	public boolean isRunning() {
		return running.get();
	}

	public void stop() {
		this.running.set(false);
		loggingThread.interrupt();
		System.out.println("Stopping logger - writing remaining " + messageQueue.size() + " messages...");
	}

	private class LoggingThread extends Thread {
		public LoggingThread() {
			setName("Logger");
		}

		@Override
		public void run() {
			while(isRunning() || !messageQueue.isEmpty()) {
				Message message;
				try {
					message = messageQueue.take();
				} catch (InterruptedException e) {
					if(isRunning() || !messageQueue.isEmpty()) {
						continue;
					} else {
						break;
					}
				}
				if(message != null) {
					out.printf("[%d] %s\n", message.getTimestamp(), message.getMessage());
				}
			}
			out.close();
			running.set(false);
			System.out.println("Logger stopped");
		}
	}
}
