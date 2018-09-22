/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometer;

import ca.mcgill.ecse211.lab2.Lab2;

public class OdometryCorrection implements Runnable {
	private static final long CORRECTION_PERIOD = 10;
	private Odometer odometer;

	//data
	private float color[];
	private float lastColor;
	private final double SQUARE_SIZE = 30.48;
	private static float csData;
	private double countY;
	private double countX;
	private double correctX, correctY;

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
		this.csData = color[0];
		countY = 0;
		countX = 0;

		correctX = 0;
		correctY =0;


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
		lastColor = 2;

		while (true) {
			correctionStart = System.currentTimeMillis();

			// TODO Trigger correction (When do I have information to correct?)
			Lab2.myColorSample.fetchSample(color, 0);
			double newColor = csData * 1000;

			// TODO Calculate new (accurate) robot position
			double[] result = odometer.getXYT();
			double x = result[0];
			double y = result[1];
			double theta = result[2];
			double correctX = 0, correctY =0;
			double[] oldResult = new double [3];
			/*
      if((theta > 350 && theta < 360)|| (theta < 10 && theta > 0)) {
    	  countY++;
    	  odometer.setXYT(0.0, countY + SQUARE_SIZE, 0.0);
      }
      else if((theta > 80 && theta < 100)) {
    	  countX++;
    	  odometer.setX(countX + SQUARE_SIZE);
      }
      else if((theta > 170 && theta < 190)) {
    	  countY--;
    	  odometer.setY(countY + SQUARE_SIZE);
      }
      else if((theta > 260 && theta < 280)) {
    	  countX--;
    	  odometer.setY(countX + SQUARE_SIZE);
      }*/
			if(lastColor/1000 - newColor/1000 > 4) {
				correctX = x + SQUARE_SIZE*Math.cos(theta);
				correctY = y + SQUARE_SIZE*Math.sin(theta);
				odometer.setXYT(correctX, correctY, theta);
				oldResult[0] = correctX;
				oldResult[1] = correctY;
				oldResult[2] = theta;
			}
			// TODO Update odometer with new calculated (and more accurate) vales

			//odometer.setXYT(0.3, 19.23, 5.0);

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
