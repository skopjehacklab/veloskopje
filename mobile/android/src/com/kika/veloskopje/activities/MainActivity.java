package com.kika.veloskopje.activities;

import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.kika.veloskopje.R;
import com.kika.veloskopje.camera.CameraView;
import com.kika.veloskopje.listeners.OnPhotoShotListener;
import com.kika.veloskopje.utils.Utils;

public class MainActivity extends Activity {

	private CameraView mPreview;
	Camera mCamera;
	int cameraCurrentlyLocked;

	private String mPhotoFileUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

		mPreview = (CameraView) findViewById(R.id.camera_view);
		mPreview.setOnPhotoShotListener(new OnPhotoShotListener() {
			@Override
			public void onPhotoShot(byte[] imageData) {
				mPhotoFileUri = String.format("%s/%s/%s.jpg", Environment.getExternalStorageDirectory(), "VeloSkopje", UUID.randomUUID());
				Utils.saveFile(imageData, mPhotoFileUri);
				switchButtons();
				mPreview.releaseCamera();
			}
		});
		
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		
		//		numberOfCameras = Camera.getNumberOfCameras();
		//
		//		CameraInfo cameraInfo = new CameraInfo();
		//		for(int i = 0; i < numberOfCameras; i++) {
		//			Camera.getCameraInfo(i, cameraInfo);
		//			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
		//				defaultCameraId = i;
		//			}
		//		}

		//		URI uri = null;
		//		try {
		//			uri = new URI("file:///android_asset/img.png");
		//		} catch (URISyntaxException e) {
		//			e.printStackTrace();
		//		}
		//		
		//		File f = null;
		//		if(uri!=null)  {
		//			f = new File(uri);
		//		}

		//		sendMail(f);

		//		File attachmentFile = new File(attachmentInputStream);
	}

	//	public static Bitmap makeBitmap(byte[] imgData) throws IOException {
	//		File tmpDir = new File(String.format("%s/%s", Environment.getExternalStorageDirectory(), "VeloSkopje"));
	//		
	//		File tmpFile = new File(tmpDir + "/shot.png");
	//
	//		if(!tmpDir.exists()) {
	//			tmpDir.mkdirs();
	//		}
	//
	//		FileOutputStream out = new FileOutputStream(tmpFile);
	//		out.write(imgData, 0, imgData.length);
	//		out.flush();
	//		out.close();
	//
	//		imgData = null;
	//
	//		SoftReference<Bitmap> bm = new SoftReference<Bitmap>(getBitmapFromFilePure(tmpFile));
	//		tmpFile.delete();
	//		tmpDir.delete();
	//
	//		return bm.get();
	//	}

	@Override
	public void onPause() {
		super.onPause();

		mPreview.releaseCamera();
	}
	
	@Override
	public void onResume() {
		super.onResume();

		mPreview.initCamera();
	}

	public void onButtonClicked(View v) {
		switch(v.getId()) {
		case R.id.shoot_button:
			mPreview.shoot();
			break;
		case R.id.send_to_mail_button:
			sendMail();
			break;
		case R.id.upload_to_server_button:
			uploadToServer();
			break;
		case R.id.discard_button:
			discard();
			break;
		}
	}

	private void sendMail() {
		Intent emailReportIntent = new Intent(this, EMailReportActivity.class);
		emailReportIntent.putExtra("photoUri", mPhotoFileUri);
		startActivity(emailReportIntent);		
	}

	private void uploadToServer() {
		Intent webReportIntent = new Intent(this, WebReportActivity.class);
		webReportIntent.putExtra("photoUri", mPhotoFileUri);
		startActivity(webReportIntent);
	}

	private void discard() {
		Utils.deleteFile(mPhotoFileUri);
		switchButtons();
		mPreview.initCamera();
	}
	
	private void switchButtons() {
		// temporary impl. just to test
		LinearLayout afterShotButtonsLayout = (LinearLayout) findViewById(R.id.after_shot_buttons_layout);
		Button shootButton = (Button) findViewById(R.id.shoot_button);
		
		int vis = afterShotButtonsLayout.getVisibility();
		if(vis==View.GONE) {
			afterShotButtonsLayout.setVisibility(View.VISIBLE);
			shootButton.setVisibility(View.GONE);
		}
		else if(vis==View.VISIBLE) {
			afterShotButtonsLayout.setVisibility(View.GONE);
			shootButton.setVisibility(View.VISIBLE);
		}
	}

}
