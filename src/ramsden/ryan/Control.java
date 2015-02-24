package ramsden.ryan;

import java.awt.Color;
import java.awt.Toolkit;
import java.util.Random;

import ramsden.ryan.GUI.DisplayPanel;
import ramsden.ryan.GUI.Information;
import ramsden.ryan.GUI.TextComponent;
import ramsden.ryan.GUI.TextInput;

public class Control {


	public static final double GAME_HERTZ = 30;

	public static DisplayPanel view;
	public static Engine model;
	public static Question currentQuestion;
	public static boolean playing = true;
	public static boolean running = true;
	public static boolean stepMode = false;
	public static double efficiency = 1.00;
	public static int sqSize = 50;
	public static int width = 1024;
	public static int height = 1024;
	public static int currentSeed = 0;
	public static Control manager;
	public static Color bgColor = new Color(0xc9c9c9);//0xb3b3b3);
	public static Color lineColor = new Color(0xb3b3b3);//0xa1a1a1);
	public static int fps = 60;
	public static Particle selected;
	private int frameCount = 0;
	private static int studentID = 0;
	private static String username;
	public static TextInput selectedTextBox;
	public static TextComponent clickedAnswer;
	private SQL sql;


	public Control(DisplayPanel view, Engine model) {
		Control.view = view;
		Control.model = model;
	}

	public void runGameLoop()
	{
		Thread loop = new Thread()
		{
			public void run()
			{
				gameLoop();
			}
		};
		loop.start();
	}

	private void gameLoop()
	{

		final double TIME_BETWEEN_UPDATES = 1000000000 / GAME_HERTZ;
		final int MAX_UPDATES_BEFORE_RENDER = 1;
		double lastUpdateTime = System.nanoTime();
		double lastRenderTime = System.nanoTime();
		final double TARGET_FPS = 60;
		final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;

		//Simple way of finding FPS.
		int lastSecondTime = (int) (lastUpdateTime / 1000000000);

		while (running)
		{
			double now = System.nanoTime();
			int updateCount = 0;
			//Do as many game updates as we need to, potentially playing catchup.
			if(playing) {
				while(now - lastUpdateTime > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER)
				{
					model.updateGame();
					lastUpdateTime += TIME_BETWEEN_UPDATES;
					updateCount++;
				}
			} else lastUpdateTime = now;
			//Render. To do so, we need to calculate interpolation for a smooth render.
			float interpolation = Math.min(1.0f, (float) ((now - lastUpdateTime) / TIME_BETWEEN_UPDATES));
			view.drawGame(interpolation);
			Toolkit.getDefaultToolkit().sync();
			frameCount++;
			lastRenderTime = now;

			//Update the frames we got.
			int thisSecond = (int) (lastUpdateTime / 1000000000);
			if (thisSecond > lastSecondTime)
			{
				fps = frameCount;
				frameCount = 0;			
				lastSecondTime = thisSecond;
				Information.toggleCursor();
			}
			DisplayPanel.fps.text = "FPS: "+fps; //We dont want to do settext, because it will update the container
			//Yield until it has been at least the target time between renders. This saves the CPU from hogging.

			while (now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS && now - lastUpdateTime < TIME_BETWEEN_UPDATES)
			{
				Thread.yield();
				//This stops the app from consuming all your CPU. It makes this slightly less accurate, but is worth it.
				//You can remove this line and it will still work (better), your CPU just climbs on certain OSes.
				//FYI on some OS's this can cause pretty bad stuttering. Scroll down and have a look at different peoples' solutions to this.
				try {Thread.sleep(1);} catch(Exception e) {} 

				now = System.nanoTime();
			}
		}
	}

	public static void setQuestion(Question question) {
		currentQuestion = question;
		Particle.r = new Random(question.seed);
	}

	public SQL getSQL() {
		if(sql == null) {
			System.out.println("Connecting to server");
			sql = new SQL();
			Thread connectThread = new Thread(){
				@Override
				public void run()
				{
					if(!sql.connect()) {
						if(view.loginButton.getText().equals("Connecting")) view.connectionError.setHidden(false);
						view.loggedinAs.setText("Failed to connect");
						
						sql = null;
					} else {
						
						DisplayPanel.login.setHidden(true);
					}
					view.loginButton.setText("Login");
				}
			};
			connectThread.start();
		} 
		return sql;
	}

	public void closeSQL() {
		sql.close();
	}
	
	public boolean isConnected()
	{
		return (sql != null && sql.isConnected());
	}

	public static String getUsername() {

		if(username == null || username.trim().isEmpty()) {
			if(studentID != 0 && username == null) return ""+studentID;
			else return "";
		} else return username.trim();
	}

	public static int getStudentID() {
		return studentID;
	}

	public static void setStudentID(int id) {
		studentID = id;
	}
}