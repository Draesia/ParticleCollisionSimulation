/** Distributed under the terms of the GPL, version 3. */
package ramsden.ryan.GUI;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;

import javax.swing.*;

import ramsden.ryan.Control;
import ramsden.ryan.Engine;
import ramsden.ryan.Particle;
import ramsden.ryan.Question;
import ramsden.ryan.SQL;
import ramsden.ryan.Vector;

@SuppressWarnings("serial")
public class DisplayPanel extends JPanel implements MouseListener, MouseMotionListener, ComponentListener, MouseWheelListener, KeyListener  {

	public static final Font font = new Font("Verdana", Font.BOLD, 18);
	public static final Font fontsmall = new Font("Verdana", Font.BOLD, 12);
	public static final DecimalFormat df = new DecimalFormat("#.##");
	public static final DecimalFormat dfe = new DecimalFormat("##0.##E0");
	public static HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();


	public static Dialog login;
	private boolean useGradient = false;
	private Engine model;
	private Particle newParticle = null;
	public Point lastMouse;
	public static Particle currentDrag;

	private float interpolation;
	private Information info;
	private TextInput studentID;

	public Dialog questionDialog;
	public TextComponent questionText;
	public TextInput questionInput;
	private ResultsViewer answerViewer;
	private TextInput username;
	private TextInput password;
	public Dialog connectionError;
	public static TextComponent fps;
	public TextComponent loggedinAs;
	public Button loginButton;
	public Button questionButton;
	private Button tloginButton;
	public TextComponent sid;
	private Button options;
	public Button add;
	public static ScrollBar currentScrollBar;

	public static Particle collided1,collided2;

	/** Construct a display panel. */
	public DisplayPanel(final Engine model) {
		this.model = model;
		Control.view = this;
		this.useGradient = true;
		this.setFocusTraversalKeysEnabled(false);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addComponentListener(this);
		this.addMouseWheelListener(this);
		this.addKeyListener(this);
		this.setFocusable(true);
		this.setFont(font);
		this.setPreferredSize(new Dimension(Control.width, Control.height));
		this.addInitialInterface();
	}

	public void useGradient(boolean state) { useGradient = state; }

	public void drawGame(float interpolation) {
		setInterpolation(interpolation);
		repaint();
	}

	/** Paint the display. */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		//Create Background
		g.setColor(Control.bgColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		//Create Lines
		g.setColor(Control.lineColor);
		int size = Control.sqSize;
		for(int i = 0; i < Control.width; i = i + size) g.drawLine(i, 0, i, Control.height);
		for(int i = 0; i < Control.height; i = i + size)g.drawLine(0, i, Control.width, i);

		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if(newParticle != null)
		{
			newParticle.updateDrawPosition();
			model.addAtom(newParticle);
			newParticle = null;
		}
		g2D.setStroke(new BasicStroke(5));

		for (Particle atom : model.getAtoms()) {
			g2D.setColor(atom.getColor());
			int dx = (int) (atom.getVelocity().x*Control.GAME_HERTZ);
			int dy = (int) (atom.getVelocity().y*Control.GAME_HERTZ);
			g2D.drawLine((int)atom.getX(), (int)atom.getY(), (int)atom.getX()+dx, (int)atom.getY()+dy);
		}
		g2D.setStroke(new BasicStroke());
		for (Particle atom : model.getAtoms()) {
			Vector drawVector = atom.getDrawPosition(interpolation);
			if (useGradient && (atom != Control.selected || atom == currentDrag)) {
				Image atomImage = atom.getImage();
				g2D.drawImage(atomImage, (int)drawVector.x, (int)drawVector.y, null);
			} else {
				Shape shape = atom.getShape(drawVector);
				g2D.setPaint(atom.getColor());
				g2D.fill(shape);
				g2D.setPaint(atom.getColor().darker().darker());
				g2D.draw(shape);
			}
		}


		if(!Control.isPlaying() && collided1 != null && collided2 != null)
		{
			//g2D.setStroke(new BasicStroke(2));
			g2D.setColor(Color.BLACK);
			g2D.setFont(DisplayPanel.font);
			g2D.drawLine((int)collided1.getX(), (int)collided1.getY(), (int)collided2.getX(), (int)collided2.getY());
			g2D.drawLine((int)collided1.getX(), (int)collided1.getY(), (int)collided1.getX(), (int)collided2.getY());
			g2D.drawLine((int)collided2.getX(), (int)collided2.getY(), (int)collided1.getX(), (int)collided2.getY());
			double dx = Math.abs(collided1.getX() - collided2.getX());
			double dy = Math.abs(collided1.getY() - collided2.getY());
			if(dx!=0)g2D.drawString(Information.format(dx/Control.sqSize),(int)(collided2.getX()+collided1.getX())/2,-4+(int)collided2.getY());
			if(dy!=0)g2D.drawString(Information.format(dy/Control.sqSize),4+(int)collided1.getX(), (int)(collided2.getY()+collided1.getY())/2);
			//collided1.getPosition().x;
		}

		for(Interface i : Interface.mainInterfaces)
		{
			if(!i.isHidden()) i.drawInterface(g2D, 0, 0);
		}



		//				if(model.lastCollide != null && model.lastCollide1 != null) {
		//					Particle[] particles = {model.lastCollide, model.lastCollide1};
		//					Vector m = Particle.getSumMomentum(particles);
		//					Vector p = Particle.centroid(particles);
		//					g2D.setColor(Color.RED);
		//					g2D.drawLine((int)p.x, (int)p.y, (int)(p.x+m.x), (int)(p.y+m.y));
		//					g2D.setColor(Color.BLUE);
		//					g2D.drawLine((int)particles[0].getX(), (int)particles[0].getY(), (int)particles[1].getX(), (int)particles[1].getY());
		//				}
	}

