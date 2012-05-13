package com.kika.veloskopje.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kika.veloskopje.R;
import com.kika.veloskopje.utils.Constants;
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
		HttpClient client = new DefaultHttpClient();
	    HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout
	                                                                            // Limit
	    HttpResponse response;
	    JSONObject json = new JSONObject();
	    try {
	        HttpPost post = new HttpPost(Constants.WEB_REPORT_URL);
	        json.put("image", Utils.getImageAsByteArray(mPhotoFileUri));
	        json.put("comment", "kaj si be per");
	        json.put("lat", "42.96");
	        json.put("lon", "22.10");
	        json.put("timestamp", System.currentTimeMillis());
	        
	        post.setHeader("json", json.toString());
	        StringEntity se = new StringEntity(json.toString());
	        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	        post.setEntity(se);
	        
	        post.addHeader("Content-Type", "application/json");
	        response = client.execute(post);
	        
	        if (response != null) {
	            InputStream in = response.getEntity().getContent(); 
	            String a = Utils.convertStreamToString(in);
	            Log.i(Constants.TAG, a);
	        }
	    } catch (Exception e) {
            Log.e(Constants.TAG, "Exception", e);
	    }
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
