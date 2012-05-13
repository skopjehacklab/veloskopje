package com.kika.veloskopje.mail;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.kika.veloskopje.utils.Constants;

/**
 * 
 * @author Bojan Drakuwa
 *
 */

public class ImageUploadTask extends AsyncTask<String, Void, String> {
	private ProgressDialog dialog;
	private Context mContext;

	public ImageUploadTask(Context c) {
		mContext = c;
	}

	/**
	 * On PreExecute, initialize and show the progress dialog.
	 */
	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(mContext);
		dialog.setTitle("Ги испраќаме податоците..");
		dialog.setMessage("Ве молиме почекајте..");
		dialog.setIndeterminate(true);
		dialog.show();
	}

	/**
	 * what to do in background while showing the progress dialog.
	 */
	protected String doInBackground(String... vlezni) {

		String result = "";
		try {
			final File file = new File("");
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4; // this will cut the sampling by 50%
			options.inPurgeable = true;
			options.inDither = false;
			Bitmap bm = BitmapFactory.decodeFile(file.toString(), options);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bm.compress(CompressFormat.JPEG, 60, bos);
			byte[] data = bos.toByteArray();
			bos.flush();
			bos.close();
			bm.recycle();

			//			String image_str = Base64.encodeBytes(data);

			data = null;

			JSONObject json = new JSONObject();
			json.put("image", "");

			String url = "http://drakuwa.no-ip.org/finki/image_upload.php";
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost request = new HttpPost(url);
			HttpEntity entity;
			StringEntity s = new StringEntity(json.toString());
			s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			entity = s;
			request.setEntity(entity);
			HttpResponse response;
			response = httpclient.execute(request);

			if (response != null) {
				InputStream in = response.getEntity().getContent();
				result = convertStreamToString(in);
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, "Exception", e);
		}
		return result;
	}

	/**
	 * What to do after the calculations are finished.
	 */
	public void onPostExecute(String result) {
		// Remove the progress dialog.
		try {
			dialog.dismiss();
			dialog = null;
		} catch (Exception e) {
			// nothing
		}

		if(result.equalsIgnoreCase("Success!")){
			Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
		}
		else Toast.makeText(mContext, "Not OK: "+result, Toast.LENGTH_SHORT).show();
		Log.d("xxx", "Rezultat: " + result);
	}

	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}