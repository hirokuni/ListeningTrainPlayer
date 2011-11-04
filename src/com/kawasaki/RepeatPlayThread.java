package com.kawasaki;

import android.media.MediaPlayer;

class RepeatPlayThread {
	private boolean mIsTerminated = false;
	private Runnable mRunnable;
	private int mloopInterval = 20;// msec
	private MediaPlayer mMediaPlayer;
	private Thread mThread;
	private notify mNotify;
	private int mRepeatStartPos;
	private int mRepeatEndPos;

	private RepeatPlayThread() {

	}

	public interface notify {
		public void onStartPos(int aPos);

		public void notifyCurrentPosition(int aPosition);

		public void onEndPos(int aPos);
	}

	public RepeatPlayThread(MediaPlayer aMediaPlayer, notify aNotify) {
		if (aMediaPlayer == null)
			new IllegalArgumentException();

		mMediaPlayer = aMediaPlayer;
		mNotify = aNotify;
		mThread = new Thread(new Runnable() {
			public void run() {
				int currentPos = 0;
				while (!mIsTerminated) {
					try {
						if ((currentPos = mMediaPlayer.getCurrentPosition()) >= mRepeatEndPos) {
							if (mNotify != null)
								mNotify.onEndPos(currentPos);
							mMediaPlayer.seekTo(mRepeatStartPos);
							if (mNotify != null)
								mNotify.onStartPos(mMediaPlayer
										.getCurrentPosition());
						}
						Thread.sleep(mloopInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	public boolean isLoopAlive(){
		return mThread.isAlive();
	}
	
	public void terminate() {
		mIsTerminated = true;
		if (mThread.isAlive())
			mThread.interrupt();
	}

	public int getLoopResolution() {
		return mloopInterval;
	}

	public void setLoopResolution(int aInterval) {
		mloopInterval = aInterval;
	}

	public void setRepeatStartPos(int aPos) {
		mRepeatStartPos = aPos;

	}

	public void setRepeatEndPos(int aPos) {
		mRepeatEndPos = aPos;

	}

	public void startRepeat() {
		if (!mThread.isAlive())
			mThread.start();

		mMediaPlayer.seekTo(mRepeatStartPos);

		if (!mMediaPlayer.isPlaying())
			mMediaPlayer.start();

	}

}
