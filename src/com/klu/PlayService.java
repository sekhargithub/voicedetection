package com.klu;

import android.app.Service;


import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.widget.Toast;

public class PlayService extends Service {
	MediaPlayer mp = null;

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			if (mp != null) {
				mp.release();
			}
			// mp.setDataSource("/sdcard/give_me_one_reason.mp3");mp.prepare();mp.start();
			// Create a new MediaPlayer to play this sound
			mp = MediaPlayer.create(getApplicationContext(), R.raw.eruption);
			mp.start();
		} catch (IllegalArgumentException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (IllegalStateException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		try {
			if (mp == null) {
				mp = MediaPlayer.create(this, R.raw.eruption);
				mp.start();
			} else if (mp.isPlaying()) {
				mp.stop();
				mp.start();
			}
		} catch (IllegalArgumentException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (IllegalStateException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		try {
			if (mp != null) {
				mp.stop();
				mp.release();
				mp = null;
			}

		} catch (IllegalArgumentException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (IllegalStateException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}

	}
}
