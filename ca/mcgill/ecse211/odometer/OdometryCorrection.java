/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometer;

import ca.mcgill.ecse211.lab2.Lab2;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;

public class OdometryCorrection implements Runnable {
	private static final long CORRECTION_PERIOD = 10;
	private Odometer odometer;

	//data
	private float color[];
	private final double SQUARE_SIZE = 30.48;
	private float[] csData;
	private double correctX, correctY;
	double[] oldResult = new double [3];
	double oldSample;
	int passedLine;
	double newColor;



	/**
	 * This is the default class constructor. An existing instance of the odometer is used. This is to
	 * ensure thread safety.
	 * 
	 * @throws OdometerExceptions
	 */
	public OdometryCorrection() throws OdometerExceptions {

		this.odometer = Odometer.getOdometer();

		//color sensor
		color = new float[Lab2.myColorSample.sampleSize()];
		this.csData = color;
		correctX = 0;
		correctY =0;
		oldSample =0;
		passedLine = 0;


	}

	/**
	 * Here is where the odometer correction code should be run.
	 * 
	 * @throws OdometerExceptions
	 */
	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;

		//color sensor and scaling
		Lab2.myColorSample.fetchSample(color, 0);
		//lastColor = csData * 1000;

		while (true) {
			correctionStart = System.currentTimeMillis();

			// TODO Trigger correction (When do I have information to correct?)
			Lab2.myColorSample.fetchSample(color, 0);
			newColor = csData[0];
			
			// TODO Calculate new (accurate) robot position
			double[] result = odometer.getXYT();
			double theta = result[2];
			
			//If line detected (intensity less than 0.3
			if((newColor) < 0.3) {
				
				//Error handling
				if(result != null) {
					
					//Beep to notify, update counter and find correct X and Y using old reference pts
					Sound.beep();
					correctX = oldResult[0] + SQUARE_SIZE*Math.sin((Math.PI /180)*theta);     //convert to radians for sin
					correctY = oldResult[1] + SQUARE_SIZE*Math.cos((Math.PI /180)*theta);	  //convert to radians for cos
					passedLine++;
					String printThis = passedLine +"";
					LCD.drawString(printThis, 0, 4);
				}
				
				//Set new correct XYT and store info for next loop
				odometer.setXYT(correctX, correctY, theta);
				oldResult[0] = correctX;
				oldResult[1] = correctY;
				oldResult[2] = theta;
				oldSample = newColor;
			}
			
			// TODO Update odometer with new calculated (and more accurate) vales

			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here
				}
			}
		}
	}
}
