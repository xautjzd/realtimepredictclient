package edu.xautjzd.activityrecognition.predict;

import edu.xaut.jzdbishe.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ShowInfo extends Activity {

	private TextView username = null;
	private TextView password = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_info);
		
		username = (TextView)findViewById(R.id.username);
		password = (TextView)findViewById(R.id.password);
		
//		Intent intent = this.getIntent();
//		Bundle bundle = intent.getBundleExtra("info");
//		username.setText(bundle.getString("username"));
//		password.setText(bundle.getString("password"));
		username.setText("This is a test program!");
		password.setText("2015.2.3");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_info, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
