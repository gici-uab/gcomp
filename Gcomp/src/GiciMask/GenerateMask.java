package GiciMask;

//import java.io.*;
import GiciException.*;

/**
 * Main class of GenerateMask application. This class allows to load a text file from disk
 * that specifies the no-data values in an image and generates a boolean mask to apply the
 * Shape-Adaptive Wavelet Transform and later a dedicated Bit Plane Encoding Method.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */

public class GenerateMask {
	
	/**
	 * This flag marks if the no-data values are set.
	 * <p>
	 * True indicates that the no-data values are set, otherwise false.
	 */
	protected boolean setNoDataValues = false;

	/**
	 * Array that contains the the no-data values.
	 * <p>
	 * Only real numbers are allowed.
	 */
	protected float[] noDataValues = null;
	
	/**
	 * Array that contains the mask for the image.
	 * <p>
	 * Only boolean values are allowed.
	 */
	protected byte[][][] maskSamples = null;

	/**
	 * Samples of the image.
	 * <p>
	 * This values has to be understood as an image samples.
	 */
	protected float[][][] imageSamples = null;
	
	
	/**
	 * Constructor of the class. Copy the image to the object.
	 * 
	 * @param imageSamples the input image to find the no-data values.
	 * 
	 * @throws WarningException if the input array is null.
	 */	
	public GenerateMask(float[][][] imageSamples) throws WarningException{
		//Ensures the validity of the parameters
		if(imageSamples == null){
			throw new WarningException("The mask can not be generated.");
		}
		//Sets the attributes
		this.imageSamples = imageSamples;
	}
	
	/**
	 * Finds the no-data values from a file sotred in the hard disk.
	 * 
	 * @param fileName path and name of the file that contains the no-data values.
	 */	
	public void setNoDataValuesFromFile(String fileName){
		System.out.println("Not avaible yet.");
	}
	
	/**
	 * Takes the no-data values from a string array taken from the application parser line.
	 * 
	 * @param noDataValues no-data values in float format
	 */	
	public void setNoDataValuesFromParser(float[] noDataValues){
		setNoDataValues = true;
		this.noDataValues = noDataValues;
		//noDataValues = new float[noDataString.length];
		
		//for(int i = 0; i < noDataString.length; i++){
			//noDataValues[i] = Float.parseFloat(noDataString[i]);
		//}
	}
	
	/**
	 * Finds the no-data values in the input image and generates a boolean mask.
	 * 
	 * @throws WarningException if the no-data values array is not set.
	 */	
	public void run() throws WarningException{
		if(!setNoDataValues){
			throw new WarningException("The no-data values are not set.");
		}
		//Image dimensions
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		
		//Memory allocation
		maskSamples = new byte[zSize][ySize][xSize];
		
		//Raster the image to set the no-data values
		for(int z = 0; z < zSize; z++){
		for(int y = 0; y < ySize; y++){
		for(int x = 0; x < xSize; x++){
			maskSamples[z][y][x] = 1;
			for(int nd = 0; nd < noDataValues.length; nd++){
				if(imageSamples[z][y][x] == noDataValues[nd]){
					maskSamples[z][y][x] = 0;
					nd = noDataValues.length;
				}
			}
		}}}
	}
	
	/**
	 * Returns the mask generated by the run method.
	 * 
	 * @return the generated mask with the no-data values.
	 */	
	public byte[][][] getMaskSamplesByte(){
		return(maskSamples);
	}
	
	/**
	 * Returns the mask generated by the run method in boolean format.
	 * 
	 * @return the generated mask with the no-data values.
	 */	
	public boolean[][][] getMaskSamplesBoolean(){
		//Image dimensions
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		
		//Memory allocation
		boolean[][][] maskSamplesBoolean = new boolean[zSize][ySize][xSize];
		
		//Raster the image to transform the values
		for(int z = 0; z < zSize; z++){
		for(int y = 0; y < ySize; y++){
		for(int x = 0; x < xSize; x++){
			if(maskSamples[z][y][x] == 0){
				maskSamplesBoolean[z][y][x] = false;
			}else{
				maskSamplesBoolean[z][y][x] = true;
			}			
		}}}
		
		return(maskSamplesBoolean);
	}
}
