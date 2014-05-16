package ch.goetschy.android.accounts.activities;

/*
 * Activity for coin recognition
 * 
 * @author		goetschy 
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

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

import com.haarman.supertooltips.ToolTip;
import com.haarman.supertooltips.ToolTipRelativeLayout;
import com.haarman.supertooltips.ToolTipView;

import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.engine.DetectCoinsTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class CoinDetectionActivity extends Activity implements
		CvCameraViewListener2, OnTouchListener, PictureCallback,
		ToolTipView.OnToolTipViewClickedListener {
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
	private ImageButton helpButton;
	private View settingsLayout;
	private View biggestLayout;
	private Spinner biggestSpinner;
	private CheckBox torchCheckbox;

	private boolean detectionOn;
	private boolean settingsOn;
	private boolean pictureTaken;

	private double amount;
	private int biggestCoinInd;
	private double biggestCircleSize;
	private Mat lastCircles;

	double maxiCannyUpperThreshold = 70;
	double maxiAccumulator = 400;
	double maxiDp = 6;
	double maxiMinRadius = 100;
	double maxiMaxRadius = 600;
	double iCannyUpperThreshold = 50;
	double iAccumulator = 100;
	double iDp = 1.3;
	double iMinRadius = 20;
	double iMaxRadius = 250;

	private Bitmap mPicture;
	private Bitmap copy;

	private ToolTipView mTooltipSettings = null;
	private ToolTipView mTooltipSettingsBut = null;
	private ToolTipView mTooltipDetectBut = null;
	private ToolTipRelativeLayout mTooltipLayout;

	// for loading spinner
	private ProgressDialog progressDialog;
	private boolean detectingTaskWorking;

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
		settingsOn = true; // show settings
		amount = 0;
		biggestCoinInd = 0;
		pictureTaken = false;
		detectingTaskWorking = false;

		// keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// set layout
		setContentView(R.layout.activity_coin_detect);

		// set tooltip view
		mTooltipLayout = (ToolTipRelativeLayout) findViewById(R.id.activity_coin_detect_tooltiplayout);

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
		helpButton = (ImageButton) findViewById(R.id.coin_detect_button_help);
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
					finish(true); // return the amount to parent activity
				} else { // start detection
					detectionOn = true;
					if (BuildConfig.DEBUG)
						Log.w(TAG, "disable view");
					// no preview any more, the last is saved
					// if (mCameraView != null)
					// mCameraView.disableView();

					// set text
					startButton.setText("Count coins");

					mCameraView.takePicture(CoinDetectionActivity.this); // give
																			// itself
																			// as
																			// callback

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
				finish(false);
			}
		});

		helpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// show tooltip
				if (mTooltipSettings == null && mTooltipSettingsBut == null
						&& mTooltipDetectBut == null)
					nextTooltip(null);
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
		SeekBar seekBar4 = (SeekBar) findViewById(R.id.coin_detect_button_seekbar4);
		seekBar4.setProgress((int) (iMinRadius / maxiMinRadius * 100));
		SeekBar seekBar5 = (SeekBar) findViewById(R.id.coin_detect_button_seekbar5);
		seekBar5.setProgress((int) (iMaxRadius / maxiMaxRadius * 100));

		final TextView tv1 = (TextView) findViewById(R.id.coin_detect_button_seekbar1_text);
		tv1.setText("canny upper threshold = " + iCannyUpperThreshold);
		final TextView tv2 = (TextView) findViewById(R.id.coin_detect_button_seekbar2_text);
		tv2.setText("accumulator = " + iAccumulator);
		final TextView tv3 = (TextView) findViewById(R.id.coin_detect_button_seekbar3_text);
		tv3.setText("dp = " + iDp);
		final TextView tv4 = (TextView) findViewById(R.id.coin_detect_button_seekbar4_text);
		tv4.setText("min radius = " + iMinRadius);
		final TextView tv5 = (TextView) findViewById(R.id.coin_detect_button_seekbar5_text);
		tv5.setText("max radius = " + iMaxRadius);

		seekBar1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				iCannyUpperThreshold = ((double) progress) / 100.0
						* maxiCannyUpperThreshold;
				tv1.setText("canny upper threshold = " + iCannyUpperThreshold);

				if (detectionOn)
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
				tv2.setText("accumulator = " + iAccumulator);

				if (detectionOn)
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
				tv3.setText("dp = " + iDp);

				if (detectionOn)
					detectAndCount();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		seekBar4.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				iMinRadius = ((double) progress) / 100.0 * maxiMinRadius;
				tv4.setText("min radius = " + iMinRadius);

				if (detectionOn)
					detectAndCount();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		seekBar5.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				iMaxRadius = ((double) progress) / 100.0 * maxiMaxRadius;
				tv5.setText("max radius = " + iMaxRadius);

				if (detectionOn)
					detectAndCount();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

	}
	

	/**
	 * finish with saving or not
	 */
	public void finish(boolean save) {
		if (save) {
			Intent resultIntent = new Intent();
			resultIntent.putExtra(Double.class.toString(), amount);
			setResult(RESULT_OK, resultIntent);
			finish();
		} else {
			setResult(RESULT_CANCELED);
			finish();
		}
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

		return inputFrame.rgba();

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.i(TAG, "onTouch event");

		return false;
	}

	// count the coins and their sizes
	private void computeAmount() {
		if (pictureTaken == false)
			return;
		if (BuildConfig.DEBUG)
			Log.w(TAG, "start compute amount");

		double scale = biggestCircleSize / COIN_SIZES[biggestCoinInd];
		ArrayList<Double> scaledSizes = new ArrayList<Double>();

		// size of coins array
		int numberCoins = COIN_SIZES.length;

		for (int i = 0; i < numberCoins; i++) {
			scaledSizes.add(COIN_SIZES[i] * scale); // scale the sizes
			if (BuildConfig.DEBUG)
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

			if (BuildConfig.DEBUG)
				Log.w(TAG, "coin : " + COIN_VALUES[minDiffInd]);
			
			// add to list
			values.add(COIN_VALUES[minDiffInd]);
			// and to amount
			amount += COIN_VALUES[minDiffInd];
			// **************************
		}
		
		if (BuildConfig.DEBUG)
			Log.w(TAG, "amount : " + amount);
		Log.w(TAG, "amount * 100 rounded : " + Math.round(amount * 100));
		// display amount in button
		startButton.setText("Amount : " + ((double)Math.round(amount * 100) / 100.0)
				+ " CHF");
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

	// get the biggest circle from lastCircles
	private int getBiggestCircle() {
		int biggestIndex = 0;
		double biggestRadius = 0;

		int cols = lastCircles.cols();
		if (cols < 1)
			return -1;

		if (BuildConfig.DEBUG)
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

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		// spinner
		showLoadingDialog();

		// load size
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);

		if (BuildConfig.DEBUG) {
			Log.w("pictureTaken", "width : " + options.outWidth);
			Log.w("pictureTaken", "height: " + options.outHeight);
		}

		// set scale
		options.inSampleSize = calculateInSampleSize(options, REQ_WIDTH,
				REQ_HEIGHT);

		// load image
		options.inJustDecodeBounds = false;
		mPicture = BitmapFactory.decodeByteArray(data, 0, data.length, options);

		if (BuildConfig.DEBUG) {
			Log.w("pictureTaken", "width scaled : " + options.outWidth);
			Log.w("pictureTaken", "height scaled: " + options.outHeight);
		}

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
		if (this.detectingTaskWorking) // prevent two detecting tasks
			return;
		detectingTaskWorking = true;

		// progress spinner if not already
		if (progressDialog.isShowing() == false)
			this.showLoadingDialog();

		copy = mPicture.copy(Bitmap.Config.ARGB_8888, true);
		// detection
		DetectCoinsTask task = new DetectCoinsTask(this, copy,
				iCannyUpperThreshold, iAccumulator, iDp, (int) iMinRadius,
				(int) iMaxRadius);
		task.execute();
	}

	public void continueDetectAndCount(Mat resultCircles) { // CALL ONLY FROM
															// DetectCoinsTask
															// !!!

		lastCircles = resultCircles;

		// biggest coin
		int biggestCircleInd = getBiggestCircle();
		if (biggestCircleInd == -1)
			biggestCircleSize = -1;
		else
			biggestCircleSize = lastCircles.get(0, biggestCircleInd)[2];

		// draw the last frame and the circles
		mCameraView.draw(copy/* mPicture */, lastCircles);

		// allows to compute amount
		pictureTaken = true;
		
		// (re)compute the amount
		computeAmount();

		// stop spinner
		this.progressDialog.dismiss();

		// "unlock"
		detectingTaskWorking = false;
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

	// loading dialog
	private void showLoadingDialog() {
		progressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Loading...");
		progressDialog.show();
	}

	// TOOLTIPS -------------------

	// display the first or the next tooltip
	private void nextTooltip(ToolTipView toolTipView) {
		if (toolTipView == null) {
			addSettingsTooltip();
		} else if (toolTipView == mTooltipSettings) {
			mTooltipSettings = null;
			addSettingsButTooltip();
		} else if (toolTipView == mTooltipSettingsBut) {
			mTooltipSettingsBut = null;
			addDetectButTooltip();
		} else if (toolTipView == mTooltipDetectBut) {
			mTooltipDetectBut = null;
		}
	}

	private void addSettingsTooltip() {
		mTooltipSettings = mTooltipLayout
				.showToolTipForView(
						new ToolTip()
								.withText(
										"Here are different parameters for the circle detection\nfunction. Will be better documented in a\nfurther version.")
								.withColor(
										getResources().getColor(
												R.color.holo_orange))
								.withShadow(true),
						findViewById(R.id.coin_detect_button_seekbar1));
		mTooltipSettings.setOnToolTipViewClickedListener(this);
	}

	private void addSettingsButTooltip() {
		mTooltipSettingsBut = mTooltipLayout
				.showToolTipForView(
						new ToolTip()
								.withText("Hide/display the settings field.")
								.withColor(
										getResources().getColor(
												R.color.holo_orange))
								.withShadow(true), showSettingsButton);
		mTooltipSettingsBut.setOnToolTipViewClickedListener(this);
	}

	private void addDetectButTooltip() {
		mTooltipDetectBut = mTooltipLayout
				.showToolTipForView(
						new ToolTip()
								.withText(
										"Click here to take a picture and detect some\ncoins. The value will then be displayed\nin this button.")
								.withColor(
										getResources().getColor(
												R.color.holo_orange))
								.withShadow(true), startButton);
		mTooltipDetectBut.setOnToolTipViewClickedListener(this);
	}

	@Override
	public void onToolTipViewClicked(ToolTipView toolTipView) {
		nextTooltip(toolTipView);
	}
}
