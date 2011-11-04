package com.kawasaki;

import java.util.concurrent.CountDownLatch;

import com.kawasaki.RepeatPlayThread;
import com.kawasaki.test.utils.SoundFileSetup;
import com.kawasaki.test.utils.TestUtils;

import android.media.MediaPlayer;
import android.test.ActivityInstrumentationTestCase2;
import android.test.AndroidTestCase;

public class URepeatPlayThread extends ActivityInstrumentationTestCase2 {
	private SoundFileSetup mSoundFileSetup;
	private String mAudioFilePath;
	private MediaPlayer mMediaPlayer;

	public URepeatPlayThread() {
		super("com.kawasaki", ListeningTrain.class);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();

		// Need test context to setup files in SDCARD with using resource files
		// of test context.
		mSoundFileSetup = new SoundFileSetup(this.getInstrumentation()
				.getContext());
		mAudioFilePath = mSoundFileSetup
				.getRequiredSoundFilePath(SoundFileSetup.M4A_256KBPS_44100HZ_00H_05MIN_54SEC);
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setDataSource(mAudioFilePath);
		mMediaPlayer.prepare();
	}

	@Override
	protected void tearDown() throws Exception {
		mSoundFileSetup.deleteAllFiles();
		mSoundFileSetup = null;
		mMediaPlayer.release();
		super.tearDown();

	}

	// normal case

	public void test_thread_loop_interval_check_with_20msec_and_with30msec() {
		// int defaultLoopResolution20msec = 20;
		// assertEquals(defaultLoopResolution20msec,
		// repeatPlayThread.getLoopResolution());
		//
		// repeatPlayThread.setLoopResolution(30);
		// assertEquals(30, repeatPlayThread.getLoopResolution());
		//
		// repeatPlayThread.setLoopResolution(20);
		// assertEquals(20, repeatPlayThread.getLoopResolution());

	}

	public void test_playback_is_not_stopped_when_RepeatPlayThread_is_terminated_during_repeat_playback() {
		RepeatPlayThread repeatPlayThread;
		int milliSec_5000 = 5000;
		int milliSec_10000 = 10000;
		final CountDownLatch lock = new CountDownLatch(1);

		repeatPlayThread = new RepeatPlayThread(mMediaPlayer, null);

		repeatPlayThread.setRepeatStartPos(milliSec_5000);
		repeatPlayThread.setRepeatEndPos(milliSec_10000);
		repeatPlayThread.startRepeat();

		TestUtils.countDownWait(lock, 1000);

		repeatPlayThread.terminate();

		assertEquals(true, mMediaPlayer.isPlaying());

		// to check thread live, give some cpu time to let the RepeatPlayThread'
		// run() finish.
		TestUtils.sleep(100);
		assertEquals(false, repeatPlayThread.isLoopAlive());

	}

	public void test_repeat_music_from_start_pos_if_playback_exceeds_configured_end_pos() {
		int loopNum = 1;

		int milliSec_5000 = 5000;
		int milliSec_10000 = 10000;
		int tolerance_20msec = 20;
		final CountDownLatch lock = new CountDownLatch(1);

		final int[] startPosAllay = new int[loopNum];
		final int[] endPosAllay = new int[loopNum];

		for (int i = 0; i < loopNum; i++) {
			startPosAllay[i] = 0;
			endPosAllay[i] = 0x0FFFFF;
		}

		mMediaPlayer.seekTo(milliSec_10000);
		mMediaPlayer.start();
		TestUtils.sleep(3000);

		RepeatPlayThread repeatPlayThread = new RepeatPlayThread(mMediaPlayer,
				new RepeatPlayThread.notify() {
					int onStartPosCBCount = 0;
					int onEndPosCBCount = 0;

					public void onStartPos(int aPos) {
						startPosAllay[onStartPosCBCount++] = aPos;
						lock.countDown();
					}

					public void notifyCurrentPosition(int aPosition) {

					}

					public void onEndPos(int aPos) {

					}
				});

		repeatPlayThread.setRepeatStartPos(milliSec_5000);
		repeatPlayThread.setRepeatEndPos(milliSec_10000);
		repeatPlayThread.startRepeat();

		TestUtils.countDownWait(lock, 10000);

		repeatPlayThread.terminate();

		for (int i = 0; i < loopNum; i++) {

			assertEquals("startPosAllay[" + i + "]==" + startPosAllay[i], true,
					milliSec_5000 + tolerance_20msec >= startPosAllay[i]);
		}

	}

