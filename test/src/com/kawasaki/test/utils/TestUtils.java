package com.kawasaki.test.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.res.Resources;

public class TestUtils {

	private static final String dataPath = "/data/data/";

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	public static boolean countDownWait(CountDownLatch lock, int waitTimeMsec) {
		try {
			lock.await(waitTimeMsec, TimeUnit.MILLISECONDS);
		} catch (InterruptedException iex) {
			return false;
		}
		return true;
	}

	public static void sleep(long waitTime) {
		try {
			Thread.sleep(waitTime);
		} catch (InterruptedException irex) {
			// nothing to do
		}
	}

	public static String writeNewFile(InputStream input, String outFilePath,
			String fileName) {

		// String dst = dataPath + context.getPackageName() + "/files/" +
		// outFilePath;
		String res = null;
		String dst = outFilePath;
		FileOutputStream output = null;
		File file = null;
		try {
			File fileDir = new File(dst);
			if (!fileDir.exists())
				if (!fileDir.mkdirs())
					return null;
			file = new File(outFilePath, fileName);
			if (file.createNewFile()) {
				output = new FileOutputStream(file);
			} else {
				return null;
			}

			res = file.getPath();

			// output =
			// context.openFileOutput(outFilePath,Context.MODE_PRIVATE);
		} catch (FileNotFoundException fnfex) {
			return null;
		} catch (IOException ioe) {
			return null;
		}

		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int n = 0;
		try {
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
			}
		} catch (IOException ioe) {

			return null;
		} finally {
			try {
				output.close();
			} catch (IOException ioe) {
				return null;
			}
		}

		return res;
	}

	public static String writeFile2AppDir(Context context, InputStream input,
			String fileName) {

		// String dst = dataPath + context.getPackageName() + "/files/" +
		// outFilePath;
		FileOutputStream output = null;

		try {
			output = context.openFileOutput(fileName,
					Context.MODE_WORLD_READABLE | Context.MODE_WORLD_READABLE);
		} catch (FileNotFoundException fnfex) {
			// output is null
			return null;
		}

		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int n = 0;
		try {
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
			}
		} catch (IOException ioe) {

			return null;
		} finally {
			try {
				output.close();
			} catch (IOException ioe) {
				return null;
			}
		}

		return dataPath + context.getPackageName() + "/files/" + fileName;
	}

	public static boolean deleteAppDirFile(Context context, String fileName) {
		try {
			context.deleteFile(fileName);
			return true;
		} catch (SecurityException sex) {
			return false;
		}
	}

	static public void delete(String path) {

		File f = new File(path);

		if (f.exists() == false) {
			return;
		}

		if (f.isFile()) {
			f.delete();
		}

		if (f.isDirectory()) {
			File[] files = f.listFiles();
			for (int i = 0; i < files.length; i++) {
				delete(files[i].getPath());
			}
			f.delete();
		}
	}

}
