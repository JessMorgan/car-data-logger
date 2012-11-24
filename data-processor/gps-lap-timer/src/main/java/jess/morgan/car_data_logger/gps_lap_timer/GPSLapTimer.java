package jess.morgan.car_data_logger.gps_lap_timer;

import java.awt.geom.Line2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jess.morgan.car_data_logger.data_processor.DataProcessor;

public class GPSLapTimer implements DataProcessor {
	private Integer lapCount;
	private Integer bestLapTime;
	private Integer lastLapTime;
	private Integer currentLapStart;
	private Double lastLongitude;
	private Double lastLatitude;
	private Double lastTimestamp;
	private Line2D finishLine = null;

	public GPSLapTimer(File track) throws IOException {
		ZipInputStream zis = new ZipInputStream(new FileInputStream(track));
		try {
			ZipEntry entry;
			while((entry = zis.getNextEntry()) != null) {
				if("track.properties".equals(entry.getName())) {
					Properties trackProperties = new Properties();
					trackProperties.load(zis);
					String finishString = trackProperties.getProperty("finish");
					if(finishString == null) {
						throw new RuntimeException("Incompatible track file - no finish line defined");
					}
					String[] finishParts = finishString.split(",");
					if(finishParts.length != 4) {
						throw new RuntimeException("Invalid track file - property \"finish\" should have four comma-separated decimal values");
					}
					try {
						finishLine = new Line2D.Double(
								Double.parseDouble(finishParts[0]),
								Double.parseDouble(finishParts[1]),
								Double.parseDouble(finishParts[2]),
								Double.parseDouble(finishParts[3]));
					} catch(NumberFormatException nfe) {
						throw new RuntimeException("Invalid track file - property \"finish\" should have four comma-separated decimal values");
					}
				}
			}
		} finally {
			zis.close();
		}
		if(finishLine == null) {
			throw new RuntimeException("Invalid track file - no track.properties file found");
		}
	}

	@Override
	public void process(List<Map<String, String>> data) {
		for(Map<String, String> line : data) {
			update(line);
			Integer currentLapTime = calculateLapTime(currentLapStart, line.get("Time"));
			line.put("Lap",           lapCount == null       ? "" : Integer.toString(lapCount));
			line.put("Lap Time",      currentLapTime == null ? "" : Integer.toString(currentLapTime));
			line.put("Best Lap Time", bestLapTime == null    ? "" : Integer.toString(bestLapTime));
			line.put("Last Lap Time", lastLapTime == null    ? "" : Integer.toString(lastLapTime));
		}
	}

	private void update(Map<String, String> line) {
		Double newLatitude  = getDoubleValue(line.get("Latitude"));
		Double newLongitude = getDoubleValue(line.get("Longitude"));
		Double newTimestamp = getDoubleValue(line.get("Time"));

		Double whenFinishLineCrossed = calculateFinishLineCrossing(newLatitude, newLongitude, newTimestamp);
		if(whenFinishLineCrossed != null) {
			newLap(whenFinishLineCrossed);
		}

		lastLatitude  = newLatitude;
		lastLongitude = newLongitude;
		lastTimestamp = newTimestamp;
	}

	private Double calculateFinishLineCrossing(Double newLatitude, Double newLongitude, Double newTimestamp) {
		if(lastLatitude == null || lastLongitude == null || newLatitude == null || newLongitude == null || lastTimestamp == null || newTimestamp == null) {
			return null;
		}
		Line2D carPath = new Line2D.Double(lastLatitude, lastLongitude, newLatitude, newLongitude);
		if(!carPath.intersectsLine(finishLine)) {
			return null;
		} else {
			// calculate the intersection point and do a linear interpolation to determine when the finish line was crossed
			// TODO: the following is a temporary calculation:
			return (newTimestamp - lastTimestamp) / 2 + lastTimestamp;
		}
	}

	private void newLap(double whenFinishLineCrossed) {
		if(currentLapStart != null) {
			lastLapTime = (int) (whenFinishLineCrossed - currentLapStart);
			if(bestLapTime == null || lastLapTime < bestLapTime) {
				bestLapTime = lastLapTime;
			}
		}
		if(lapCount == null) {
			lapCount = 1;
		} else {
			lapCount++;
		}
		currentLapStart = (int) whenFinishLineCrossed;
	}

	private Integer calculateLapTime(Integer _currentLapStart, String currentTime) {
		Double _currentTime = getDoubleValue(currentTime);
		if(_currentLapStart == null || _currentTime == null) {
			return null;
		}

		return _currentTime.intValue() - _currentLapStart;
	}

	private Double getDoubleValue(String source) {
		if(source == null) {
			return null;
		}
		try {
			return Double.parseDouble(source);
		} catch(NumberFormatException nfe) {
			return null;
		}
	}

//	private static final DateFormat dateFormat = new SimpleDateFormat("m:ss.SSS");
//	private String formatTime(Double time) {
//		if(time == null || time < 0) {
//			return "-";
//		}
//		long millis = (long) (time * 1000.0);
//		return dateFormat.format(new Date(millis));
//	}

	@Override
	public Map<String, String> getAdditionalParameters() {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("Lap", "Count");
		params.put("Lap Time", "milliseconds");
		params.put("Last Lap Time", "milliseconds");
		params.put("Best Lap Time", "milliseconds");
		return params;
	}
}
