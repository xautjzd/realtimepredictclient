package edu.xautjzd.activityrecognition.predict;

import edu.xaut.jzdbishe.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentPage2 extends Fragment{

	private View view = null;
	private ImageView headshot = null;
	private TextView result = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.fragment_2, null);
		headshot = (ImageView)view.findViewById(R.id.headshot);
		headshot.setImageResource(R.drawable.photo);
		
		result = (TextView)view.findViewById(R.id.tv_state);
		ActivityRecognition activity = (ActivityRecognition)getActivity();
		if (activity.getResult() != null)
			result.setText("当前状态:" + activity.getResult());
		
		return view;		
	}	

}