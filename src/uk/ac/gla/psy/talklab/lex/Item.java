package uk.ac.gla.psy.talklab.lex;

public class Item {
	public static final String DEBUG_TAG = "Item";
	protected int mID = 0;
	protected String[] mAOI = new String[4];
	protected String mSpeech = "";
	public static final int mAllAOIs = 4;
	
	public Item(int id, String aoi0, String aoi1, String aoi2, String aoi3, String sfile) { // constructor
		setData(id,aoi0,aoi1,aoi2,aoi3,sfile);
	}
	
	public void setData(int id, String aoi0, String aoi1, String aoi2, String aoi3, String sfile) {
		mID = id;
		mAOI[0] = aoi0;
		mAOI[1] = aoi1;
		mAOI[2] = aoi2;
		mAOI[3] = aoi3;
		mSpeech = sfile;
		//Log.i(DEBUG_TAG, String.valueOf(mID) + " " + mAOI[0] + " " + mAOI[1] + " " + mAOI[2] + " " + mAOI[3] + " " + mSpeech);
	}
}
