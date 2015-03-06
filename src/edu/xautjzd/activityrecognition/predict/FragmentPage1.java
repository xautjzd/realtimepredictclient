package edu.xautjzd.activityrecognition.predict;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.xaut.jzdbishe.R;
import edu.xautjzd.activityrecognition.predict.util.PredictAttribute;

public class FragmentPage1 extends Fragment implements SensorEventListener {
	private View view = null;
	private Spinner spinner = null;
	// 获取Activity,以与Fragment2交互
	private ActivityRecognition activity = null;

	private SensorManager sensor = null;
	private Sensor accelerometer = null;
	
	// 存储训练集中所有的动作
	List<String> actions = null;

	// 存储三轴加速度值,以便特征提取
	private ArrayList<Double> accx = new ArrayList<Double>();
	private ArrayList<Double> accy = new ArrayList<Double>();
	private ArrayList<Double> accz = new ArrayList<Double>();

	// 特征提取的窗口大小
	private int window_size = 20;

	private ArrayList<SpinnerOption> algorithms; // 算法下拉框
	private Button btnClick = null; // 确定按钮

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_1, null);

		// 从系统服务中获取传感器管理器
		sensor = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);

		// 获取加速度传感器实例
		accelerometer = sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// 准备下拉框选项内容
		algorithms = new ArrayList<SpinnerOption>();
		SpinnerOption so = new SpinnerOption("支持向量机", "SVM");
		algorithms.add(so);
		so = new SpinnerOption("ID3", "ID3");
		algorithms.add(so);
		so = new SpinnerOption("朴素贝叶斯", "NBC");
		algorithms.add(so);

		// 填充下拉框内容
		spinner = (Spinner) view.findViewById(R.id.spinner);
		ArrayAdapter<SpinnerOption> adapter = new ArrayAdapter<SpinnerOption>(
				getActivity().getApplicationContext(),
				android.R.layout.simple_spinner_dropdown_item, algorithms);
		spinner.setAdapter(adapter);

		btnClick = (Button) view.findViewById(R.id.sure);
		btnClick.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String sp_value = ((SpinnerOption) spinner.getSelectedItem())
						.getValue();

				switch (sp_value) {
				case "SVM":
					break;
				case "ID3":
					ID3();
					break;
				case "NBC":
					break;
				default:
					break;
				}
				// btnClick.setText(sp_value);
			}
		});

		return view;
	}

	public void ID3() {
		// Android4.0以后访问网络的代码不能放置在主进程中
		new Thread() {
			@Override
			public void run() {
				PredictAttribute attribute = new PredictAttribute(5.1386863,
						-7.990457, -1.983885, 0.894269, 2.1234569, 1.785352145,
						-0.4950536, -0.52649176, 0.1778393798, -0.1501501,
						0.1145146, -1.737244787, 0.2548593687, -1.135281978,
						2.303285);
				try {
					HttpClient httpclient = new DefaultHttpClient();
					String requestURL = "http://202.200.119.163:8080/realtimepredictserver/DecisionTreeServlet";

					HttpPost httppost = new HttpPost(requestURL);
					httppost.setHeader("Content-Type",
							"application/json;charset=UTF-8");

					Gson gson = new Gson();
					StringEntity params = new StringEntity(
							gson.toJson(attribute), "UTF-8");
					httppost.setEntity(params);

					HttpResponse response = httpclient.execute(httppost);
					if (response.getEntity() != null) {
						String result = EntityUtils.toString(
								response.getEntity()).trim();
						activity = (ActivityRecognition) getActivity();
						activity.setResult(result);

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
				} catch (Exception e) {
					Log.e("ID3", e.getMessage());
				}
			}
		}.start();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		double x = event.values[0];
		double y = event.values[1];
		double z = event.values[2];

		// 获取指定窗口大小数据后,开始进行特征提取
		if (accx.size() == window_size) {
			attributeExtraction(accx, accy, accz);
			accx.clear();
			accy.clear();
			accz.clear();
		}

		accx.add(x);
		accy.add(y);
		accz.add(z);
	}

	// 获取数据库训练集中所有动作的类型
	public void getActions() {
		// Android4.0以后访问网络的代码不能放置在主进程中
		new Thread() {
			@Override
			public void run() {
				HttpClient httpclient = new DefaultHttpClient();
				String requestURL = "http://202.200.119.163:8080/realtimepredictserver/getActions";

				HttpGet httpget = new HttpGet(requestURL);
				try {
					HttpResponse response = httpclient.execute(httpget);
					if (response.getEntity() != null) {
						// 反序列化传过来的动作列表
						String json = EntityUtils.toString(response.getEntity());
						Gson gson = new Gson();
						actions = gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
						Log.i("GetActions", EntityUtils.toString(response.getEntity()));
					}
				} catch (Exception e) {
					Log.e("GetActions", e.getMessage());
				}
			}
		}.start();
	}

	// 实时提取一条记录用于实时识别
	private void attributeExtraction(ArrayList<Double> accx,
			ArrayList<Double> accy, ArrayList<Double> accz) {
		getActions();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i("TAG", "---onActivityCreated");
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		Log.i("TAG", "---onAttach");
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("TAG", "---onCreate");
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		Log.i("TAG", "---onDestroy");
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onPause() {
		Log.i("TAG", "---onPause");
		// TODO Auto-generated method stub
		super.onPause();
		sensor.unregisterListener(this);
	}

	@Override
	public void onResume() {
		Log.i("TAG", "---onResume");
		super.onResume();
		// 将Sensor实例与SensorEventListener实例相互绑定，50000microseconds采样一次。
		sensor.registerListener(this, accelerometer, 50000);
	}
}
