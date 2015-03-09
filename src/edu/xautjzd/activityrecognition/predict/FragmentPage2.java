package edu.xautjzd.activityrecognition.predict;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.xaut.jzdbishe.R;
import edu.xautjzd.activityrecognition.predict.util.Acceleration;
import edu.xautjzd.activityrecognition.predict.util.PredictAttribute;

public class FragmentPage2 extends Fragment implements SensorEventListener {

	private View view = null;
	private ImageView headshot = null;
	private TextView action = null; // 识别动作

	private TextView x_avg = null; // 均值
	private TextView y_avg = null;
	private TextView z_avg = null;

	private TextView x_deviation = null; // 方差
	private TextView y_deviation = null;
	private TextView z_deviation = null;

	private TextView xy_correlation = null; // 相关系数
	private TextView yz_correlation = null;
	private TextView xz_correlation = null;

	private TextView x_skewness = null; // 偏度
	private TextView y_skewness = null;
	private TextView z_skewness = null;

	private TextView x_kurto = null; // 峰度
	private TextView y_kurto = null;
	private TextView z_kurto = null;

	private String serverurl = "http://202.200.119.163:8080";

	// 获取手机内置三轴加速度传感器数据
	private SensorManager sensor = null;
	private Sensor accelerometer = null;

	// 存储训练集中所有的动作
	List<String> actions = null;

	// 存储三轴加速度值,以便特征提取
	private ArrayList<Acceleration> accs = new ArrayList<Acceleration>();

	private PredictAttribute attribute; // 存储实时提取的特征属性

	// 特征提取的窗口大小
	private int window_size = 20;

	private Timer timer;

	private Handler myHanler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			action.setText(msg.obj.toString()); // 更新识别动作结果
			
			x_avg.setText(msg.getData().getString("x_avg"));
			y_avg.setText(msg.getData().getString("y_avg"));
			z_avg.setText(msg.getData().getString("z_avg"));
			
			x_deviation.setText(msg.getData().getString("x_deviation"));
			y_deviation.setText(msg.getData().getString("y_deviation"));
			z_deviation.setText(msg.getData().getString("z_deviation"));
			
			xy_correlation.setText(msg.getData().getString("xy_correlation"));
			yz_correlation.setText(msg.getData().getString("yz_correlation"));
			xz_correlation.setText(msg.getData().getString("xz_correlation"));
			
			x_skewness.setText(msg.getData().getString("x_skewness"));
			y_skewness.setText(msg.getData().getString("y_skewness"));
			z_skewness.setText(msg.getData().getString("z_skewness"));
			
