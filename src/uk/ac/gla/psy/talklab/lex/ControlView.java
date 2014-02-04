package uk.ac.gla.psy.talklab.lex;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.os.SystemClock;


public class ControlView extends View {
	
	public static final String DEBUG_TAG = "Control";
	
	private ShapeDrawable mVertRect = null;
	private ShapeDrawable mHorzRect = null;
	private int mMidX = 0;
	private int mMidY = 0;
	//private int mDeadZone = 0;

	private ControlViewListener listener = null;
	public  boolean mRecording = false;

	public ControlView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setControlViewListener(ControlViewListener l) {
        listener = l;
	}
	
	@Override
	 public void onWindowFocusChanged(boolean hasFocus) {
	  super.onWindowFocusChanged(hasFocus);
	  mMidX = this.getWidth()/2; 
	  mMidY = this.getHeight()/2;
	  //mDeadZone = this.getHeight()/8;
	  if (mVertRect == null) {
		mVertRect = new ShapeDrawable(new RectShape());
		mVertRect.getPaint().setColor(0xFF000000);
		mVertRect.setBounds(mMidX-2, 0, mMidX+2, this.getHeight());
		mHorzRect = new ShapeDrawable(new RectShape());
		mHorzRect.getPaint().setColor(0xFF000000);
		mHorzRect.setBounds(0, mMidY-2, this.getWidth(), mMidY+2);		  
	  }
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){ 
		boolean bHandled = false;
		long msCurrent = SystemClock.uptimeMillis();
		int quad = -1;

		int action = MotionEventCompat.getActionMasked(event);
		boolean bWrite = false;

		if ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_MOVE)) {
			bHandled = true;
			bWrite = true;
			quad = event.getX() > mMidX ? 1 : 0;
			quad |= event.getY() > mMidY ? 2 : 0;
		} else { // not a down or move event
			if (action == MotionEvent.ACTION_UP) {
				bWrite = true;
			} else {}
			bHandled = super.onTouchEvent(event);
		}

		if (bWrite) {
			if (listener != null) {
				// TODO create string including motion
				String s = "MOVE";
				switch (action) {
				case MotionEvent.ACTION_UP :
					s = "UP";
				case MotionEvent.ACTION_DOWN :
					s = "DOWN";
				}
				listener.onMotionEvent(quad, s, String.format("%.04f %.04f", (event.getX()-mMidX)/mMidX, (event.getY()-mMidY)/mMidY));
			} else {}
		} else {}


		return bHandled;
	}	

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mVertRect.draw(canvas);
		mHorzRect.draw(canvas);
	}
}
