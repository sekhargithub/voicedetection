
package com.klu;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.util.Log;
import android.view.View;

@SuppressLint({ "ViewConstructor", "DrawAllocation" })
public class FaceView extends View {
	public static int PICS_COUNT = 0;
	private static final int NUM_FACES = 10; // max is 64
	private static final boolean DEBUG = true;

	private FaceDetector arrayFaces;
	private FaceDetector.Face getAllFaces[] = new FaceDetector.Face[NUM_FACES];
	private FaceDetector.Face getFace = null;
	
	private PointF eyesMidPts[] = new PointF[NUM_FACES];
	private float  eyesDistance[] = new float[NUM_FACES];
	
	private Bitmap sourceImage;
	
	private Paint tmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint pOuterBullsEye = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint pInnerBullsEye = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private int picWidth, picHeight;
	private float xRatio, yRatio;
	
	public FaceView(Context context, Bitmap bitmap) {
		super(context);
		
		pInnerBullsEye.setStyle(Paint.Style.FILL);
		pInnerBullsEye.setColor(Color.RED);
		
		pOuterBullsEye.setStyle(Paint.Style.STROKE);
		pOuterBullsEye.setColor(Color.RED);
		
		tmpPaint.setStyle(Paint.Style.STROKE);
		tmpPaint.setTextAlign(Paint.Align.CENTER);
		
		BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inPreferredConfig = Bitmap.Config.RGB_565;
		
		//sourceImage = BitmapFactory.decodeResource( getResources() ,R.drawable.group, bfo);

		sourceImage = bitmap;

		picWidth = sourceImage.getWidth();
		picHeight = sourceImage.getHeight();
		
		arrayFaces = new FaceDetector( picWidth, picHeight, NUM_FACES );
		arrayFaces.findFaces(sourceImage, getAllFaces);
		
		for (int i = 0; i < getAllFaces.length; i++)
		{
			getFace = getAllFaces[i];
			try {
				PointF eyesMP = new PointF();
				getFace.getMidPoint(eyesMP);
				eyesDistance[i] = getFace.eyesDistance();
				eyesMidPts[i] = eyesMP;
				
				if (DEBUG)
				{
					Log.i("Face",
						i +  " " + getFace.confidence() + " " + getFace.eyesDistance() + " "
						+ "Pose: ("+ getFace.pose(FaceDetector.Face.EULER_X) + ","
						+ getFace.pose(FaceDetector.Face.EULER_Y) + ","
						+ getFace.pose(FaceDetector.Face.EULER_Z) + ")"
						+ "Eyes Midpoint: ("+eyesMidPts[i].x + "," + eyesMidPts[i].y +")"
					);
				}
			}
			catch (Exception e)
			{
				if (DEBUG) Log.e("Face", i + " is null");
			}
		
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{	PICS_COUNT = eyesMidPts.length;
		xRatio = getWidth()*1.0f / picWidth;
		yRatio = getHeight()*1.0f / picHeight;
		canvas.drawBitmap( sourceImage, null , new Rect(0,0,getWidth(),getHeight()),tmpPaint);
		for (int i = 0; i < eyesMidPts.length; i++)
		{
			if (eyesMidPts[i] != null)
			{
				pOuterBullsEye.setStrokeWidth(eyesDistance[i] /18);
				canvas.drawCircle(eyesMidPts[i].x*xRatio, eyesMidPts[i].y*yRatio, eyesDistance[i] / 2 , pOuterBullsEye);
				//canvas.drawCircle(eyesMidPts[i].x*xRatio, eyesMidPts[i].y*yRatio, eyesDistance[i] / 6 , pInnerBullsEye);
			}
		}
	}
}
