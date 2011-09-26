package jess.morgan.car_data_logger.decode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractDataDecoder implements DataDecoder {
	public final void decodeStream(InputStream is, OutputStream os) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		PrintWriter out = new PrintWriter(os);

		List<String> header = getAvailableParameters();
		header.add(0, "timestamp");
		try {
			// Print file header
			out.println(buildLineCSV(header));

			String line;
			while(null != (line = br.readLine())) {
				// Decode line
				Map<String, String> data = decodeLine(line);
				// Match up position of decoded items with proper labels
				List<String> values = new ArrayList<String>();
				for(String parameter : header) {
					String value = data.get(parameter);
					if(value == null) {
						value = "";
					}
					value.replace(',', ';');
					values.add(value);
				}
				// Print decoded line
				out.println(buildLineCSV(values));
			}
		} finally {
			try {
				br.close();
			} catch(IOException ioe) {
				// Ignore
			}
			out.close();
		}
	}

	private String buildLineCSV(List<String> values) {
		StringBuilder sb = new StringBuilder();
		for(String value : values) {
			sb.append(value).append(',');
		}
		return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
	}
}
