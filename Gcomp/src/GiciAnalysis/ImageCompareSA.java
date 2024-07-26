/*
 * GICI Library -
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
package GiciAnalysis;
import java.util.AbstractSet;
import java.util.concurrent.ConcurrentSkipListSet;

import GiciException.*;


/**
 * This class receives two images and calculates its difference information (MSE and PSNR).
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class ImageCompareSA{

	/**
	 * variance for each image component.
	 * <p>
	 * Only positive values allowed.
	 */
	double[] variance = null;
	
	/**
	 * total variance
	 * <p>
	 * Only positive values allowed.
	 */
	double totalVariance;
	
	/**
	 * energy for each image component.
	 * <p>
	 * Only positive values allowed.
	 */
	double[] energy = null;
	
	/**
	 * Total energy.
	 * <p>
	 * Only positive values allowed.
	 */
	double totalEnergy=0;
	
	/**
	 * Mean Absolute Error (MAE) for each image component.
	 * <p>
	 * Only positive values allowed.
	 */
	double[] mae = null;

	/**
	 * Global Mean Absolute Error (MSE) of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	double totalMAE = 0;

	/**
	 * Peak Absolute Error (MAE) for each image component.
	 * <p>
	 * Only positive values allowed.
	 */
	double[] pae = null;

	/**
	 * Global Peak Absolute Error (PAE) of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	double totalPAE = 0;

	/**
	 * Mean Squared Error (MSE) for each image component.
	 * <p>
	 * Only positive values allowed.
	 */
	double[] mse = null;

	/**
	 * Global Mean Squared Error (MSE) of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	double totalMSE = 0;

	/**
	 * Root Mean Squared Error (MSE) for each image component.
	 * <p>
	 * Only positive values allowed.
	 */
	double[] rmse = null;

	/**
	 * Global Root Mean Squared Error (MSE) of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	double totalRMSE=0;

	/**
	 * Mean Error (ME) for each image component.
	 * <p>
	 * All values allowed.
	 */
	double[] me = null;

	/**
	 * Global Mean Error (MSE) of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	double totalME = 0;

	/**
	 * Signal to Noise Ratio (SNR) for each image component.
	 * <p>
	 * Only positive values allowed.
	 */
	double[] snr = null;
	
	/**
	 * Signal to Noise Ratio (SNR) for each image component calculated with original image Variance.
	 * <p>
	 * Only positive values allowed.
	 */
	double[] snrVar = null;

	/**
	 * Global Signal to Noise Ratio (SNR) of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	double totalSNR = 0;
	
	/**
	 * Global Signal to Noise Ratio calculated with original image variance (SNR) of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	double totalSNRVAR = 0;

	/**
	 * Peak Signal to Noise Ratio (PSNR) for each image component.
	 * <p>
	 * Only positive values allowed.
	 */
	double[] psnr = null;
	double[] psnrSalomon = null;

	/**
	 * Global Peak Signal to Noise Ratio (PSNR) of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	double totalPSNR;
	double totalPSNRSALOMON;

	/**
	 * Equality for each image component.
	 * <p>
	 * True or false.
	 */
	boolean[] equal;

	/**
	 * Global equality of the image.
	 * <p>
	 * True or false.
	 */
	boolean totalEQUAL;

	/**
	 * Number of pixels encoded for the current mask.
	 * <p>
	 * Only positive values allowed.
	 */
	long[] imagePixels = null;
	
	/**
	 * Contains the relationship between mask values and weights to be applied during the distortion measure calculation
	 * <p>
	 * Only positive values allowed
	 */
	float[] ROIValues = null;
	
	/**
	 * Indicates if the definition of the mask is inverted or not.
	 */
	int inverse = 0;
	
	/**
	 * Indicates which is the component to compare.
	 */
	AbstractSet<Integer> components = new ConcurrentSkipListSet<Integer>();

	/**
	 * Original Image
	 */
	float[][][] image1 = null;
	
	/**
	 * Recovered Image
	 */
	float[][][] image2 = null;
	
	/**
	 * mask
	 */
	byte[][][] mask = null;
	
	/**
	 * Constructor that does all the operations to compare images.
	 *
	 * @param image1 a 3D float array of image samples (index are [z][y][x])
	 * @param image2 a 3D float array of image samples (index are [z][y][x])
	 * @param pixelBitDepth number of bits for the specified image sample type (for each component)
	 * @param variance_deprecated is no longer used because is cheap to compute and the aggregated 
	 * variance is not trivial to compute (although not impossible if you have the energy)
	 *
	 * @throws WarningException when image sizes are not the same
	 */
	public ImageCompareSA(float[][][] image1, float[][][] image2, int[] pixelBitDepth, byte[][][] mask, float[] ROIValues, int inverse, int component, int measure, double[] energy_deprecated, double[] variance_deprecated) throws WarningException{
		
		this.image1 = image1;
		this.image2 = image2;
		this.mask = mask;
		
		//Size set
		int zSize1 = image1.length;
		int ySize1 = image1[0].length;
		int xSize1 = image1[0][0].length;

		int zSize2 = image2.length;
		int ySize2 = image2[0].length;
		int xSize2 = image2[0][0].length;
		
		//FIXME: component will be a set someday
		if (component == -1) {	
			for(int z = 0; z < zSize1; z++) {
				this.components.add(z);
			}
		} else {
			this.components.add(component - 1);
		}
		
		// Check if images have the same size
		if(component == -1){
			if((zSize1 != zSize2) || (ySize1 != ySize2) || (xSize1 != xSize2)){
				throw new WarningException("Image sizes must be the same to perform comparisons.");
			}
		}else{
			if((ySize1 != ySize2) || (xSize1 != xSize2)){
				throw new WarningException("Image sizes (ySize and xSize) must be the same to perform comparisons for a specific component.");
			}
		}
		
		int maskZSize = 0;
		int maskYSize = 0;
		int maskXSize = 0;
		
		if(mask != null){
			maskZSize = mask.length;
			maskYSize = mask[0].length;
			maskXSize = mask[0][0].length;
	
			//Check if the mask has same sizes
			if((maskZSize != zSize2) || (maskYSize != ySize2) || (maskXSize != xSize2)){
				throw new WarningException("Mask sizes must be the same to perform comparisons.");
			}
		}
		
		// Check for overflows
		// We are using IEEE 754 binary64 doubles with 53 bits of mantisa 	
		// New strategy: check for the error after it occurs		
		int imprecisionBits = 0;
				
		// ROI
		this.ROIValues = ROIValues;
		
		// Count the mask pixels		
		imagePixels = new long[zSize1];
		long totalImagePixels = 0;

		for(int z: components) {
			if(mask != null) {
				//Number of pixels in the foreground of the image
				for(int y = 0; y < ySize1; y++) {
					for(int x = 0; x < xSize1; x++) {
						if(mask[z][y][x] == 1) {
							imagePixels[z]++;
						}
					}
				}
			} else {
				imagePixels[z] = xSize1 * ySize1;
			}
			
			totalImagePixels += imagePixels[z];
		}
		
		// Do all the energy things before
		energy = new double[zSize1];
		variance = new double[zSize1];
		totalEnergy = 0;
		totalVariance = 0;
		
		double[] mean = new double[zSize1];
		double totalMean = 0;
		double totalSquaredDifferenceSum = 0;
		
		// Two iterations are need for numerical stability
		// It is _not_ possible to use the abbreviated formula
		for(int z: components) {
			double sum = 0;
			double squaredSum = 0;
			
			for(int y = 0; y < image1[z].length; y++) {
				for(int x = 0; x < image1[z][y].length; x++) {
					if (mask == null || mask[z][y][x] == 1) {
						double value = image1[z][y][x];
						sum += value;
						squaredSum += value * value;
					}
				}
			}
			
			mean[z] = sum / imagePixels[z];
			totalMean += sum;
			
			energy[z] = squaredSum;
			totalEnergy += energy[z];
		}
		
		// Overflow check
		imprecisionBits = imprecisionBits(totalEnergy, imprecisionBits);
		imprecisionBits = imprecisionBits(totalMean, imprecisionBits);
		
		totalMean /= totalImagePixels;
		
		for(int z: components) {
			double squaredDifferenceSum = 0;
			
			for(int y = 0; y < image1[z].length; y++) {
				for(int x = 0; x < image1[z][y].length; x++) {
					if (mask == null || mask[z][y][x] == 1) {
						double value = image1[z][y][x] - mean[z];
						squaredDifferenceSum += value * value;
						
						double totalValue = image1[z][y][x] - totalMean;
						totalSquaredDifferenceSum += totalValue * totalValue;
					}
				}
			}
			
			variance[z] = squaredDifferenceSum / imagePixels[z];
		}
		
		// Overflow check
		imprecisionBits = imprecisionBits(totalSquaredDifferenceSum, imprecisionBits);
		
		totalVariance = totalSquaredDifferenceSum / totalImagePixels;
		
		// Memory allocation for the intermediate results
		double[] absoluteErrorSum = new double[zSize1];
		double[] absoluteErrorPeak = new double[zSize1];
		double[] squaredErrorSum = new double[zSize1];
		double[] errorSum = new double[zSize1];
		
		//long[] squaredErrorSumInt = new long[zSize1];
		
		// Fill the intermediate results
		for(int z: components) {
			// Initialize (all the others are properly initialized as 0)
			absoluteErrorPeak[z] = Double.NEGATIVE_INFINITY;
			
			// Main loop
			for(int y = 0; y < image1[z].length; y++){
				for(int x = 0; x < image1[z][y].length; x++){
					double error = getDiff(z, y, x);

					// Fill
					absoluteErrorSum[z] += Math.abs(error);
					absoluteErrorPeak[z] = Math.max(absoluteErrorPeak[z], Math.abs(error));
					squaredErrorSum[z] += error * error;
					errorSum[z] += error;
					
					//squaredErrorSumInt[z] += (long) error * error;
				}
			}
		}
		
		// Memory allocation for the results
		mae = new double[zSize1];
		pae = new double[zSize1];
		mse = new double[zSize1];
		rmse = new double[zSize1];
		me = new double[zSize1];
		snr = new double[zSize1];
		snrVar = new double[zSize1];
		psnr = new double[zSize1];
		psnrSalomon = new double[zSize1];
		equal = new boolean[zSize1];
		
		// Generate final per component results
		for(int z: components) {
			
			double range = getRange(pixelBitDepth[z]);
			
			mae[z] = absoluteErrorSum[z] / imagePixels[z];
			pae[z] = absoluteErrorPeak[z];
			mse[z] = squaredErrorSum[z] / imagePixels[z];
			rmse[z] = Math.sqrt(mse[z]);
			me[z] = errorSum[z] / imagePixels[z];
			snr[z] = 10 * Math.log10( energy[z]  / (mse[z] * imagePixels[z]) );
			snrVar[z] = 10 * Math.log10( variance[z] / mse[z] );
			psnr[z] = 10 * Math.log10( range*range / mse[z] );
			psnrSalomon[z] = 10 * Math.log10( (range+1)*(range+1) / (4 * mse[z]) );
			equal[z] = ( absoluteErrorSum[z] == 0 );

		}
		
		// Calculus of Total Intermediate results
		double totalAbsoluteErrorSum = 0;
		double totalAbsoluteErrorPeak = Double.NEGATIVE_INFINITY;
		double totalSquaredErrorSum = 0;
		double totalErrorSum = 0;
		
		//long totalSquaredErrorSumInt = 0;
		
		for(int z: components) {
			totalAbsoluteErrorSum += absoluteErrorSum[z];
			totalAbsoluteErrorPeak = Math.max(totalAbsoluteErrorPeak, absoluteErrorPeak[z]);
			totalSquaredErrorSum += squaredErrorSum[z];
			totalErrorSum += errorSum[z];
			
			//totalSquaredErrorSumInt += squaredErrorSumInt[z];
		}
		
		// Overflow check
		imprecisionBits = imprecisionBits(totalAbsoluteErrorSum, imprecisionBits);
		imprecisionBits = imprecisionBits(totalAbsoluteErrorPeak, imprecisionBits);
		imprecisionBits = imprecisionBits(totalSquaredErrorSum, imprecisionBits);
				
		// Calculus of Total final results
		double totalRange = getRange(pixelBitDepth[0]);
		
		for (int bitDepth: pixelBitDepth) {
			if (getRange(bitDepth) != totalRange) {
				throw new WarningException("Totals may be undefined as bit depth varies across components.");
			}
		}
		
		totalMAE = totalAbsoluteErrorSum / totalImagePixels;
		totalPAE = totalAbsoluteErrorPeak;
		totalMSE = totalSquaredErrorSum / totalImagePixels;
		//totalMSE = ((double) totalSquaredErrorSumInt) / (double) totalImagePixels; in case of precision doubts  
		totalRMSE = Math.sqrt(totalMSE);
		totalME = totalErrorSum / totalImagePixels;
		totalSNR = 10 * Math.log10( totalEnergy  / (totalMSE * totalImagePixels) );
		totalSNRVAR = 10 * Math.log10( totalVariance / totalMSE );
		totalPSNR = 10 * Math.log10( totalRange * totalRange / totalMSE );
		totalPSNRSALOMON = 10 * Math.log10( (totalRange + 1)*(totalRange + 1) / (4 * totalMSE));
		totalEQUAL = ( totalAbsoluteErrorSum == 0 );
		
		// Report overflows in case they occur
		if (imprecisionBits > 0) {
			System.err.println("Inexact results may be produced due to insufficient mantissa bits (at least " + imprecisionBits + " more bits required).");
			
			double voxels = zSize1 * ySize1 * xSize1;

			double maxBitDepthRange = 0;

			for (int z: components) {
				if (pixelBitDepth[z] > 12) {
					maxBitDepthRange = Math.max(getRealRange(z), maxBitDepthRange);
				} else {
					maxBitDepthRange = Math.max(getRange(pixelBitDepth[z]), maxBitDepthRange);
				}
			}

			// requiredBits be inacurate due to insufficient mantisa bits, but it shall work anyway
			// maxBitDepthRange is squared because it is also squared in the computation
			double requiredBits = Math.ceil(Math.log(voxels * maxBitDepthRange * maxBitDepthRange)/Math.log(2));
			double requiredBitsNoMSE = Math.ceil(Math.log(voxels * maxBitDepthRange)/Math.log(2));
			double requiredBitsNoMulti = Math.ceil(Math.log(ySize1 * xSize1 * maxBitDepthRange * maxBitDepthRange)/Math.log(2));
			double requiredBitsNoMultiMSE = Math.ceil(Math.log(ySize1 * xSize1 * maxBitDepthRange)/Math.log(2));

			final int maxMantisa = 52; 

			if (requiredBits > maxMantisa) {
				System.err.println("Image too large for exact computations. Required precision for this image is " + requiredBits + " bits.");

				if (requiredBitsNoMSE <= maxMantisa) {
					System.err.println("* All but MSE related measures (MSE, PSNR, SNR) are still accurate.");
				}

				if (requiredBitsNoMulti <= maxMantisa) {
					System.err.println("* Individual component results are still accurate");
				}

				if (requiredBitsNoMultiMSE <= maxMantisa && requiredBitsNoMSE > maxMantisa && requiredBitsNoMulti > maxMantisa) {
					System.err.println("* Individual component results of all but MSE related measures (MSE, PSNR, SNR) are still accurate.");
				}
			}
		}
	}
	
	private int imprecisionBits(final double a, final int previousMax) {
		final double largestExactDouble = 0x1FFFFFFFFFFFFFl;
		int r = 0;
		
		if (a > largestExactDouble) {
			r = Math.max(previousMax, (int)Math.ceil(Math.log(a / largestExactDouble) / Math.log(2)));
		}
		
		return r;
	}
	
	/**
	 * @param z indicating the component
	 * @return range for an specific component
	 */
	public double getRealRange(int z){
		
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;

		for (int y = 0; y < image1[z].length; y++) {
			for (int x = 0; x < image1[z][y].length; x++) {
				min = Math.min(Math.min(image1[z][y][x], image2[z][y][x]), min);
				max = Math.max(Math.max(image1[z][y][x], image2[z][y][x]), max);
			}
		}
		
		// assume signed
		int pixelBitDepth = (int)Math.max(Math.ceil(Math.log(max + 1) / Math.log(2)), Math.ceil(Math.log(Math.abs(min)) / Math.log(2))) + 1;
		// (int)Math.ceil(Math.log(max + 1) / Math.log(2));
		
		double range = 0;
		double maxFactor = 0;
		if(ROIValues != null){
			for(int i = 0; i < ROIValues.length / 2; i++){
				//first measure
				if(ROIValues[(i*2)+1] > maxFactor){
					maxFactor = ROIValues[(i*2)+1];
				}
			}
			range = Math.pow(2D, (double) pixelBitDepth) * maxFactor - 1;
		}else{
			range = Math.pow(2D, (double) pixelBitDepth) - 1;
		}
		return(range);
	}
	
	/**
	 * @param z indicating the component
	 * @param pixelBitDepth
	 * @return range for an specific component
	 */
	public double getRange(int pixelBitDepth){
		double range = 0;
		double maxFactor = 0;
		if(ROIValues != null){
			for(int i = 0; i < ROIValues.length / 2; i++){
				//first measure
				if(ROIValues[(i*2)+1] > maxFactor){
					maxFactor = ROIValues[(i*2)+1];
				}
			}
			range = Math.pow(2D, (double) pixelBitDepth) * maxFactor - 1;
		}else{
			range = Math.pow(2D, (double) pixelBitDepth) - 1;
		}
		return(range);
	}
	
	/**
	 * @param z indicating de component
	 * @param x 
	 * @param y
	 * @return difference between original and recovered, considering the factor used in prioritized distortion measures
	 */
	private double getDiff(int z, int y, int x){
		double factor = 1D;
		double diff = 0D;
		
		if(ROIValues != null){
			//Difference for the P-MSE
			for(int i = 0; i < ROIValues.length / 2; i++){
				if(ROIValues[i*2] - 128 == mask[z][y][x]){
					factor = ROIValues[(i*2)+1];
					i = ROIValues.length;
				}
			}
			diff = (image1[z][y][x] - image2[z][y][x]) * factor;
		}else{
			//Standard difference calculation
			if(mask == null || mask[z][y][x] == 1){
				diff = (image1[z][y][x] - image2[z][y][x]);
			}
		}
		return(diff);
	}

	/**
	 * @return mae definition in this class
	 */
	public double[] getMAE(){
		return(mae);
	}

	/**
	 * @return totalMAE definition in this class
	 */
	public double getTotalMAE(){
		return(totalMAE);
	}

	/**
	 * @return pae definition in this class
	 */
	public double[] getPAE(){
		return(pae);
	}

	/**
	 * @return totalPAE definition in this class
	 */
	public double getTotalPAE(){
		return(totalPAE);
	}

	/**
	 * @return mse definition in this class
	 */
	public double[] getMSE(){
		return(mse);
	}

	/**
	 * @return totalMSE definition in this class
	 */
	public double getTotalMSE(){
		return(totalMSE);
	}

	/**
	 * @return rmse definition in this class
	 */
	public double[] getRMSE(){
		return(rmse);
	}

	/**
	 * @return totalRMSE definition in this class
	 */
	public double getTotalRMSE(){
		return(totalRMSE);
	}

		/**
	 * @return me definition in this class
	 */
	public double[] getME(){
		return(me);
	}

	/**
	 * @return totalME definition in this class
	 */
	public double getTotalME(){
		return(totalME);
	}

	/**
	 * @return snr definition in this class
	 */
	public double[] getSNR(){
		return(snr);
	}

	/**
	 * @return totalSNR definition in this class
	 */
	public double getTotalSNR(){
		return(totalSNR);
	}

	/**
	 * @return psnr definition in this class
	 */
	public double[] getPSNR(){
		return(psnr);
	}

	/**
	 * @return totalPSNR definition in this class
	 */
	public double getTotalPSNR(){
		return(totalPSNR);
	}
	
	/**
	 * @return psnr definition in this class
	 */
	public double[] getPSNRSALOMON(){
		return(psnrSalomon);
	}

	/**
	 * @return totalPSNR definition in this class
	 */
	public double getTotalPSNRSALOMON(){
		return(totalPSNRSALOMON);
	}

	/**
	 * @return equal definition in this class
	 */
	public boolean[] getEQUAL(){
		return(equal);
	}

	/**
	 * @return totalEQUAL definition in this class
	 */
	public boolean getTotalEQUAL(){
		return(totalEQUAL);
	}

	/**
	 * @return snr definition in this class
	 */
	public double[] getSNRVAR(){
		return(snrVar);
	}

	/**
	 * @return totalSNR definition in this class
	 */
	public double getTotalSNRVAR(){
		return(totalSNRVAR);
	}
}
