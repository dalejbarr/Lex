package uk.ac.gla.psy.talklab.lex;

import uk.ac.gla.psy.talklab.lex.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class DashboardActivity extends Activity {

	public static final String DEBUG_MESSAGE = "Dash";
	public static final String PREFS_NAME = "MyPrefsFile";
	public static int mSessionID = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_layout);
	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    mSessionID = settings.getInt("SessionID", 0) + 1;
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	    Button b1 = (Button) findViewById(R.id.session_button);
	    b1.setText(String.format("Start Session %d", mSessionID));
	}
	
	@Override
	public void onResume() {
		if (MainActivity.mSessionCompleted) {
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt("SessionID", mSessionID);
			editor.commit();			
			mSessionID = mSessionID + 1;
			MainActivity.mSessionCompleted = false;
		} else {}
		super.onResume();
	}
	
	public void startExperiment(View v) {
		Intent intent = new Intent(this, MainActivity.class);
		Log.i(DEBUG_MESSAGE, "button clicked");
		startActivity(intent);
		//EditText editText = (EditText) findViewById(R.id.edit_message);
		//String message = editText.getText().toString();
		//intent.putExtra(EXTRA_MESSAGE, message);
	}
}
