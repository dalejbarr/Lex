package uk.ac.gla.psy.talklab.lex;

import uk.ac.gla.psy.talklab.lex.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.SystemClock;
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
		return mContext.getResources().getIdentifier("drawable/" + var, null,
				mContext.getPackageName());
	}

	public void prepare(Item item) {
		
		for (int i = 0; i < Item.mAOIs; i++) {
			
			String AOI = item.mAOI[i];
			String smallAOI = AOI + "_small";
			
			mAOI[i] = bitmap(AOI);
			
			if (mAOI[i] == null) {
				Log.e(DEBUG_TAG, "ERROR with " + String.valueOf(item.mID) + " "
						+ AOI);
			}
			msAOI[i] = bitmap(smallAOI);
			if (msAOI[i] == null) {
				Log.e(DEBUG_TAG, "ERROR with " + String.valueOf(item.mID) + " "
						+ smallAOI);
			}
		}
		mSelected = false;
		mQuad = -1;
		invalidate();
	}
	
	/** Helper method to create bitmap from item */
	private Bitmap bitmap(String aoi) {
		Resources resource = getResources();
		int resourceID = getResId(aoi);
		return BitmapFactory.decodeResource(resource, resourceID);
	}

	public void setStageViewListener(StageViewListener l) {
		mListener = l;
	}

	public void highlight(int quad) {
		mSelected = true;
		mQuad = quad;
		invalidate();
		// Log.i(DEBUG_TAG, "highlight");
	}

	/** given a particular quadrant (0,1,2,3)
	 * return the leftmost X coord */
	public int quadX(int i) {
		return (i % 2) == 1 ? getWidth() - msAOI[i].getWidth() - 4 : 4;
	}

	/** given a particular quadrant (0,1,2,3)
	 *  return the upper Y coord */
	public int quadY(int i) {
		return (i > 1) ? getHeight() - msAOI[i].getHeight() - 4 : 4;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	    if (hasFocus) {
	        this.setSystemUiVisibility(
	                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
	                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
	                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
	                | View.SYSTEM_UI_FLAG_FULLSCREEN
	                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
	    
		mMidX = this.getWidth() / 2;
		mMidY = this.getHeight() / 2;
		if (mHighlight == null) {
			mHighlight = BitmapFactory.decodeResource(getResources(),
					R.drawable.highlight);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int i = 0;
		// draw all of the blurred bitmaps
		for (i = 0; i < Item.mAOIs; i++) {
			if (msAOI[i] != null) {
				canvas.drawBitmap(msAOI[i], quadX(i), quadY(i), null);
			}
		}
		// mQuad is -1 if no picture has been selected
		if (mQuad != -1) {
			canvas.drawBitmap(mAOI[mQuad], quadX(mQuad), quadY(mQuad), null);
			if (mSelected) {
				canvas.drawBitmap(mHighlight, quadX(mQuad), quadY(mQuad), null);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// new, from Control View
		//long msCurrent = SystemClock.uptimeMillis();
		int quad = -1;
		boolean isHandled = false;
		boolean bWrite = false;

		//int action = MotionEventCompat.getActionMasked(event);
		int action = event.getAction();
		boolean actionDOWN = (action == MotionEvent.ACTION_DOWN);
		boolean actionMOVE = (action == MotionEvent.ACTION_MOVE);

		if (actionDOWN || actionMOVE) {
			quad = event.getX() > mMidX ? 1 : 0;
			quad |= event.getY() > mMidY ? 2 : 0;
			isHandled = true;
			// put stuff here
			bWrite = isHighlighted(actionDOWN, quad);
		} else { // not a down or move event
			if (action == MotionEvent.ACTION_UP) {
				bWrite = true;
			}
			isHandled = super.onTouchEvent(event);
		}

		if (bWrite) writeMotion(event, action, quad);
		return isHandled;
	}
	
	// TODO: only select if already highlighted, cur quad = mQuad
	private boolean isHighlighted(boolean actionDOWN, int quad) {
		boolean bWrite = false;
		if (actionDOWN && (quad == mQuad)) {
			mListener.onSelect();
		} else {
			bWrite = true;
		}
		return bWrite;
	}

	private void writeMotion(MotionEvent event, int action, int quad) {
		if (mListener != null) {
			// TODO create string including motion
			String state = "MOVE";
			double getX = (event.getX() - mMidX) / mMidX;
			double getY = (event.getY() - mMidY) / mMidY;

			String coord = String.format("%.04f %.04f", getX , getY);

			switch (action) {
			case MotionEvent.ACTION_UP:
				state = "UP";
			case MotionEvent.ACTION_DOWN:
				state = "DOWN";
			}
			mListener.onMotionEvent(quad, state, coord);
		}
	}
}
