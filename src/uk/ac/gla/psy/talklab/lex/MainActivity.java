package uk.ac.gla.psy.talklab.lex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.ac.gla.psy.talklab.lex.R;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.XmlResourceParser;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.Window;

public class MainActivity extends Activity {
	
	public class MyWriter extends BufferedWriter {
		public void writeTS(String key, String val) throws IOException {
				write(String.valueOf(SystemClock.uptimeMillis() + " " + key + " " + val));
				newLine();
		}
		public MyWriter(String filename) throws IOException {
			super(new BufferedWriter(new FileWriter(filename)));
		}
	}
	
	// these variables are static
	public static final String DEBUG_TAG = "Main";
	public static final int PREVIEW = 0;
	public static final int MAINPHASE = 1;
	public static final long mPreviewMS = 2500;
	public static final int NUMFILLERS = 48;
	public static final long DWELLLIMIT = 1000;
	public static final int NUMPRACTICE = 10; // number of practice trials

	// these variables need to be set just one time, at initialization/completion
	public static boolean mSessionCompleted = false;
	private StageView mStage = null;
	private MediaPlayer mPlayer = null;
	private MyWriter mFile = null;
	protected Handler mHandler = new Handler();
	private Vibrator mVib = null;
	protected MyThread mThread = null;
	protected HashMap<Integer,Item> mItems = null;
	protected Integer[] mOrder = null;
	protected XmlResourceParser mXml;

	protected class TouchTimeout implements Runnable {
		private long mEventMS = 0;
		public void run() {
			if ((mEventMS==mLastUpdate) && (!mSelected)) {
				mStage.mQuad = -1;
				mStage.invalidate();
				mLastQuad = -1;
			} else {}
		}
		public TouchTimeout(long ms) {
			mEventMS = ms;
		}
	};

	// these variables need to be reset/updated at each trial
	private int mPhase = PREVIEW;
	private int mLastQuad = -1;
	private long mLastUpdate = 0;
	private boolean mbFirst = true;  // first time subject selects any region
	private HashSet<Integer> mVisited = null; // which quadrants have been visited
	private boolean mSelected = false;
	private int mTrial = 0;