	/** Creates the GUI Elements for the start of the simulation */
	private void addInitialInterface()
	{

		info = new Information();
		fps = new TextComponent(null, "FPS: ", new Point(200, 2));
		fps.setHidden(true);
		loggedinAs = new TextComponent(null, "", new Point(20, 2));

		questionDialog = new Dialog(null, new Rectangle(20, 90, 160, 80)) {
			@Override
			public void onClose() {
				super.onClose();
				Control.currentQuestion = null;
				add.setHidden(false);
			}
		};

		new TextComponent(questionDialog, "Question", new Point(5, 9), 16, Font.BOLD);
		questionButton = new Button(questionDialog, new Rectangle(112, 52, 70, 20), "Mark") {
			@Override
			public void click(Point p, boolean onClickDown) {
				super.click(p, onClickDown);
				if(!onClickDown) {
					String answer = questionInput.getText();
					if(!answer.isEmpty()) {
						if(Control.currentQuestion.answer(answer)) {
							setText("Correct");
						} else {
							setText("Incorrect");
						}
					}
				}
			}
		};
		questionInput = new TextInput(questionDialog, "", new Rectangle(8, 52, 70, 20)) {
			@Override
			public void enter()
			{
				Control.currentQuestion.answer(text);
			}
			@Override
			public boolean validate(char c)
			{
				return (Character.isDigit(c) || (c+"").equals(".") || (c+"").equals("-"));
			}
		};

		questionText = new TextComponent(questionDialog, "", new Point(5, 32), 14);
		questionDialog.setCloseable(true);
		questionDialog.setHidden(true);
		addMainButtons();
		createViewer().setHidden(true);
		createLogin();

		connectionError = new Dialog(null, new Rectangle(0,0,250,50)) {
			@Override
			public void setHidden(boolean hidden)
			{
				super.setHidden(hidden);
				if(!hidden) container.setLocation(Control.width/2 - 50, Control.height/2 - 40);
			}
		};
		new TextComponent(connectionError, "Failed to connect to server", new Point(20, 20));	
		connectionError.setHidden(true);
		connectionError.setCloseable(true);

	}



