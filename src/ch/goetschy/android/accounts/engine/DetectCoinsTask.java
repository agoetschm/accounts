package ch.goetschy.android.accounts.engine;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.activities.CoinDetectionActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

/**
 * detecting coins class <br/>
 * takes a bitmap as param and returns the circles
 */

public class DetectCoinsTask extends AsyncTask<Void, Void, Mat> {
	private double cannyUpperThreshold;
	private double accumulator;
	private double dp;
	private int minRadius;
	private int maxRadius;
	private Bitmap input;
	private Mat circles;

	private CoinDetectionActivity parent;

	public DetectCoinsTask(CoinDetectionActivity mParent, Bitmap pInput,
			double mCannyUpperThreshold, double mAccumulator, double mDp,
			int mMinRadius, int mMaxRadius) {
		parent = mParent;
		input = pInput;
		cannyUpperThreshold = mCannyUpperThreshold;
		accumulator = mAccumulator;
		dp = mDp;
		minRadius = mMinRadius;
		maxRadius = mMaxRadius;
	}

	@Override
	protected Mat doInBackground(Void... params) {

		// bmp to mat
		Mat colorMat = new Mat();
		if (BuildConfig.DEBUG)
			Log.w("detectCoins", "bmp to mat");
		Utils.bitmapToMat(input, colorMat);

		// gray
		Mat grayMat = new Mat();
		if (BuildConfig.DEBUG)
			Log.w("detectCoins", "gray mat");
		Imgproc.cvtColor(colorMat, grayMat, Imgproc.COLOR_BGR2GRAY);

		// mat of circles
		circles = new Mat();

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
		if (BuildConfig.DEBUG)
			Log.w("detectCoins", "blur");
		Imgproc.GaussianBlur(grayMat, grayMat, new Size(9, 9), 3, 3);

		// detecting vars

		// detect circles
		if (BuildConfig.DEBUG)
			Log.w("detectCoins", "hough circles");
		Imgproc.HoughCircles(grayMat, circles, Imgproc.CV_HOUGH_GRADIENT, dp,
				minRadius * 2, cannyUpperThreshold, accumulator, minRadius,
				maxRadius);

		// tmp
		Utils.matToBitmap(grayMat, input);

		return circles;
	}

	@Override
	protected void onPostExecute(Mat result) {
		parent.continueDetectAndCount(result);
	}

}
