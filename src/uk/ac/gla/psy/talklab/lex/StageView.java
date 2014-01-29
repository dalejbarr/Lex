package uk.ac.gla.psy.talklab.lex;

import uk.ac.gla.psy.talklab.lex.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class StageView extends View {
	
	public static Context mContext = null;
	private static final String DEBUG_TAG = "Stage";	
	private Bitmap[] mAOI = new Bitmap[4]; // large AOIs!
	private Bitmap[] msAOI = new Bitmap[4]; // small AOIs!
	private Bitmap mShadow = null;
	private Bitmap[] mHBM = new Bitmap[4];
	private Bitmap mHighlight = null;
	public int mQuad = -1;
	private StageViewListener mListener = null;
	private boolean mSelected = false;
	
	public StageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}
	
	public static int getResId(String var) {
		return mContext.getResources().getIdentifier("drawable/" + var, null, mContext.getPackageName());
	}
	/*
	
	public static int getResId(String variableName) {
	    try {
	        Field idField = Drawable.class.getDeclaredField(variableName);
	        return idField.getInt(idField);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return -1;
	    } 
	}
	*/
	
	/*
	public static Bitmap getBitmapFromAsset(Context context, String strName) {
	    AssetManager assetManager = context.getAssets();
	    InputStream istr;
	    Bitmap bitmap = null;
	    try {
	        istr = assetManager.open(strName);
	        bitmap = BitmapFactory.decodeStream(istr);
	    } catch (IOException e) {
	        return null;
	    }
	    return bitmap;
	}
	*/
	
	public void prepare(Item it) {
		// TODO prepare
		for (int i = 0; i < Item.mAllAOIs; i++) {
			mAOI[i] = BitmapFactory.decodeResource(getResources(), getResId(it.mAOI[i]));
			if (mAOI[i] == null) {
			   Log.e(DEBUG_TAG, "ERROR with " + String.valueOf(it.mID) + " " + it.mAOI[i]);
			} else {}
			msAOI[i] = BitmapFactory.decodeResource(getResources(), getResId(it.mAOI[i]+"_small"));
			if (msAOI[i] == null) {
				   Log.e(DEBUG_TAG, "ERROR with " + String.valueOf(it.mID) + " " + it.mAOI[i] + "_small");
			} else {}
		}
		if (mShadow == null) {
			mShadow = Bitmap.createBitmap(mAOI[0].getWidth(), mAOI[0].getHeight(), Bitmap.Config.ARGB_8888);
			mShadow.eraseColor(Color.argb(158,0,0,0));
		} else {}
		mSelected = false;
		mQuad = -1;
		invalidate();
	}
	
	public void setStageViewListener(StageViewListener l) {
		mListener = l;
	}
	
	public void highlight(int quad) {
		mSelected = true;
		mQuad = quad;
		invalidate();
		//Log.i(DEBUG_TAG, "highlight");
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (mHBM[0] == null) {
			mHBM[0] = BitmapFactory.decodeResource(getResources(), R.drawable.select_topleft);
			mHBM[1] = BitmapFactory.decodeResource(getResources(), R.drawable.select_topright);
			mHBM[2] = BitmapFactory.decodeResource(getResources(), R.drawable.select_botleft);
			mHBM[3] = BitmapFactory.decodeResource(getResources(), R.drawable.select_botright);
			mHighlight = BitmapFactory.decodeResource(getResources(), R.drawable.highlight);
		} else {}
	}

	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int bord = (getWidth()-mShadow.getWidth())/2; 
		int i = 0;
		int xDir = 0;
		int yDir = 0;
		int x = 0;
		int y = 0;
		for (i = 0; i < 4; i++) {
			if (msAOI[i] != null) {
				canvas.drawBitmap(msAOI[i], (i % 2)==1 ? getWidth()-msAOI[i].getWidth()-4 : 4, (i > 1) ? getHeight()-msAOI[i].getHeight()-4 : 4, null);
			} else {}
		}
		if (mQuad != -1) {
			xDir = ((mQuad % 2)*2-1);
			yDir = (int) (Math.floor(mQuad/2)*2-1);
			x = bord + xDir*3*bord/4;
			y = bord + yDir*3*bord/4;
			canvas.drawBitmap(mShadow, x-xDir*10, y-yDir*10, null);
			canvas.drawBitmap(mAOI[mQuad], x, y, null);
			canvas.drawBitmap(mHBM[mQuad], (mQuad % 2)==1 ? this.getWidth()-mHBM[mQuad].getWidth() : 0, (mQuad > 1) ? this.getHeight()-mHBM[mQuad].getHeight() : 0, null);
			if (mSelected) {
				canvas.drawBitmap(mHighlight, x, y, null);
			} else {}
		} else {}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean bHandled = false;
		int action = MotionEventCompat.getActionMasked(event);
		if (action == MotionEvent.ACTION_DOWN) {
			mListener.onSelect();
			bHandled = true;
		} else {}
		return bHandled;
	}
}
