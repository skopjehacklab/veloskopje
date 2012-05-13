package com.kika.veloskopje.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Utils {
	
	public static Bitmap getBitmapFromFile(String imgFilePath) {
		return getBitmapFromFile(new File(imgFilePath));
	}
	
	public static Bitmap getBitmapFromFile(File imgFile) {
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
	
	public static byte[] getImageAsByteArray(String path) {
		Bitmap bm = BitmapFactory.decodeFile(path);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);   
		byte[] b = baos.toByteArray();
		
		return b;
	}
	
	public static void saveFile(byte[] fileData, String uri) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(uri);
			fos.write(fileData);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean deleteFile(String filePath) {
		return new File(filePath).delete();
	}
	
	public static String convertStreamToString(InputStream is) {

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
