/*
 * Copyright 2011 Jess Morgan
 *
 * This file is part of car-data-logger.
 *
 * car-data-logger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * car-data-logger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with car-data-logger.  If not, see <http://www.gnu.org/licenses/>.
 */
package jess.morgan.car_data_logger.track_image_downloader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

public class TrackImageDownloader {
	private static final int IMAGE_WIDTH  = 640;
	private static final int IMAGE_HEIGHT = 640;
	private static final int IMAGE_LOGO_HEIGHT = 40;
	private static final String IMAGE_FORMAT = "png32";
	private static final String DOWNLOAD_URL = "http://maps.googleapis.com/maps/api/staticmap?size=" +
			IMAGE_WIDTH + "x" + IMAGE_HEIGHT +
			"&maptype=satellite&center=%f,%f&sensor=false&zoom=%d&format=" + IMAGE_FORMAT;

	public static void downloadTrack(
			File outFile,
			String name,
			int zoom,
			BigDecimal xMin, BigDecimal xMax, BigDecimal xStep,
			BigDecimal yMin, BigDecimal yMax, BigDecimal yStep) throws IOException {
		int xCount = 0;
		int yCount = 0;
		// Iterating North to South means going down in value
		for(BigDecimal y = yMax; y.compareTo(yMin) >= 0; y = y.subtract(yStep)) {
			xCount = 0;
			// Iterating West to East means going up in value
			for(BigDecimal x = xMin; x.compareTo(xMax) <= 0; x = x.add(xStep)) {
				File file = new File(getFileName(xCount, yCount));
				if(!file.exists()) {
					try {
						downloadImage(getImageDownloadUrl(x, y, zoom), file);
					} catch(IOException ioe) {
						handle403(ioe);
					}
				}
				if(xCount % 5 == 4) {
					try {
						System.out.println("Completed 5 images - waiting 5 seconds so Google can breathe");
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// Ignore
					}
				}
				xCount++;
			}
			try {
				System.out.println("Completed a line - waiting 10 seconds so Google can breathe");
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// Ignore
			}
			yCount++;
		}

		List<Integer> xOffsets = new ArrayList<Integer>(xCount);
		List<Integer> yOffsets = new ArrayList<Integer>(yCount);
		// Measure offsets from center, so any drift is reduced
		int xMid = xCount / 2;
		int yMid = yCount / 2;
		BufferedImage image1;
		BufferedImage image2 = ImageIO.read(new File(getFileName(0, yMid)));
		for(int x = 0; x < (xCount - 1); x++) {
			System.out.println("Calculating horizontal offsets: images " + x + " and " + (x + 1) + " of " + xCount);
			image1 = image2;
			image2 = ImageIO.read(new File(getFileName(x + 1, yMid)));
			xOffsets.add(getImageOffset(image1, image2, false));
		}
		image2 = ImageIO.read(new File(getFileName(xMid, 0)));
		for(int y = 0; y < (yCount - 1); y++) {
			System.out.println("Calculating vertical offsets: images " + y + " and " + (y + 1) + " of " + yCount);
			image1 = image2;
			image2 = ImageIO.read(new File(getFileName(xMid, y + 1)));
			yOffsets.add(getImageOffset(image1, image2, true));
		}

		// Find average offsets so we can measure how much latitude/longitude change is represented by each pixel
		double avgXOffset = average(xOffsets);
		double avgYOffset = average(yOffsets);
		System.out.println("Average X Offset: " + avgXOffset + "    " + xOffsets);
		System.out.println("Average Y Offset: " + avgYOffset + "    " + yOffsets);

		double longitudePerPixel = xStep.doubleValue() / avgXOffset;
		double latitudePerPixel = yStep.doubleValue() / avgYOffset;

		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outFile));
		double leftMostLongitude = xMin.doubleValue() - (longitudePerPixel * IMAGE_WIDTH / 2);
		double rightMostLongitude = xMax.doubleValue() + (longitudePerPixel * IMAGE_WIDTH / 2);
		double topMostLatitude = yMin.doubleValue() - (latitudePerPixel * IMAGE_HEIGHT / 2);
		double bottomMostLatitude = yMax.doubleValue() + (latitudePerPixel * IMAGE_HEIGHT / 2);
		writeProperties(zos, name, leftMostLongitude, rightMostLongitude, topMostLatitude, bottomMostLatitude, longitudePerPixel, latitudePerPixel);

		BufferedImage completeImage = constructLargeStitchedImage(xCount, yCount, avgXOffset, avgYOffset);
		zos.putNextEntry(new ZipEntry("track.png"));
		ImageIO.write(completeImage, "png", zos);
		zos.closeEntry();
		zos.close();
	}

	private static String getFileName(int x, int y) {
		return String.format("track_%02dx%02d.png", y, x);
	}

	private static BufferedImage constructLargeStitchedImage(int xCount, int yCount, double avgXOffset, double avgYOffset) throws IOException {
		System.out.println("Constructing complete track image");
		int completeWidth  = IMAGE_WIDTH  + (int)(avgXOffset * (xCount - 1));
		int completeHeight = IMAGE_HEIGHT + (int)(avgYOffset * (yCount - 1)) - IMAGE_LOGO_HEIGHT;
		BufferedImage completeImage = new BufferedImage(completeWidth, completeHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D completeImageGraphics = completeImage.createGraphics();
		double yPos = 0;
		for(int y = 0; y < yCount; y++) {
			double xPos = 0;
			for(int x = 0; x < xCount; x++) {
				BufferedImage smallImage = ImageIO.read(new File(getFileName(x, y)));
				completeImageGraphics.drawImage(smallImage, (int)xPos, (int)yPos, null);
				xPos += avgXOffset;
			}
			yPos += avgYOffset;
		}
		completeImageGraphics.dispose();
		return completeImage;
	}

	private static void writeProperties(
			ZipOutputStream zos, String name,
			double leftMostLongitude, double rightMostLongitude,
			double topMostLatitude, double bottomMostLatitude,
			double longitudePerPixel, double latitudePerPixel) throws IOException {
		zos.putNextEntry(new ZipEntry("track.properties"));
		PrintStream ps = new PrintStream(zos);
		ps.printf("name=%s\n", name);
		ps.printf("left=%.15f\n", leftMostLongitude);
		ps.printf("right=%.15f\n", rightMostLongitude);
		ps.printf("top=%.15f\n", topMostLatitude);
		ps.printf("bottom=%.15f\n", bottomMostLatitude);
		ps.printf("longitudePerPixel=%.15f\n", longitudePerPixel);
		ps.printf("latitudePerPixel=%.15f\n", latitudePerPixel);
		ps.flush();
		zos.closeEntry();
	}

	private static double average(List<Integer> xOffsets) {
		int total = 0;
		for(int offset : xOffsets) {
			total += offset;
		}
		return (double)total / xOffsets.size();
	}

	private static void handle403(IOException ioe) throws IOException {
		if(ioe.getMessage() != null && ioe.getMessage().matches(".*\\b403\\b.*")) {
			// HTTP 403 - Google's telling us to cool it, so wait a minute
			try {
				System.err.println("Got a 403 from Google, waiting 60 seconds...");
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// Ignore
			}
		} else {
			// Not an HTTP 403
			throw ioe;
		}
	}

	private static void downloadImage(String url, File outFile) throws IOException {
		URL _url = new URL(url);
		ReadableByteChannel rbc = Channels.newChannel(_url.openStream());
		FileOutputStream fos = new FileOutputStream(outFile);
		try {
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		} finally {
			try {
				rbc.close();
			} finally {
				fos.close();
			}
		}
	}

	private static String getImageDownloadUrl(BigDecimal x, BigDecimal y, int zoom) {
		return String.format(DOWNLOAD_URL, y, x, zoom);
	}

	private static int getImageOffset(BufferedImage image1, BufferedImage image2, boolean vertical) {
		int width = image1.getWidth();
		int height = image1.getHeight() - IMAGE_LOGO_HEIGHT;
		int bands = 3; // RGB
		int[] image1Buffer = new int[width*height];
		int[] image2Buffer = new int[width*height];
		long minAccumulatedDifference = Long.MAX_VALUE;
		int bestMatch = -1;
		int[] image1Samples;
		int[] image2Samples;
		int distance = (vertical ? height : width);
		for(int i = 0; i < distance; i++) {
			long accumulatedDifference = 0;
			for(int band = 0; band < 3; band++) {
				if(vertical) {
					image1Samples = image1.getData().getSamples(0, i, width, (height-i), band, image1Buffer);
					image2Samples = image2.getData().getSamples(0, 0, width, (height-i), band, image2Buffer);
				} else {
					image1Samples = image1.getData().getSamples(i, 0, (width-i), height, band, image1Buffer);
					image2Samples = image2.getData().getSamples(0, 0, (width-i), height, band, image2Buffer);
				}

				int length = (vertical ? width * (height - i) : (width - i) * height);
				for(int j = 0; j < length; j++) {
					accumulatedDifference += Math.abs(image1Samples[j] - image2Samples[j]);
				}
			}
			if(vertical) {
				accumulatedDifference = accumulatedDifference * 10000 / (width * (height - i) * bands);
			} else {
				accumulatedDifference = accumulatedDifference * 10000 / ((width - i) * height * bands);
			}

			if(accumulatedDifference < minAccumulatedDifference) {
				minAccumulatedDifference = accumulatedDifference;
				bestMatch = i;
			}
		}
		return bestMatch;
	}

	public static void main(String[] args) throws IOException {
		TrackImageDownloader.downloadTrack(new File("Albany-Saratoga Speedway.trk"), "Albany-Saratoga Speedway", 20,
				new BigDecimal("-73.7830"), new BigDecimal("-73.7815"), new BigDecimal("0.0005"),
				new BigDecimal("42.9875"), new BigDecimal("42.9895"), new BigDecimal("0.0005"));
	}
}
