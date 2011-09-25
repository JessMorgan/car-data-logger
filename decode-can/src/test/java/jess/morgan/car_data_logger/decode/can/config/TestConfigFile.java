package jess.morgan.car_data_logger.decode.can.config;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TestConfigFile {

	@Test
	public void testValidConfig() throws IOException {
		assertEquals(18, ConfigFile.readConfig(new File("config/2004-mazda-rx8-us.cfg")).size());
	}

}