	private void addMainButtons()
	{

		Button.getImage("image:play.png"); //This adds it to our cache, so we wont have to look again.
		new Button(null, new Rectangle(20+(0*70), 20, 64, 64), "image:pause.png") {

			boolean paused = false;

			@Override
			public void click(Point p, boolean onClickDown) {
				super.click(p, onClickDown);
				if(onClickDown) {
					Control.setPlaying(!Control.isPlaying());
					Control.stepMode = false;
					paused = !paused;
					setText(Control.isPlaying() ? "image:pause.png" : "image:play.png");

				}
			}

			@Override
			public void drawInterface(Graphics2D g2d, int offsetX, int offsetY) {
				super.drawInterface(g2d, offsetX, offsetY);
				if(paused == Control.isPlaying()) {
					paused = !paused;
					setText(Control.isPlaying() ? "image:pause.png" : "image:play.png");
				}
			}

		};

		new Button(null, new Rectangle(20+(1*70), 20, 64, 64), "image:step.png") {
			@Override
			public void click(Point p, boolean onClickDown) {
				super.click(p, onClickDown);
				if(!onClickDown) {
					Control.stepMode = true;
					Control.setPlaying(true);
				}
			}
		};
		new Button(null, new Rectangle(20+(2*70), 20, 64, 64), "image:reset.png") {
			@Override
			public void click(Point p, boolean onClickDown) {
				super.click(p, onClickDown);
				if(!onClickDown) {
					if(Control.currentQuestion == null) {
						model.clear();
						Control.setPlaying(true);
					} else {
						model.setState(Control.currentQuestion.generateQuestionEngine());
						model.updateImages();
						collided1=null;collided2=null;
						Control.setPlaying(false);
					}
				}
			}
		};

		add = new Button(null, new Rectangle(20+(3*70), 20, 64, 64), "image:add.png") {
			@Override
			public void click(Point p, boolean onClickDown) {
				super.click(p, onClickDown);
				if(!onClickDown) {
					newParticle = null;
					int attempts = 0;
					while(newParticle == null && attempts < 100) {
						newParticle = model.getNewAtom();
						Random r = new Random();
						int x = (int) (r.nextInt(Control.width) - newParticle.getR()*2);
						int y = (int) (r.nextInt(Control.height) - newParticle.getR()*2);
						newParticle.setPosition(newParticle.getR()+x, newParticle.getR()+y);
						newParticle.setVelocity(0, 0);
						attempts++;
						if(model.willCollideWhenPlaced(newParticle)) newParticle = null;
					}
				}
			}
		};


		new Button(null, new Rectangle(20+(4*70), 20, 64, 64), "image:question.png") {
			@Override
			public void click(Point p, boolean onClickDown) {
				super.click(p, onClickDown);
				if(!onClickDown) {
					Control.setQuestion(new Question());
				}
			}
		};

		new Button(null, new Rectangle(20+(5*70), 20, 64, 64), "image:login.png") {
			@Override
			public void click(Point p, boolean onClickDown) {
				super.click(p, onClickDown);
				login.centerOnScreen();
				if(!onClickDown) {
					login.setHidden(!login.isHidden());

				}
			}
		};

		options = new Button(null, new Rectangle(20+(6*70), 20, 64, 64), "image:options.png") {
			@Override
			public void click(Point p, boolean onClickDown) {
				super.click(p, onClickDown);
				if(!onClickDown) {
					if(Control.getUsername().isEmpty())
						login.setHidden(false);
					else {
						login.setHidden(true);
						answerViewer.toggle();
					}
				}
			}
		};
		options.setHidden(true);
	}

