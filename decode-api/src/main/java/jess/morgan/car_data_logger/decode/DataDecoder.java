package jess.morgan.car_data_logger.decode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface DataDecoder {
	public List<String> getAvailableParameters();
	public void decodeStream(InputStream is, OutputStream os) throws IOException;
	public Map<String, String> decodeLine(String line);
}
