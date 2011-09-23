package jess.morgan.car_data_logger.decode_can;

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
