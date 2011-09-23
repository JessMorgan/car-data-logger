package jess.morgan.car_data_logger.decode_can.config;

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
