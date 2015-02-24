package ramsden.ryan;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Question {

	public int seed;
	public String question;
	public String answer; //This is the correct answer
	private Random random;

	private static Engine initialEngine;
	private static Engine simulatedEngine;
	
	public enum Dimension { ONE, TWO };
	public enum Color { RED, BLUE, GREEN, YELLOW, ORANGE };
	public enum Value { MASS, SPEED, XVELOCITY, YVELOCITY, MOMENTUM };
	public enum Direction { UP, DOWN, LEFT, RIGHT };
	
	
	public Question()
	{
		seed = (new Random()).nextInt();
	}

	public Question(int seed)
	{
		this.seed = seed;
	}

	public Engine generateQuestionEngine() //Returns the inital start position of the new engine.
	{
		System.out.println("Generating seed: "+seed);
		random = new Random(seed);
		Control.stepMode = false; //this will interfere with the generation
		initialEngine = new Engine();
		Control.setQuestion(this);
		int width = 1000, height = 1000; // Because we do not know the width or height of our user, we must scale it up/down.
		int dimension = 1;//+random.nextInt(2);
		initialEngine.clear();
		if(dimension == 1) {
			int y = (3+random.nextInt((height/Control.sqSize)-6))*Control.sqSize;
			int atoms = random.nextInt(2)+2;
			for(int i = 0; i < atoms; i++) //We will say that atoms(0) is the particle we want to know about.
			{
				Particle p = new Particle();
				int radius = ((1+random.nextInt(3))*Control.sqSize/2);
				p.setR(radius);
				p.setVelocity((random.nextDouble()-0.5)*10, 0);
				p.setPosition(random.nextInt(width), y);
				while(initialEngine.willCollideWhenPlaced(p)) p.setPosition(random.nextInt(width), y);
				initialEngine.addAtom(p);
			}
			
		} else { //2 dimensional problem, uh oh.
			Particle p = new Particle();
			int radius = ((1+random.nextInt(3))*Control.sqSize/2);
			p.setR(radius);
			p.setVelocity(random.nextDouble()*10, random.nextDouble()*10);
			p.setPosition(random.nextInt(Control.width), random.nextInt(Control.height));
			initialEngine.addAtom(p);
		}
		
		simulatedEngine = new Engine(initialEngine);
		while(simulatedEngine.collisions < 2) simulatedEngine.updateGame();
		answer = "10";
		return initialEngine;
	}

	public static int getStudentID() {
		return Control.getStudentID();
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

	private SQL getSQL()
	{
		return Control.manager.getSQL();
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

	public void answer(String input)
	{
		if(getSQL().connect() && validate(""+Control.getStudentID()))
		{
			String sql = "INSERT INTO `answers`(`studentID`, `seed`, `answer`, `isCorrect`) VALUES ("+Control.getStudentID()+","+seed+",'"+input+"',"+(answer.equalsIgnoreCase(input))+")";
			getSQL().updateQuery(sql);
		} else {
			System.out.println("Cannot connect to SQL server");
		}
	}
}
