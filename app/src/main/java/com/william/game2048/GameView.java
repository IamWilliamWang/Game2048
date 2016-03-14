package com.william.game2048;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class GameView extends LinearLayout {

	public GameView(Context context) {
		super(context);

		initGameView();
	}

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initGameView();
	}

	private void initGameView(){
		setOrientation(LinearLayout.VERTICAL);
		setBackgroundColor(0xffbbada0);


		setOnTouchListener(new View.OnTouchListener() {

			private float startX, startY, offsetX, offsetY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						startX = event.getX();
						startY = event.getY();
						break;
					case MotionEvent.ACTION_UP:
						offsetX = event.getX() - startX;
						offsetY = event.getY() - startY;


						if (Math.abs(offsetX) > Math.abs(offsetY)) {
							if (offsetX < -5) {
								swipeLeft();
							} else if (offsetX > 5) {
								swipeRight();
							}
						} else {
							if (offsetY < -5) {
								swipeUp();
							} else if (offsetY > 5) {
								swipeDown();
							}
						}

						break;
				}
				return true;
			}
		});
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		Config.CARD_WIDTH = (Math.min(w, h)-10)/Config.LINES;

		addCards(Config.CARD_WIDTH, Config.CARD_WIDTH);

		startGame();
	}

	private void addCards(int cardWidth,int cardHeight){

		Card c;

		LinearLayout line;
		LinearLayout.LayoutParams lineLp;
		
		for (int y = 0; y < Config.LINES; y++) {
			line = new LinearLayout(getContext());
			lineLp = new LinearLayout.LayoutParams(-1, cardHeight);
			addView(line, lineLp);
			
			for (int x = 0; x < Config.LINES; x++) {
				c = new Card(getContext());
				line.addView(c, cardWidth, cardHeight);

				cardsMap[x][y] = c;
			}
		}
	}

	public void startGame(){
		/*检查有没有存档*/
		FileInputStream checkFileExist;
		try {
			checkFileExist = MainActivity.getMainActivity().openFileInput(saveFileName);
			checkFileExist.close();
			recoverCards();
			MainActivity.getMainActivity().showScore();
			MainActivity.getMainActivity().showBestScore(MainActivity.getMainActivity().getBestScoreData());
		} catch (Exception e) {
			e.printStackTrace();

			MainActivity aty = MainActivity.getMainActivity();
			aty.clearScore();
			aty.showBestScore(aty.getBestScoreData());

			restartGame();
		}
	}

	public void restartGame(){
		clearDataBase();

		MainActivity aty = MainActivity.getMainActivity();
		aty.clearScore();
		aty.showBestScore(aty.getBestScoreData());

		for (int y = 0; y < Config.LINES; y++) {
			for (int x = 0; x < Config.LINES; x++) {
				cardsMap[x][y].setNum(0);
			}
		}

		addRandomNum();
		addRandomNum();
	}

	private void clearDataBase() {
		MainActivity.getMainActivity().clearScore();
		MainActivity.getMainActivity().saveScore(0);
		try {
			FileOutputStream outputCardsStream =
					MainActivity.getMainActivity().openFileOutput(saveFileName, Context.MODE_PRIVATE);
			DataOutputStream dos = new DataOutputStream(outputCardsStream);

			dos.writeInt(2);
			for(int i=1; i<Config.LINES*Config.LINES; i++) {
					dos.writeInt(0);
			}
			dos.flush();
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void swipeLeft(){
		saveCards();
		MainActivity.getMainActivity().setLastScore();

		boolean merge = false;

		for (int y = 0; y < Config.LINES; y++) {
			for (int x = 0; x < Config.LINES; x++) {

				for (int x1 = x+1; x1 < Config.LINES; x1++) {
					if (cardsMap[x1][y].getNum()>0) {

						if (cardsMap[x][y].getNum()<=0) {

							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x1][y],cardsMap[x][y], x1, x, y, y);

							cardsMap[x][y].setNum(cardsMap[x1][y].getNum());
							cardsMap[x1][y].setNum(0);

							x--;
							merge = true;

						}else if (cardsMap[x][y].equals(cardsMap[x1][y])) {
							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x1][y], cardsMap[x][y],x1, x, y, y);
							cardsMap[x][y].setNum(cardsMap[x][y].getNum()*2);
							cardsMap[x1][y].setNum(0);

							MainActivity.getMainActivity().addScore(cardsMap[x][y].getNum());
							merge = true;
						}

						break;
					}
				}
			}
		}

		if (merge) {
			addRandomNum();
			checkComplete();
			MainActivity.getMainActivity().showTips();
		}
	}
	private void swipeRight(){
		saveCards();
		MainActivity.getMainActivity().setLastScore();

		boolean merge = false;

		for (int y = 0; y < Config.LINES; y++) {
			for (int x = Config.LINES-1; x >=0; x--) {

				for (int x1 = x-1; x1 >=0; x1--) {
					if (cardsMap[x1][y].getNum()>0) {

						if (cardsMap[x][y].getNum()<=0) {
							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x1][y], cardsMap[x][y],x1, x, y, y);
							cardsMap[x][y].setNum(cardsMap[x1][y].getNum());
							cardsMap[x1][y].setNum(0);

							x++;
							merge = true;
						}else if (cardsMap[x][y].equals(cardsMap[x1][y])) {
							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x1][y], cardsMap[x][y],x1, x, y, y);
							cardsMap[x][y].setNum(cardsMap[x][y].getNum()*2);
							cardsMap[x1][y].setNum(0);
							MainActivity.getMainActivity().addScore(cardsMap[x][y].getNum());
							merge = true;
						}

						break;
					}
				}
			}
		}

		if (merge) {
			addRandomNum();
			checkComplete();
			MainActivity.getMainActivity().showTips();
		}
	}
	private void swipeUp(){
		saveCards();
		MainActivity.getMainActivity().setLastScore();

		boolean merge = false;

		for (int x = 0; x < Config.LINES; x++) {
			for (int y = 0; y < Config.LINES; y++) {

				for (int y1 = y+1; y1 < Config.LINES; y1++) {
					if (cardsMap[x][y1].getNum()>0) {

						if (cardsMap[x][y].getNum()<=0) {
							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x][y1],cardsMap[x][y], x, x, y1, y);
							cardsMap[x][y].setNum(cardsMap[x][y1].getNum());
							cardsMap[x][y1].setNum(0);

							y--;

							merge = true;
						}else if (cardsMap[x][y].equals(cardsMap[x][y1])) {
							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x][y1],cardsMap[x][y], x, x, y1, y);
							cardsMap[x][y].setNum(cardsMap[x][y].getNum()*2);
							cardsMap[x][y1].setNum(0);
							MainActivity.getMainActivity().addScore(cardsMap[x][y].getNum());
							merge = true;
						}

						break;

					}
				}
			}
		}

		if (merge) {
			addRandomNum();
			checkComplete();
			MainActivity.getMainActivity().showTips();
		}
	}
	private void swipeDown(){
		saveCards();
		MainActivity.getMainActivity().setLastScore();

		boolean merge = false;

		for (int x = 0; x < Config.LINES; x++) {
			for (int y = Config.LINES-1; y >=0; y--) {

				for (int y1 = y-1; y1 >=0; y1--) {
					if (cardsMap[x][y1].getNum()>0) {

						if (cardsMap[x][y].getNum()<=0) {
							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x][y1],cardsMap[x][y], x, x, y1, y);
							cardsMap[x][y].setNum(cardsMap[x][y1].getNum());
							cardsMap[x][y1].setNum(0);

							y++;
							merge = true;
						}else if (cardsMap[x][y].equals(cardsMap[x][y1])) {
							MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardsMap[x][y1],cardsMap[x][y], x, x, y1, y);
							cardsMap[x][y].setNum(cardsMap[x][y].getNum()*2);
							cardsMap[x][y1].setNum(0);
							MainActivity.getMainActivity().addScore(cardsMap[x][y].getNum());
							merge = true;
						}

						break;
					}
				}
			}
		}

		if (merge) {
			addRandomNum();
			checkComplete();
			MainActivity.getMainActivity().showTips();
		}
	}

	private void addRandomNum(){

		emptyPoints.clear();

		for (int y = 0; y < Config.LINES; y++) {
			for (int x = 0; x < Config.LINES; x++) {
				if (cardsMap[x][y].getNum()<=0) {
					emptyPoints.add(new Point(x, y));
				}
			}
		}

		if (emptyPoints.size()>0) {

			Point p = emptyPoints.remove((int)(Math.random()*emptyPoints.size()));
			cardsMap[p.x][p.y].setNum(Math.random() > 0.1 ? 2 : 4);

			MainActivity.getMainActivity().getAnimLayer().createScaleTo1(cardsMap[p.x][p.y]);
		}
	}

	private void checkComplete(){

		complete = true;
		win = false;
		OUT:
			for (int y = 0; y < Config.LINES; y++) {
				for (int x = 0; x < Config.LINES; x++) {
					if (cardsMap[x][y].getNum()==0||//判断没有死
							(x>0&&cardsMap[x][y].equals(cardsMap[x-1][y]))||
							(x<Config.LINES-1&&cardsMap[x][y].equals(cardsMap[x+1][y]))||
							(y>0&&cardsMap[x][y].equals(cardsMap[x][y-1]))||
							(y<Config.LINES-1&&cardsMap[x][y].equals(cardsMap[x][y+1]))) {

						complete = false;
						break OUT;
					}
				}
			}

		WIN:
			for (int y = 0; y < Config.LINES; y++) {
				for (int x = 0; x < Config.LINES; x++) {
					if (cardsMap[x][y].getNum() >= 2048) {//判断赢了
						win = true;
						break WIN;
					}
				}
			}

		if (complete==true && win==false) {
			new AlertDialog.Builder(getContext())
					.setTitle("对不起")
					.setMessage("游戏结束")
					.setPositiveButton("重新开始", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							restartGame();
						}
					})
					.setNegativeButton("分享至QQ空间", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainActivity.getMainActivity().doshareToQzone();
						}
					})
					.show();
		}

		if(win==true && MainActivity.getMainActivity().gameNeverWin == false) {
			new AlertDialog.Builder(getContext())
					.setTitle("恭喜")
					.setMessage("你赢了！")
					.setIcon(R.drawable.ic_launcher)
					.setPositiveButton("重新开始", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							restartGame();
						}
					})
					.setNegativeButton("分享至QQ空间", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainActivity.getMainActivity().doshareToQzone();
						}
					})
					.show();
		}
	}

	public void saveCards(){
		try {
			FileOutputStream outputCardsStream =
					MainActivity.getMainActivity().openFileOutput(saveFileName, Context.MODE_PRIVATE);
			DataOutputStream dos = new DataOutputStream(outputCardsStream);

			for(Card[] cardRow : cardsMap){
				for(Card cardColumn : cardRow){
					dos.writeInt(cardColumn.getNum());
					System.out.printf("%4d", cardColumn.getNum());
				}
				System.out.println();
			}
			dos.flush();
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
			showException("游戏存档失败。");
		}
	}

	public void recoverCards() {
		/*加载分数*/
		MainActivity aty = MainActivity.getMainActivity();
		aty.setScore(aty.getScoreData());

		/*
		 * 画方块
		 */
		AllPoints.clear();

		for (int y = 0; y < Config.LINES; y++) {
			for (int x = 0; x < Config.LINES; x++) {
				AllPoints.add(new Point(x, y));
			}
		}

		if (AllPoints.size() > 0) {

			try {
				FileInputStream inputCardsStream =
						MainActivity.getMainActivity().openFileInput(saveFileName);
				DataInputStream dis = new DataInputStream(inputCardsStream);

				int[][] cardMapNumber = new int[Config.LINES][Config.LINES];
				for (int i = 0; i < Config.LINES; i++)
					for (int j = 0; j < Config.LINES; j++) {
						cardMapNumber[i][j] = dis.readInt();
						cardsMap[i][j].setNum(cardMapNumber[i][j]);
					}
				dis.close();

			} catch (IOException e) {
				showException("存档读取失败。");
				e.printStackTrace();
			}

			for (int y = 0; y < Config.LINES; y++) {
				for (int x = 0; x < Config.LINES; x++) {
					MainActivity.getMainActivity().getAnimLayer().createScaleTo1(cardsMap[x][y]);
				}
			}
		}
	}

	/**
	 * 展示错误警告框
	 */
	public void showException(String ExceptionString){
		Toast toast = Toast.makeText(MainActivity.getMainActivity().getApplicationContext(), ExceptionString,Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER,0,0);
		toast.show();
	}

	public boolean isComplete() {
		return complete;
	}

	private boolean complete = true;
	private boolean win = false;

	private Card[][] cardsMap = new Card[Config.LINES][Config.LINES];
	private List<Point> emptyPoints = new ArrayList<Point>();
	private List<Point> AllPoints = new ArrayList<Point>();
	private final String saveFileName = "cards";
}