	private Dialog createLogin()
	{
		login = new Dialog(null, new Rectangle(0, 82, 126, 106));
		login.setHidden(true);
		login.setCloseable(true);
		login.centerOnScreen();
		new TextComponent(login, "Login", new Point(8, 8), 16, Font.BOLD);
		sid = new TextComponent(login, "Student ID", new Point(10, 30), 14);
		final TextComponent usr = new TextComponent(login, "Username", new Point(10, 100), 12);
		final TextComponent pss = new TextComponent(login, "Password", new Point(10, 140), 12);

		studentID = new TextInput(login, "", new Rectangle(10, 50, 60, 20)) {
			@Override
			public boolean validate(char keyChar)
			{
				if(text.length() == 0) return (keyChar == "s".charAt(0) || Character.isDigit(keyChar));
				return (text.charAt(0) == "s".charAt(0) ? text.length() < 6 : text.length() < 5) && Character.isDigit(keyChar);
			}
			@Override
			public void enter()
			{
				loginButton.click(new Point(), false);
			}
		};
		loginButton = new Button(login, new Rectangle(76, 50, 40, 20), "Login") {
			@Override
			public void click(Point p, boolean onClickDown) {
				super.click(p, onClickDown);
				if(!onClickDown) {
					//SELECT * FROM `teachers` WHERE `username`='PDacey' AND `password`='fermentum'
					if(getText().equals("Logout"))
					{
						Control.setUsername("");
						Control.setStudentID("");
						studentID.setText("");
						username.setText("");
						password.setText("");
						setText("Login");
						return;
					}
					if(!studentID.getText().isEmpty() || (!username.getText().isEmpty()&&!password.getText().isEmpty())) {
						sid.setText("Connecting");
						SQL sql = Control.manager.getSQL();
						options.setHidden(true);
						if(Control.manager.isConnected()) {
							if(!studentID.getText().isEmpty()) {
								Control.setStudentID(studentID.getText());
								login.setHidden(true);
								sid.setText("Student ID");
								setText("Logout");
								loggedinAs.setText("Logged in as: "+Control.getUsername());
							} else {
								String user = sql.getValue("SELECT * FROM `teachers` WHERE `username`='"+username.getText()+"' AND `password`='"+password.text+"'", "username");
								System.out.println(user);
								if(!user.isEmpty()) {
									Control.setUsername(user);
									options.setHidden(false);
									login.setHidden(true);
									sid.setText("Student ID");
									setText("Logout");
									loggedinAs.setText("Logged in as: "+Control.getUsername());
								} else {
									sid.setText("Incorrect!");
									sid.setColor(Color.RED);
								}
							}
						} else sid.setText("Student ID");
					}
				}
			}
		};
		tloginButton = new Button(login, new Rectangle(10, 76, 106, 20), "Teacher Login") {
			boolean tLogin = false;
			@Override
			public void click(Point p, boolean onClickDown) {
				super.click(p, onClickDown);
				if(!onClickDown) {
					tLogin = !tLogin;
					usr.setHidden(!tLogin);
					pss.setHidden(!tLogin);
					username.setHidden(!tLogin);
					password.setHidden(!tLogin);
					if(tLogin) {
						login.setSize(126,186);
						setText("Hide T.Login");
					} else {
						login.setSize(126, 106);
						setText("Teacher Login");
					}
				}
			}
		};
		username = new TextInput(login, "", new Rectangle(10, 116, 106, 20));
		password = new TextInput(login, "", new Rectangle(10, 156, 106, 20)) {
			@Override
			public String getText()
			{
				return "*************************".substring(0, text.length());
			}
		};
		username.setHidden(true);
		password.setHidden(true);
		usr.setHidden(true);
		pss.setHidden(true);
		return login;
	}

	private ResultsViewer createViewer()
	{
		answerViewer = new ResultsViewer(new Rectangle(20, 180, 310, 600));
		return answerViewer;
	}



