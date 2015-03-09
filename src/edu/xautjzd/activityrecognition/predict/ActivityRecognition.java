package edu.xautjzd.activityrecognition.predict;

import edu.xaut.jzdbishe.R;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ActivityRecognition extends FragmentActivity implements OnClickListener{
	protected LinearLayout ll_algorithm = null;
	protected LinearLayout ll_result = null;
	protected LinearLayout ll_setting = null;
	protected ImageView image_algorithm = null;
	protected ImageView image_result = null;
	protected ImageView image_setting = null;
	//Fragment管理器
	protected FragmentManager fm = this.getSupportFragmentManager();
	protected FragmentTransaction ft = null;
	protected FragmentPage1 fragmentPage1 = null;
	protected FragmentPage2 fragmentPage2 = null;
	protected FragmentPage3 fragmentPage3 = null;
	
	public String algorithm = "SVM";  // 存储选择的算法, 默认为SVM
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity_recognition);
		
		initView();
		//开始事务（每次改变Fragment管理器之后都要提交）
		ft = fm.beginTransaction();
		algorithm();
		//提交事务
		ft.commit();
	}

	private void initView(){
		ll_algorithm = (LinearLayout)findViewById(R.id.ll_algorithm);
		ll_result = (LinearLayout)findViewById(R.id.ll_result);
		ll_setting = (LinearLayout)findViewById(R.id.ll_setting);
		
		
		image_algorithm = (ImageView)findViewById(R.id.image_algorithm);
		image_result = (ImageView)findViewById(R.id.image_result);
		image_setting = (ImageView)findViewById(R.id.image_setting);
		
		
		ll_algorithm.setOnClickListener(this);
		ll_result.setOnClickListener(this);
		ll_setting.setOnClickListener(this);
		
		ll_algorithm.setSelected(true);
		image_algorithm.setSelected(true);
		
	}
	
	@Override
	public void onClick(View v) {
		//每次点击时都需要重新开始事务
		ft = fm.beginTransaction();
		//把显示的Fragment隐藏
		setSelected();
		switch (v.getId()) {
		case R.id.ll_algorithm:
			ll_algorithm.setSelected(true);
			image_algorithm.setSelected(true);
			algorithm();
			break;
		case R.id.ll_result:
			ll_result.setSelected(true);
			image_result.setSelected(true);
			result();
			
			break;
		case R.id.ll_setting:
			ll_setting.setSelected(true);
			image_setting.setSelected(true);
			setting();
			break;
		}
		ft.commit();
	}
	
	public String getAlgorithm() {
		return algorithm;
	}
	
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	
	public void setSelected(){
		ll_algorithm.setSelected(false);
		ll_result.setSelected(false);
		ll_setting.setSelected(false);
		
		image_algorithm.setSelected(false);
		image_result.setSelected(false);
		image_setting.setSelected(false);
		
		if(fragmentPage1 != null){
			//隐藏Fragment
			ft.hide(fragmentPage1);
		}
		if(fragmentPage2 != null){
			ft.hide(fragmentPage2);
		}
		if(fragmentPage3 != null){
			ft.hide(fragmentPage3);
		}

	}

	private void algorithm(){
		if(fragmentPage1 == null){
			fragmentPage1 = new FragmentPage1();
			/*添加到Fragment管理器中
			这里如果用replace，
			当每次调用时都会把前一个Fragment给干掉，
			这样就导致了每一次都要创建、销毁，
			数据就很难保存，用add就不存在这样的问题了，
			当Fragment存在时候就让它显示，不存在时就创建，
			这样的话数据就不需要自己保存了，
			因为第一次创建的时候就已经保存了，
			只要不销毁一直都将存在*/
			ft.add(R.id.fl_content, fragmentPage1);
		}else{
			//显示Fragment
			ft.show(fragmentPage1);
		}
	}
	
	public void result(){
		if(fragmentPage2 == null){
			fragmentPage2 = new FragmentPage2();
			ft.add(R.id.fl_content, fragmentPage2);
		}else{
			ft.show(fragmentPage2);
		}
	}
	private void setting(){
		if(fragmentPage3 == null){
			fragmentPage3 = new FragmentPage3();
			ft.add(R.id.fl_content, fragmentPage3);
		}else{
			ft.show(fragmentPage3);
		}
	}
	
}
