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
	
	private final int RED = 0;
	private final int GREEN = 1;
	private final int BLUE = 2;

	private final String IMAGES_LOCATION = "images/TestIMG/";
	private ArrayList<File> imagefiles;
	private ColorCollector data;
	private BufferedWriter output;

	double[][][] aerotest;
	double[][][] chemextest;
	double[][][] frenchpresstest;

	// note, normalization is not used right now much

	public ClassifierMachine() {
		URL url = ClassLoader.getSystemResource(IMAGES_LOCATION);
		imagefiles = new ArrayList<File>();
		File folder = new File(url.getPath());
		File[] files = folder.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().contains("jpeg ")
					|| files[i].getName().contains("jpg")
					|| files[i].getName().contains("png"))

				imagefiles.add(files[i]);
		}

		try {
			output = new BufferedWriter(new FileWriter("output.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		data = new ColorCollector();

		for (int i = 0; i < imagefiles.size(); i++) {

			try {
				aerotest = data.test( data.rescale(getBufferedImage (ImageIO.read(imagefiles.get(i)))), AERO-OFFSET);
				chemextest = data.test( data.rescale(getBufferedImage (ImageIO.read(imagefiles.get(i)))), CHEMEX-OFFSET);
				frenchpresstest =data.test( data.rescale(getBufferedImage (ImageIO.read(imagefiles.get(i)))), FRENCHPRESS-OFFSET);
				output.write(imagefiles.get(i).getName() + " match for Aero	"
						+ analyze(aerotest, AERO));
				output.write(imagefiles.get(i).getName() + " match for Chemex	"
						+ analyze(chemextest, CHEMEX));
				output.write(imagefiles.get(i).getName()
						+ " match for Frenchpress	"
						+ analyze(frenchpresstest, FRENCHPRESS));
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

	private boolean analyze(double[][][] bigArrayOfDifferences, int whichArray)
			throws IOException // throws exception from output
	{
		boolean match = false; // burden of proof on us
		double[][][] test = bigArrayOfDifferences;
		double[][][] standard;
		/*
		 * BIG NOTE: MUST FIND A WAY TO GENERALIZE THIS.
		 */
		switch (whichArray) {

		case AERO:
			standard = data.getAero();
			break;
		case CHEMEX:
			standard = data.getChemex();
			break;
		case FRENCHPRESS:
			standard = data.getFrenchPress();
			break;
		default:
			output.write("Using Aero as default because of invalid input\n");
			standard = data.getAero();
			break;
		}
		
		int countOfPositives[] = new int[standard.length];

		double testred;
		double testblue;
		double testgreen;
		double standardred;
		double standardblue;
		double standardgreen;
		// there are 5 folds now for each category, more as more images get
		// added
		for (int fold = 0; fold < test.length; fold++) {
			for (int i = 0; i < test[fold].length; i++) {
				testred = test[fold][RED][i];
				testblue = test[fold][BLUE][i];
				testgreen = test[fold][GREEN][i];
				standardred = standard[fold][RED][i];
				standardblue = standard[fold][BLUE][i];
				standardgreen = test[fold][GREEN][i];

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

}
