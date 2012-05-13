package com.kika.veloskopje.activities;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kika.veloskopje.R;
import com.kika.veloskopje.utils.Utils;

public class WebReportActivity extends Activity {

	private ImageView mPhotoImageView;
	private Button mSendButton;
	private Button mDiscardButton;

	private TextView mLocationTextview;
	private TextView mTimeTextview;
	
	private String mPhotoFileUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.web_report_layout);

		mPhotoImageView = (ImageView) findViewById(R.id.photo_imageview);

		Bundle bundle = getIntent().getExtras();
		if(bundle != null && getIntent().hasExtra("photoUri")) {
			mPhotoFileUri = bundle.getString("photoUri");
			mPhotoImageView.setImageBitmap(Utils.getBitmapFromFile(mPhotoFileUri));
		}
		
		mSendButton = (Button) findViewById(R.id.send_button);
		mDiscardButton = (Button) findViewById(R.id.discard_button);
		
		mLocationTextview = (TextView) findViewById(R.id.location_textview);
		mTimeTextview = (TextView) findViewById(R.id.time_textview);
		
		mSendButton.setOnClickListener(mButtonClickListener);
		mDiscardButton.setOnClickListener(mButtonClickListener);
		
		mTimeTextview.setText(getTime());
		
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private void publish() {
		Toast.makeText(this, "E ne e implementirano uste", Toast.LENGTH_LONG).show();
	}
	
	private OnClickListener mButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.send_button:
				publish();
				break;
			case R.id.discard_button:
				finish();
				break;
			}
		}
	};
	
	private String getTime() {
		return DateFormat.getDateTimeInstance().format(new Date());
	}


}
