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

public class UCheapAAC extends InstrumentationTestCase {

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
				.getRequiredSoundFilePath(SoundFileSetup.M4A_256KBPS_44100HZ_00H_05MIN_54SEC);
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

	public void test_get_aac_file_parameters() {
		assertEquals(mCheapAACSoundFile.getChannels(), 2);
		assertEquals(mCheapAACSoundFile.getFiletype(), "AAC");
		assertEquals(mCheapAACSoundFile.getFileSizeBytes(), 11699197);
		assertEquals(mCheapAACSoundFile.getSampleRate(), 44100);
	}

	public void test_one_time_playback_with_specific_byte_and_length() {
		double position_10_msec = 10 * 1000;
		double position_15_msec = 15 * 1000;
		int frame_number_at_10_Sec = 0;
		int frame_number_at_nextTo_15_Sec = 0;
		int byte_number_at_10_sec = 0;
		int byte_number_at_nextTo_15_sec = 0;

		// information at 10 seconds in position.
		assertEquals(
				true,
				0 < (frame_number_at_10_Sec = ListenTrainPlayer
						.secondsToFrames(position_10_msec / 1000,
								mCheapAACSoundFile.getSampleRate(),
								mCheapAACSoundFile.getSamplesPerFrame())));
		assertEquals(
				true,
				0 < (byte_number_at_10_sec = mCheapAACSoundFile
						.getSeekableFrameOffset(frame_number_at_10_Sec)));
		assertEquals(true, 11699197 > byte_number_at_10_sec);

		// information at 15 seconds in position.
		assertEquals(
				true,
				0 < (frame_number_at_nextTo_15_Sec = ListenTrainPlayer
						.secondsToFrames(position_15_msec / 1000,
								mCheapAACSoundFile.getSampleRate(),
								mCheapAACSoundFile.getSamplesPerFrame()) + 1));
		assertEquals(
				true,
				0 < (byte_number_at_nextTo_15_sec = mCheapAACSoundFile
						.getSeekableFrameOffset(frame_number_at_nextTo_15_Sec)));
		assertEquals(true, 11699197 > byte_number_at_nextTo_15_sec);

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(filePath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			assertNotNull(fis);
		}

		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(fis.getFD(), 0,
					byte_number_at_nextTo_15_sec);
			mediaPlayer.prepare();
			mediaPlayer.seekTo((int) position_10_msec);
			//mediaPlayer.setLooping(true);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}

		assertEquals((int) position_10_msec, mediaPlayer.getCurrentPosition());

		final CountDownLatch lock = new CountDownLatch(1);

		mediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer arg0) {
						lock.countDown();
					}
				});

		mediaPlayer.start();
		// TestUtils.countDownWait(lock,
		// (int)(position_15_msec-position_10_msec+1));
		TestUtils.countDownWait(lock, (int) (20000));
		// assertEquals(position_15_msec-position_10_msec,
		// mediaPlayer.getDuration());
		assertEquals((int) position_15_msec, mediaPlayer.getCurrentPosition());
	}

	private void afterSavingRingtone(CharSequence title, String outPath,
			File outFile, int duration) {
		long length = outFile.length();
		if (length <= 512) {
			outFile.delete();

			return;
		}

		// Create the database record, pointing to the existing file path

		long fileSize = outFile.length();
		String mimeType = "audio/mpeg";

		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, outPath);
		values.put(MediaStore.MediaColumns.TITLE, title.toString());
		values.put(MediaStore.MediaColumns.SIZE, fileSize);
		values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
		values.put(MediaStore.Audio.Media.DURATION, duration);
		values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
		values.put(MediaStore.Audio.Media.IS_ALARM, false);
		values.put(MediaStore.Audio.Media.IS_MUSIC, true);

		// Insert it into the database
		Uri uri = MediaStore.Audio.Media.getContentUriForPath(outPath);

	}

	

	public void test_make_part_of_file_and_play_it() {
		double position_10_msec = 10 * 1000;
		double position_15_msec = 15 * 1000;
		int frame_number_at_10_Sec = 0;
		int frame_number_at_nextTo_15_Sec = 0;

		int startFrame = 0;
		int numFrames = 0;
		File outputFile;

		String portion_of_file_path = null;

		// information at 10 seconds in position.
		assertEquals(
				true,
				0 < (frame_number_at_10_Sec = ListenTrainPlayer
						.secondsToFrames(position_10_msec / 1000,
								mCheapAACSoundFile.getSampleRate(),
								mCheapAACSoundFile.getSamplesPerFrame())));

		// information at 15 seconds in position.
		assertEquals(
				true,
				0 < (frame_number_at_nextTo_15_Sec = ListenTrainPlayer
						.secondsToFrames(position_15_msec / 1000,
								mCheapAACSoundFile.getSampleRate(),
								mCheapAACSoundFile.getSamplesPerFrame()) + 1));

		// make a file of a part of an original file

		outputFile = new File(portion_of_file_path = SoundFileSetup.PATH + "/"
				+ "portion_of_file." + mCheapAACSoundFile.getFiletype());
		outputFile.delete();
		startFrame = frame_number_at_10_Sec;
		numFrames = frame_number_at_nextTo_15_Sec - frame_number_at_10_Sec;
		try {
			mCheapAACSoundFile.WriteFile(outputFile, startFrame, numFrames);
		} catch (IOException e) {
			e.printStackTrace();
			assertFalse(e.toString(), true);
		}

		afterSavingRingtone("tmp_title", portion_of_file_path, outputFile, 5000);

		// setup media player
		// FileInputStream fis = null;
		// try {
		// fis = new FileInputStream(new File(filePath));
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } finally {
		// assertNotNull(fis);
		// }

		// check the file attributes
		CheapSoundFile portion_of_file = null;

		try {
			assertNotNull(portion_of_file = CheapSoundFile.create(
					portion_of_file_path,
					new CheapSoundFile.ProgressListener() {
						public boolean reportProgress(double fractionComplete) {
							return true;
						}
					}));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertEquals("AAC", portion_of_file.getFiletype());
		assertEquals(numFrames, portion_of_file.getNumFrames());

		//
		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(portion_of_file_path);
			mediaPlayer.prepare();
			mediaPlayer.seekTo((int) position_10_msec);

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

		mediaPlayer.start();

		// TestUtils.countDownWait(lock,
		// (int)(position_15_msec-position_10_msec+1));
		TestUtils.countDownWait(lock, (int) (10000));

		// duration.
		// Result was whole duration time. Not 5 seconds. The duration in atom
		// seems not to be changed.
		assertEquals(position_15_msec - position_10_msec,
				mediaPlayer.getDuration());

		// end position
		assertEquals((int) position_15_msec, mediaPlayer.getCurrentPosition());

		// finalize
		new File(portion_of_file_path).delete();

	}

}