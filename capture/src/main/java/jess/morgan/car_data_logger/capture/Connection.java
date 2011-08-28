package jess.morgan.car_data_logger.capture;

import java.math.BigDecimal;

public class Connection {
	private final String portName;
	private final int speed;

	private Connection(String portName, int speed) {
		this.portName = portName;
		this.speed = speed;
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

		return new Connection(portName, speed.intValue());
	}

	public String getPortName() {
		return portName;
	}

	public int getSpeed() {
		return speed;
	}
}
