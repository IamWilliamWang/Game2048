package com.william.game2048;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.util.ArrayList;

public class MainActivity extends Activity {

	public MainActivity() {
		mainActivity = this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		/*
		 * 游戏的模式选择
		 */
		Switch gameModeSwitch = (Switch) findViewById(R.id.modeSwitch);
		gameModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				System.out.println(isChecked);//检查按钮状态
				gameNeverWin = isChecked;
			}
		});
		gameModeSwitch.setButtonDrawable(R.drawable.ic_launcher);
		/*
		 * 作者介绍栏
		 */
		findViewById(R.id.btngameInfo).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://baike.baidu.com/item/2048%E6%B8%B8%E6%88%8F/15605189")));
			}
		});
		/*
		 * 撤销
		 */
		findViewById(R.id.btnUndo).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(score<26000) {
					Toast toast = Toast.makeText(getApplicationContext(), "看你态度诚恳，让你一次，下不为例。",Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER,0,0);
					toast.show();

					saveScore(lastScore);
					gameView.resetCards();
					showScore();
				}
				else {
					new AlertDialog.Builder(getMainActivity()).
							setTitle("方了没？").
							setMessage("程序猿掐指一算，此时不宜撤销！").
							setIcon(R.drawable.ic_launcher).
							create().
							show();
				}
			}
		});

		root = (LinearLayout) findViewById(R.id.container);
		root.setBackgroundColor(0xfffaf8ef);

		tvScore = (TextView) findViewById(R.id.tvScore);
		tvBestScore = (TextView) findViewById(R.id.tvBestScore);

		gameView = (GameView) findViewById(R.id.gameView);

		btnNewGame = (Button) findViewById(R.id.btnNewGame);
		
		animLayer = (AnimLayer) findViewById(R.id.animLayer);

		/*
		 * 重新开始
		 */
		btnNewGame.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gameView.restartGame();
			}
		});

		mTencent = Tencent.createInstance("1105183766", getApplicationContext());
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
		menu.add(0, itemShareToQQZone,0,"分享到QQ空间");
		menu.add(0, itemSettings,0,"→高级设定←");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case itemShareToQQZone:
				doshareToQzone();
				break;
			case itemSettings:
				Toast.makeText(getApplicationContext(), "尽请期待 - ( ゜- ゜)つロ  乾杯~", Toast.LENGTH_SHORT).show();
				break;
		}
		return true;
//		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		gameView.saveCards();
	}
	/* qq空间分享函数 */
	private void doshareToQzone () {
		final Activity activity = this;

		final Bundle params = new Bundle();
		params.putString(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, String.valueOf(QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT));
		params.putString(QzoneShare.SHARE_TO_QQ_TITLE, "谁敢来战我的2048！");
		params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, "我在2048游戏里成功的玩到了" + score + "分，厉害吧！谁敢来战！");
		params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, "http://zhushou.360.cn/detail/index/soft_id/3224288");
		params.putString(QzoneShare.SHARE_TO_QQ_IMAGE_URL, "http://p19.qhimg.com/t013e7394393c4f9b88.png");
		params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, new ArrayList<String>());

		new Thread(new Runnable() {
			@Override
			public void run() {
				mTencent.shareToQzone(activity, params, myListener);
			}
		}).run();
	}
	/* 分享监听器 */
	private class ShareListener implements IUiListener {

		@Override
		public void onComplete(Object arg0) {
			Toast.makeText(getApplicationContext(), "分享成功", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onCancel() {
			Toast.makeText(getApplicationContext(), "分享取消", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onError(UiError uiError) {
			Toast.makeText(getApplicationContext(), "分享出错", Toast.LENGTH_LONG).show();
		}
	}
	/* 分享回调 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ShareListener myListener = new ShareListener();
		Tencent.onActivityResultData(requestCode, resultCode, data, myListener);
	}
	/* 清除分数 */
	public void clearScore() {
		score = 0;
		showScore();
	}
	/* 加分 */
	public void addScore(int s){
		score+=s;
		saveScore(score);
		showScore();
		int maxScore = Math.max(score, getBestScoreData());
		saveBestScore(maxScore);
		showBestScore(maxScore);
	}
	/* 储存分数 */
	public void saveScore(int s){
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		Editor e = preferences.edit();
		e.putInt(SP_KEY_SCORE, s);
		e.commit();
	}
	/* 储存最高分 */
	public void saveBestScore(int s){
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		Editor e = preferences.edit();
		e.putInt(SP_KEY_BEST_SCORE, s);
		e.commit();
	}
	/* 获取分数数据 */
	public int getScoreData(){
		return getPreferences(MODE_PRIVATE).getInt(SP_KEY_SCORE, 0);
	}
	/* 获取最高分数据 */
	public int getBestScoreData(){
		return getPreferences(MODE_PRIVATE).getInt(SP_KEY_BEST_SCORE, 0);
	}
	/* getter */
	public int getScore() {
		return score;
	}
	/* setter */
	public void setScore(int s){
		score = s;
	}
	/* getter */
	public boolean getShowTips() {
		return showTips;
	}
	/* setter */
	public void setShowTips(boolean showTips) {
		this.showTips = showTips;
	}
	/* 保存上一次动作的分数 */
	public void setLastScore(){
		lastScore = score;
	}
	/* 分数栏的显示 */
	public void showScore(){
		tvScore.setText(score + "");
	}
	/* 最高分栏的显示 */
	public void showBestScore(int s){
		tvBestScore.setText(s+"");
	}
	/* getter */
	public AnimLayer getAnimLayer() {
		return animLayer;
	}
	/* optionMenu按钮ID */
	private final int itemShareToQQZone = 1;//QQ空间
	private final int itemSettings = 2;//高级设定

	private int score = 0;//分数
	boolean gameNeverWin = false;//无限模式
	private TextView tvScore,tvBestScore;//分数的两个TextView
	private LinearLayout root = null;
	private Button btnNewGame;//重新开始按钮
	private GameView gameView;//主游戏的View
	private AnimLayer animLayer = null;//animLayer
	private int lastScore;//上一次操作的分数

	private boolean showTips = true;//好评广告

	// 这两行是Tencent SDK里给的，以作分享
	public static Tencent mTencent;
	private ShareListener myListener = new ShareListener();
 	/* 主类 */
	private static MainActivity mainActivity = null;
	/* GameView类获取此最高权限之用 */
	public static MainActivity getMainActivity() {
		return mainActivity;
	}
	/* Preference存储里的参数键 */
	public static final String SP_KEY_SCORE = "Score";
	public static final String SP_KEY_BEST_SCORE = "bestScore";
}
