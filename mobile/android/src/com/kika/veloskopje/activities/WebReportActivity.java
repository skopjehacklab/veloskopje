package com.kika.veloskopje.activities;


import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONStringer;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kika.veloskopje.R;
import com.kika.veloskopje.helpers.HttpUtils;
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

	public void publish() {
		final ProgressDialog pD = ProgressDialog.show(this, "Пачекајте", "Податоците се испраќаат...");

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				HttpPost request = new HttpPost(Constants.WEB_REPORT_URL);
				String imgBase64 = Base64.encodeToString(Utils.getImageAsByteArray(mPhotoFileUri), Base64.DEFAULT);

				String comment = "" + mInfoEditText.getText().toString();
				JSONStringer json = null;
				try {
					json = new JSONStringer()
					.object()
					.key("comment").value(comment)
					.key("image").value(imgBase64)
					.key("lat").value("42.60")
					.key("lon").value("22.3")
					.key("timestamp").value(System.currentTimeMillis())
					.endObject();
				} catch (JSONException e2) {
					e2.printStackTrace();
				}

				StringEntity entity = null;
				try {
					entity = new StringEntity(json.toString());
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				entity.setContentType("application/json;charset=UTF-8");
				entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
				entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_LEN, String.valueOf(json.toString().length())));

				request.setHeader("Content-Range", "10" + Integer.valueOf(json.toString().length()-1));
				request.setHeader("Accept-Ranges", "bytes");

				request.setEntity(entity);

				HttpResponse response = null;
				try {
					response = HttpUtils.getNewHttpClient().execute(request);
				} catch (Exception e) {
					e.printStackTrace();
				} 

				return null;
			}

			@Override
			public void onPostExecute(Void result) {
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
