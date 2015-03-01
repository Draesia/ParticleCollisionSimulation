package ramsden.ryan;

import java.awt.Color;
import java.util.Random;

public class Question {

	public int seed;
	public String question;
	public double answer; //This is the correct answer
	public String myAnswer;
	private Random random;
	private Engine initialEngine;
	private Value toFindValue;
	private int toFind;
	private static Engine simulatedEngine;

	public enum Dimension { ONE, TWO };

	public enum Colour { RED(new Color(0xFF6666)), BLUE(new Color(0x6666ff)), GREEN(new Color(0x66ff66));
		private Color color;
		private Colour(Color color) { this.color = color;}
		public Color getColor() { return color;}};
	
	public enum Value { VELOCITY("Velocity"), MOMENTUMY("Y Momentum"), MOMENTUMX("X Momentum"), KE("Kinetic Energy");
		public String str;
		private Value(String s) {this.str = s;};};

		
	public Question()
	{
		this((new Random()).nextInt());
	}

	public Question(int seed)
	{
		this.seed = seed;
		generateQuestionText();
	}
	
	public Question(boolean isOneDimensional)
	{
		seed = (new Random()).nextInt();
		if(!isOneDimensional) seed-=seed%2; //Make even
		else if(seed%2==0)seed++; //Make odd
		generateQuestionText();
	}	

	public String generateQuestionText()
	{
		random = new Random(seed);
		toFindValue = Value.values()[random.nextInt(Value.values().length)];
		toFind = random.nextInt(2);
		question = "What is the "+toFindValue.str+" of the "+(toFind == 0 ? "red" : "blue")+" ball after the collision?";
		return question;
	}

