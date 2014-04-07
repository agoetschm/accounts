package ch.goetschy.android.accounts.activities;

/*
 * Activity for coin recognition
 * 
 * @author		goetschy 
 */

import java.util.ArrayList;
import java.util.Arrays;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import ch.goetschy.android.accounts.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class CoinDetectionActivity extends Activity implements
		CvCameraViewListener2, OnTouchListener, PictureCallback {
	private static final String TAG = "coinDetection";
	private static final int REQ_WIDTH = 1024, REQ_HEIGHT = 768;
	private static final int FPS = 10;
	private static final double COIN_VALUES[] = { 5, 2, 1, 0.5, 0.2, 0.1, 0.05 };
	private static final String STR_COIN_VALUES[] = { "5.-", "2.-", "1.-",
			"0.50", "0.20", "0.10", "0.05" };
	private static final double COIN_SIZES[] = { 31.45, 27.4, 23.2, 18.2,
			21.05, 19.15, 17.15 }; // [mm]
	private static final int BLOCK_COLOR = Color.argb(100, 200, 200, 200);

	private CameraView mCameraView;
	private Button startButton;
	private Button quitButton;
	private Button showSettingsButton;
	private View settingsLayout;
	private View biggestLayout;
	private Spinner biggestSpinner;
	private CheckBox torchCheckbox;

	private boolean detectionOn;
	private boolean countingOn;
	private boolean settingsOn;

	private double amount;
	private int biggestCoinInd;
	private double biggestCircleSize;
	private Mat lastCircles;

	double maxiCannyUpperThreshold = 70;
	double maxiAccumulator = 400;
	double maxiDp = 6;
	double iCannyUpperThreshold = 50;
	double iAccumulator = 100;
	double iDp = 3.0;

	int circlesLineThickness = 5;

	private Bitmap mPicture;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mCameraView.enableView();
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
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);

		// init vars
		detectionOn = false;
		countingOn = false;
		settingsOn = true; // show settings
		amount = 0;
		biggestCoinInd = 0;

		// keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// set layout
		setContentView(R.layout.activity_coin_detect);

		// set camera
		mCameraView = (CameraView) findViewById(R.id.openCvView);
		mCameraView.setVisibility(SurfaceView.VISIBLE);

		mCameraView.setCvCameraViewListener(this);

		// lower resolution
		// mCameraView.setMaxFrameSize(400, 400);

		// get views
		startButton = (Button) findViewById(R.id.coin_detect_button_start);
		quitButton = (Button) findViewById(R.id.coin_detect_button_quit);
		showSettingsButton = (Button) findViewById(R.id.coin_detect_button_settings);
		settingsLayout = (View) findViewById(R.id.coin_detect_settings);
		biggestLayout = (View) findViewById(R.id.coin_detect_biggest_value);
		biggestSpinner = (Spinner) findViewById(R.id.coin_detect_biggest_value_spinner);
		torchCheckbox = (CheckBox) findViewById(R.id.coin_detect_torch);

		// set biggest value unvisible
		biggestLayout.setVisibility(View.GONE);

		// set layouts backgroud
		settingsLayout.setBackgroundColor(BLOCK_COLOR);
		biggestLayout.setBackgroundColor(BLOCK_COLOR);

		// LISTENERS

		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (detectionOn) { // if already detecting
					if (countingOn) { // if already counted
						// add amount in extras
						// TODO
						finish();
					} else { // start counting coins
						countingOn = true;

						Log.w(TAG, "disable view");
						// no preview any more, the last is saved
						// if (mCameraView != null)
						// mCameraView.disableView();

						mCameraView.takePicture(CoinDetectionActivity.this); // give
																				// itself
																				// as
																				// callback
					}
				} else { // start detection
					detectionOn = true;
					// set the low frame rate
					mCameraView.setFps(FPS);
					// set text
					startButton.setText("Count coins");
				}
			}
		});

		// show or hide settings
		showSettingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (settingsOn) {
					settingsOn = false;
					settingsLayout.setVisibility(View.GONE);
					showSettingsButton
							.setText(R.string.coin_detect_button_settings_off);
				} else {
					settingsOn = true;
					settingsLayout.setVisibility(View.VISIBLE);
					showSettingsButton
							.setText(R.string.coin_detect_button_settings_on);
				}
			}
		});

		quitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// quit activity
				finish();
			}
		});

		// spinner change -> count
		biggestSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// set biggest coin value
						biggestCoinInd = position;

						// compute amount
						computeAmount();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}

				});

		torchCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// turn light on or off
				mCameraView.light(isChecked);

			}
		});

		// TEMP
		SeekBar seekBar1 = (SeekBar) findViewById(R.id.coin_detect_button_seekbar1);
		seekBar1.setProgress((int) (iCannyUpperThreshold
				/ maxiCannyUpperThreshold * 100));
		SeekBar seekBar2 = (SeekBar) findViewById(R.id.coin_detect_button_seekbar2);
		seekBar2.setProgress((int) (iAccumulator / maxiAccumulator * 100));
		SeekBar seekBar3 = (SeekBar) findViewById(R.id.coin_detect_button_seekbar3);
		seekBar3.setProgress((int) (iDp / maxiDp * 100));

		final TextView tv1 = (TextView) findViewById(R.id.coin_detect_button_seekbar1_text);
		tv1.setText("iCannyUpperThreshold = " + iCannyUpperThreshold);
		final TextView tv2 = (TextView) findViewById(R.id.coin_detect_button_seekbar2_text);
		tv2.setText("iAccumulator = " + iAccumulator);
		final TextView tv3 = (TextView) findViewById(R.id.coin_detect_button_seekbar3_text);
		tv3.setText("iDp = " + iDp);

		seekBar1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				iCannyUpperThreshold = ((double) progress) / 100.0
						* maxiCannyUpperThreshold;
				tv1.setText("iCannyUpperThreshold = " + iCannyUpperThreshold);

				if (countingOn)
					detectAndCount();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		seekBar2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				iAccumulator = ((double) progress) / 100.0 * maxiAccumulator;
				tv2.setText("iAccumulator = " + iAccumulator);

				if (countingOn)
					detectAndCount();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		seekBar3.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				iDp = ((double) progress) / 100.0 * maxiDp;
				tv3.setText("iDp = " + iDp);

				if (countingOn)
					detectAndCount();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

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
		if (mCameraView != null)
			mCameraView.disableView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mCameraView != null)
			mCameraView.disableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		Log.i(TAG, "called onCameraViewStarted");

	}

	@Override
	public void onCameraViewStopped() {

	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

		if (detectionOn) {
			// temp
			Mat grayFrame = inputFrame.gray();
			// end temp

			// detect
			Mat circles = detectCoins(grayFrame);

			// draw circles
			grayFrame = drawCircles(grayFrame, circles);

			// save circles
			lastCircles = circles;

			// mat with circles drawn
			return grayFrame;
		} else
			return inputFrame.rgba();

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.i(TAG, "onTouch event");

		return false;
	}

	// count the coins and their sizes
	private void computeAmount() {
		double scale = biggestCircleSize / COIN_SIZES[biggestCoinInd];
		ArrayList<Double> scaledSizes = new ArrayList<Double>();

		// size of coins array
		int numberCoins = COIN_SIZES.length;

		for (int i = 0; i < numberCoins; i++) {
			scaledSizes.add(COIN_SIZES[i] * scale); // scale the sizes
			Log.w(TAG, "scaled : " + scaledSizes.get(i));
		}

		// value of each coin detected
		ArrayList<Double> values = new ArrayList<Double>();
		amount = 0;

		// each circle
		for (int x = 0; x < lastCircles.cols(); x++) {
			double circle[] = lastCircles.get(0, x);

			if (circle == null)
				break;

			double radius = circle[2];

			// determine the value *********
			ArrayList<Double> diffs = new ArrayList<Double>();
			// array of differences
			for (double size : scaledSizes) {
				diffs.add(Math.pow(radius - size, 2));
			}
			// find min diff
			int minDiffInd = 0;
			double minDiff = diffs.get(minDiffInd);
			for (int i = 0; i < numberCoins; i++) {
				if (diffs.get(i) < minDiff) {
					minDiffInd = i;
					minDiff = diffs.get(i);
				}

			}

			// add to list
			values.add(COIN_VALUES[minDiffInd]);
			// and to amount
			amount += COIN_VALUES[minDiffInd];
			// **************************
		}

		// display amount in button
		startButton.setText("Amount : " + amount + " CHF");
	}

	private Mat detectCoins(Mat input) {
		// gray
		Mat grayFrame = input;

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
		Imgproc.GaussianBlur(grayFrame, grayFrame, new Size(9, 9), 2, 2);

		// detecting vars
		int iMinRadius = 20;
		int iMaxRadius = 150;

		// Imgproc.HoughCircles(grayFrame, circles,
		// Imgproc.CV_HOUGH_GRADIENT, iAccumulator, iMinRadius);
		// detect circles
		Imgproc.HoughCircles(grayFrame, circles, Imgproc.CV_HOUGH_GRADIENT, iDp,
				grayFrame.rows() / 8, iCannyUpperThreshold, iAccumulator,
				iMinRadius, iMaxRadius);

		return circles;
	}

	// TODO
	// private Mat detectCoins(Bitmap input) {
	// // mat of circles
	// Mat circles = new Mat();
	//
	// // split into boxes
	//
	// return circles;
	// }

	private Mat detectCoins(Bitmap input) { // private Mat
											// detectSingleCoin(Bitmap input) {
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
		Imgproc.HoughCircles(grayMat, circles, Imgproc.CV_HOUGH_GRADIENT, 4,
				grayMat.rows() / 8, iCannyUpperThreshold, iAccumulator,
				iMinRadius, iMaxRadius);

		// tmp
		Utils.matToBitmap(grayMat, mPicture);

		return circles;
	}

	// get the biggest circle from lastCircles
	private int getBiggestCircle() {
		int biggestIndex = 0;
		double biggestRadius = 0;

		int cols = lastCircles.cols();
		if (cols < 1)
			return -1;

		Log.w(TAG, "biggest circle");
		for (int x = 0; x < cols; x++) {
			double circle[] = lastCircles.get(0, x);

			if (circle == null)
				break;

			double actRadius = circle[2];
			if (actRadius > biggestRadius) {
				biggestIndex = x;
				biggestRadius = actRadius;
			}
		}

		return biggestIndex;
	}

	private Mat drawCircles(Mat image, Mat circles) {
		// thickness

		// for each circle
		if (circles.cols() > 0) {
			Log.w(TAG, "circles found " + circles.cols());
			for (int x = 0; x < circles.cols(); x++) {
				double vCircle[] = circles.get(0, x);

				if (vCircle == null)
					break;

				Log.w(TAG, "circle " + x + " : " + vCircle[0] + " "
						+ vCircle[1] + " " + vCircle[2]);

				Point pt = new Point(Math.round(vCircle[0]),
						Math.round(vCircle[1]));
				int radius = (int) Math.round(vCircle[2]);

				// draw the found circle
				Core.circle(image, pt, radius, new Scalar(0, 0, 0),
						circlesLineThickness);
			}
		}

		return image;
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {

		// load size
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);

		Log.w("pictureTaken", "width : " + options.outWidth);
		Log.w("pictureTaken", "height: " + options.outHeight);
		
		// set scale
		options.inSampleSize = calculateInSampleSize(options, REQ_WIDTH, REQ_HEIGHT);

		// load image
		options.inJustDecodeBounds = false;
		mPicture = BitmapFactory.decodeByteArray(data, 0, data.length, options);
		

		Log.w("pictureTaken", "width scaled : " + options.outWidth);
		Log.w("pictureTaken", "height scaled: " + options.outHeight);

		// detect and count
		detectAndCount();

		// show biggest value spinner
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
				CoinDetectionActivity.this,
				android.R.layout.simple_spinner_item, STR_COIN_VALUES);
		biggestSpinner.setAdapter(spinnerAdapter);
		// biggestSpinner.performClick(); // unroll
		biggestLayout.setVisibility(View.VISIBLE);

		// set text
		startButton.setText("Counting...");
	}

	// do detecting and counting (called when params change or picture taken)
	private void detectAndCount() {
		lastCircles = detectCoins(mPicture);

		// biggest coin
		int biggestCircleInd = getBiggestCircle();
		if (biggestCircleInd == -1)
			biggestCircleSize = -1;
		else
			biggestCircleSize = lastCircles.get(0, biggestCircleInd)[2];

		// draw the last frame and the circles
		mCameraView.draw(mPicture, lastCircles);

	}

	
	// from developer.android.com/
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
}
