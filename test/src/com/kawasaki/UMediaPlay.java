package com.kawasaki;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import com.kawasaki.test.utils.SoundFileSetup;
import com.kawasaki.test.utils.TestUtils;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.test.ActivityInstrumentationTestCase2;

public class UMediaPlay extends ActivityInstrumentationTestCase2 {

	private Context mAppContext;

	ListenTrainPlayer mListenTrainPlayer;

	private int startTime = 0;
	private int endTime = 0;

	private SoundFileSetup mSoundFileSetup;

	@SuppressWarnings("unchecked")
	public UMediaPlay() {
		super("com.kawasaki", ListeningTrain.class);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();

		startTime = 0;
		endTime = 0;
		mAppContext = this.getActivity().getApplicationContext();

		// Need test context to setup files in SDCARD with using resource files
		// of test context.
		mSoundFileSetup = new SoundFileSetup(this.getInstrumentation()
				.getContext());
		String file_path = mSoundFileSetup
				.getRequiredSoundFilePath(SoundFileSetup.M4A_256KBPS_44100HZ_00H_05MIN_54SEC);
		mListenTrainPlayer = new ListenTrainPlayer(mAppContext, file_path);
	}

	@Override
	protected void tearDown() throws Exception {

		mSoundFileSetup.deleteAllFiles();
		mSoundFileSetup = null;

		if (null != mListenTrainPlayer)
			mListenTrainPlayer.release();
		super.tearDown();

	}

	public void test_play_one_local_media_file() {
		
		Uri fileUri = Uri.parse(mSoundFileSetup
				.getRequiredSoundFilePath(SoundFileSetup.M4A_256KBPS_44100HZ_00H_05MIN_54SEC));
		assertEquals(true,mListenTrainPlayer.play(fileUri));
		TestUtils.sleep(5000);//To hear playback from speaker.
	}

	public void test_player_creation_return_false_due_to_no_exist_file() {
		Uri fileUri = Uri.parse("http://www.personal.test/test.mp3");
		assertEquals(false,mListenTrainPlayer.play(fileUri));
	}
	
	public void test_repeat_playback_between_x_seconds_and_y_seconds() {
		//三角測量により二つのテストケースを書いておく
		int fiveMilliSecond = 5*1000;
		int tenMilliSecond = 10*1000;
		repeat(fiveMilliSecond,tenMilliSecond);
		
		int elevenMilliSecond = 11*1000;
		int fifteenMilliSecond = 15*1000;
		repeat(elevenMilliSecond,fifteenMilliSecond);
				
	}
	
	private void repeat(int repeatStartPosMilli,int repeatEndPosMilli){
final CountDownLatch lock = new CountDownLatch(1);
		
		mListenTrainPlayer.setRepeatStartPos(repeatStartPosMilli);
		mListenTrainPlayer.setRepeatEndPos(repeatEndPosMilli);
		mListenTrainPlayer.startRepeat();
		
		// time as repeat duration. So extra
				// time is added.
		int extraTime_2000MilliSeconds = 2000;
		TestUtils.countDownWait(lock, (repeatStartPosMilli - repeatEndPosMilli)
						+ extraTime_2000MilliSeconds);
		int currentPos = mListenTrainPlayer.getCurrentPosition();
		
		//大小比較だと、失敗したときに、どの値で失敗したのか、トレースができない。なので、assertを失敗するときに比較値をメッセージで出力するようにした。
		//こうすると、良い副作用として、一つのテストケース内で、repeat()関数がどの引数によるテストで呼ばれたかがトレースできる。
		assertEquals("Result should be smaller than : "+repeatEndPosMilli + " Current Pos : "+currentPos,true,repeatEndPosMilli > currentPos);
		currentPos = mListenTrainPlayer.getCurrentPosition();
		assertEquals("Result should be larger than : "+repeatStartPosMilli + " Current Pos : "+currentPos,true,repeatStartPosMilli < currentPos);
		
		mListenTrainPlayer.cancelRepeat();
		
		TestUtils.countDownWait(lock, (repeatStartPosMilli - repeatEndPosMilli)
				+ extraTime_2000MilliSeconds);
			
		currentPos = mListenTrainPlayer.getCurrentPosition();
		assertEquals("Result should be larger than : "+(repeatEndPosMilli + extraTime_2000MilliSeconds) + " Current Pos : "+currentPos,true,repeatEndPosMilli + extraTime_2000MilliSeconds < currentPos);
	}


	

	

	
	
}
