package jess.morgan.car_data_logger.plugin;

import java.util.Map;

public class ConfigParameter {
	private final String name;
	private final String displayName;
	private final Class<?> type;
	private final boolean required;

	public ConfigParameter(String name, String displayName, Class<?> type, boolean required) {
		this.name = name;
		this.displayName = displayName;
		this.type = type;
		this.required = required;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Class<?> getType() {
		return type;
	}

	public boolean isRequired() {
		return required;
	}

	public String toString() {
		return new StringBuilder(this.getClass().getSimpleName())
				.append("{displayName:'").append(displayName).append("', ")
				.append("type:").append(type.getSimpleName()).append("}")
				.toString();
	}

	@SuppressWarnings("unchecked")
	public static <T> T getParameter(Map<String, Object> config, String parameterName, Class<T> type, boolean required) {
		Object value = null;
		if((config == null || (value = config.get(parameterName)) == null) && required) {
			throw new IllegalArgumentException(parameterName + " is required");
		}
		if(value != null && !(type.isInstance(value))) {
			throw new IllegalArgumentException(
					parameterName + " is a " + value.getClass().getSimpleName()
					+ ", but must be a " + type.getSimpleName());
		}
		return (T)value;
	}
}
