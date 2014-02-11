package uk.ac.gla.psy.talklab.lex;

import uk.ac.gla.psy.talklab.lex.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class StageView extends View {
	
	public static Context mContext = null;
	private static final String DEBUG_TAG = "Stage";	
	private Bitmap[] mAOI = new Bitmap[4]; // sharp (formerly large) AOIs!
	private Bitmap[] msAOI = new Bitmap[4]; // blurred (formerly small) AOIs!
	private Bitmap mHighlight = null;
	private int mMidX = 0;
	private int mMidY = 0;
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
	
	// given a particular quadrant (0,1,2,3), return the leftmost X coord
	public int quadX(int i) {
		return (i % 2)==1 ? getWidth()-msAOI[i].getWidth()-4 : 4;
	}
	
	// given a particular quadrant (0,1,2,3), return the upper Y coord
	public int quadY(int i) {
		return (i > 1) ? getHeight()-msAOI[i].getHeight()-4 : 4;
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		mMidX = this.getWidth()/2; 
		mMidY = this.getHeight()/2;
		if (mHighlight == null) {
			mHighlight = BitmapFactory.decodeResource(getResources(), R.drawable.highlight);
		} else {}
	}

	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int i = 0;
		// draw all of the blurred bitmaps
		for (i = 0; i < 4; i++) {
			if (msAOI[i] != null) {
				canvas.drawBitmap(msAOI[i], quadX(i), quadY(i), null);
			} else {}
		}
		// mQuad is -1 if no picture has been selected
		if (mQuad != -1) {
			canvas.drawBitmap(mAOI[mQuad], quadX(mQuad), quadY(mQuad), null);
			if (mSelected) {
				canvas.drawBitmap(mHighlight, quadX(mQuad), quadY(mQuad), null);
			} else {}
		} else {}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// new, from Control View
		long msCurrent = SystemClock.uptimeMillis();
		int quad = -1;
		boolean bWrite = false;

		// old stuff
		boolean bHandled = false;
		
		int action = MotionEventCompat.getActionMasked(event);
		
		if ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_MOVE)) {
			quad = event.getX() > mMidX ? 1 : 0;
			quad |= event.getY() > mMidY ? 2 : 0;
			bHandled = true;
			// TODO: only select if already highlighted, cur quad = mQuad
			if ((action == MotionEvent.ACTION_DOWN) && (quad == mQuad)) {
				mListener.onSelect();
			} else {
				bWrite = true;				
			}
		} else { // not a down or move event
			if (action == MotionEvent.ACTION_UP) {
				bWrite = true;
			} else {}
			bHandled = super.onTouchEvent(event);
		}
		
 		if (bWrite) {
			if (mListener != null) {
				// TODO create string including motion
				String s = "MOVE";
				switch (action) {
				case MotionEvent.ACTION_UP :
					s = "UP";
				case MotionEvent.ACTION_DOWN :
					s = "DOWN";
				}
				mListener.onMotionEvent(quad, s, String.format("%.04f %.04f", (event.getX()-mMidX)/mMidX, (event.getY()-mMidY)/mMidY));
			} else {}
		} else {}
		
		return bHandled;
	}
}
