package engine;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

/*
 * detecting coins class
 * takes a bitmap as param and returns the circles
 */

public class DetectCoinsTask extends AsyncTask<Bitmap, Void, Mat> {
	private double cannyUpperThreshold;
	private double accumulator;
	private double dp;
	private Bitmap input;
	
	public DetectCoinsTask(double mCannyUpperThreshold, double mAccumulator, double mDp){
		 cannyUpperThreshold = mCannyUpperThreshold;
		 accumulator = mAccumulator;
		 dp = mDp;
	}
	
	@Override
	protected Mat doInBackground(Bitmap... params) {
		input = params[0];
		
		
		
		// bmp to mat
		Mat colorMat = new Mat();
		Log.w("detectCoins", "bmp to mat");
		Utils.bitmapToMat(input, colorMat);

		// gray
		Mat grayMat = new Mat();
		Log.w("detectCoins", "gray mat");
		Imgproc.cvtColor(colorMat, grayMat, Imgproc.COLOR_BGR2GRAY);

		// mat of circles
		Mat circles = new Mat();

		// higher contrast
		// int cols = grayFrame.cols();
		// int rows = grayFrame.rows();
		// for (int i = 0; i < cols; i++) {
		// for (int j = 0; j < rows; j++) {
		// double[] pix = grayFrame.get(j, i);
		// pix[0] *= 2;
		// grayFrame.put(j, i, pix);
		// }
		// }

		// gaussian blur
		Log.w("detectCoins", "blur");
		Imgproc.GaussianBlur(grayMat, grayMat, new Size(9, 9), 2, 2);

		// detecting vars
		int iMinRadius = 20;
		int iMaxRadius = 150;

		// detect circles
		Log.w("detectCoins", "hough circles");
		Imgproc.HoughCircles(grayMat, circles, Imgproc.CV_HOUGH_GRADIENT, dp,
				grayMat.rows() / 8, cannyUpperThreshold, accumulator,
				iMinRadius, iMaxRadius);

		// tmp
		Utils.matToBitmap(grayMat, input);

		return circles;
	}

}
