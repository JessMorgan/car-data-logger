package jess.morgan.car_data_logger.app.decode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Config {
	private static final File configFile = new File("config.properties");
	private List<File> pluginDirectories;
	private List<String> decoders;
	private Map<String, Map<String, String>> decoderConfig;
	private List<String> dataProcessors;
	private Map<String, Map<String, String>> dataProcessorConfig;

	public Config() {
		try {
			loadConfigFile();
		} catch (IOException e) {
			System.out.println("Cannot load configuration file, using defaults (" + e.getLocalizedMessage() + ")");
			loadDefaults();
		}
	}

	public void loadDefaults() {
		pluginDirectories = Collections.singletonList(new File("plugins"));
		decoders = null;
		decoderConfig = new HashMap<String, Map<String, String>>();
		dataProcessors = null;
		dataProcessorConfig = new HashMap<String, Map<String, String>>();
	}

	public void loadConfigFile() throws IOException {
		Properties properties;
		InputStream is = new FileInputStream(configFile);
		try {
			properties = new Properties();
			properties.load(is);
		} finally {
			is.close();
		}
		try {
			pluginDirectories = new ArrayList<File>();
			for(int i = 1; ; i++) {
				String path = properties.getProperty("plugin.path." + i);
				if(path == null) {
					if(pluginDirectories.isEmpty()) {
						System.out.println("Invalid properties file, using defaults (property missing: 'plugin.path." + i + "')");
						loadDefaults();
						return;
					}
					break;
				}
				pluginDirectories.add(new File(path));
			}

			decoders = new ArrayList<String>();
			decoderConfig = new HashMap<String, Map<String, String>>();
			for(int i = 1; ; i++) {
				String plugin = properties.getProperty("plugin.decode." + i);
				if(plugin == null) {
					if(decoders.isEmpty()) {
						System.out.println("Invalid properties file, using defaults (property missing: 'plugin.decode." + i + "')");
						loadDefaults();
						return;
					}
					break;
				}
				decoders.add(plugin);

				// Load config for this plugin
				Map<String, String> config = new HashMap<String, String>();
				for(int j = 1; ; j++) {
					String key   = properties.getProperty("plugin.decode." + i + ".config." + j + ".key");
					String value = properties.getProperty("plugin.decode." + i + ".config." + j + ".value");
					if(key == null) {
						break;
					}
					config.put(key, value);
				}
				decoderConfig.put(plugin, config);
			}

			dataProcessors = new ArrayList<String>();
			dataProcessorConfig = new HashMap<String, Map<String, String>>();
			for(int i = 1; ; i++) {
				String plugin = properties.getProperty("plugin.data_processor." + i);
				if(plugin == null) {
					if(dataProcessors.isEmpty()) {
						System.out.println("Invalid properties file, using defaults (property missing: 'plugin.data_processor." + i + "')");
						loadDefaults();
						return;
					}
					break;
				}
				dataProcessors.add(plugin);

				// Load config for this plugin
				Map<String, String> config = new HashMap<String, String>();
				for(int j = 1; ; j++) {
					String key   = properties.getProperty("plugin.data_processor." + i + ".config." + j + ".key");
					String value = properties.getProperty("plugin.data_processor." + i + ".config." + j + ".value");
					if(key == null) {
						break;
					}
					config.put(key, value);
				}
				dataProcessorConfig.put(plugin, config);
			}
		} catch(NumberFormatException nfe) {
			System.err.println("Invalid properties file, using defaults (" + nfe.getLocalizedMessage() + ")");
			loadDefaults();
		}
	}

	public void saveConfigFile() {
		// TODO
	}

	public List<File> getPluginDirectories() {
		return Collections.unmodifiableList(pluginDirectories);
	}

	public void setPluginDirectories(List<File> pluginDirectories) {
		this.pluginDirectories = new ArrayList<File>(pluginDirectories);
	}

	public List<String> getDecoders() {
		return Collections.unmodifiableList(decoders);
	}

	public void setDecoders(List<String> decoders) {
		if(decoders == null) {
			this.decoders = new ArrayList<String>();
		} else {
			this.decoders = new ArrayList<String>(decoders);
		}
	}

	public Map<String, String> getDecoderConfig(String decoder) {
		Map<String, String> config = decoderConfig.get(decoder);
		if(config == null) {
			return new  HashMap<String, String>();
		}
		return Collections.unmodifiableMap(config);
	}

	public void setDecoderConfig(String decoder, Map<String, String> decoderConfig) {
		if(decoderConfig == null) {
			this.decoderConfig.remove(decoder);
		} else {
			this.decoderConfig.put(decoder, new HashMap<String, String>(decoderConfig));
		}
	}

	public List<String> getDataProcessors() {
		return dataProcessors;
	}

	public void setDataProcessors(List<String> dataProcessors) {
		this.dataProcessors = dataProcessors;
	}
}
