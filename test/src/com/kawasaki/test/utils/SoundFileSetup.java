package com.kawasaki.test.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import com.kawasaki.test.R;

public class SoundFileSetup {

	public static final String PATH = Environment
			.getExternalStorageDirectory().toString() + "/" + "UMediaPlay";
	private Context mContext;
	private List<String> mFilePathList;
	
	public static final String MP3_KBPS_HZ_H_MIN_SEC = "mp3_kbs_hz_h_min_sec.mp3";
	public static final String M4A_256KBPS_44100HZ_00H_05MIN_54SEC = "m4a_256kbps_44100hz_00h_05min_54sec_11699197byte.m4a";
	public static final String M4A_256KBPS_44100HZ_00H_05MIN_54SEC_COPIED_DATA = "m4a_256kbps_44100hz_00h_05min_54sec_155335byte_copied_data.m4a";
	

	public SoundFileSetup(Context context) {

		mContext = context;
		mFilePathList = Collections.synchronizedList(new ArrayList<String>());
		Resources res = context.getResources();
		TestUtils.delete(PATH);
		mFilePathList.add(TestUtils.writeNewFile(
				res.openRawResource(R.raw.mp3_kbs_hz_h_min_sec), PATH,
				MP3_KBPS_HZ_H_MIN_SEC));
		mFilePathList.add(TestUtils.writeNewFile(
				res.openRawResource(R.raw.m4a_256kbps_44100hz_00h_05min_54sec_11699197byte),
				PATH, M4A_256KBPS_44100HZ_00H_05MIN_54SEC));
		mFilePathList.add(TestUtils.writeNewFile(
				res.openRawResource(R.raw.m4a_256kbps_44100hz_00h_05min_54sec_155335byte_copied_data),
				PATH, M4A_256KBPS_44100HZ_00H_05MIN_54SEC_COPIED_DATA));
	}

	public List<String> getFilePathList() {
		return mFilePathList;
	}

	public String getRequiredSoundFilePath(String fileName){
		
		for (Iterator<String> i = mFilePathList.iterator(); i.hasNext();) {
			String filePath = (String) i.next();
		    File f = new File(filePath);
		    if(f.getName().equalsIgnoreCase(fileName))
		    	return filePath;
		}
		return null;
	}
	
	    
	public List<String> getAllAacFiles() {

		List<String> res = Collections
				.synchronizedList(new ArrayList<String>());
		
		for (Iterator<String> i = mFilePathList.iterator(); i.hasNext();) {
			String filePath = (String) i.next();
			if (getSuffix(filePath).equalsIgnoreCase("m4a")) {
				res.add(filePath);
			}
		}

		return res;
	}

   
	private static String getSuffix(String fileName) {
		if (fileName == null)
			return null;
		int point = fileName.lastIndexOf(".");
		if (point != -1) {
			return fileName.substring(point + 1);
		}
		return fileName;
	}

	public void deleteAllFiles() {
		TestUtils.delete(PATH);
	}

}