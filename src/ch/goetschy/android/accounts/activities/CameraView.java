package ch.goetschy.android.accounts.activities;

import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

public class CameraView extends JavaCameraView{

	private static final String TAG = "cameraView";
	private static final int CIRCLES_COLOR = Color.BLUE;
	private static final int BIGGEST_COLOR = Color.RED;

	private SurfaceHolder surfaceHolder;
	private int offsetTop, offsetLeft;
	private double scale;

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		surfaceHolder = getHolder();
	}

	public void setFps(int fps) {
		if (mCamera != null) {
			Camera.Parameters params = mCamera.getParameters();
			params.setPreviewFrameRate(fps);
			mCamera.setParameters(params);
		}
	}

	public void light(boolean on) {
		if (mCamera != null) {
			Camera.Parameters params = mCamera.getParameters();
			if (on)
				params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			else
				params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(params);
		}
	}

	public void draw(Mat image, Mat circles, int biggest, int thickness) {
		Log.w(TAG, "begin draw");

		// lock surface
		Canvas canvas = surfaceHolder.lockCanvas();

		int imgW = image.cols(), imgH = image.rows();
		offsetLeft = (getWidth() - imgW) / 2;
		offsetTop = (getHeight() - imgH) / 2;
		Log.w(TAG, "convert to bmp : " + imgW + " x " + imgH);
		// convert the Mat to Bitmap
		Bitmap bmp = Bitmap.createBitmap(imgW, imgH, Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(image, bmp);

		Log.w(TAG, "draw on canvas");
		// draw on canvas
		canvas.drawBitmap(bmp, offsetLeft, offsetTop, null);

		// for each circle
		if (circles.cols() > 0) {
			Log.w(TAG, "circles found " + circles.cols());
			for (int x = 0; x < circles.cols(); x++) {
				double circle[] = circles.get(0, x);

				if (circle == null)
					break;

				Log.w(TAG, "radius : " + circle[2]);

				Paint paint = new Paint();
				paint.setStyle(Paint.Style.STROKE);
				if (x == biggest)
					paint.setColor(BIGGEST_COLOR);
				else
					paint.setColor(CIRCLES_COLOR);

				// radius
				canvas.drawText(String.valueOf(Math.round(circle[2])),
						(float) circle[0] + offsetLeft, (float) circle[1]
								+ offsetTop, paint);

				paint.setStrokeWidth(thickness); // thickness

				canvas.drawCircle((float) circle[0] + offsetLeft,
						(float) circle[1] + offsetTop, (float) circle[2], paint);

			}
		}

		// unlock
		surfaceHolder.unlockCanvasAndPost(canvas);
	}
	
	public void draw(Bitmap image, Mat circles) {
		Log.w(TAG, "begin draw");

		// lock surface
		Canvas canvas = surfaceHolder.lockCanvas();

		Log.w(TAG, "draw bmp on canvas");
		
		// fill with black
		canvas.drawColor(Color.BLACK);
		
		// get dims of screen and img
		int screenW = this.getWidth(), 
			screenH = this.getHeight(),
			imageW = image.getWidth(),
			imageH = image.getHeight();
		
		double wScale = (double)(screenW) / imageW,
				hScale = (double)(screenH) / imageH;
		
		// set scale and offsets to fit the screen
		if(wScale > hScale){ // space on sides
			scale = hScale;
			this.offsetTop = 0;
			this.offsetLeft =  (int) (screenW - scale * imageW) / 2;
		}else{ // space on top and bottom
			scale = wScale;
			this.offsetLeft = 0;
			this.offsetTop =   (int) (screenH - scale * imageH) / 2;
		}
		
		// draw on canvas
		canvas.drawBitmap(Bitmap.createScaledBitmap(image, (int)(scale * imageW), (int)(scale * imageH), false), offsetLeft, offsetTop, null);
		
		
		
		// for each circle
		if (circles.cols() > 0) {
			Log.w(TAG, "circles found " + circles.cols());
			for (int x = 0; x < circles.cols(); x++) {
				double circle[] = circles.get(0, x);

				if (circle == null)
					break;

				Log.w(TAG, "radius : " + circle[2]);

				Paint paint = new Paint();
				paint.setStyle(Paint.Style.STROKE);
				paint.setColor(CIRCLES_COLOR);

				// radius
				canvas.drawText(String.valueOf(Math.round(circle[2])),
						(float) (circle[0] * scale + offsetLeft), (float) (circle[1] * scale
								+ offsetTop), paint);

				paint.setStrokeWidth(3); // thickness

				canvas.drawCircle((float) (circle[0] * scale) + offsetLeft,
						(float) (circle[1] * scale) + offsetTop, (float) (circle[2] * scale), paint);

			}
		}

		// unlock
		surfaceHolder.unlockCanvasAndPost(canvas);
	}

	public void takePicture(PictureCallback callback) {
		Log.i(TAG, "Taking picture");
		// Postview and jpeg are sent in the same buffers if the queue is not
		// empty when performing a capture.
		// Clear up buffers to avoid mCamera.takePicture to be stuck because of
		// a memory issue
		mCamera.setPreviewCallback(null);

		// PictureCallback is implemented by the activity class
		mCamera.takePicture(null, null, callback);
	}

}
