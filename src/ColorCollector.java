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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class ColorCollector {

	private final int NUMCLASSES = 3;
	public final int BASE_WIDTH = 300;

	private final int AERO = 90238;
	private final int CHEMEX = 90239;
	private final int FRENCHPRESS = 90240;
	private final int OFFSET = 90238;

	// locations for the images as long as the class file is inside same folder
	private final String AEROPRESS_LOCATION = "images/Aeropress/";
	private final String CHEMEX_LOCATION = "images/Chemex/";
	private final String FRENCHPRESS_LOCATION = "images/FrenchPress/";

	// gets a set of all images for easy access
	private ArrayList<BufferedImage> imageStackAero;
	private ArrayList<BufferedImage> imageStackChemex;
	private ArrayList<BufferedImage> imageStackFrenchPress;

	// image to analyze

	private Color[][] differenceAero;
	private Color[][] differenceChemex;
	private Color[][] differenceFrenchPress;

	/*
	 * This will set up all the difference arrays and perform the 5 cross
	 * validation training
	 */
	public ColorCollector() {

		new Thread() {
			public void run() {
				loadStackAero();
				// differenceAero = new
				// Color[imageStackAero.size()][BASE_WIDTH][BASE_HEIGHT];
				differenceAero = new Color[imageStackAero.size()][BASE_WIDTH];
				try {
					difference(imageStackAero, AERO);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

		new Thread() {
			public void run() {
				loadStackChemex();
				// differenceChemex = new
				// Color[imageStackChemex.size()][BASE_WIDTH][BASE_HEIGHT];
				differenceChemex = new Color[imageStackChemex.size()][BASE_WIDTH];
				try {
					difference(imageStackChemex, CHEMEX);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

		new Thread() {
			public void run() {
				loadStackFrenchPress();
				// differenceFrenchPress = new
				// Color[imageStackFrenchPress.size()][BASE_WIDTH][BASE_HEIGHT];
				differenceFrenchPress = new Color[imageStackFrenchPress.size()][BASE_WIDTH];
				try {
					difference(imageStackFrenchPress, FRENCHPRESS);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

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
		input = (BufferedImage) input.getScaledInstance(BASE_WIDTH, height
				* width / BASE_WIDTH, java.awt.Image.SCALE_DEFAULT);
		return input;
	}

	/*
	 * Gets all the training images for the AeroPress
	 */
	private void loadStackAero() {
		URL url = getClass().getResource(AEROPRESS_LOCATION);
		File folder = new File(url.getPath());
		File[] imageFiles = folder.listFiles();
		BufferedImage[] images = new BufferedImage[imageFiles.length];
		for (int i = 0; i < imageFiles.length; i++) {
			if (imageFiles[i].getName().contains("jpeg ")
					|| imageFiles[i].getName().contains("jpg")
					|| imageFiles[i].getName().contains("png")) {
				try {

					images[i] = ImageIO.read(imageFiles[i]);
				}

				catch (IOException exception) {
					System.out.println("Some file was moved.");
				}
				imageStackAero.add(rescale(images[i]));
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
		BufferedImage[] images = new BufferedImage[imageFiles.length];
		for (int i = 0; i < imageFiles.length; i++) {
			if (imageFiles[i].getName().contains("jpeg ")
					|| imageFiles[i].getName().contains("jpg")
					|| imageFiles[i].getName().contains("png")) {
				try {

					images[i] = ImageIO.read(imageFiles[i]);
				}

				catch (IOException exception) {
					System.out.println("Some file was moved.");
				}
				imageStackChemex.add(rescale(images[i]));
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
		BufferedImage[] images = new BufferedImage[imageFiles.length];
		for (int i = 0; i < imageFiles.length; i++) {
			if (imageFiles[i].getName().contains("jpeg ")
					|| imageFiles[i].getName().contains("jpg")
					|| imageFiles[i].getName().contains("png")) {
				try {

					images[i] = ImageIO.read(imageFiles[i]);
				}

				catch (IOException exception) {
					System.out.println("Some file was moved.");
				}
				imageStackFrenchPress.add(rescale(images[i]));
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
	private void difference(ArrayList<BufferedImage> imagevectors,
			int whichArray) throws Exception {
		// Do the 5 fold training here
		// use one image and then the others
		Color[][] unfoldedDifferenceArray = new Color[imagevectors.size()][BASE_WIDTH];
		for (int m = 0; m < imagevectors.size(); m++) {

			BufferedImage testimage = imagevectors.get(m);

			ArrayList<BufferedImage> vectors = new ArrayList<BufferedImage>();
			for (int i = 0; i < imagevectors.size(); i++) {
				if (i != m) {
					vectors.add(imagevectors.get(i));
				}
			}

			// collect height data for normalization purposes
			int[] imageHeightRatio = new int[vectors.size()];
			Color[][][] colorDifferences = new Color[vectors.size()][BASE_WIDTH][testimage
					.getHeight()];

			for (int i = 0; i < vectors.size(); i++) {
				imageHeightRatio[i] = (int) ((double) testimage.getHeight() / (double) vectors
						.get(i).getHeight());
			}

			for (int k = 0; k < vectors.size(); k++) {
				// processing across images
				for (int i = 0; i < testimage.getWidth(); i++) {
					int averageRed;
					int averageBlue;
					int averageGreen;
					for (int j = 0; j < testimage.getHeight(); j++) {
						int[] distance = vectors.get(k).getRGB(i,
								j * imageHeightRatio[j], 1,
								imageHeightRatio[j], null, 0, 1);
						averageRed = 0;
						averageBlue = 0;
						averageGreen = 0;
						for (int h = 0; h < distance.length; h++) {
							averageRed += new Color(distance[i]).getRed();
							averageBlue += new Color(distance[i]).getBlue();
							averageGreen += new Color(distance[i]).getGreen();
						}
						averageRed /= distance.length;
						averageBlue /= distance.length;
						averageGreen /= distance.length;
						Color averageColor = new Color(averageRed,
								averageGreen, averageBlue);
						Color currentpixel = new Color(testimage.getRGB(i, j));
						colorDifferences[k][i][j] = new Color(
								currentpixel.getRed() - averageColor.getRed(),
								currentpixel.getGreen()
										- averageColor.getGreen(),
								currentpixel.getBlue() - averageColor.getBlue());

					}
				}

			}
			// Now the colorDifferences array has
			// [m-1][BASE_WIDTH][testimage.height] dimensions. average per row
			// now

			Color[][] differenceColumn = new Color[vectors.size()][BASE_WIDTH];

			for (int k = 0; k < vectors.size(); k++) {

				for (int w = 0; w < testimage.getWidth(); w++) {
					int averageRed = 0;
					int averageGreen = 0;
					int averageBlue = 0;
					for (int i = 0; i < testimage.getHeight(); i++) {
						averageRed += colorDifferences[k][w][i].getRed();
						averageGreen += colorDifferences[k][w][i].getGreen();
						averageBlue += colorDifferences[k][w][i].getBlue();
					}
					averageRed = (int) averageRed / testimage.getHeight();
					averageBlue = (int) averageBlue / testimage.getHeight();
					averageGreen = (int) averageGreen / testimage.getHeight();
					differenceColumn[k][w] = new Color(averageRed,
							averageGreen, averageBlue);
				}
			} // the column differences are now collected
			Color[] averagek = new Color[testimage.getWidth()];

			for (int w = 0; w < testimage.getWidth(); w++) {
				int averageRed = 0;
				int averageGreen = 0;
				int averageBlue = 0;
				for (int k = 0; w < vectors.size(); k++) {
					averageRed += differenceColumn[k][w].getRed();
					averageGreen += differenceColumn[k][w].getGreen();
					averageBlue += differenceColumn[k][w].getBlue();
				}
				averagek[w] = new Color(averageRed / vectors.size(),
						averageGreen / vectors.size(), averageBlue
								/ vectors.size());
			}

			for (int i = 0; i < testimage.getWidth(); i++) {
				unfoldedDifferenceArray[m][i] = averagek[i];
			}

			// end of m loop (the m loop goes through one of the 5 cross fold
			// evaluations
		}
		switch (whichArray) {
		case AERO:
			setAero(unfoldedDifferenceArray);
			break;
		case CHEMEX:
			setChemex(unfoldedDifferenceArray);
			break;
		case FRENCHPRESS:
			setFrenchPress(unfoldedDifferenceArray);
			break;
		default:
			throw new Exception("No proper array to put data in");

		}

	}

	public Color[][] getAero() {
		return differenceAero;
	}

	public Color[][] getChemex() {
		return differenceChemex;
	}

	public Color[][] getFrenchPress() {
		return differenceFrenchPress;
	}

	private void setAero(Color[][] differenceArray) {
		differenceAero = differenceArray;
	}

	private void setFrenchPress(Color[][] differenceArray) {
		differenceFrenchPress = differenceArray;
	}

	private void setChemex(Color[][] differenceArray) {
		differenceChemex = differenceArray;
	}

	public Color[][][] test(BufferedImage testimage) {
		testimage = rescale(testimage);
		Color[][][] test;

		test = new Color[NUMCLASSES][][];

		test[AERO - OFFSET] = new Color[imageStackAero.size()][BASE_WIDTH];
		test[CHEMEX - OFFSET] = new Color[imageStackChemex.size()][BASE_WIDTH];
		test[FRENCHPRESS - OFFSET] = new Color[imageStackFrenchPress.size()][BASE_WIDTH];

		// do the test for Aero, then Chemex, then FrenchPress

		for (int m = 0; m < imageStackAero.size(); m++) {

			ArrayList<BufferedImage> aeros = new ArrayList<BufferedImage>();

			for (int i = 0; i < imageStackAero.size(); i++) {
				if (i != m) {
					aeros.add(imageStackAero.get(i));
				}
			}

			int[] imageHeightRatioAero = new int[aeros.size()];

			Color[][][] colorDifferences = new Color[aeros.size()][BASE_WIDTH][testimage
					.getHeight()];

			for (int i = 0; i < aeros.size(); i++) {
				imageHeightRatioAero[i] = (int) ((double) testimage.getHeight() / (double) aeros
						.get(i).getHeight());
			}

			for (int k = 0; k < aeros.size(); k++) {
				// processing across images
				for (int i = 0; i < testimage.getWidth(); i++) {
					int averageRed;
					int averageBlue;
					int averageGreen;
					for (int j = 0; j < testimage.getHeight(); j++) {
						int[] distance = aeros.get(k).getRGB(i,
								j * imageHeightRatioAero[j], 1,
								imageHeightRatioAero[j], null, 0, 1);
						averageRed = 0;
						averageBlue = 0;
						averageGreen = 0;
						for (int h = 0; h < distance.length; h++) {
							averageRed += new Color(distance[i]).getRed();
							averageBlue += new Color(distance[i]).getBlue();
							averageGreen += new Color(distance[i]).getGreen();
						}
						averageRed /= distance.length;
						averageBlue /= distance.length;
						averageGreen /= distance.length;
						Color averageColor = new Color(averageRed,
								averageGreen, averageBlue);
						Color currentpixel = new Color(testimage.getRGB(i, j));
						colorDifferences[k][i][j] = new Color(
								currentpixel.getRed() - averageColor.getRed(),
								currentpixel.getGreen()
										- averageColor.getGreen(),
								currentpixel.getBlue() - averageColor.getBlue());

					}
				}

			}
			// Now the colorDifferences array has
			// [m-1][BASE_WIDTH][testimage.height] dimensions. average per row
			// now

			Color[][] differenceColumn = new Color[aeros.size()][BASE_WIDTH];

			for (int k = 0; k < aeros.size(); k++) {

				for (int w = 0; w < testimage.getWidth(); w++) {
					int averageRed = 0;
					int averageGreen = 0;
					int averageBlue = 0;
					for (int i = 0; i < testimage.getHeight(); i++) {
						averageRed += colorDifferences[k][w][i].getRed();
						averageGreen += colorDifferences[k][w][i].getGreen();
						averageBlue += colorDifferences[k][w][i].getBlue();
					}
					averageRed = (int) averageRed / testimage.getHeight();
					averageBlue = (int) averageBlue / testimage.getHeight();
					averageGreen = (int) averageGreen / testimage.getHeight();
					differenceColumn[k][w] = new Color(averageRed,
							averageGreen, averageBlue);
				}
			} // the column differences are now collected
			Color[] averagek = new Color[testimage.getWidth()];

			for (int w = 0; w < testimage.getWidth(); w++) {
				int averageRed = 0;
				int averageGreen = 0;
				int averageBlue = 0;
				for (int k = 0; w < aeros.size(); k++) {
					averageRed += differenceColumn[k][w].getRed();
					averageGreen += differenceColumn[k][w].getGreen();
					averageBlue += differenceColumn[k][w].getBlue();
				}
				averagek[w] = new Color(averageRed / aeros.size(), averageGreen
						/ aeros.size(), averageBlue / aeros.size());
			}

			for (int i = 0; i < testimage.getWidth(); i++) {
				test[AERO - OFFSET][m][i] = averagek[i];
			}

			// end of m loop (the m loop goes through one of the 5 cross fold
			// evaluations
		}

		for (int m = 0; m < imageStackChemex.size(); m++) {

			ArrayList<BufferedImage> chemexes = new ArrayList<BufferedImage>();

			for (int i = 0; i < imageStackChemex.size(); i++) {
				if (i != m) {
					chemexes.add(imageStackChemex.get(i));
				}
			}

			int[] imageHeightRatioAero = new int[chemexes.size()];

			Color[][][] colorDifferences = new Color[chemexes.size()][BASE_WIDTH][testimage
					.getHeight()];

			for (int i = 0; i < chemexes.size(); i++) {
				imageHeightRatioAero[i] = (int) ((double) testimage.getHeight() / (double) chemexes
						.get(i).getHeight());
			}

			for (int k = 0; k < chemexes.size(); k++) {
				// processing across images
				for (int i = 0; i < testimage.getWidth(); i++) {
					int averageRed;
					int averageBlue;
					int averageGreen;
					for (int j = 0; j < testimage.getHeight(); j++) {
						int[] distance = chemexes.get(k).getRGB(i,
								j * imageHeightRatioAero[j], 1,
								imageHeightRatioAero[j], null, 0, 1);
						averageRed = 0;
						averageBlue = 0;
						averageGreen = 0;
						for (int h = 0; h < distance.length; h++) {
							averageRed += new Color(distance[i]).getRed();
							averageBlue += new Color(distance[i]).getBlue();
							averageGreen += new Color(distance[i]).getGreen();
						}
						averageRed /= distance.length;
						averageBlue /= distance.length;
						averageGreen /= distance.length;
						Color averageColor = new Color(averageRed,
								averageGreen, averageBlue);
						Color currentpixel = new Color(testimage.getRGB(i, j));
						colorDifferences[k][i][j] = new Color(
								currentpixel.getRed() - averageColor.getRed(),
								currentpixel.getGreen()
										- averageColor.getGreen(),
								currentpixel.getBlue() - averageColor.getBlue());

					}
				}

			}
			// Now the colorDifferences array has
			// [m-1][BASE_WIDTH][testimage.height] dimensions. average per row
			// now

			Color[][] differenceColumn = new Color[chemexes.size()][BASE_WIDTH];

			for (int k = 0; k < chemexes.size(); k++) {

				for (int w = 0; w < testimage.getWidth(); w++) {
					int averageRed = 0;
					int averageGreen = 0;
					int averageBlue = 0;
					for (int i = 0; i < testimage.getHeight(); i++) {
						averageRed += colorDifferences[k][w][i].getRed();
						averageGreen += colorDifferences[k][w][i].getGreen();
						averageBlue += colorDifferences[k][w][i].getBlue();
					}
					averageRed = (int) averageRed / testimage.getHeight();
					averageBlue = (int) averageBlue / testimage.getHeight();
					averageGreen = (int) averageGreen / testimage.getHeight();
					differenceColumn[k][w] = new Color(averageRed,
							averageGreen, averageBlue);
				}
			} // the column differences are now collected
			Color[] averagek = new Color[testimage.getWidth()];

			for (int w = 0; w < testimage.getWidth(); w++) {
				int averageRed = 0;
				int averageGreen = 0;
				int averageBlue = 0;
				for (int k = 0; w < chemexes.size(); k++) {
					averageRed += differenceColumn[k][w].getRed();
					averageGreen += differenceColumn[k][w].getGreen();
					averageBlue += differenceColumn[k][w].getBlue();
				}
				averagek[w] = new Color(averageRed / chemexes.size(),
						averageGreen / chemexes.size(), averageBlue
								/ chemexes.size());
			}

			for (int i = 0; i < testimage.getWidth(); i++) {
				test[CHEMEX - OFFSET][m][i] = averagek[i];
			}

		}

		for (int m = 0; m < imageStackFrenchPress.size(); m++) {

			ArrayList<BufferedImage> frenchpresses = new ArrayList<BufferedImage>();

			for (int i = 0; i < imageStackFrenchPress.size(); i++) {
				if (i != m) {
					frenchpresses.add(imageStackFrenchPress.get(i));
				}
			}

			int[] imageHeightRatioFrenchPress = new int[frenchpresses.size()];

			Color[][][] colorDifferences = new Color[frenchpresses.size()][BASE_WIDTH][testimage
					.getHeight()];

			for (int i = 0; i < frenchpresses.size(); i++) {
				imageHeightRatioFrenchPress[i] = (int) ((double) testimage
						.getHeight() / (double) frenchpresses.get(i)
						.getHeight());
			}

			for (int k = 0; k < frenchpresses.size(); k++) {
				// processing across images
				for (int i = 0; i < testimage.getWidth(); i++) {
					int averageRed;
					int averageBlue;
					int averageGreen;
					for (int j = 0; j < testimage.getHeight(); j++) {
						int[] distance = frenchpresses.get(k).getRGB(i,
								j * imageHeightRatioFrenchPress[j], 1,
								imageHeightRatioFrenchPress[j], null, 0, 1);
						averageRed = 0;
						averageBlue = 0;
						averageGreen = 0;
						for (int h = 0; h < distance.length; h++) {
							averageRed += new Color(distance[i]).getRed();
							averageBlue += new Color(distance[i]).getBlue();
							averageGreen += new Color(distance[i]).getGreen();
						}
						averageRed /= distance.length;
						averageBlue /= distance.length;
						averageGreen /= distance.length;
						Color averageColor = new Color(averageRed,
								averageGreen, averageBlue);
						Color currentpixel = new Color(testimage.getRGB(i, j));
						colorDifferences[k][i][j] = new Color(
								currentpixel.getRed() - averageColor.getRed(),
								currentpixel.getGreen()
										- averageColor.getGreen(),
								currentpixel.getBlue() - averageColor.getBlue());

					}
				}

			}
			// Now the colorDifferences array has
			// [m-1][BASE_WIDTH][testimage.height] dimensions. average per row
			// now

			Color[][] differenceColumn = new Color[frenchpresses.size()][BASE_WIDTH];

			for (int k = 0; k < frenchpresses.size(); k++) {

				for (int w = 0; w < testimage.getWidth(); w++) {
					int averageRed = 0;
					int averageGreen = 0;
					int averageBlue = 0;
					for (int i = 0; i < testimage.getHeight(); i++) {
						averageRed += colorDifferences[k][w][i].getRed();
						averageGreen += colorDifferences[k][w][i].getGreen();
						averageBlue += colorDifferences[k][w][i].getBlue();
					}
					averageRed = (int) averageRed / testimage.getHeight();
					averageBlue = (int) averageBlue / testimage.getHeight();
					averageGreen = (int) averageGreen / testimage.getHeight();
					differenceColumn[k][w] = new Color(averageRed,
							averageGreen, averageBlue);
				}
			} // the column differences are now collected
			Color[] averagek = new Color[testimage.getWidth()];

			for (int w = 0; w < testimage.getWidth(); w++) {
				int averageRed = 0;
				int averageGreen = 0;
				int averageBlue = 0;
				for (int k = 0; w < frenchpresses.size(); k++) {
					averageRed += differenceColumn[k][w].getRed();
					averageGreen += differenceColumn[k][w].getGreen();
					averageBlue += differenceColumn[k][w].getBlue();
				}
				averagek[w] = new Color(averageRed / frenchpresses.size(),
						averageGreen / frenchpresses.size(), averageBlue
								/ frenchpresses.size());
			}

			for (int i = 0; i < testimage.getWidth(); i++) {
				test[FRENCHPRESS - OFFSET][m][i] = averagek[i];
			}
		}
		return test;
	}
}