			x_kurto.setText(msg.getData().getString("x_kurto"));
			y_kurto.setText(msg.getData().getString("y_kurto"));
			z_kurto.setText(msg.getData().getString("z_kurto"));
			
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_2, null);
		headshot = (ImageView) view.findViewById(R.id.headshot);
		headshot.setImageResource(R.drawable.photo);

		// 从系统服务中获取传感器管理器
		sensor = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		// 获取加速度传感器实例
		accelerometer = sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		action = (TextView) view.findViewById(R.id.tv_state);
		x_avg = (TextView) view.findViewById(R.id.x_average);
		y_avg = (TextView) view.findViewById(R.id.y_average);
		z_avg = (TextView) view.findViewById(R.id.z_average);

		x_deviation = (TextView) view.findViewById(R.id.x_deviation);
		y_deviation = (TextView) view.findViewById(R.id.y_deviation);
		z_deviation = (TextView) view.findViewById(R.id.z_deviation);

		xy_correlation = (TextView) view.findViewById(R.id.xy_correlation);
		yz_correlation = (TextView) view.findViewById(R.id.yz_correlation);
		xz_correlation = (TextView) view.findViewById(R.id.xz_correlation);

		x_skewness = (TextView) view.findViewById(R.id.x_skewness);
		y_skewness = (TextView) view.findViewById(R.id.y_skewness);
		z_skewness = (TextView) view.findViewById(R.id.z_skewness);

		x_kurto = (TextView) view.findViewById(R.id.x_kurto);
		y_kurto = (TextView) view.findViewById(R.id.y_kurto);
		z_kurto = (TextView) view.findViewById(R.id.z_kurto);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		final ActivityRecognition activity = (ActivityRecognition) getActivity();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				switch (activity.getAlgorithm()) {
				case "SVM":
					SVM();
					Log.i("SVM", "SVM algorithm called!");
					break;
				case "ID3":
					ID3();
					Log.i("ID3", "ID3 algorithm called!");
					break;
				default:
					SVM();
					break;
				}
			}
		};

		timer = new Timer();
		timer.schedule(task, 2 * 1000, 2 * 1000); // 2s发送一次识别请求
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (null != timer) {
			timer.cancel();
			timer = null;
		}
	}

	public void SVM() {
		String requestURL = serverurl + "/realtimepredictserver/svm";
		predict(requestURL);
	}

	public void ID3() {
		String requestURL = serverurl + "/realtimepredictserver/decisiontree";
		predict(requestURL);
	}

	public void predict(String requestURL) {
		/*
		 * PredictAttribute attribute = new PredictAttribute(5.1386863,
		 * -7.990457, -1.983885, 0.894269, 2.1234569, 1.785352145, -0.4950536,
		 * -0.52649176, 0.1778393798, -0.1501501, 0.1145146, -1.737244787,
		 * 0.2548593687, -1.135281978, 2.303285);
		 */
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(requestURL);
			httppost.setHeader("Content-Type", "application/json;charset=UTF-8");

			Gson gson = new Gson();
			StringEntity params = new StringEntity(gson.toJson(attribute),
					"UTF-8");
			httppost.setEntity(params);

			HttpResponse response = httpclient.execute(httppost);
			if (response.getEntity() != null) {

				String result = EntityUtils.toString(response.getEntity())
						.trim();

				Message msg = myHanler.obtainMessage();
				// 传递特征属性
				Bundle bundle = new Bundle();
				bundle.putString("x_avg",
						Double.toString(attribute.getX_Average()));
				bundle.putString("y_avg",
						Double.toString(attribute.getY_Average()));
				bundle.putString("z_avg",
						Double.toString(attribute.getZ_Average()));
				bundle.putString("x_deviation",
						Double.toString(attribute.getX_Deviation()));
				bundle.putString("y_deviation",
						Double.toString(attribute.getY_Deviation()));
				bundle.putString("z_deviation",
						Double.toString(attribute.getZ_Deviation()));
				bundle.putString("xy_correlation",
						Double.toString(attribute.getXY_Correlation()));
				bundle.putString("yz_correlation",
						Double.toString(attribute.getYZ_Correlation()));
				bundle.putString("xz_correlation",
						Double.toString(attribute.getXZ_Correlation()));
				bundle.putString("x_skewness",
						Double.toString(attribute.getX_Skewness()));
				bundle.putString("y_skewness",
						Double.toString(attribute.getY_Skewness()));
				bundle.putString("z_skewness",
						Double.toString(attribute.getZ_Skewness()));
				bundle.putString("x_kurto",
						Double.toString(attribute.getX_Kurtosis()));
				bundle.putString("y_kurto",
						Double.toString(attribute.getY_Kurtosis()));
				bundle.putString("z_kurto",
						Double.toString(attribute.getZ_Kurtosis()));
				// 传递识别结果
				msg.setData(bundle);
				msg.obj = result;
				myHanler.sendMessage(msg);
			}
		} catch (Exception e) {
			Log.e("Recognition Algorithm", e.getMessage());
		}
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
				String requestURL = serverurl
						+ "/realtimepredictserver/getActions";

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
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i("TAG", "---onActivityCreated");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		Log.i("TAG", "---onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("TAG", "---onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		Log.i("TAG", "---onDestroy");
		super.onDestroy();
	}

	@Override
	public void onPause() {
		Log.i("TAG", "---onPause");
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