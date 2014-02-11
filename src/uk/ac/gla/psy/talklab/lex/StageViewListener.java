package uk.ac.gla.psy.talklab.lex;

public interface StageViewListener {
	void onSelect();
	void onMotionEvent(int quad, String key, String val);
}
