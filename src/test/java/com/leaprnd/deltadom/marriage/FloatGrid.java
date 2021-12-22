package com.leaprnd.deltadom.marriage;

public class FloatGrid {

	private final float[][] strengths;
	private final int width;
	private final int height;

	public FloatGrid(float[][] strengths, int width, int height) {
		this.strengths = strengths;
		this.width = width;
		this.height = height;
	}

	public float getStrengthOfPotentialMarriageBetween(int indexOfMan, int indexOfWoman) {
		return strengths[indexOfMan][indexOfWoman];
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}