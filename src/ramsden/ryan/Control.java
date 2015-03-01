package ramsden.ryan;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import ramsden.ryan.GUI.DisplayPanel;
import ramsden.ryan.GUI.Information;
import ramsden.ryan.GUI.TextComponent;
import ramsden.ryan.GUI.TextInput;

public class Control {


	public static final double GAME_HERTZ = 30;

	public static DisplayPanel view;
	public static Engine model;
	public static Question currentQuestion;
	private static boolean playing = true;
	public static boolean questionFinished = false;
	public static boolean running = true;
	public static boolean stepMode = false;
	public static double efficiency = 1.00;
	public static int sqSize = 50;
	public static int width = 1000; //same ratio as 1920/1058 (Maximised on 1080p)
	public static int height = 551; //Minimum height and width
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


	//TODO REMOVE THIS

	public static void takeSnapShot() {
		System.out.println("Taking screenshot");
		BufferedImage bufImage = new BufferedImage(view.getSize().width, view.getSize().height,BufferedImage.TYPE_INT_RGB);
		view.paint(bufImage.createGraphics());
		String filename = "" + (System.currentTimeMillis()/1000);
		File imageFile = new File("screenshots"+File.separator+filename+".png");

		try{
			System.out.println("Saved at "+imageFile.getAbsolutePath());
			imageFile.createNewFile();
			ImageIO.write(bufImage, "png", imageFile);

		}catch(Exception ex){ ex.printStackTrace();}

		PrintWriter writer;
		try {
			writer = new PrintWriter("screenshots"+File.separator+filename+".txt", "UTF-8");
			double totalKE =0, totalMx=0, totalMy=0; int pindex = 0;
			for(Particle p : model.getAtoms()) {
				pindex++;
				double ke = p.getConvertedKE();
				double mx = p.getConvertedMomentumX();
				double my = p.getConvertedMomentumY();
				totalKE += ke;
				totalMx += mx;
				totalMy += my;
				writer.println("Particle "+pindex+": K.E = "+Information.format(ke)+"   Mx = "+Information.format(mx)+"   My = "+Information.format(my)+"   Mass = "+Information.format(p.getConvertedMass())+" Velocity = "+Information.format(p.getConvertedVelocity())+" Vx = "+Information.format(p.getConvertedVelocityX())+" Vy = "+Information.format(p.getConvertedVelocityY()));
				
			}
			writer.println("Total K.E. : "+Information.format(totalKE));		
			writer.println("Total X Momentum : "+Information.format(totalMx));
			writer.println("Total Y Momentum : "+Information.format(totalMy));
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		//outputValues();
	}


	public Control(DisplayPanel view, Engine model) {
		Control.view = view;
		Control.model = model;
	}

	public static boolean isPlaying() {
		return playing;
	}


	public static void setPlaying(boolean playing) {
		if(playing) {
			DisplayPanel.collided1 = null;
			DisplayPanel.collided2 = null;
		}
		model.updateDrawPositions();
		Control.playing = playing;
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
			if(isPlaying()) {
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
				try {Thread.sleep(1);} catch(Exception e) {} 

				now = System.nanoTime();
			}
		}
	}

	public static void setQuestion(Question question) {
		currentQuestion = question;
		Particle.r = new Random(question.seed);
		view.questionText.setText(Control.currentQuestion.question);
		view.questionInput.setText(question.myAnswer);
		view.questionButton.setText("Mark");
		view.questionButton.setSize(40, 20);
		view.questionDialog.setSize(Math.max(view.questionText.getContainer().width+16, 160), 80);
		view.questionDialog.setHidden(false);
		view.add.setHidden(true);
		DisplayPanel.collided1=null;DisplayPanel.collided2=null;
		model.setState(Control.currentQuestion.generateQuestionEngine());
		model.updateImages();
	}

	public SQL getSQL() {
		if(sql == null) {
			System.out.println("Connecting to server");
			sql = new SQL();
//			Thread connectThread = new Thread(){
//				@Override
//				public void run()
//				{
					if(!sql.connect()) {
						if(view.sid.getText().equals("Connecting")) view.connectionError.setHidden(false);
						view.loggedinAs.setText("Failed to connect");

						sql = null;
					}
//				}
//			};
//			connectThread.start();
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
	
	public static void setStudentID(String studentID) {
		if(studentID.trim().isEmpty()) {
			Control.setStudentID(0);
			return;
		}
		if(validate(studentID)) {
			if(studentID.length() == 6) {
				studentID = studentID.substring(1);
			}
		}
		Control.setStudentID(Integer.parseInt(studentID));
	}
	
	public static boolean validate(String studentID)
	{
		if(studentID.length() == 5) {
			Pattern p = Pattern.compile("\\d{5}$");
			Matcher matcher = p.matcher(studentID);
			return matcher.find();
		} else if(studentID.length() == 6){
			Pattern p = Pattern.compile("s\\d{5}$");
			Matcher matcher = p.matcher(studentID);
			return matcher.find();
		}
		return false;
	}


	public static void setUsername(String user) {
		username = user;
	}
}