	public void test_repeat_music_from_start_pos_if_playback_pos_is_before_configured_start_pos() {
		int loopNum = 1;

		int milliSec_5000 = 5000;
		int milliSec_10000 = 10000;
		int tolerance_20msec = 20;
		final CountDownLatch lock = new CountDownLatch(1);

		final int[] startPosAllay = new int[loopNum];
		final int[] endPosAllay = new int[loopNum];

		for (int i = 0; i < loopNum; i++) {
			startPosAllay[i] = 0;
			endPosAllay[i] = 0x0FFFFF;
		}
		
		mMediaPlayer.start();
		TestUtils.sleep(1000);

		RepeatPlayThread repeatPlayThread = new RepeatPlayThread(mMediaPlayer,
				new RepeatPlayThread.notify() {
					int onStartPosCBCount = 0;
					int onEndPosCBCount = 0;

					public void onStartPos(int aPos) {
						startPosAllay[onStartPosCBCount++] = aPos;
						lock.countDown();
					}

					public void notifyCurrentPosition(int aPosition) {

					}

					public void onEndPos(int aPos) {

					}
				});

		repeatPlayThread.setRepeatStartPos(milliSec_5000);
		repeatPlayThread.setRepeatEndPos(milliSec_10000);
		repeatPlayThread.startRepeat();

		TestUtils.countDownWait(lock, 10000);

		repeatPlayThread.terminate();

		for (int i = 0; i < loopNum; i++) {

			assertEquals("startPosAllay[" + i + "]==" + startPosAllay[i], true,
					milliSec_5000 + tolerance_20msec >= startPosAllay[i]);
		}
	}

	public void test_repeat_music_from_current_pos_if_playback_pos_is_between_configured_start_pos_and_end_pos() {

	}

	public void test_it_can_change_start_pos_and_end_pos_during_repeat_playback() {

	}

	public void test_repeat_one_sound_file_3times_between_5seconds_and_10seconds() {
		int loopNum = 5;
		RepeatPlayThread repeatPlayThread;
		int milliSec_5000 = 5000;
		int milliSec_10000 = 10000;
		final CountDownLatch lock = new CountDownLatch(loopNum);

		final int[] startPosAllay = new int[loopNum];
		final int[] endPosAllay = new int[loopNum];

		for (int i = 0; i < loopNum; i++) {
			startPosAllay[i] = 0;
			endPosAllay[i] = 0x0FFFFF;
		}

		repeatPlayThread = new RepeatPlayThread(mMediaPlayer,
				new RepeatPlayThread.notify() {
					int onStartPosCBCount = 0;
					int onEndPosCBCount = 0;

					public void onStartPos(int aPos) {
						startPosAllay[onStartPosCBCount++] = aPos;
					}

					public void notifyCurrentPosition(int aPosition) {

					}

					public void onEndPos(int aPos) {
						endPosAllay[onEndPosCBCount++] = aPos;
						lock.countDown();
					}
				});

		repeatPlayThread.setRepeatStartPos(milliSec_5000);
		repeatPlayThread.setRepeatEndPos(milliSec_10000);
		repeatPlayThread.startRepeat();

		TestUtils.countDownWait(lock, 300000);

		repeatPlayThread.terminate();
		// to check thread live, give some cpu time to let thread run finish.
		TestUtils.sleep(100);
		assertEquals(false, repeatPlayThread.isLoopAlive());
		// Because of following reasons, the tolerance is 20msec.
		// RepeatPlayThread is looping with 20msec interval. Max tolerance is
		// 20msec.
		// 1 frame sampling is 1024. So, the interval of one frame is
		// around23msec.
		int tolerance_20msec = 20;

		for (int i = 0; i < loopNum; i++) {

			assertEquals("startPosAllay[" + i + "]==" + startPosAllay[i], true,
					milliSec_5000 + tolerance_20msec >= startPosAllay[i]);
			assertEquals("endPosAllay[" + i + "]==" + endPosAllay[i], true,
					milliSec_10000 + tolerance_20msec >= endPosAllay[i]);

		}

	}

	// Error case

	public void test_RuntimeException_is_thrown_when_repeat_is_called_after_it_was_terminated_once() {

	}

	public void test_error_termination() {

	}

	public void test_fail_if_it_configures_end_pos_which_value_is_smaller_than_start_pos() {

	}

	public void test_fail_if_it_configures_start_pos_which_value_is_bigger_than_end_pos() {

	}

}
