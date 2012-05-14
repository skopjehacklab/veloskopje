package com.kika.veloskopje.activities;

import static com.kika.veloskopje.utils.Constants.Buttons.REPORT;
import static com.kika.veloskopje.utils.Constants.Buttons.SHOOT;

import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
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
import com.kika.veloskopje.utils.Constants;
import com.kika.veloskopje.utils.Utils;

public class MainActivity extends Activity {

	private CameraView mPreview;
	private String mPhotoFileUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

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

	}

	@Override
	public void onPause() {
		super.onPause();

		mPreview.releaseCamera();
	}
	
	private void initCameraView() {
		mPreview = (CameraView) findViewById(R.id.camera_view);
		mPreview.setOnPhotoShotListener(new OnPhotoShotListener() {
			@Override
			public void onPhotoShot(byte[] imageData) {
				mPhotoFileUri = String.format("%s/%s/%s.jpg", Environment.getExternalStorageDirectory(), "VeloSkopje", UUID.randomUUID());
				Utils.saveFile(imageData, mPhotoFileUri);
				showButtons(REPORT);
				mPreview.releaseCamera();
			}
		});
		
		showButtons(SHOOT);
		mPreview.initCamera();
	}
	
	@Override
	public void onResume() {
		super.onResume();

		initCameraView();
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
		mPreview.releaseCamera();
		mPreview = null;
		Utils.deleteFile(mPhotoFileUri);
		showButtons(SHOOT);
		initCameraView();
	}
	
	private void showButtons(Constants.Buttons buttons) {
		// temporary impl. just to test
		LinearLayout afterShotButtonsLayout = (LinearLayout) findViewById(R.id.after_shot_buttons_layout);
		Button shootButton = (Button) findViewById(R.id.shoot_button);
		
		switch(buttons) {
		case SHOOT:
			afterShotButtonsLayout.setVisibility(View.GONE);
			shootButton.setVisibility(View.VISIBLE);
			break;
		case REPORT:
			afterShotButtonsLayout.setVisibility(View.VISIBLE);
			shootButton.setVisibility(View.GONE);
			break;
		}
	}

}