	public void setInterpolation(float interp)
	{
		interpolation = interp;
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		Control.model.scale(Control.width, Control.height, arg0.getComponent().getWidth(), arg0.getComponent().getHeight());
		Control.width = arg0.getComponent().getWidth();
		Control.height = arg0.getComponent().getHeight();
		info.setContainer(new Rectangle(0, Control.height-Information.HEIGHT, Control.width, Information.HEIGHT));
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {
		double clicks = event.getPreciseWheelRotation();
		boolean hasPressed = false;
		for(Interface i : Interface.mainInterfaces) {
			if(i.canClick() && !i.isHidden() && i.getContainer().contains(event.getPoint())) {
				if(i instanceof TableDialog)
				{
					answerViewer.getScrollBar().scroll(clicks*5);
				}
				hasPressed = true;
			}
		}
		if(!hasPressed && model.getAtoms().size() > 0 && Control.currentQuestion == null)
		{
			for(Particle p : model.getAtoms()) {
				double radius = p.getR();
				Vector mouseClick = new Vector(event.getX(), event.getY());
				mouseClick = mouseClick.subtract(p.getPosition());
				if (mouseClick.norm() < radius) {
					p.setR(p.getR()+(clicks*-25));
					if(p.getR() < 25) p.setR(25);
					if(p.getR() > 250) p.setR(250);
					p.updateImage();
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent event) {
		boolean hasPressed = false;
		Information.selected = -1;
		for(Interface i : Interface.mainInterfaces) {
			if(i.canClick() && !i.isHidden() && i.getContainer().contains(event.getPoint())) {
				i.clicked(event.getPoint(), true);
				hasPressed = !(i instanceof Information); //Cheaty way of letting you click through the info box.
			}
		}
		if(!hasPressed && model.getAtoms().size() > 0 && Control.currentQuestion == null)
		{
			for(int i = model.getAtoms().size()-1; i >= 0; i--)
			{
				Particle p = model.getAtoms().get(i);
				double radius = p.getR();
				Vector mouseClick = new Vector(event.getX(), event.getY());
				mouseClick = mouseClick.subtract(p.getPosition()); //Relative Vector
				if (mouseClick.norm() < radius) {
					if(event.getButton() == 1) {
						currentDrag = p;
						return;
					} else if(event.getButton() == 2) {
						p.setVelocity(new Vector());
						return;
					} else if(event.getButton() == 3) {
						currentDrag = p;
						return;
					}
				}
			}
		}
		//if(currentDrag != null) model.getAtoms().remove(currentDrag);
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		//if(currentDrag == null) model.addAtom(currentDrag);
		currentDrag = null;
		currentScrollBar = null;
		for(Interface i : Interface.mainInterfaces)
			if(i.canClick() && !i.isHidden() && i.getContainer().contains(event.getPoint())) i.clicked(event.getPoint(), false);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(currentDrag != null) {
			Control.selected = currentDrag;
			if(SwingUtilities.isRightMouseButton(e)) {
				double rX = e.getX() - currentDrag.getX();
				double rY = e.getY() - currentDrag.getY();
				int signX = rX >= 0 ? 1 : -1;
				int signY = rY >= 0 ? 1 : -1;
				double newX = ((int)(rX + signX*Control.sqSize/2)/Control.sqSize) * Control.sqSize / Control.GAME_HERTZ;
				double newY = ((int)(rY + signY*Control.sqSize/2)/Control.sqSize) * Control.sqSize / Control.GAME_HERTZ;
				//currentDrag.setVelocity(currentDrag.getPosition().subtract(new Vector(newX, newY)).divide(Control.GAME_HERTZ));
				currentDrag.setVelocity(newX, newY);
			} else if (SwingUtilities.isLeftMouseButton(e)) {
				int newX = (e.getX() % Control.sqSize > Control.sqSize/2 ? e.getX() - e.getX() % Control.sqSize + Control.sqSize : e.getX() - e.getX() % Control.sqSize);
				int newY = (e.getY() % Control.sqSize > Control.sqSize/2 ? e.getY() - e.getY() % Control.sqSize + Control.sqSize : e.getY() - e.getY() % Control.sqSize);
				currentDrag.setPosition(newX, newY);
				currentDrag.updateDrawPosition();
			}
		}
		if(currentScrollBar != null) {
			currentScrollBar.scroll(e.getPoint());
		}

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		lastMouse = e.getPoint();
		if(currentDrag == null && info != null && !info.hover(lastMouse.x, lastMouse.y))
			Control.selected = null;
	}

	@Override
	public void mouseClicked(MouseEvent event){}
	@Override
	public void mouseEntered(MouseEvent event){}
	@Override
	public void mouseExited(MouseEvent event){}
	@Override
	public void componentHidden(ComponentEvent arg0){}
	@Override
	public void componentMoved(ComponentEvent arg0){}
	@Override
	public void componentShown(ComponentEvent arg0){}

	@Override
	public void keyPressed(KeyEvent k) {
		if(k.getKeyCode() == KeyEvent.VK_F1) Control.takeSnapShot(); 
		if(Control.selected != null && Information.selected != -1) {
			char keyChar = k.getKeyChar();
			if(Character.isDigit(keyChar) || keyChar == (char)46 || keyChar == (char)45) {
				info.tempText = info.tempText + keyChar;
			} else if(keyChar == (char)8 && info.tempText.length() > 0) {
				info.tempText = info.tempText.substring(0, info.tempText.length()-1);
			} else if(keyChar == (char)10) {
				info.saveValue(Control.selected);
				Information.selected = -1;
			}
		}
		else if(Control.selectedTextBox != null)
			Control.selectedTextBox.addCharacter(k.getKeyCode(), k.getKeyChar());
		else if(k.getKeyChar() == KeyEvent.VK_SPACE) {
			Control.setPlaying(!Control.isPlaying());
			Control.stepMode = false;
		}
	}

	@Override
	public void keyReleased(KeyEvent k) {
	}

	@Override
	public void keyTyped(KeyEvent k) {
	}



}
