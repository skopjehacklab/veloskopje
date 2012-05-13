package com.kika.veloskopje.camera;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.kika.veloskopje.listeners.OnPhotoShotListener;
import com.kika.veloskopje.utils.Constants;

public class CameraView extends ViewGroup implements SurfaceHolder.Callback {

	private SurfaceView mSurfaceView;
	private SurfaceHolder mHolder;
	private Size mPreviewSize;
	private List<Size> mSupportedPreviewSizes;
	private Camera mCamera;

	private OnPhotoShotListener mOnPhotoShotListener;

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mSurfaceView = new SurfaceView(context);
		addView(mSurfaceView);

		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void setCamera(Camera camera) {
		mCamera = camera;
		if (mCamera != null) {
			mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
			requestLayout();
		}
	}

	public void switchCamera(Camera camera) {
		setCamera(camera);
		try {
			camera.setPreviewDisplay(mHolder);
		} catch (IOException exception) {
			Log.e(Constants.TAG, "IOException caused by setPreviewDisplay()", exception);
		}
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		
		requestLayout();

		camera.setParameters(parameters);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// We purposely disregard child measurements because act as a
		// wrapper to a SurfaceView that centers the camera preview instead
		// of stretching it.
		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		setMeasuredDimension(width, height);

		if (mSupportedPreviewSizes != null) {
			mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed && getChildCount() > 0) {
			final View child = getChildAt(0);

			final int width = r - l;
			final int height = b - t;

			int previewWidth = width;
			int previewHeight = height;
			if (mPreviewSize != null) {
				previewWidth = mPreviewSize.width;
				previewHeight = mPreviewSize.height;
			}

			// Center the child SurfaceView within the parent.
			if (width * previewHeight > height * previewWidth) {
				final int scaledChildWidth = previewWidth * height / previewHeight;
				child.layout((width - scaledChildWidth) / 2, 0,
						(width + scaledChildWidth) / 2, height);
			} else {
				final int scaledChildHeight = previewHeight * width / previewWidth;
				child.layout(0, (height - scaledChildHeight) / 2,
						width, (height + scaledChildHeight) / 2);
			}
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			if (mCamera != null) {
				mCamera.setPreviewDisplay(holder);
			}
		} catch (IOException exception) {
			Log.e(Constants.TAG, "IOException caused by setPreviewDisplay()", exception);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null) {
			mCamera.stopPreview();
		}
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null) return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		List<Size> sizes = parameters.getSupportedPictureSizes();
		parameters.setPictureSize(sizes.get(2).width, sizes.get(2).height);
		requestLayout();

		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

	public void initCamera() {
		mCamera = Camera.open();
		setCamera(mCamera);
	}

	public void releaseCamera() {
		if (mCamera != null) {
			mCamera.release();
			setCamera(null);
		}
	}

	public void shoot() {
		mCamera.takePicture(null, null, mPictureCallback);
	}

	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] imageData, Camera c) {

			if(imageData != null) {
				try {
					onPhotoShot(imageData);
				}
				catch (Exception e) {
					Log.e(Constants.TAG, "Exception", e);
				}
			}
		}
	};
	
	public void setOnPhotoShotListener(OnPhotoShotListener onPhotoShotListener) {
		mOnPhotoShotListener = onPhotoShotListener;
	}
	
	public void onPhotoShot(byte[] data) {
		if(mOnPhotoShotListener!=null) {
			mOnPhotoShotListener.onPhotoShot(data);
		}
	}
}
