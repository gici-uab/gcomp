/*
 * GICI Applications -
 * Copyright (C) 2007  Group on Interactive Coding of Images (GICI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Group on Interactive Coding of Images (GICI)
 * Department of Information and Communication Engineering
 * Autonomous University of Barcelona
 * 08193 - Bellaterra - Cerdanyola del Valles (Barcelona)
 * Spain
 *
 * http://gici.uab.es
 * gici-info@deic.uab.es
 */
package Gcomp;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
//import java.nio.ByteBuffer;

import GiciAnalysis.*;
import GiciException.*;
import GiciFile.*;
import GiciMask.GenerateMask;
//import GiciParser.*;
import GiciMask.LoadMask;


/**
 * Application to compare 2 images.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class Gcomp{

	/**
	 * Main method of Gcomp application. It takes program arguments, loads images and compare them.
	 *
	 * @param args an array of strings that contains program parameters
	 */
	public static void main(String[] args)throws ErrorException{
		//Parse arguments
		GcompParser parser = null;
		RandomAccessFile energyFile = null;
		RandomAccessFile varianzeFile = null;
		try{
			parser = new GcompParser(args);
		}catch(ErrorException e){
			System.out.println("RUN ERROR:");
			e.printStackTrace();
			System.out.println("Please report this error (specifying image type and parameters) to: gici-dev@abra.uab.es");
			System.exit(1);
		}catch(ParameterException e){
			System.out.println("ARGUMENTS ERROR: " +  e.getMessage());
			System.exit(2);
		}

		//Images load
		String imageFile1     = parser.getImageFile1();
		int[]  imageGeometry1 = parser.getImageGeometry1();
		String imageFile2     = parser.getImageFile2();
		int[]  imageGeometry2 = parser.getImageGeometry2();
		String maskFile       = parser.getMaskFile();
		int inverse		 	  = parser.getInverse();
		float[] ROIValues	  = parser.getROIValues();
		int[] pixelBitDepth   = parser.getPixelBitDepth();
		int component		  = parser.getComponent();
		String energyInput	  = parser.getEnergyFile();
		String varianzeInput  = parser.getVarianzeFile();
		LoadFile image1 = null;
		LoadFile image2 = null;
		
		//Images load
		try{
			if(LoadFile.isRaw(imageFile1)){
				image1 = new LoadFile(imageFile1, imageGeometry1[0], imageGeometry1[1], imageGeometry1[2], imageGeometry1[3], imageGeometry1[4], false);
			}else{
				image1 = new LoadFile(imageFile1);
			}

			if(LoadFile.isRaw(imageFile2)){
				image2 = new LoadFile(imageFile2, imageGeometry2[0], imageGeometry2[1], imageGeometry2[2], imageGeometry2[3], imageGeometry2[4], false);
			}else{
				image2 = new LoadFile(imageFile2);
			}
		}catch(IllegalArgumentException e){
			System.out.println("IMAGE LOAD ERROR Valid formats are: pgm, ppm, pbm, jpg, tiff, png, bmp, gif, fpx. If image is raw data file extension must be \".raw\" or \".img\"");
			System.exit(3);
			
		}catch(RuntimeException e){
			System.out.println("IMAGE LOAD ERROR Valid formats are: pgm, ppm, pbm, jpg, tiff, png, bmp, gif, fpx. If image is raw data file extension must be \".raw\" or \".img\"");
			System.exit(3);
			
		}catch(WarningException e){
			System.out.println("IMAGE LOAD ERROR: " + e.getMessage());
			System.exit(3);
		}

		//Mask load
		byte[][][] maskSamples = null;
		if(maskFile != null){
			
			LoadMask lm = null;
			
			try{
				if (! LoadFile.isRaw(maskFile)) {
					lm = new LoadMask(maskFile);
				} else {
					lm = new LoadMask(maskFile, imageGeometry1[0], imageGeometry1[1], imageGeometry1[2], 0, 1);
				}
			}catch(WarningException e){
				System.out.println(e.getMessage());
				System.exit(0);
			}

			if(ROIValues != null){
				//ROI
				maskSamples = lm.getMaskSamplesByteValue();
			}else{
				//NO-DATA
				maskSamples = lm.getMaskSamplesByte();
			}
			
			// If only one mask component is provided
			// set the same mask for all the components
			int zSize = image1.getImage().length;
			if(maskSamples.length == 1 && maskSamples.length != zSize){
				byte[][][] newMaskSamples = maskSamples;
				
				maskSamples = new byte[zSize][][];
				for(int z = 0; z < zSize; z++){
					maskSamples[z] = newMaskSamples[0];
				}
			}
		}
		
		
		//Invert the mask for the ROI applications
		if((inverse == 1) && (ROIValues != null)){
			for(int z = 0; z < maskSamples.length; z++){
			for(int y = 0; y < maskSamples[z].length; y++){
			for(int x = 0; x < maskSamples[z][y].length; x++){
				if(maskSamples[z][y][x] == -128){
					maskSamples[z][y][x] = 127;
				}else{
					maskSamples[z][y][x] = -128;
				}
			}}}
		}
		
		//Invert the mask for the no-data applications
		if((inverse == 1) && (ROIValues == null)){
			for(int z = 0; z < maskSamples.length; z++){
			for(int y = 0; y < maskSamples[z].length; y++){
			for(int x = 0; x < maskSamples[z][y].length; x++){
				if(maskSamples[z][y][x] == 0){
					maskSamples[z][y][x] = 1;
				}else{
					maskSamples[z][y][x] = 0;
				}
			}}}
		}
		
		//Sets the mask from a list of no-data values in the original image
		float[] noDataValues = parser.getNoDataValues();
		if(noDataValues != null){
			try{
				if(maskSamples != null){
					throw new WarningException("The no-data mask can not be defined twice.");
				}
				GenerateMask gm = new GenerateMask(image1.getImage());
				gm.setNoDataValuesFromParser(noDataValues);
				gm.run();
				maskSamples = gm.getMaskSamplesByte();
			}catch(WarningException e){
				System.out.println("GENERATE MASK PROCESS ERROR: " + e.getMessage());
				System.exit(5);
			}
		}

		//Images compare
		try{
			//Check image types
			Class[] classImage1 = image1.getTypes();
			Class[] classImage2 = image2.getTypes();
			if(component == -1){
				if(classImage1.length != classImage2.length){
					throw new WarningException("Number of image components must be the same for both images.");
				}
				for(int z = 0; z < classImage1.length; z++){
					//if(classImage1[z] != classImage2[z]){
					//	throw new WarningException("Image class types must be the same for both images.");
					//}
				}
			}else{
				if(component - 1 > classImage1.length){
					throw new WarningException("The original image does not have so many components.");
				}
				if(classImage1[component-1] != classImage2[0]){
					throw new WarningException("Image class types must be the same for both images.");
				}
			}
			
			int measure = parser.getMeasure();
			
			int zSize = classImage1.length;
			double energy[] = null; 
			double varianze[] = null;
			
			if(energyInput != ""){
				energy = new double[zSize+1];
				try {
					energyFile = new RandomAccessFile(energyInput, "rw");
					for(int z = 0; z < zSize + 1; z++){
						try {
							energy[z] = Float.parseFloat(energyFile.readLine());
						}catch (IOException e) {
							throw new ErrorException("Energy file can not be read");
						}
					}
				}catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
			}
			if(varianzeInput != ""){
				varianze = new double[zSize+1];
				try {
					varianzeFile = new RandomAccessFile(varianzeInput, "rw");
					for(int z = 0; z < zSize + 1; z++){
						try {
							varianze[z] = Float.parseFloat(varianzeFile.readLine());
						} catch (IOException e) {
							throw new ErrorException("Varianze file can not be read");
						}						
					}
				}catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			//Compare
			ImageCompareSA ic = null;
			if(pixelBitDepth != null){
				int bitDepth[] = null;
				if (pixelBitDepth.length < zSize){
					bitDepth = new int[zSize];
					for(int z = 0; z < pixelBitDepth.length; z++){
						bitDepth[z] = pixelBitDepth[z];
					}
					for(int z = pixelBitDepth.length; z < zSize; z++){
						bitDepth[z] = pixelBitDepth[pixelBitDepth.length-1];
					}
				} else{
					bitDepth = pixelBitDepth;
				}
				ic = new ImageCompareSA(image1.getImage(), image2.getImage(), bitDepth, maskSamples, ROIValues, inverse, component, measure, energy, varianze);
			} else {
				ic = new ImageCompareSA(image1.getImage(), image2.getImage(), image1.getPixelBitDepth(), maskSamples, ROIValues, inverse, component, measure, energy, varianze);
			}
			
			
			double[] mae = ic.getMAE();
			double totalMAE = ic.getTotalMAE();
			double[] pae = ic.getPAE();
			double totalPAE = ic.getTotalPAE();
			double[] mse = ic.getMSE();
			double totalMSE = ic.getTotalMSE();
			double[] rmse = ic.getRMSE();
			double totalRMSE = ic.getTotalRMSE();
			double[] me = ic.getME();
			double totalME = ic.getTotalME();
			double[] snr = ic.getSNR();
			double totalSNR = ic.getTotalSNR();
			double[] psnr = ic.getPSNR();
			double totalPSNR = ic.getTotalPSNR();
			double[] psnrSalomon = ic.getPSNRSALOMON();
			double totalPSNRSALOMON = ic.getTotalPSNRSALOMON();
			boolean[] equal = ic.getEQUAL();
			boolean totalEQUAL = ic.getTotalEQUAL();
			double[] snrVar = ic.getSNRVAR();
			double totalSNRVAR = ic.getTotalSNRVAR();

			//Show metrics
			int totals = parser.getTotals();
			int format = parser.getFormat();
			zSize = classImage1.length;
			
			//To compare a specific component
			if(component != -1){
				zSize = 1;
			}
			
			// TODO: TO BE if(totals < 2){
			if(((zSize > 1) && (totals <= 1)) || (zSize == 1)){
				for(int z = 0; z < zSize; z++){
					if(component != -1){
						z = component - 1;
					}
					if(format == 0) System.out.println("COMPONENT " + z + ":");
					if((measure == 0) || (measure == 1)){
						if(format == 0) System.out.println("  MAE    : " +(float) mae[z]);
						if(format == 1) System.out.print((float)mae[z]);
					}
					if((measure == 0) && (format == 1)) System.out.print(":");
					if((measure == 0) || (measure == 2)){
						if(format == 0) System.out.println("  PAE    : " + (float)pae[z]);
						if(format == 1) System.out.print((float)pae[z]);
					}
					if((measure == 0) && (format == 1)) System.out.print(":");
					if((measure == 0) || (measure == 3)){
						if(ROIValues != null){
							if(format == 0) System.out.println("  P-MSE    : " + (float) mse[z]);
						}else{
							if(format == 0) System.out.println("  MSE    : " + (float) mse[z]);
						}
						if(format == 1) System.out.print((float) mse[z]);
					}
					if((measure == 0) && (format == 1)) System.out.print(":");
					if((measure == 0) || (measure == 4)){
						if(format == 0) System.out.println("  RMSE   : " +(float) rmse[z]);
						if(format == 1) System.out.print((float)rmse[z]);
					}
					if((measure == 0) && (format == 1)) System.out.print(":");
					if((measure == 0) || (measure == 5)){
						if(format == 0) System.out.println("  ME     : " + (float)me[z]);
						if(format == 1) System.out.print((float)me[z]);
					}
					if((measure == 0) && (format == 1)) System.out.print(":");
					if((measure == 0) || (measure == 6)){
						if(format == 0) System.out.println("  SNR    : " +(float) snr[z]);
						if(format == 1) System.out.print((float)snr[z]);
					}
					if((measure == 0) && (format == 1)) System.out.print(":");
					if((measure == 0) || (measure == 7)){
						if(ROIValues != null){
							if(format == 0) System.out.println("  P-PSNR : " + (float)psnr[z]);
						}else{
							if(format == 0) System.out.println("  PSNR   : " + (float)psnr[z]);
						}
						if(format == 1) System.out.print((float)psnr[z]);
					}
					if((measure == 0) && (format == 1)) System.out.print(":");
					if((measure == 0) || (measure == 8)){
						if(format == 0) System.out.println("  PSNR-S : " + (float)psnrSalomon[z]);
						if(format == 1) System.out.print((float)psnrSalomon[z]);
					}
					if((measure == 0) && (format == 1)) System.out.print(":");
					if((measure == 0) || (measure == 9)){
						if(format == 0) System.out.println("  SNRVAR : " + (float)snrVar[z]);
						if(format == 1) System.out.print((float)snrVar[z]);
					}
					if((measure == 0) && (format == 1)) System.out.print(":");
					if((measure == 0) || (measure == 10)){
						if(format == 0) System.out.println("  EQUAL  : " + equal[z]);
						if(format == 1) System.out.print(equal[z]);
					}
					if(format == 1) System.out.print("\n");
					if(component != -1){
						z = zSize;
					}
				}
			}

			//TODO REPLACE THE NEXT LINE WITH THIS if(totals > 0){
			if((zSize > 1) && (totals >= 1)){
				if(format == 0) System.out.println("TOTALS:");
				if((measure == 0) || (measure == 1)){
					if(format == 0) System.out.println("  MAE    : " + (float)totalMAE);
					if(format == 1) System.out.print((float)totalMAE);
				}
				if((measure == 0) && (format == 1)) System.out.print(":");
				if((measure == 0) || (measure == 2)){
					if(format == 0) System.out.println("  PAE    : " + (float)totalPAE);
					if(format == 1) System.out.print((float)totalPAE);
				}
				if((measure == 0) && (format == 1)) System.out.print(":");
				if((measure == 0) || (measure == 3)){
					if(format == 0) System.out.println("  MSE    : " + (float) totalMSE);
					if(format == 1) System.out.print((float) totalMSE);
				}
				if((measure == 0) && (format == 1)) System.out.print(":");
				if((measure == 0) || (measure == 4)){
					if(format == 0) System.out.println("  RMSE   : " + (float) totalRMSE);
					if(format == 1) System.out.print((float)totalRMSE);
				}
				if((measure == 0) && (format == 1)) System.out.print(":");
				if((measure == 0) || (measure == 5)){
					if(format == 0) System.out.println("  ME     : " +(float) totalME);
					if(format == 1) System.out.print((float)totalME);
				}
				if((measure == 0) && (format == 1)) System.out.print(":");
				if((measure == 0) || (measure == 6)){
					if(format == 0) System.out.println("  SNR    : " +(float) totalSNR);
					if(format == 1) System.out.print((float)totalSNR);
				}
				if((measure == 0) && (format == 1)) System.out.print(":");
				if((measure == 0) || (measure == 7)){
					if(totalPSNR < 0){
						if(format == 0) System.out.println("  PSNR   : Pixel bit depth must be de the same for each component.");
						if(format == 1) System.out.print("Error");
					}else{
						if(format == 0) System.out.println("  PSNR   : " + (float) totalPSNR);
						if(format == 1) System.out.print((float) totalPSNR);
					}
				}
				if((measure == 0) && (format == 1)) System.out.print(":");
				if((measure == 0) || (measure == 8)){
					if(totalPSNRSALOMON < 0){
						if(format == 0) System.out.println("  PSNR   : Pixel bit depth must be de the same for each component.");
						if(format == 1) System.out.print("Error");
					}else{
						if(format == 0) System.out.println("  PSNR-S : " + (float) totalPSNRSALOMON);
						if(format == 1) System.out.print((float) totalPSNRSALOMON);
					}
				}
				if((measure == 0) && (format == 1)) System.out.print(":");
				if((measure == 0) || (measure == 9)){
					if(format == 0) System.out.println("  SNRVAR : " +(float) totalSNRVAR);
					if(format == 1) System.out.print((float)totalSNRVAR);
				}
				if((measure == 0) && (format == 1)) System.out.print(":");
				if((measure == 0) || (measure == 10)){
					if(format == 0) System.out.println("  EQUAL  : " + totalEQUAL);
					if(format == 1) System.out.print(totalEQUAL);
				}
				if(format == 1) System.out.print("\n");
			}

		}catch(WarningException e){
			System.out.println("IMAGE COMPARE ERROR: " + e.getMessage());
			System.exit(4);
		}

	}

}
