package ch.goetschy.android.accounts.activities;

/*
 * Activity for coin recognition
 * 
 * @author		goetschy 
 */

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ch.goetschy.android.accounts.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public class CoinDetectionActivity extends Activity implements
		CvCameraViewListener2 {
	private static final String TAG = "coinDetection";

	private CameraBridgeViewBase mOpenCvCameraView;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_coin_detect);

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.openCvView);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

		mOpenCvCameraView.setCvCameraViewListener(this);
	}

	@Override
	public void onResume() {
		Log.i(TAG, "called onResume");
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this,
				mLoaderCallback);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub

	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// temp
		double iCannyUpperThreshold = 100;
		int iMinRadius = 20;
		int iMaxRadius = 400;
		double iAccumulator = 300;
		int iLineThickness = 4;
		// end temp
		Mat grayFrame = inputFrame.gray();
		// mat of circles
		Mat circles = new Mat();

		// detect circles
		Imgproc.HoughCircles(grayFrame, circles, Imgproc.CV_HOUGH_GRADIENT,
				2.0, grayFrame.rows() / 8, iCannyUpperThreshold, iAccumulator,
				iMinRadius, iMaxRadius);

		if (circles.cols() > 0) {
			for (int x = 0; x < circles.cols(); x++) {
				double vCircle[] = circles.get(0, x);

				if (vCircle == null)
					break;

				Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
				int radius = (int) Math.round(vCircle[2]);

				// draw the found circle
				Core.circle(grayFrame, pt, radius, new Scalar(0, 255, 0),
						iLineThickness);
				Core.circle(grayFrame, pt, 3, new Scalar(0, 0, 255),
						iLineThickness);
			}
		}

		return grayFrame;
	}
}
