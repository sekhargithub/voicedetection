package com.klu;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

public class SpeechActivity extends Activity implements OnInitListener {

	private static final int PIC_REQUEST = 100;
	private static final int VOICE_REQUEST_CODE = 101;
	private static final int TTS_REQUEST_CODE = 102;// {"open", "close", "find"}
	private static final String[] commands = { "send sms", "google", "find",
			"open", "stop", "video", "detect face" };
	private static final int PICTURE_REQUEST = 0;

	private int selectedCommand = -1;
	private String speachMessage = "I am sorry. could you please repeat that.";
	private boolean isTtsInitialized = false;
	TextToSpeech tts;
	SensorManager sensorManager;
	Sensor proximitySensor;

	MediaPlayer mp = null;

	ImageView iv_detect = null, iv_img = null;
	VideoView video = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		iv_img = (ImageView) this.findViewById(R.id.iv_img);
		video = (VideoView) findViewById(R.id.video);
		// check proximity sensor
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		if (proximitySensor == null) {
			Toast.makeText(this, "No proximity sensor", Toast.LENGTH_SHORT)
					.show();
		} else {
			sensorManager.registerListener(proximitySensorEventListener,
					proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
		}

		// check for TTS data
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, TTS_REQUEST_CODE);

	}

	SensorEventListener proximitySensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub

			if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
				if (event.values[0] < proximitySensor.getMaximumRange()) {
					startSpeachRecognition();
				}

			}
		}
	};

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

	// Initialization response of TTS engine
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
			Toast.makeText(this, "Initialization failed", Toast.LENGTH_SHORT)
					.show();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			for (int i = 0; i < commands.length; i++) {
				if (matches.contains(commands[i])) {
					selectedCommand = i;
				}
			}
			if (isTtsInitialized) {
				// //////////////////////////////////////////////////////////
				if (selectedCommand >= 0) {
					tts.speak("you have selected" + commands[selectedCommand]
							+ " command.", TextToSpeech.QUEUE_FLUSH, null);
					switch (selectedCommand) {
					case 0:
						try {
							Intent sendIntent = new Intent(Intent.ACTION_VIEW);
							sendIntent.putExtra("sms_body", "default content");
							sendIntent.setType("vnd.android-dir/mms-sms");
							startActivity(sendIntent);
						} catch (Exception e) {

							Toast.makeText(getApplicationContext(),
									"sms failed pls try again after some time",
									Toast.LENGTH_LONG).show();

							e.printStackTrace();

						}
						/*
						 * startActivity(new Intent(this,FaceTest.class));
						 * Intent intent = new Intent(Intent.ACTION_VIEW);
						 * intent.setData(Uri.parse("http://www.google.com"));
						 * startActivity(intent);
						 */
						selectedCommand = -1;
						break;
					case 1:
						Intent intent1 = new Intent(Intent.ACTION_VIEW);
						intent1.setData(Uri.parse("http://www.google.com"));
						startActivity(intent1);
						selectedCommand = -1;
						break;
					case 2:
						/*
						 * Intent pic_intent = new
						 * Intent(getApplicationContext(), CameraView.class);
						 * pic_intent.putExtra("PIC_ID", "photo");
						 * startActivityForResult(pic_intent, PIC_REQUEST);
						 */

						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("geo:17.6883,83.2186"));
						startActivity(intent);

						selectedCommand = -1;
						break;
					
					case 3:
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
					case 4:
						try {
							if (mp != null) {
								if (mp.isPlaying() || mp.isLooping())
									mp.stop();
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
					case 5:
						try {
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

					case 6:
						Intent det_intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						startActivityForResult(det_intent, PICTURE_REQUEST);
						selectedCommand = -1;
						break;
					}

				} else {
					tts.speak(speachMessage, TextToSpeech.QUEUE_FLUSH, null);
				}
				// ///////////////////////////////////////////////////////
			}

		}

		else if (requestCode == TTS_REQUEST_CODE) {
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
		} else if (requestCode == PICTURE_REQUEST && resultCode == RESULT_OK) {
			Bitmap bm = (Bitmap) data.getExtras().get("data");
			// beg sear
			int width = bm.getWidth();
			int height = bm.getHeight();

			FaceDetector detector = new FaceDetector(width, height, 5);
			Face[] faces = new Face[5];

			Bitmap bm565 = Bitmap.createBitmap(width, height, Config.RGB_565);
			Paint ditherPaint = new Paint();
			Paint drawPaint = new Paint();

			ditherPaint.setDither(true);
			drawPaint.setColor(Color.RED);
			drawPaint.setStyle(Paint.Style.STROKE);
			drawPaint.setStrokeWidth(2);

			Canvas canvas = new Canvas();
			canvas.setBitmap(bm565);
			canvas.drawBitmap(bm, 0, 0, ditherPaint);

			int detected_faces = detector.findFaces(bm565, faces);
			PointF midpt = new PointF();
			float eye_dist = 0.0f;

			if (detected_faces > 0) {
				for (int i = 0; i < detected_faces; ++i) {
					faces[i].getMidPoint(midpt);
					eye_dist = faces[i].eyesDistance();
					canvas.drawRect((int) midpt.x - eye_dist, (int) midpt.y
							- eye_dist, (int) midpt.x + eye_dist, (int) midpt.y
							+ eye_dist, drawPaint);
				}
			}

			String filepath = Environment.getExternalStorageDirectory() + "/my"
					+ System.currentTimeMillis() + "pic.jpg";

			try {
				FileOutputStream fos = new FileOutputStream(filepath);

				bm565.compress(CompressFormat.JPEG, 90, fos);

				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			iv_img.setImageBitmap(bm565);

			// iv_img.setImageURI(Uri.parse(img_path));
			
			
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStop() {
		if (sensorManager != null) {
			sensorManager.unregisterListener(proximitySensorEventListener);
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// sensorManager.registerListener(proximitySensorEventListener,
		// proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// sensorManager.unregisterListener(proximitySensorEventListener);
	}
}
