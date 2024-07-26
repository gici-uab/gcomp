package GiciMask;

import GiciException.*;
import GiciFile.LoadFile;

/**
 * Main class of LoadMask application.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */

public class LoadMask{
	/**
	 * Mask samples (index meaning [z][y][x]).
	 * <p>
	 * All values allowed.
	 */
	float[][][] maskSamplesFloat = null;

	/**
	 * Main method of LoadMask application. It loads a mask from a visible format file as .pgm.
	 *
	 * @param maskFile the file where the mask is stored
	 */
	public LoadMask(String maskFile) throws WarningException{
		LoadFile maskLoad;
		
		maskLoad = new LoadFile(maskFile);
		
		maskSamplesFloat = maskLoad.getImage();
	}

	/**
	 * Main method of LoadMask application. It loads a mask from a file in raw data format..
	 *
	 * @param maskFile the file where the mask is stored
	 * @param zSize number of mask componets
	 * @param ySize mask height
	 * @param xSize mask width
	 * @param sampleType an integer representing the class of image samples type
	 * @param byteOrder 0 if BIG_ENDIAN, 1 if LITTLE_ENDIAN
	 */
	public LoadMask(String maskFile, int zSize, int ySize, int xSize, int sampleType, int byteOrder) throws WarningException{
		LoadFile maskLoad;
		
		maskLoad = new LoadFile(maskFile, zSize, ySize, xSize, sampleType, byteOrder, false);
		
		maskSamplesFloat = maskLoad.getImage();
	}

	/**
	 * Returns the mask in a boolean structure.
	 *
	 */
	public byte[][][] getMaskSamples(){
		int zSize = maskSamplesFloat.length;
		int ySize = maskSamplesFloat[0].length;
		int xSize = maskSamplesFloat[0][0].length;
		byte[][][] maskSamples = null;
		
		maskSamples = new byte[zSize][ySize][xSize];
		
		for(int z = 0; z < zSize; z++){
			for(int y = 0; y < ySize; y++){
				for(int x = 0; x < xSize; x++){
					if(maskSamplesFloat[z][y][x] != 0){
						maskSamples[z][y][x] = (byte)maskSamplesFloat[z][y][x];
					}
				}
			}
		}
		return(maskSamples);
	}
	
	/**
	 * Returns the mask in a boolean structure.
	 *
	 */
	public boolean[][][] getMaskSamplesBoolean(){
		int zSize = maskSamplesFloat.length;
		int ySize = maskSamplesFloat[0].length;
		int xSize = maskSamplesFloat[0][0].length;
		boolean[][][] maskSamplesBoolean;
		
		maskSamplesBoolean = new boolean[zSize][ySize][xSize];
		
		for(int z = 0; z < zSize; z++){
			for(int y = 0; y < ySize; y++){
				for(int x = 0; x < xSize; x++){
					if(maskSamplesFloat[z][y][x] > 0){
						maskSamplesBoolean[z][y][x] = true;
					}else{
						maskSamplesBoolean[z][y][x] = false;
					}
				}
			}
		}
		
		return(maskSamplesBoolean);
	}

	/**
	 * Returns the mask in a byte structure.
	 *
	 */
	public byte[][][] getMaskSamplesByte(){
		int zSize = maskSamplesFloat.length;
		int ySize = maskSamplesFloat[0].length;
		int xSize = maskSamplesFloat[0][0].length;
		byte[][][] maskSamplesByte;
		
		maskSamplesByte = new byte[zSize][ySize][xSize];
		
		for(int z = 0; z < zSize; z++){
			for(int y = 0; y < ySize; y++){
				for(int x = 0; x < xSize; x++){
					if(maskSamplesFloat[z][y][x] != 0){
						maskSamplesByte[z][y][x] = 1;
					}
				}
			}
		}
		
		return(maskSamplesByte);
	}

	/**
	 * Returns the mask in a byte structure.
	 *
	 */
	public byte[][][] getMaskSamplesByteValue(){
		int zSize = maskSamplesFloat.length;
		int ySize = maskSamplesFloat[0].length;
		int xSize = maskSamplesFloat[0][0].length;
		byte[][][] maskSamplesByte;
		
		maskSamplesByte = new byte[zSize][ySize][xSize];
		
		for(int z = 0; z < zSize; z++){
			for(int y = 0; y < ySize; y++){
				for(int x = 0; x < xSize; x++){
						maskSamplesByte[z][y][x] = (byte)(maskSamplesFloat[z][y][x] - 128);
				}
			}
		}
		
		return(maskSamplesByte);
	}	
	
	/**
	 * Returns the mask in an integer structure.
	 *
	 */
	public int[][][] getMaskSamplesInt(){
		int zSize = maskSamplesFloat.length;
		int ySize = maskSamplesFloat[0].length;
		int xSize = maskSamplesFloat[0][0].length;
		int[][][] maskSamplesInt;
		
		maskSamplesInt = new int[zSize][ySize][xSize];
		
		for(int z = 0; z < zSize; z++){
			for(int y = 0; y < ySize; y++){
				for(int x = 0; x < xSize; x++){
					maskSamplesInt[z][y][x] = (int) maskSamplesFloat[z][y][x];
				}
			}
		}
		
		return(maskSamplesInt);
	}

}
