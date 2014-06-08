/**
/**
 * @author Nabeel Sarwar nsarwar@princeton.edu
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
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.imageio.ImageIO;

/**
 * Data collection for the test image is mostly located in the constructor.
 * Improvement of the machine is done by adding correctly classified images in
 * the appropriate folder.
 * 
 */
public class ClassifierMachine {

	// the test image goes through a similar process to the 5 fold evaluation
	private final int AERO = 90238;
	private final int CHEMEX = 90239;
	private final int FRENCHPRESS = 90240;
	private final int OFFSET = 90238;

	private final String IMAGES_LOCATION = "images/TestIMG/";

	private ArrayList<BufferedImage> testimages;
	private ColorCollector data;
	private BufferedWriter output;
	private Color[][][] testimageDifferences; // dimensions of [number of
												// categories][size of the
												// categories][normalized width
												// of images]

	// note, normalization is not used right now much

	public ClassifierMachine() {
		URL url = ClassLoader.getSystemResource(IMAGES_LOCATION);
		File folder = new File(url.getPath());
		File[] imageFiles = folder.listFiles();
		testimages = new ArrayList<BufferedImage>();
		for (int i = 0; i < imageFiles.length; i++) {
			if (imageFiles[i].getName().contains("jpeg ")
					|| imageFiles[i].getName().contains("jpg")
					|| imageFiles[i].getName().contains("png")) {
				try {

					BufferedImage image= ImageIO.read(imageFiles[i]);
					testimages.add(image);
					image = null;
				}

				catch (IOException exception) {
					System.out.println("Some file was moved.");
				}	
			}
		}

		try {
			output = new BufferedWriter(new FileWriter("output.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		data = new ColorCollector();

		for (int i = 0; i < testimages.size(); i++) {
			testimageDifferences = data.test(testimages.get(i));

			try {
				output.write(imageFiles[i].getName() + " match for Aero	"
						+ analyze(testimageDifferences, AERO-OFFSET));
				output.write(imageFiles[i].getName() + " match for Chemex	"
						+ analyze(testimageDifferences, CHEMEX-OFFSET));
				output.write(imageFiles[i].getName() + " match for Frenchpress	"
						+ analyze(testimageDifferences, FRENCHPRESS-OFFSET));
			} catch (IOException e) {
				e.printStackTrace(); // this try/catch is here in case output
										// was not able to be opened
			}

		}

		// should not need to need to write anymore if we are done analyzing
		try {
			output.close();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	private boolean analyze(Color[][][] bigArrayOfDifferences, int whichArray)
			throws IOException // throws exception from output
	{
		boolean match = false; // burden of proof on us
		Color[][] test = bigArrayOfDifferences[whichArray]; 
		Color[][] standard;
		/*
		 * BIG NOTE: MUST FIND A WAY TO GENERALIZE THIS.
		 */
		switch (whichArray) {

		case AERO:
			standard = data.getAero();
			break;
		case CHEMEX:
			standard = data.getChemex();
		case FRENCHPRESS:
			standard = data.getFrenchPress();
		default:
			output.write("Using Aero as default because of invalid input");
			standard = data.getAero();
			break;
		}
		int countOfPositives[] = new int[standard.length];

		int testred;
		int testblue;
		int testgreen;
		int standardred;
		int standardblue;
		int standardgreen;
		// there are 5 folds now for each category, more as more images get
		// added
		for (int fold = 0; fold < test.length; fold++) {
			for (int i = 0; i < test[fold].length; i++) {
				testred = test[fold][i].getRed();
				testblue = test[fold][i].getBlue();
				testgreen = test[fold][i].getBlue();
				standardred = standard[fold][i].getRed();
				standardblue = standard[fold][i].getBlue();
				standardgreen = standard[fold][i].getGreen();

				if (Math.abs(testred) < Math.abs(standardred)
						&& Math.abs(testblue) < Math.abs(standardblue)
						&& Math.abs(testgreen) < Math.abs(standardgreen)) {
					countOfPositives[fold]++;

				}
			}
		}
		boolean[] foldagreement = new boolean[test.length];
		// 60% of folds must agree, for now
		// 90% of length must agree for a fold to agree
		for (int fold = 0; fold < test.length; fold++) {
			if ((double) countOfPositives[fold] / (double) test[fold].length >= 0.9)
				foldagreement[fold] = true;
			else
				foldagreement[fold] = false;
		}
		int goodfolds = 0;
		for (int i = 0; i < foldagreement.length; i++) {
			if (foldagreement[i])
				goodfolds++;
		}
		if ((double) goodfolds / (double) test.length >= 0.6)
			match = true;
		return match;
	}


}
