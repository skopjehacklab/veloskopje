package com.kika.veloskopje.activities;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.kika.veloskopje.R;
import com.kika.veloskopje.mail.GMailSender;
import com.kika.veloskopje.utils.Constants;
import com.kika.veloskopje.utils.Utils;

public class EMailReportActivity extends Activity {

	private ImageView mPhotoImageView;
	private Button mSendButton;
	private Button mDiscardButton;

	private String mPhotoFileUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.email_report_layout);

		mPhotoImageView = (ImageView) findViewById(R.id.photo_imageview);
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null && getIntent().hasExtra("photoUri")) {
			mPhotoFileUri = bundle.getString("photoUri");
			mPhotoImageView.setImageBitmap(Utils.getBitmapFromFile(mPhotoFileUri));
		}
		
		mSendButton = (Button) findViewById(R.id.send_button);
		mDiscardButton = (Button) findViewById(R.id.discard_button);

		mSendButton.setOnClickListener(mButtonClickListener);
		mDiscardButton.setOnClickListener(mButtonClickListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private OnClickListener mButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.send_button:
				send("Lokacija ova ona");
				break;
			case R.id.discard_button:
				finish();
				break;
			}
		}
	};

	private void send(String message) {
		try {  
			File attachment = new File(mPhotoFileUri);
			GMailSender sender = new GMailSender(Constants.MAIL_USERNAME, Constants.MAIL_PASSWORD);
			sender.sendImage("S.O.S.", message, "mailer@deamon.org", Constants.MAIL_RECEPIENT, attachment);
		} catch (Exception e) {   
			Log.e(Constants.TAG, e.getMessage(), e);   
		}
	}
	
}