	// // // MEMBER FUNCTIONS
	protected class MyThread implements Runnable {
		public void run() {
			if (mPlayer != null) {
				mPlayer.start();
			} else {}
			try {
				mFile.writeTS("SYNCTIME", mItems.get(mOrder[mTrial]).mSpeech);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private StageViewListener stageListener = new StageViewListener() {
		public void onSelect() {
			if ((mVib != null) && (mPhase == MAINPHASE) && (mLastQuad != -1) && !mSelected) {
				try {
					mFile.writeTS("SELECT", String.valueOf(mLastQuad));
				} catch (IOException e) {
					e.printStackTrace();
				}
				mVib.vibrate(50);
				mStage.highlight(mLastQuad);
				mSelected = true;
				mTrial = mTrial + 1;
				nextTrialDialog();
			} else {}
		}
        public void onMotionEvent(int quad, String key, String val) {
			if ((quad != mLastQuad) && (quad != -1) && !mSelected) {
				if (!mbFirst) {
					if ((SystemClock.uptimeMillis() - mLastUpdate) > 250) { // must be at least 250 ms on pic
			        	mVisited.add(mLastQuad);
			        	//Log.i(DEBUG_TAG, mLastQuad + " visited " + ((SystemClock.uptimeMillis() - mLastUpdate)));
					} else {
						//Log.i(DEBUG_TAG, "too short on " + mLastQuad + " not added");
					}
				} else {
					mbFirst = false;
				}
				mLastQuad = quad;
	        	mStage.mQuad = quad;
	        	mStage.invalidate();
				mLastUpdate = SystemClock.uptimeMillis();
				mHandler.postDelayed(new TouchTimeout(mLastUpdate), DWELLLIMIT);
	        	if ((mVisited.size()==Item.mAllAOIs) && (mPhase == PREVIEW)) {
	    			mPhase = MAINPHASE;
	        		mHandler.postDelayed(mThread, mPreviewMS);
	        	} else {}
	        } else {
	        	if (quad == -1) {
	        		mStage.invalidate();
	        	} else {}
	        } // quadrant unchanged, do nothing
			try {
				mFile.writeTS(key, val);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	};
	
	private void nextTrial() {
		//Log.i(DEBUG_TAG, "next trial!");
		try {
			mFile.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mPhase = PREVIEW;
		mLastQuad = -1;
		mLastUpdate = 0;
		mbFirst = true;  // first time subject selects any region
		mVisited.clear(); // which quadrants have been visited
		mSelected = false;
		Item iCur = mItems.get(mOrder[mTrial]);
		try {
			mFile.writeTS("BEGIN", String.valueOf(mOrder[mTrial]));
			mFile.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mStage.prepare(iCur);
		mPlayer = MediaPlayer.create(this, getResources().getIdentifier("raw/" + iCur.mSpeech, null, getPackageName()));
	}
	
	private void nextTrialDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		if (mTrial < mItems.size()) {
			// set title
			alertDialogBuilder.setTitle("Round completed");
			// set dialog message
			String msg = "rounds";
			if ((mItems.size()-mTrial) == 1) {
				msg = "round";
			} else {}
			alertDialogBuilder
			.setMessage(String.format("%d %s remaining", mItems.size() - mTrial, msg))
			.setCancelable(false)
			/*
			.setPositiveButton("Quit",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, close
					// current activity
					MainActivity.this.finish();
				}
			})
			*/
			.setPositiveButton("Next Round",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, just close
					// the dialog box and do nothing
					dialog.cancel();
					MainActivity.this.nextTrial();
				}
			});
		} else {
			try {
				mFile.writeTS("SESSION_END", String.valueOf(DashboardActivity.mSessionID));
			} catch (IOException e) {
				e.printStackTrace();
			}
			MainActivity.mSessionCompleted = true;
			alertDialogBuilder
			.setMessage("Experiment Completed!")
			.setCancelable(false)
			.setPositiveButton("OK",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, close
					// current activity
					MainActivity.this.finish();
				}
			});
		}
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		String basepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Lex/";
		File f1 = new File(basepath);
		f1.mkdirs();
		
		String fname = basepath + String.format("s%07d.txt", DashboardActivity.mSessionID);
		//String fname = getFilesDir() + String.format("/s%07d.txt", DashboardActivity.mSessionID);
		try {
			mFile = new MyWriter(fname);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		if (mFile == null) {
			Log.i(DEBUG_TAG, "didn't open" + fname);
		}
		try {
			mFile.writeTS("SESSION_BEGIN", String.valueOf(DashboardActivity.mSessionID));
		} catch (IOException e) {
			e.printStackTrace();
		}

		mStage = (StageView) findViewById(R.id.stage);
		mStage.setStageViewListener(stageListener);
		mVib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);//Initiate the vibrate service
		mThread = new MyThread();
		mVisited = new HashSet<Integer>(Item.mAllAOIs);
		mItems = new HashMap<Integer,Item>();
		loadXML();
		// load all the items into an integer array, then shuffle
		mOrder = new Integer[mItems.size()];
		Iterator<Map.Entry<Integer,Item>> it = mItems.entrySet().iterator();
		int i = 0;
	    while (it.hasNext()) {
	        Map.Entry<Integer,Item> pairs = it.next();
	        mOrder[i++] = pairs.getKey();
	    }
		if (mItems.size() > MainActivity.NUMPRACTICE) { // full experiment!
			// first create just an array with the fillers, and shuffle it
			Integer[] itFillers = new Integer[MainActivity.NUMFILLERS];
			for (i=0; i<MainActivity.NUMFILLERS; i++) { // filler numbers
				itFillers[i] = (mItems.size()-MainActivity.NUMFILLERS) + i + 1; // fillers at the end of list
				// e.g., 49-96
			}
			itFillers = new ShuffleArray(itFillers).get();
			
			// now copy over the first NUMPRACTICE filler trials
			for (i=0; i<MainActivity.NUMPRACTICE; i++) { 
				mOrder[i] = itFillers[i];
			}
			
			// now temporarily store the unused fillers and the remaining items, then shuffle.
			// itRest will hold all of the data
			Integer[] itRest = new Integer[mItems.size()-MainActivity.NUMPRACTICE];
			// first the fillers
			for (i=0; i < (MainActivity.NUMFILLERS-MainActivity.NUMPRACTICE); i++) {
				itRest[i] = itFillers[i+MainActivity.NUMPRACTICE]; // copy over remaining fillers
			}
			// now the remaining items
			for (int j=0; j < mItems.size()-MainActivity.NUMFILLERS; j++) {
				itRest[j+i] = j+1;
			}
			itRest = new ShuffleArray(itRest).get();
			for (i = MainActivity.NUMPRACTICE; i < mItems.size(); i++) {
				mOrder[i] = itRest[i-MainActivity.NUMPRACTICE];
			}
		} else {
		    mOrder = new ShuffleArray(mOrder).get();
		}
	    //Log.i(DEBUG_TAG, mOrder[0] + " is first");
		
		nextTrial();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void loadXML() {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
		    builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
		    e.printStackTrace();  
		}
		InputStream is = getResources().openRawResource(R.raw.items);
	    //parse using builder to get DOM representation of the XML file
		
		try {
			Document doc = builder.parse(is, null);
			doc.getDocumentElement().normalize();
			Element rootElement = doc.getDocumentElement();
			NodeList nodes = rootElement.getChildNodes();
			Node iNode = null;
			int thisID = 0;
			String[] aoi = new String[Item.mAllAOIs];
			String sfile;
			
			// each node is an item
			for(int i=0; i<nodes.getLength(); i++){
			  iNode = nodes.item(i);

			  if (iNode.getNodeType() == Node.ELEMENT_NODE) {
				  
				  Element eElement = (Element) iNode;
				  thisID = Integer.parseInt(eElement.getAttribute("id"));
				  aoi[0] = eElement.getElementsByTagName("aoi0").item(0).getTextContent();
				  aoi[1] = eElement.getElementsByTagName("aoi1").item(0).getTextContent();
				  aoi[2] = eElement.getElementsByTagName("aoi2").item(0).getTextContent();
				  aoi[3] = eElement.getElementsByTagName("aoi3").item(0).getTextContent();
				  sfile = eElement.getElementsByTagName("speech").item(0).getTextContent();
				  mItems.put(thisID, new Item(thisID,aoi[0],aoi[1],aoi[2],aoi[3],sfile));
			  } else {} // ignore
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Log.i(DEBUG_TAG, "read " + mItems.size() + " items");
	}
	
	@Override
	public void onDestroy() {
		if (mFile != null) {
			try {
				mFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {}
		super.onDestroy();
	}
}
