package com.klu;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FaceTest extends Activity {
	private ProgressDialog progDailog = null;
	private String imageInSD = "";
	private String strResponse = "", result = "";
	protected static final int PIC_REQUEST = 0;
	LinearLayout ll;
	TextView tv_msg;
	EditText et_identi;
	Button btn_pic;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.face);
		tv_msg = (TextView) findViewById(R.id.tv_msg);

		ll = (LinearLayout) findViewById(R.id.ll);

		btn_pic = (Button) findViewById(R.id.btn_pic);
		btn_pic.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent pic_intent = new Intent(getApplicationContext(),
						CameraView.class);
				pic_intent.putExtra("PIC_ID", "duplicate");
				startActivityForResult(pic_intent, PIC_REQUEST);
				// setContentView(tv_sample);
			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == PIC_REQUEST) {
			if (resultCode == RESULT_OK) {
				String img_name = intent.getStringExtra("PIC_ID");

				/* ImageView image = (ImageView) this.findViewById(R.id.iv_img); */
				String img = String.format("%s.jpg", img_name);
				File sdcard = Environment.getExternalStorageDirectory();
				imageInSD = sdcard.getAbsolutePath() + File.separator + img;

				Bitmap bitmap = BitmapFactory.decodeFile(imageInSD);

				/*
				 * ImageView myImageView =
				 * (ImageView)findViewById(R.id.imageview);
				 * myImageView.setImageBitmap(bitmap);
				 */

				FaceView faceView = new FaceView(this, bitmap);
				LayoutParams ll_params = new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				LayoutParams params = new LayoutParams(90,
						LayoutParams.WRAP_CONTENT);
				LinearLayout ll_temp = new LinearLayout(this);

				ll_temp.setOrientation(LinearLayout.HORIZONTAL);
				ll_temp.setLayoutParams(ll_params);
				Button btn_home = new Button(getApplicationContext());
				btn_home.setLayoutParams(params);
				btn_home.setText("Home");
				btn_home.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						// TODO Auto-generated method stub
						startActivity(new Intent(getApplicationContext(),
								MainActivity.class));
					}
				});

				ll.removeAllViews();
				ll.forceLayout();
				ll_temp.addView(btn_home);
				if (FaceView.PICS_COUNT == 1) {
				}

				ll.addView(ll_temp);
				ll.addView(faceView);

				// setContentView(faceView);
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
				btn_pic.setText("Sorry, You cancelled the job");
			}
		}
	}

	public ProgressDialog getProgDailog() {
		return progDailog;
	}

	public void setProgDailog(ProgressDialog progDailog) {
		this.progDailog = progDailog;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getStrResponse() {
		return strResponse;
	}

	public void setStrResponse(String strResponse) {
		this.strResponse = strResponse;
	}

}