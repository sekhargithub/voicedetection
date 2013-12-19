package com.klu;


import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends Activity implements OnInitListener,
		SensorEventListener {
	private static final int VOICE_REQUEST_CODE = 101;
	private static final int TTS_REQUEST_CODE = 102;
	private static final int PIC_REQUEST = 0;

	/*
	 * private static final String[] commands = { "open google", "photo",
	 * "find", "sing", "play" };
	 */
	private static final String[] commands = { "google", "take pic", "song",
			"close", "play" };

	private int selectedCommand = -1;
	private String speachMessage = "I am sorry. could you please repeat that.";
	private boolean isTtsInitialized = false;
	TextToSpeech tts;
	SensorManager sm;
	Sensor psensor;
	MediaPlayer mp = null;

	ImageView iv_detect = null, iv_img = null;
	VideoView video = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		iv_img = (ImageView) this.findViewById(R.id.iv_img);
		// mp = new MediaPlayer();
		video = (VideoView) findViewById(R.id.video);
		// Load and start the movie

		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		psensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		if (psensor == null) {
			Toast.makeText(this, "No proximity sensor", Toast.LENGTH_SHORT)
					.show();
		} else {
			sm.registerListener(this, psensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		}

		// check for TTS data
		Intent intent = new Intent();
		intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(intent, TTS_REQUEST_CODE);

		// iv_detect = (ImageView) findViewById(R.id.iv_detect);
		// startService(new Intent(MainActivity.this,AudioService.class));
	}

	/*
	 * public void onClickHandle(View view) { switch(view.getId()) { case
	 * R.id.iv_detect: startActivity(new Intent(this,FaceTest.class)); break;
	 * 
	 * } }
	 */

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Toast.makeText(this, "Language is not available",
						Toast.LENGTH_SHORT).show();
			} else {
				isTtsInitialized = true;
			}
		} else {
			Toast.makeText(this, "Initialization failed", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent se) {
		if (se.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			if (se.values[0] < psensor.getMaximumRange()) {
				startSpeachRecognition();  
			}
		}
	}

	private void startSpeachRecognition() {
		// Check to see if a recognition activity is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.ACTION_RECOGNIZE_SPEECH,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice Command");
			startActivityForResult(intent, VOICE_REQUEST_CODE);
		} else {
			Toast.makeText(this, "Recognizer not present", Toast.LENGTH_SHORT)
					.show();
		}

	}

	@Override
	protected void onStop() {
		if (sm != null) {
			sm.unregisterListener(this);

		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mp != null) {
			mp.stop();
			mp.release();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			for (int i = 0; i < commands.length; i++) {
				// if (matches.contains(commands[i])) {
				if (matches.get(0).startsWith(commands[i].substring(0, 2))) {
					selectedCommand = i;
					break;
				}
			}
			// commands = { "google", "take pic", "song","close", "play" };
			if (isTtsInitialized) {
				if (selectedCommand >= 0) {
					switch (selectedCommand) {
					case 0:
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("http://www.google.com"));
						startActivity(intent);
						selectedCommand = -1;
						break;

					case 1:
						Intent pic_intent = new Intent(getApplicationContext(),
								CameraView.class);
						pic_intent.putExtra("PIC_ID", "photo");
						startActivityForResult(pic_intent, PIC_REQUEST);
						selectedCommand = -1;
						break;

					case 2:
						try {
							// Release any resources from previous MediaPlayer
							if (mp != null) {
								mp.release();
							}
							// mp.setDataSource("/sdcard/give_me_one_reason.mp3");mp.prepare();mp.start();
							// Create a new MediaPlayer to play this sound
							mp = MediaPlayer.create(this, R.raw.eruption);
							mp.start();
						} catch (IllegalArgumentException e) {
							Toast.makeText(this, e.getMessage(),
									Toast.LENGTH_SHORT).show();
						} catch (IllegalStateException e) {
							Toast.makeText(this, e.getMessage(),
									Toast.LENGTH_SHORT).show();
						}
						selectedCommand = -1;
						break;
					case 3:
						/*
						 * tts.speak("You can look to the stars in search of the "
						 * +
						 * "answers Look for God and life on distant planets Have your faith in the ever after While each of us holds inside the map to the labyrinth And heaven is	 here on earth "
						 * , TextToSpeech.QUEUE_FLUSH, null);
						 */try {
							if (mp != null) {
								if (mp.isPlaying() || mp.isLooping())
									mp.stop();
								mp.release();
								mp = null;
							} else {
								mp.release();
								mp = null;
							}
							if (video != null && video.isPlaying())
								video.stopPlayback();
						} catch (IllegalArgumentException e) {
							Toast.makeText(this, e.getMessage(),
									Toast.LENGTH_SHORT).show();
						} catch (IllegalStateException e) {
							Toast.makeText(this, e.getMessage(),
									Toast.LENGTH_SHORT).show();
						}
						selectedCommand = -1;
						break;
					case 4:
						/*
						 * tts.speak("You can look to the stars in search of the "
						 * +
						 * "answers Look for God and life on distant planets Have your faith in the ever after While each of us holds inside the map to the labyrinth And heaven is	 here on earth "
						 * , TextToSpeech.QUEUE_FLUSH, null);
						 */try {
							// video.setVideoPath("/data/samplevideo.3gp" );
							// Uri uri =
							// Uri.parse("android.resource://[package]/raw/video")
							// or
							Uri uri = Uri.parse("android.resource://"
									+ getPackageName() + "/"
									+ R.raw.angry_birds);
							video.setVideoURI(uri);
							video.start();

						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalStateException e) {
							e.printStackTrace();
						}
						selectedCommand = -1;
						break;

					default:
						tts.speak("you have selected"
								+ commands[selectedCommand] + " command.",
								TextToSpeech.QUEUE_FLUSH, null);
						selectedCommand = -1;
						break;
					}
				} else {
					tts.speak(speachMessage, TextToSpeech.QUEUE_FLUSH, null);
				}

			}

		} else if (requestCode == VOICE_REQUEST_CODE
				&& resultCode == RESULT_CANCELED) {
			if (mp != null) {
				if (mp.isPlaying() || mp.isLooping())
					mp.stop();
				mp.release();
				mp = null;
			} else {
				mp.release();
				mp = null;
			}
			if (video != null && video.isPlaying())
				video.stopPlayback();
		} else if (requestCode == TTS_REQUEST_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				tts = new TextToSpeech(this, this);
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		} else if (requestCode == PIC_REQUEST) {
			if (resultCode == RESULT_OK) {
				String img_name = data.getStringExtra("PIC_ID");

				// iv_img = (ImageView) this.findViewById(R.id.iv_img);
				String img = String.format("%s.jpg", img_name);
				File sdcard = Environment.getExternalStorageDirectory();
				String img_path = sdcard.getAbsolutePath() + File.separator
						+ img;

				iv_img.setImageURI(Uri.parse(img_path));
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}