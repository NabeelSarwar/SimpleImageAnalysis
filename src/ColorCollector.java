/**
 * @author Nabeel Sarwar nsarwar@princeton.edu
 *  Colors pixel data for the libraries
 *
 * 
    Copyright (C) <2014>  

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/
 *
 */
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class ColorCollector {

	public final int BASE_WIDTH = 300;

	// locations for the images as long as the class file is inside same folder
	private final String AEROPRESS_LOCATION = "images/Aeropress/";
	private final String CHEMEX_LOCATION = "images/Chemex/";
	private final String FRENCHPRESS_LOCATION = "images/FrenchPress/";

	private ArrayList<File> aeroimages;
	private ArrayList<File> chemeximages;
	private ArrayList<File> frenchpressimages;
	private BufferedWriter datafile;

	// image to analyze

	private final int RED = 35;
	private final int GREEN = 36;
	private final int BLUE = 37;
	private final int OFFSET = 35;

	private final int AERO = 50;
	private final int CHEMEX = 51;
	private final int FRENCHPRESS = 52;
	private final int OFFSET_CATEGORIES = 50;

	private double[][][] aerod;
	private double[][][] chemexd;
	private double[][][] frenchpressd;

	/*
	 * This will set up all the difference arrays and perform the 5 cross
	 * validation training
	 */
	public ColorCollector() {
		try {
			datafile = new BufferedWriter(new FileWriter("datafile.txt"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		aeroimages = new ArrayList<File>();
		chemeximages = new ArrayList<File>();
		frenchpressimages = new ArrayList<File>();
		loadStackAero();
		try {
			difference(aeroimages, AERO);
		} catch (Exception e) {
			e.printStackTrace();
		}

		loadStackChemex();
		try {
			difference(chemeximages, CHEMEX);
		} catch (Exception e) {
			e.printStackTrace();
		}

		loadStackFrenchPress();
		try {
			difference(frenchpressimages, FRENCHPRESS);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * This method could be used later for images that are not of same
	 * dimensions. As the difference are recorded by differences in the column,
	 * we try to get same width across both images, and try to keep the image
	 * height to width ratios for each image as close to the original value as
	 * possible.
	 */
	public BufferedImage rescale(BufferedImage input) {
		int width = input.getWidth();
		int height = input.getHeight();
		input = getBufferedImage(input.getScaledInstance(BASE_WIDTH, height
				* BASE_WIDTH / width, java.awt.Image.SCALE_DEFAULT));
		return input;
	}

	/*
	 * Gets all the training images for the AeroPress
	 */
	private void loadStackAero() {
		URL url = getClass().getResource(AEROPRESS_LOCATION);
		File folder = new File(url.getPath());
		File[] imageFiles = folder.listFiles();
		for (int i = 0; i < imageFiles.length; i++) {
			if (imageFiles[i].getName().contains("jpeg ")
					|| imageFiles[i].getName().contains("jpg")
					|| imageFiles[i].getName().contains("png")) {
				aeroimages.add(imageFiles[i]);
			}
		}
		return;
	}

	/*
	 * Collection of training images
	 */
	private void loadStackChemex() {
		URL url = getClass().getResource(CHEMEX_LOCATION);
		File folder = new File(url.getPath());
		File[] imageFiles = folder.listFiles();
		for (int i = 0; i < imageFiles.length; i++) {
			if (imageFiles[i].getName().contains("jpeg ")
					|| imageFiles[i].getName().contains("jpg")
					|| imageFiles[i].getName().contains("png")) {
				chemeximages.add(imageFiles[i]);

			}
		}
		return;
	}

	/*
	 * Collection of training images
	 */
	private void loadStackFrenchPress() {
		URL url = getClass().getResource(FRENCHPRESS_LOCATION);
		File folder = new File(url.getPath());
		File[] imageFiles = folder.listFiles();
		for (int i = 0; i < imageFiles.length; i++) {
			if (imageFiles[i].getName().contains("jpeg ")
					|| imageFiles[i].getName().contains("jpg")
					|| imageFiles[i].getName().contains("png")) {
				frenchpressimages.add(imageFiles[i]);
			}
		}
	}

	/**
	 * This is the main data collection algorithm It does 5 fold evaluation and
	 * then collapses that data into a 2D matrix with dimensions [number of
	 * images][normalized row length of those images] It records the difference
	 * in Color data between pixel regions (regions of 1 pixel width and length
	 * of normalized height)
	 * 
	 * @param imagevectors
	 * @param whichArray
	 * @throws Exception
	 *             when the indicated array is not valid
	 */

	private void difference(ArrayList<File> imagefiles, int whichArray)
			throws Exception {
		double[][][] foldArray = new double[imagefiles.size()][3][BASE_WIDTH]; // 3
		ArrayList<File> otherimages; // colors

		// for each of the images
		for (int i = 0; i < imagefiles.size(); i++) {
			otherimages = new ArrayList<File>();
			File testfile = imagefiles.get(i);
			for (int j = 0; j < imagefiles.size(); j++) {
				if (i != j) {
					otherimages.add(imagefiles.get(j));
				}
			}
			// array of (number of images-1)x3x BASE_WIDTH dimensions
			double[][][] preaverages = new double[otherimages.size()][3][BASE_WIDTH];

			for (int j = 0; j < otherimages.size(); j++) {
				try {
					BufferedImage test = rescale(downsample(rotation(translation(getBufferedImage(ImageIO.read(testfile))))));
					BufferedImage other = rescale(downsample(rotation(translation(getBufferedImage(ImageIO.read(otherimages.get(j)))))));
					preaverages[j] = compareImages(test, other);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			foldArray[i] = average(preaverages);
		}
		datafile.write(which(whichArray));
		write(datafile, foldArray);
		datafile.newLine();
		switch (whichArray) {
		case AERO:
			setAero(foldArray);
			break;
		case CHEMEX:
			setChemex(foldArray);
			break;
		case FRENCHPRESS:
			setFrenchPress(foldArray);
			break;
		default:
			throw new Exception("No proper array to put data in");
		}

	}

	private void setAero(double[][][] array) {
		aerod = array;
	}

	private void setChemex(double[][][] array) {
		chemexd = array;
	}

	private void setFrenchPress(double[][][] array) {
		frenchpressd = array;
	}

	private double[][] compareImages(BufferedImage test, BufferedImage template) {
		double[][] differences = new double[3][BASE_WIDTH]; // three colors
		int imageHeightRatio;

		if (test.getHeight() <= template.getHeight()) {
			double[][] heightdata = new double[3][test.getHeight()];
			for (int i = 0; i < test.getWidth(); i++) {
				imageHeightRatio = (int) ((double) template.getHeight() / (double) test
						.getHeight());
				if (imageHeightRatio < 1)
					imageHeightRatio = 1;
				int averageRed;
				int averageBlue;
				int averageGreen;
				for (int j = 0; j < test.getHeight(); j++) {
					int[] distance = template.getRGB(i, j * imageHeightRatio,
							1, imageHeightRatio, null, 0, 1);
					averageRed = 0;
					averageBlue = 0;
					averageGreen = 0;
					for (int h = 0; h < distance.length; h++) {
						averageRed += new Color(distance[h]).getRed();
						averageBlue += new Color(distance[h]).getBlue();
						averageGreen += new Color(distance[h]).getGreen();
					}
					averageRed /= distance.length;
					averageBlue /= distance.length;
					averageGreen /= distance.length;
					heightdata[RED - OFFSET][j] = new Color(test.getRGB(i, j))
							.getRed() - averageRed;
					heightdata[GREEN - OFFSET][j] = new Color(test.getRGB(i, j))
							.getGreen() - averageGreen;
					heightdata[BLUE - OFFSET][j] = new Color(test.getRGB(i, j))
							.getBlue() - averageBlue;
				}
				differences[RED - OFFSET][i] = average(heightdata[RED - OFFSET]);
				differences[GREEN - OFFSET][i] = average(heightdata[GREEN
						- OFFSET]);
				differences[BLUE - OFFSET][i] = average(heightdata[BLUE
						- OFFSET]);
			}
		} else {
			imageHeightRatio = (int) ((double) test.getHeight() / (double) template
					.getHeight());
			if (imageHeightRatio < 1)
				imageHeightRatio = 1;
			double[][] heightdata = new double[3][test.getHeight()];
			for (int i = 0; i < template.getWidth(); i++) {
				int averageRed;
				int averageBlue;
				int averageGreen;
				for (int j = 0; j < template.getHeight(); j++) {
					int[] distance = test.getRGB(i, j * imageHeightRatio, 1,
							imageHeightRatio, null, 0, 1);
					averageRed = 0;
					averageBlue = 0;
					averageGreen = 0;
					for (int h = 0; h < distance.length; h++) {
						averageRed += new Color(distance[h]).getRed();
						averageBlue += new Color(distance[h]).getBlue();
						averageGreen += new Color(distance[h]).getGreen();
					}
					averageRed /= distance.length;
					averageBlue /= distance.length;
					averageGreen /= distance.length;
					heightdata[RED - OFFSET][j] = new Color(template.getRGB(i,
							j)).getRed() - averageRed;
					heightdata[GREEN - OFFSET][j] = new Color(template.getRGB(
							i, j)).getGreen() - averageGreen;
					heightdata[BLUE - OFFSET][j] = new Color(template.getRGB(i,
							j)).getBlue() - averageBlue;
				}
				differences[RED - OFFSET][i] = average(heightdata[RED - OFFSET]);
				differences[GREEN - OFFSET][i] = average(heightdata[GREEN
						- OFFSET]);
				differences[BLUE - OFFSET][i] = average(heightdata[BLUE
						- OFFSET]);

			}
		}
		return differences;
	}

	public double average(double[] array) {
		int length = array.length;
		double total = 0;
		for (int i = 0; i < length; i++)
			total += array[i];
		return total / length;
	}

	/*
	 * returns the average of the image differences for the folds
	 */
	private double[][] average(double[][][] preaverage) {
		double[][] averages = new double[3][BASE_WIDTH];
		double total = 0;
		for (int i = 0; i < 3; i++) // since we have 3 colors
		{
			for (int j = 0; j < BASE_WIDTH; j++) {
				total = 0;
				for (int k = 0; k < preaverage.length; k++) {
					total += preaverage[k][i][j];
				}
				total /= (double) preaverage.length;
				averages[i][j] = total;
			}
		}
		return averages;
	}

	public double[][][] getAero() {
		return aerod;
	}

	public double[][][] getChemex() {
		return chemexd;
	}

	public double[][][] getFrenchPress() {
		return frenchpressd;
	}

	public double[][][] test(BufferedImage test, int whichArray) {
		ArrayList<File> files;
		double[][][] testArray;
		switch (whichArray) {
		case (AERO - OFFSET_CATEGORIES):
			files = aeroimages;
			break;
		case (CHEMEX - OFFSET_CATEGORIES):
			files = chemeximages;
			break;
		case (FRENCHPRESS - OFFSET_CATEGORIES):
			files = frenchpressimages;
			break;
		default:
			files = null;
			break;
		}

		testArray = new double[files.size()][3][BASE_WIDTH];
		for (int i = 0; i < files.size(); i++) {
			try {
				testArray[i] = compareImages(test,
						getBufferedImage(ImageIO.read(files.get(i))));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return testArray;
	}

	/**
	 * shows what is being calculated when loading up the training data
	 * 
	 * @param which
	 * @return
	 */
	private String which(int which) {
		switch (which) {
		case AERO:
			return "Aero";
		case CHEMEX:
			return "Chemex";
		case FRENCHPRESS:
			return "FrenchPress";
		default:
			return "Not valid";
		}
	}

	/**
	 * Writes the data in the array
	 * 
	 * @param writer
	 * @param data
	 * @throws IOException
	 */
	private void write(BufferedWriter writer, double[][][] data)
			throws IOException {
		String s;
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < 3; j++) // 3 colors
			{
				for (int k = 0; k < data[i][j].length; k++) {
					s = data[i][j][k] + " ";
					writer.write(s);
				}
			}
			writer.newLine();
		}
	}

	// code found on
	// http://blog.pengoworks.com/index.cfm/2008/2/8/The-nightmares-of-getting-images-from-the-Mac-OS-X-clipboard-using-Java
	// coverts apple.awt.OSXImage into a BufferedImgage
	public static BufferedImage getBufferedImage(Image img) {
		if (img == null)
			return null;
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		// draw original image to thumbnail image object and
		// scale it to the new size on-the-fly
		BufferedImage bufimg = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = bufimg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(img, 0, 0, w, h, null);
		g2.dispose();
		g2 = null;
		return bufimg;
	}

	private BufferedImage downsample(BufferedImage input) {
		int width = input.getWidth();
		int height = input.getHeight();
		int red;
		int green;
		int blue;
		int rgb;
		Color middle;
		Color top;
		Color left;
		Color right;
		Color down;
		Color topleft;
		Color topright;
		Color downleft;
		Color downright;
		for (int w = 1; w < width - 1; w++) {
			for (int h = 1; h < height - 1; h++) {
				middle = new Color(input.getRGB(w, h));
				top = new Color(input.getRGB(w, h - 1));
				left = new Color(input.getRGB(w - 1, h));
				right = new Color(input.getRGB(w + 1, h));
				down = new Color(input.getRGB(w, h+1));
				topleft = new Color(input.getRGB(w - 1, h-1));
				topright = new Color(input.getRGB(w + 1, h-1));
				downleft  = new Color(input.getRGB(w -1, h+1));
				downright = new Color(input.getRGB(w+1, h+1));
				red = (int) ((middle.getRed() + top.getRed() + left.getRed() + right.getRed() + down.getRed() +
						topleft.getRed() + topright.getRed() + downleft.getRed() + downright.getRed() ) / 9.0);
				green = (int) ((middle.getGreen() + top.getGreen() + left.getGreen() + right.getGreen() + down.getGreen() +
						topleft.getGreen() + topright.getGreen() + downleft.getGreen() + downright.getGreen() ) / 9.0);
				blue = (int) ((middle.getBlue() + top.getBlue() + left.getBlue() + right.getBlue() + down.getBlue() +
						topleft.getBlue() + topright.getBlue() + downleft.getBlue() + downright.getBlue() ) / 9.0);
				rgb = new Color(red,green,blue).getRGB();
				input.setRGB(w,h,rgb);
			}
		}
		return input;
	}

	/**
	 * grays out an image
	 * @param input
	 * @return
	 */
	private BufferedImage grayed(BufferedImage input) {
		BufferedImage converted = new BufferedImage(input.getWidth(), input.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = converted.getGraphics();
		g.drawImage(input, 0, 0, null);
		g.dispose();
		return converted;
	}

	/**
	 * @param input
	 * @return
	 */
	private BufferedImage translation(BufferedImage input) {
		int horizontalMovement = (int) ( input.getWidth() * (Math.random() * 0.2));
		int verticalMovement = (int) (input.getHeight() *Math.random() * 0.2);
		return input.getSubimage(horizontalMovement, verticalMovement, input.getWidth() - horizontalMovement *2, 
				input.getHeight() -verticalMovement*2);

	}

	private BufferedImage rotation(BufferedImage input) {
		final int norotation =1;
		final int clockwise = 2;
		final int doublexclockwise = 3;
		final int triplexclockwise = 4;
		int roll  = (int) (Math.round(Math.random() *4) + 1);
		AffineTransform transform;
		AffineTransformOp filter;
		switch (roll)
		{
		case clockwise:
			transform = new AffineTransform();
			transform.rotate(Math.PI/2, input.getWidth()/2, input.getHeight() /2);
			filter = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
			return filter.filter(input, null);
		case doublexclockwise:
			transform = new AffineTransform();
			transform.rotate(Math.PI, input.getWidth()/2, input.getHeight() /2);
			filter = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
			return filter.filter(input, null);
		case triplexclockwise: 
			transform = new AffineTransform();
			transform.rotate(Math.PI * 3.0/4.0, input.getWidth()/2, input.getHeight() /2);
			filter = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
			return filter.filter(input, null);
		default: //case as norotation, the outcome was 1
			return input;
		}
	}
}
