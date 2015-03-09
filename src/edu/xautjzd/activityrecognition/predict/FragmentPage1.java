package edu.xautjzd.activityrecognition.predict;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import edu.xaut.jzdbishe.R;

public class FragmentPage1 extends Fragment {
	private View view = null;
	private Spinner spinner = null;

	// 获取Activity,以与Fragment2交互
	private ActivityRecognition activity = null;

	private ArrayList<SpinnerOption> algorithms; // 算法下拉框
	private Button btnClick = null; // 确定按钮

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_1, null);

		// 准备下拉框选项内容
		algorithms = new ArrayList<SpinnerOption>();
		SpinnerOption so = new SpinnerOption("支持向量机", "SVM");
		algorithms.add(so);
		so = new SpinnerOption("决策树", "ID3");
		algorithms.add(so);
		so = new SpinnerOption("朴素贝叶斯", "NBC");
		algorithms.add(so);

		// 填充下拉框内容
		spinner = (Spinner) view.findViewById(R.id.spinner);
		ArrayAdapter<SpinnerOption> adapter = new ArrayAdapter<SpinnerOption>(
				getActivity().getApplicationContext(),
				android.R.layout.simple_spinner_dropdown_item, algorithms);
		spinner.setAdapter(adapter);
		
		activity = (ActivityRecognition) getActivity();

		btnClick = (Button) view.findViewById(R.id.sure);
		btnClick.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String sp_value = ((SpinnerOption) spinner.getSelectedItem())
						.getValue();

				switch (sp_value) {
				case "SVM":
					activity.setAlgorithm("SVM");
					toFramgment2();
					break;
				case "ID3":
					activity.setAlgorithm("ID3");
					toFramgment2();
					break;
				case "NBC":
					break;
				default:
					break;
				}
			}
		});
		return view;
	}
	
	// 切换到Fragment2
	public void toFramgment2() {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// This code will always run on the UI thread,
				// therefore is safe to modify UI elements.
				activity.setSelected();
				activity.ll_result.setSelected(true);
				activity.image_result.setSelected(true);

				activity.ft = getFragmentManager()
						.beginTransaction();
				if (activity.fragmentPage2 == null) {
					activity.fragmentPage2 = new FragmentPage2();
					activity.ft.add(R.id.fl_content,
							activity.fragmentPage2);
				} else {
					activity.ft.show(activity.fragmentPage2);
				}
				activity.ft.commit();
			}
		});
	}
}
