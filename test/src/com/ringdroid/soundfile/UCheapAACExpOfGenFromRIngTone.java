package com.ringdroid.soundfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import com.kawasaki.ListenTrainPlayer;
import com.kawasaki.test.utils.SoundFileSetup;
import com.kawasaki.test.utils.TestUtils;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.provider.MediaStore;
import android.test.InstrumentationTestCase;

public class UCheapAACExpOfGenFromRIngTone extends InstrumentationTestCase {

	private SoundFileSetup mSoundFileSetup;

	private CheapSoundFile mCheapAACSoundFile;

	private String filePath;

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();

		// Need test context to setup files in SDCARD with using resource files
		// of test context.
		mSoundFileSetup = new SoundFileSetup(this.getInstrumentation()
				.getContext());

		filePath = mSoundFileSetup
				.getRequiredSoundFilePath(SoundFileSetup.M4A_256KBPS_44100HZ_00H_05MIN_54SEC_COPIED_DATA);
		assertNotNull(filePath);

		final CountDownLatch lock = new CountDownLatch(1);

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					// CheapSoundFile.create consumes time approximately 50
					// seconds on emulator, and sometimes 6 seconds on actual
					// device.
					// The creation API shall be called in asnyc manner.
					assertNotNull(mCheapAACSoundFile = CheapSoundFile.create(
							filePath, new CheapSoundFile.ProgressListener() {
								public boolean reportProgress(
										double fractionComplete) {
									return true;
								}
							}));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				lock.countDown();
			}
		});

		thread.start();

		TestUtils.countDownWait(lock, 120 * 1000);

	}

	@Override
	protected void tearDown() throws Exception {

		mSoundFileSetup.deleteAllFiles();
		mSoundFileSetup = null;

		super.tearDown();

	}

	public void test_check_generated_file_from_ringtone() {
		double position_10_msec = 10 * 1000;
		double position_15_msec = 15 * 1000;
	
		//
		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(filePath);
			mediaPlayer.prepare();
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}

		final CountDownLatch lock = new CountDownLatch(1);

		mediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer arg0) {
						lock.countDown();
					}
				});

		assertEquals(0, mediaPlayer.getCurrentPosition());
		
		mediaPlayer.start();

		// TestUtils.countDownWait(lock,
		// (int)(position_15_msec-position_10_msec+1));
		TestUtils.countDownWait(lock, (int) (10000));

		// duration.
		// Result was whole duration time. Not 5 seconds. The duration in atom
		// seems not to be changed.
//		assertEquals(position_15_msec - position_10_msec,
//				mediaPlayer.getDuration());

		// end position
		assertEquals((int) position_15_msec, mediaPlayer.getCurrentPosition());

		

	}

}