package com.kika.veloskopje.activities;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.kika.veloskopje.R;
import com.kika.veloskopje.mail.GMailSender;
import com.kika.veloskopje.utils.Constants;
import com.kika.veloskopje.utils.Utils;

public class EMailReportActivity extends Activity {

	private ImageView mPhotoImageView;
	private Button mSendButton;
	private Button mDiscardButton;
	
	private EditText mInfoEditText;

	private String mPhotoFileUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.email_report_layout);

		mPhotoImageView = (ImageView) findViewById(R.id.photo_imageview);
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null && getIntent().hasExtra("photoUri")) {
			mPhotoFileUri = bundle.getString("photoUri");
			mPhotoImageView.setImageBitmap(Utils.getBitmapFromFile(mPhotoFileUri));
		}
		
		mSendButton = (Button) findViewById(R.id.send_button);
		mDiscardButton = (Button) findViewById(R.id.discard_button);
		
		mInfoEditText = (EditText) findViewById(R.id.info_edittext);

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
				String geoLoc = "http://maps.openstreetmaps.org";
				if(Utils.isOnline(EMailReportActivity.this)) {
					send(String.format("Граѓанин ја испрати сликата во атачмент со следнава порака:\n\n%s\n\nГеолокација: %s", mInfoEditText.getText().toString(), geoLoc));
				}
				else {
					Toast.makeText(EMailReportActivity.this, "Нема активна конекција", Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.discard_button:
				finish();
				break;
			}
		}
	};

	private void send(final String message) {
		
		final ProgressDialog pD = ProgressDialog.show(this, "Пачекајте", "Податоците се испраќаат...");
		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... arg0) {
				try {  
					File attachment = new File(mPhotoFileUri);
					GMailSender sender = new GMailSender(Constants.MAIL_USERNAME, Constants.MAIL_PASSWORD);
					sender.sendImage("S.O.S.", message, "mailer@deamon.org", Constants.MAIL_RECEPIENT, attachment);
					return true;
				} catch (Exception e) { 
					Log.e(Constants.TAG, e.getMessage(), e);
					return false;
				}
			}
			
			@Override
			public void onPostExecute(Boolean result) {
				String statusMsg = "Се случи грешка при испраќањето. Обидете се повторно."; 
				if(result) {
					statusMsg = "Вашата слика е успешно испратена.";
					finish();
				}
				
				Toast.makeText(EMailReportActivity.this, statusMsg, Toast.LENGTH_LONG).show();
				pD.dismiss();
			}
		}.execute();
	}
	
}