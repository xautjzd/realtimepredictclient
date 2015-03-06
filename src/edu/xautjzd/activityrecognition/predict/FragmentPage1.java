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
import edu.xautjzd.activityrecognition.predict.util.Acceleration;
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
	private ArrayList<Acceleration> accs = new ArrayList<Acceleration>();

	private PredictAttribute attribute; // 存储实时提取的特征属性

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
				/*PredictAttribute attribute = new PredictAttribute(5.1386863,
						-7.990457, -1.983885, 0.894269, 2.1234569, 1.785352145,
						-0.4950536, -0.52649176, 0.1778393798, -0.1501501,
						0.1145146, -1.737244787, 0.2548593687, -1.135281978,
						2.303285);*/
				if (attribute == null) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Log.e("ID3", e.getMessage());
						Thread.currentThread().interrupt();
					}
				}
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
		if (accs.size() == window_size) {
			attributeExtraction(accs);
			accs.clear();
		}
		Acceleration acc = new Acceleration(x, y, z);
		accs.add(acc);
	}

	// 实时提取一条记录用于实时识别
	private void attributeExtraction(ArrayList<Acceleration> accs) {
		/*getActions();
		if (actions == null || actions.size() != 4) {
			try {
				Thread.currentThread().wait();
			} catch (InterruptedException e) {
				Log.e("Actions", e.getMessage());
			}
		}*/

		// 提取三轴均值
		double x_sum = 0.0, y_sum = 0.0, z_sum = 0.0;
		for (Acceleration acc : accs) {
			x_sum += acc.getX();
			y_sum += acc.getY();
			z_sum += acc.getZ();
		}
		double x_average, y_average, z_average;
		x_average = x_sum / window_size;
		y_average = y_sum / window_size;
		z_average = z_sum / window_size;

		// 提取三轴方差与相关系数
		double x_deviation = 0.0, y_deviation = 0.0, z_deviation = 0.0;
		double l_xy = 0.0, l_yz = 0.0, l_xz = 0.0;
		for (Acceleration acc : accs) {
			x_deviation += Math.pow(acc.getX() - x_average, 2);
			y_deviation += Math.pow(acc.getY() - y_average, 2);
			z_deviation += Math.pow(acc.getZ() - z_average, 2);

			l_xy += (acc.getX() - x_average) * (acc.getY() - y_average);
			l_yz += (acc.getY() - y_average) * (acc.getZ() - z_average);
			l_xz += (acc.getX() - x_average) * (acc.getZ() - z_average);
		}
		// 方差
		x_deviation = x_deviation / window_size;
		y_deviation = y_deviation / window_size;
		z_deviation = z_deviation / window_size;
		// 相关系数
		double xy_correlation, yz_correlation, xz_correlation;
		xy_correlation = l_xy
				/ (Math.sqrt(x_deviation * y_deviation) * window_size);
		yz_correlation = l_yz
				/ (Math.sqrt(y_deviation * z_deviation) * window_size);
		xz_correlation = l_xz
				/ (Math.sqrt(x_deviation * z_deviation) * window_size);

		// 偏度
		double x_skewness = 0.0, y_skewness = 0.0, z_skewness = 0.0;
		for (Acceleration acc : accs) {
			x_skewness += Math.pow(acc.getX() - x_average, 3);
			y_skewness += Math.pow(acc.getY() - y_average, 3);
			z_skewness += Math.pow(acc.getZ() - z_average, 3);
		}
		x_skewness = x_skewness
				* window_size
				/ ((window_size - 1) * (window_size - 2) * Math.pow(
						Math.sqrt(x_deviation), 3));
		y_skewness = y_skewness
				* window_size
				/ ((window_size - 1) * (window_size - 2) * Math.pow(
						Math.sqrt(y_deviation), 3));
		z_skewness = z_skewness
				* window_size
				/ ((window_size - 1) * (window_size - 2) * Math.pow(
						Math.sqrt(z_deviation), 3));

		// 峰度
		double x_kurtosis = 0.0, y_kurtosis = 0.0, z_kurtosis = 0.0;
		for (Acceleration acc : accs) {
			x_kurtosis += Math.pow(acc.getX() - x_average, 4);
			y_kurtosis += Math.pow(acc.getY() - y_average, 4);
			z_kurtosis += Math.pow(acc.getZ() - z_average, 4);
		}
		x_kurtosis = (window_size * (window_size + 1) * x_kurtosis - 3
				* (window_size - 1) * Math.pow(x_deviation, 2)
				* Math.pow(window_size, 2))
				/ ((window_size - 1) * (window_size - 2) * (window_size - 3) * Math
						.pow(Math.sqrt(x_deviation), 4));
		y_kurtosis = (window_size * (window_size + 1) * y_kurtosis - 3
				* (window_size - 1) * Math.pow(y_deviation, 2)
				* Math.pow(window_size, 2))
				/ ((window_size - 1) * (window_size - 2) * (window_size - 3) * Math
						.pow(Math.sqrt(y_deviation), 4));
		z_kurtosis = (window_size * (window_size + 1) * z_kurtosis - 3
				* (window_size - 1) * Math.pow(z_deviation, 2)
				* Math.pow(window_size, 2))
				/ ((window_size - 1) * (window_size - 2) * (window_size - 3) * Math
						.pow(Math.sqrt(z_deviation), 4));
		
		attribute = new PredictAttribute();
		attribute.setX_Average(x_average);
		attribute.setY_Average(y_average);
		attribute.setZ_Average(z_average);
		attribute.setX_Deviation(x_deviation);
		attribute.setY_Deviation(y_deviation);
		attribute.setZ_Deviation(z_deviation);
		attribute.setXY_Correlation(xy_correlation);
		attribute.setYZ_Correlation(yz_correlation);
		attribute.setXZ_Correlation(xz_correlation);
		attribute.setX_Skewness(x_skewness);
		attribute.setY_Skewness(y_skewness);
		attribute.setZ_Skewness(z_skewness);
		attribute.setX_Kurtosis(x_kurtosis);
		attribute.setY_Kurtosis(y_kurtosis);
		attribute.setZ_Kurtosis(z_kurtosis);
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
						String json = EntityUtils
								.toString(response.getEntity());
						Gson gson = new Gson();
						actions = gson.fromJson(json,
								new TypeToken<List<String>>() {
								}.getType());
						Log.i("GetActions",
								EntityUtils.toString(response.getEntity()));
						notify(); // 当完成动作的获取后,唤醒主线程,进行下面特征的提取
					}
				} catch (Exception e) {
					Log.e("GetActions", e.getMessage());
				}
			}
		}.start();
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
