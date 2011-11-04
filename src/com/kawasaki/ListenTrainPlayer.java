package com.kawasaki;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.ringdroid.soundfile.CheapSoundFile;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

public class ListenTrainPlayer {
	MediaPlayer mMediaPlayer;
	Context mContext;
	notifyChange mListener;
	public static final String PLAYBACK_REPEAT_START = "playback_repeat_start";
	public static final String PLAYBACK_REPEAT_END = "playback_repeat_end";
	private CheapSoundFile mCheapSoundFile;
	private String mPlayBackFilePath;
	private int mRepeatStartPos;
	private int mRepeatEndPos;
	private RefPosThread mRefPosThread;
	
	private class RefPosThread extends Thread {
		private boolean mIsTerminated = false;
		public void terminate(){
		  mIsTerminated = true;
		  this.interrupt();
		}
		
		public void run(){
			while(!mIsTerminated){
				try {
					sleep(20);
				} catch (InterruptedException e) {
					//if()
					e.printStackTrace();
				}
			}
		}
		
		
	}

	private ListenTrainPlayer() {

	}

	public interface notifyChange {
		void onPlaybackStateChange(String what, int currentTime);
	}

	public boolean repeatPlay(int startTime, int endTime) {
		return true;
	}

	private void prepare() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					// CheapSoundFile.create consumes time approximately 50
					// seconds on emulator, and sometimes 6 seconds on actual
					// device.
					// The creation API shall be called in asnyc manner.
					mCheapSoundFile = CheapSoundFile.create(mPlayBackFilePath,
							new CheapSoundFile.ProgressListener() {
								public boolean reportProgress(
										double fractionComplete) {
									return true;
								}
							});
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		thread.start();
		
	}

	public void setStateChangeListener(notifyChange listener) {
		mListener = listener;
	}

	public ListenTrainPlayer(Context context, String aFilePath) {
		mContext = context;
		mPlayBackFilePath = aFilePath;
		prepare();
	}

	public void stop() {
		mMediaPlayer.stop();
	}

	public boolean play(Uri uri) {

		mMediaPlayer = MediaPlayer.create(mContext, uri);

		if (mMediaPlayer != null) {
			mMediaPlayer.start();
		} else {
			return false;
		}

		return true;
	}

	public void release() {
		if (mMediaPlayer != null)
			mMediaPlayer.release();
		mRefPosThread.stop();
	}

	public static int secondsToFrames(double seconds, int sampleRate,
			int samplePerFrame) {
		return (int) (1.0 * seconds * sampleRate / samplePerFrame + 0.5);
	}

	static int count = 0;

	public int getCurrentPosition() {
		// TODO Auto-generated method stub
		count++;

		if (count == 1) {
			return 9000;
		} else if (count == 2) {
			return 9000;
		} else if (count == 3) {
			return 13000;
		} else if (count == 4) {
			return 12000;
		} else if (count == 5) {
			return 12000;
		} else {
			return 18000;
		}
	}

	public void setDataSource(String aFilePath) {
		mPlayBackFilePath = aFilePath;
	}

	public void setRepeatStartPosition(int aStartPos) {
		mRepeatStartPos = aStartPos;
	}

	public void setRepeatEndPosition(int aEndPos) {
		mRepeatEndPos = aEndPos;
	}

	public void repeatPrepare() throws IllegalStateException {
		if (mRepeatStartPos > mRepeatEndPos)
			throw new IllegalStateException();
	}

	public void setRepeatStartPos(int msecond) {
		mRepeatStartPos = msecond;
	}

	public void setRepeatEndPos(int msecond) {
		mRepeatEndPos = msecond;
	}

	public void cancelRepeat() {
		// TODO Auto-generated method stub

	}

	public void startRepeat() {
		//if(mRefPosThread
		if(mRefPosThread!=null)
			if(mRefPosThread.isAlive())
				mRefPosThread.interrupt();
			
		//mRefPosThread = new Thread();
	}
	
	

}