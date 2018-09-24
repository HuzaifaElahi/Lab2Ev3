/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometer;

import ca.mcgill.ecse211.lab2.Lab2;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;

/**
 * 
 * @author Huzaifa, Jake
 * This class is used to apply correction upon detection of a black line
 * using theta, sin, cos functions, storing data from previous line detection
 * and thw size of each square. Correction algorithm explained in run()
 * 
 */
public class OdometryCorrection implements Runnable {
	private static final long CORRECTION_PERIOD = 10;
	private Odometer odometer;

	//Data: constants and variables
	private float color[];
	private static final double SQUARE_SIZE = 30.48;
	private float[] csData;
	private double correctX, correctY;
	double[] oldResult = new double [3];
	double oldSample;
	int passedLine;
	double newColor;



	/**
	 * This is the default class constructor. An existing instance of the odometer is used. This is to
	 * ensure thread safety. Variables and color sensor data are initialized. 
	 * 
	 * @throws OdometerExceptions
	 */
	public OdometryCorrection() throws OdometerExceptions {

		this.odometer = Odometer.getOdometer();

		//Color sensor data and variable initialization
		color = new float[Lab2.myColorSample.sampleSize()];
		this.csData = color;
		correctX = 0;
		correctY = 0;
		oldSample = 0;
		passedLine = 0;


	}

	/**
	 * Here is where the odometer correction code is run. Color sensor data is checked to detect black line
	 * If black line is found, correction is applied using theta value and constant size of the square 
	 * using geometric distance calculations. Number of lines detected is kept track of on the LCD and
	 * Old values are stored for reference for the next time a line is detected.
	 * 
	 * Sin and Cos are used instead of if statements for varying cases of theta as the errors in theta are 
	 * dealt with by rounding to only change for the direction in which the robot is current moving in.
	 * The nature of Sin and Cos being negative or positive at the appropriate values of theta along 
	 * the journey ensures an easy to maintain and understand algorithm in 2 lines.
	 * 
	 * 
	 * @throws OdometerExceptions
	 * @return void
	 */
	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;

		//color sensor and scaling
		Lab2.myColorSample.fetchSample(color, 0);
		newColor = csData[0];

		while (true) {
			correctionStart = System.currentTimeMillis();

			// Trigger correction : store data in newColor
			Lab2.myColorSample.fetchSample(color, 0);
			newColor = csData[0];

			// Store current robot position and current theta
			double[] result = odometer.getXYT();
			double theta = result[2];

			//If line detected (intensity less than 0.3), only count once by keeping track of last value
			if((newColor) < 0.3 && oldSample > 0.3) {

				//Error handling 
				if(result != null) {

					//Beep to notify, update counter and find and set correct X and Y using old reference pts
					passedLine++;
					Sound.beep();
					if((passedLine < 4) || ((passedLine > 6)&&(passedLine < 10))) {
						correctY = oldResult[1] + SQUARE_SIZE * Math.round(Math.cos((Math.PI /180)*theta));	  //convert to radians for cos
						odometer.setY(correctY);
					}else {
						correctX = oldResult[0] + SQUARE_SIZE * Math.round(Math.sin((Math.PI /180)*theta));     //convert to radians for sin
						odometer.setX(correctX);
					}
					
					//Print to LCD
					String printThis = "Lines passed: "+passedLine;
					LCD.drawString(printThis, 0, 3);
				}

				//Set new correct XYT and store info for next loop
				oldResult[0] = correctX;
				oldResult[1] = correctY;
				oldResult[2] = theta;
				oldSample = newColor;
			}
			
			//Store color sample
			oldSample = newColor;


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
