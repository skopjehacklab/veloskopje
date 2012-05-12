package com.kika.veloskopje;

import java.io.File;

import android.app.Application;
import android.os.Environment;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		String appPath = String.format("%s/%s", Environment.getExternalStorageDirectory(), "VeloSkopje");

		File appFolder = new File(appPath);
		if(!appFolder.exists()) {
			appFolder.mkdir();
		}
	}
}
