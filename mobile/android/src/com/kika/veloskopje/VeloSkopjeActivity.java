package com.kika.veloskopje;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.kika.veloskopje.camera.CameraView;

public class VeloSkopjeActivity extends Activity {

	private CameraView mPreview;
	Camera mCamera;
//	int numberOfCameras;
	int cameraCurrentlyLocked;

//	int defaultCameraId;

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
				Bitmap bm = null;
				try {
					bm = makeBitmap(imageData);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
//				String imgPath = String.format("%s/%s/%s.jpg", Environment.getExternalStorageDirectory(), "VeloSkopje", UUID.randomUUID());
//				FileOutputStream fos=new FileOutputStream(imgPath);
//				fos.write(imageData);
//				fos.close();
			}
		});

//		numberOfCameras = Camera.getNumberOfCameras();
//
//		CameraInfo cameraInfo = new CameraInfo();
//		for (int i = 0; i < numberOfCameras; i++) {
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
	
	public static Bitmap makeBitmap(byte[] imgData) throws IOException {
		File tmpDir = new File(String.format("%s/%s", Environment.getExternalStorageDirectory(), "VeloSkopje"));
		
		File tmpFile = new File(tmpDir + "/shot.png");

		if(!tmpDir.exists()) {
			tmpDir.mkdirs();
		}

		FileOutputStream out = new FileOutputStream(tmpFile);
		out.write(imgData, 0, imgData.length);
		out.flush();
		out.close();

		imgData = null;

		SoftReference<Bitmap> bm = new SoftReference<Bitmap>(getBitmapFromFilePure(tmpFile));
		tmpFile.delete();
		tmpDir.delete();

		return bm.get();
	}
	
	private static Bitmap getBitmapFromFilePure(File imgFile) {
		Bitmap bm = null;
		
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inDither = false;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		opt.inTempStorage = new byte[32 * 1024]; 
		
		FileInputStream fs=null;
		try {
			fs = new FileInputStream(imgFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			if(fs!=null) bm = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, opt);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fs!=null) {
				try {
					fs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return bm;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mPreview.initCamera();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		mPreview.releaseCamera();
	}
	
	public void onShoot(View v) {
		mPreview.shoot();
	}
	

}
