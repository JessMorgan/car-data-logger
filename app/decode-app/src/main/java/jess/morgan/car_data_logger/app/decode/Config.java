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
	private String interpolator;

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
		interpolator = null;
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
			int pluginPathCount = Integer.parseInt(properties.getProperty("plugin.path.count", "0"));
			pluginDirectories = new ArrayList<File>();
			for(int i = 1; i <= pluginPathCount; i++) {
				String path = properties.getProperty("plugin.path." + i);
				if(path == null) {
					System.out.println("Invalid properties file, using defaults (property missing: 'plugin.path." + i + "')");
					loadDefaults();
					return;
				}
				pluginDirectories.add(new File(path));
			}

			int decodePluginCount = Integer.parseInt(properties.getProperty("plugin.decode.count", "0"));
			decoders = new ArrayList<String>();
			decoderConfig = new HashMap<String, Map<String, String>>();
			for(int i = 1; i <= decodePluginCount; i++) {
				String plugin = properties.getProperty("plugin.decode." + i);
				if(plugin == null) {
					System.out.println("Invalid properties file, using defaults (property missing: 'plugin.decode." + i + "')");
					loadDefaults();
					return;
				}
				decoders.add(plugin);

				// Load config for this plugin
				int decodePluginConfigCount = Integer.parseInt(properties.getProperty("plugin.decode." + i + ".config.count", "0"));
				if(decodePluginConfigCount > 0) {
					Map<String, String> config = new HashMap<String, String>();
					for(int j = 1; j <= decodePluginConfigCount; j++) {
						config.put(
								properties.getProperty("plugin.decode." + i + ".config." + j + ".key"),
								properties.getProperty("plugin.decode." + i + ".config." + j + ".value"));
					}
					decoderConfig.put(plugin, config);
				}
			}

			interpolator = properties.getProperty("plugin.interpolate");
		} catch(NumberFormatException nfe) {
			System.err.println("Invalid properties file, using defaults (" + nfe.getLocalizedMessage() + ")");
			loadDefaults();
		}
	}

	public void saveConfigFile() {
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

	public String getInterpolator() {
		return interpolator;
	}

	public void setInterpolator(String interpolator) {
		this.interpolator = interpolator;
	}
}
