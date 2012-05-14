package com.kika.veloskopje.activities;


import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONStringer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.kika.veloskopje.helpers.EasySSLSocketFactory;
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
				request.setEntity(entity); 

				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
				schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
				 
				HttpParams httpParams = new BasicHttpParams();
				httpParams.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
				httpParams.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
				httpParams.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
				HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
				 
				ClientConnectionManager cm = new SingleClientConnManager(httpParams, schemeRegistry);
				HttpClient httpClient = new DefaultHttpClient(cm, httpParams);
				
				HttpResponse response = null;
				try {
					response = httpClient.execute(request);
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
}