	public Engine generateQuestionEngine() //Returns the inital start position of the new engine.
	{
		if(initialEngine != null) return initialEngine;

		System.out.println("Generating seed: "+seed);
		random = new Random(seed);

		Control.stepMode = false; //this will interfere with the generation
		Control.setPlaying(false);
		int width = 800, height = 450; // Because we do not know the width or height of our user, we must scale it.
		initialEngine = new Engine(width,height);
		Control.setQuestion(this);
		int padding = 50;  // Min distance from the edge of the wall.
		int dimension = 1 + (seed+1)%2;
		initialEngine.clear();
		if(dimension == 1) {
			int y = ((height/3)/Control.sqSize + random.nextInt((height/3)/Control.sqSize))*Control.sqSize;
			System.out.println("Y: "+y);
			int atoms = 2; //No point adding more atoms.
			int collideX = random.nextInt(width-2*padding)+padding;
			float scale = 1+random.nextInt(3);
			for(int i = 0; i < atoms; i++) //We will say that atoms(0) is the particle we want to know about.
			{
				Particle p = new Particle();
				int radius = ((2+random.nextInt(2))*Control.sqSize/2);
				p.setR(radius);
				p.setM(1+random.nextInt(10));
				p.setColor(Colour.values()[i].getColor());
				p.setPosition(radius+random.nextInt(width-radius*2), y);
				while(initialEngine.willCollideWhenPlaced(p)) p.setPosition(radius+random.nextInt(width-radius*2), y);
				p.setVelocity(scale*(collideX-p.getX())/Control.GAME_HERTZ, 0);
				initialEngine.addAtom(p);
			}

		} else { //2 dimensional problem, uh oh.

			Particle p1 = new Particle();
			int radius1 = ((2+random.nextInt(3))*Control.sqSize/2);
			int radius2 = ((2+random.nextInt(3))*Control.sqSize/2);
			p1.setR(Math.min(radius1, radius2));
			p1.setM(1+random.nextInt(5));
			p1.setColor(Colour.RED.getColor());
			p1.setPosition(random.nextInt(width-2*radius1-2*padding)+radius1+padding, random.nextInt(height-2*radius1-2*padding)+radius1+padding);
			Particle p2 = new Particle();
			p2.setColor(Colour.BLUE.getColor());
			p2.setR(Math.max(radius1, radius2));
			p2.setM(1+random.nextInt(10));
			int attempts = 0;
			boolean placed = false; //We dont want to place the 2nd particle too close.

			while(!placed && ++attempts < 100) {
				placed = true;
				int x = random.nextInt(width-2*radius2)+radius2;
				int y = random.nextInt(height-2*radius2)+radius2;
				int minDistance = radius1 + radius2 + padding;
				p2.setPosition(x, y);
				if(new Vector(p1.getPosition()).subtract(p2.getPosition()).norm() < minDistance) placed = false;
			} if(attempts == 100) {
				//Not sure how this would be possible, it doesnt really make sense to happen.
				//But we would never want to be stuck in a loop, so if it does happen, lets just regenerate the question
				//This means that the current seed will have the same question as the newly generated one.
				//Reducing the number of questions by 1, but we have 4.94 Billion anyways.
				seed = random.nextInt();
				return generateQuestionEngine();
			}

			//Ok we have two particles, we just need them to collide
			//Rather than doing complicated maths for no reason, we should just set a point
			//Where we want them to collide, and then set their vectors to a scaled down version of the
			//Vector to the collision
			int maxRadius = Math.max(radius1, radius2);
			double gaussian1 = Math.max(Math.min(1, random.nextGaussian()*0.1), -1);
			double gaussian2 = Math.max(Math.min(1, random.nextGaussian()*0.1), -1);
			int collideX = (int) (width/2 + gaussian1*(width-(maxRadius+padding)*2));
			int collideY = (int) (height/2 + gaussian2*(height-(maxRadius+padding)*2));
			//System.out.println("P1: "+p1.getPosition().toString());
			//System.out.println("P2: "+p2.getPosition().toString());
			//System.out.println("X: "+collideX + " Y: " + collideY);
			//Calclulate the Vector to get to cX cY for each particle
			Vector v1 = new Vector(collideX, collideY).subtract(p1.getPosition());
			Vector v2 = new Vector(collideX, collideY).subtract(p2.getPosition());
			//Ok we have the vector to get to the collide position, we just need to scale it down
			//Currently it is measured in pixels per tick, tick = 30 ticks per second
			float scale = 1+random.nextInt(3); //Very important that we scale them by the same amount
			v1.scale(scale/Control.GAME_HERTZ); //The numerator is how many seconds it will take 
			v2.scale(scale/Control.GAME_HERTZ); // until the collision happens
			//Ok now lets just set the particles' velocity
			p1.setVelocity(v1);
			p2.setVelocity(v2);
			initialEngine.addAtom(p1);
			initialEngine.addAtom(p2);

		}

		simulatedEngine = new Engine(initialEngine);
		while(simulatedEngine.collisions == 0) simulatedEngine.updateGame();
		initialEngine.maxGameTicks = simulatedEngine.gameTicks;
		//System.out.println("Seconds until Collision: "+initialEngine.maxGameTicks/Control.GAME_HERTZ);
		Particle ap = simulatedEngine.getAtoms().get(toFind);
		switch(toFindValue) {
		case KE:
			answer = ap.getConvertedKE();
			break;
		case MOMENTUMX:
			answer = ap.getConvertedMomentumX();
			break;
		case MOMENTUMY:
			answer = ap.getConvertedMomentumY();
			break;
		case VELOCITY:
		default:
			answer = ap.getConvertedVelocity();
			break;
		}
		System.out.println("Answer: "+answer);
		return initialEngine;
	}
	
	private SQL getSQL()
	{
		return Control.manager.getSQL();
	}

	public boolean isCorrect(String input)
	{
		try {
			double value = Double.parseDouble(input);
			return(Math.abs(value-answer) < 0.05);
		} catch(Exception e) {};
		return false;
	}

	public boolean answer(String input)
	{
		boolean isCorrect = isCorrect(input);
		if(myAnswer == null) {
			myAnswer = input;
			if(getSQL().isConnected() && !Control.getUsername().isEmpty())
			{
				String sql = "INSERT INTO `answers`(`studentID`, `seed`, `answer`, `isCorrect`) VALUES ("+Control.getStudentID()+","+seed+",'"+input+"',"+isCorrect+")";
				getSQL().updateQuery(sql);
			} else {
				System.out.println("Cannot connect to SQL server");
			}
		}
		return isCorrect;
	}


}
