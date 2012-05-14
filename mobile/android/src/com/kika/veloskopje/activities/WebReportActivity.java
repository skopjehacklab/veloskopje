package com.kika.veloskopje.activities;


import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kika.veloskopje.R;
import com.kika.veloskopje.utils.Constants;
import com.kika.veloskopje.utils.Utils;

public class WebReportActivity extends Activity {

	private ImageView mPhotoImageView;
	private Button mSendButton;
	private Button mDiscardButton;

	private TextView mLocationTextview;
	private TextView mTimeTextview;

	private EditText mInfoEditText;

	private String mPhotoFileUri;

	LocationManager mLocationManager;
	protected double mLat;
	protected double mLon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

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
		mInfoEditText = (EditText) findViewById(R.id.info_edittext); 

		mSendButton.setOnClickListener(mButtonClickListener);
		mDiscardButton.setOnClickListener(mButtonClickListener);

		mTimeTextview.setText(getTime());

		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		Criteria coarse = new Criteria();
		coarse.setAccuracy(Criteria.ACCURACY_COARSE);

		String bestProvider = mLocationManager.getBestProvider(criteria, true);
		mLocationManager.requestLocationUpdates(bestProvider, 0, 0, mLocationListener);
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
		
		final ProgressDialog pD = ProgressDialog.show(this, "Пачекајте", "Податоците се испраќаат...");
		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... arg0) {
				HttpClient client = new DefaultHttpClient();
				HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
				HttpResponse response;
				JSONObject json = new JSONObject();
				try {
					HttpPost post = new HttpPost(Constants.WEB_REPORT_URL);
					String imgBase64 = Base64.encodeToString(Utils.getImageAsByteArray(mPhotoFileUri), Base64.NO_WRAP);
					
					String comment = mInfoEditText.getText().toString();
					if(comment==null) comment = "n/a";
					json.put("image", imgBase64);
					json.put("comment", comment);
					json.put("latitude", String.valueOf(mLat));
					json.put("longitude", String.valueOf(mLon));
					json.put("timestamp", System.currentTimeMillis());

//					post.setHeader("json", json.toString());
					
					StringEntity se = new StringEntity(json.toString());
					se.setContentEncoding("application/json;charset=UTF-8");
					post.setEntity(se);

					post.addHeader("Content-Type", "application/json");
					post.addHeader("Content-Lenght", String.valueOf(json.toString().length()));
					
//					Header[] headers = post.getAllHeaders();
//					System.out.println(headers.toString());

					response = client.execute(post);

					if (response != null) {
						InputStream in = response.getEntity().getContent(); 
						String a = Utils.convertStreamToString(in);
						Log.i(Constants.TAG, a);
						return true;
					}
				} catch (Exception e) {
					Log.e(Constants.TAG, "Exception", e);
					return false;
				}
				
				return false;
			}
			
			@Override
			public void onPostExecute(Boolean result) {
				String statusMsg = "Се случи грешка при испраќањето. Обидете се повторно."; 
				if(result) {
					statusMsg = "Вашата слика е успешно испратена.";
					finish();
				}
				
				Toast.makeText(WebReportActivity.this, statusMsg, Toast.LENGTH_LONG).show();
				pD.dismiss();
			}
		}.execute();
	}

	private OnClickListener mButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.send_button:
				if(Utils.isOnline(WebReportActivity.this)) {
					publish();
				}
				else {
					Toast.makeText(WebReportActivity.this, "Нема активна конекција", Toast.LENGTH_LONG).show();
				}
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

	LocationListener mLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			mLat = location.getLatitude();
			mLon = location.getLongitude();

			String openStreetMapUrl = String.format("http://www.openstreetmap.org/?lat=%f&lon=%f&zoom=15&layers=B000FTF", mLat, mLon);
			mLocationTextview.setText(openStreetMapUrl);
			Linkify.addLinks(mLocationTextview, Linkify.ALL);
			mLocationTextview.setMovementMethod(LinkMovementMethod.getInstance());
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {}

		public void onProviderEnabled(String provider) {}

		public void onProviderDisabled(String provider) {}
	};
}
