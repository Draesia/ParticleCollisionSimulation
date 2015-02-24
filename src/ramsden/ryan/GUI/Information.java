package ramsden.ryan.GUI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import ramsden.ryan.Control;
import ramsden.ryan.Particle;

public class Information extends Interface {

	public final static int HEIGHT = 120;
	public final static int WIDTH = 150;
	public final static int PADDING = 5;
	public final static int SPACE = 14;
	public static final Color highlighed = Color.BLUE;
	public int selected = -1;
	public int hover = -1;
	public String tempText = "";
	public static String cursor = "";
	
	public Information() {
		super(null, new Rectangle(0, Control.height-HEIGHT, Control.width, HEIGHT));
	}

	@Override
	public void drawInterface(Graphics2D g2d, int offsetX, int offsetY) {
		for(int i = 0; i < Control.model.getAtoms().size(); i++)
		{
			Particle p = Control.model.getAtoms().get(i);
			boolean hov = (Control.selected == p);
			int x = 50+i*(WIDTH+PADDING);
			int y = Control.height-(hov ? HEIGHT:50);

			g2d.setColor(p.getColor());
			g2d.fillRect(x, y, WIDTH, HEIGHT);
			g2d.setColor(Color.BLACK);
			g2d.drawRect(x, y, WIDTH, HEIGHT);
			//If we have a text box, we need to draw it
			if(selected != -1) {
				g2d.setColor(p.getColor().brighter().brighter());
				g2d.fillRect(x, y+2+SPACE*selected, WIDTH, SPACE);
			}
			//Here is where we draw the mass info
			g2d.setFont(DisplayPanel.fontsmall);
			String value;
			int valueWidth;

			g2d.setColor(Color.BLACK);
			value = (selected == 0 && hov ? cursor+tempText : (p.view[0] ? DisplayPanel.df.format(3*p.getVNorm()/5) : "???"));
			valueWidth = g2d.getFontMetrics().stringWidth(value);
			g2d.drawString("Velocity: ", x+2, y+1*SPACE);

			g2d.drawString(value, x+WIDTH-valueWidth-2, y+1*SPACE);
			//g2d.setColor(Color.BLACK);
			g2d.drawString("Mass:", x+2, y+2*SPACE);
			value = (selected == 1 && hov ? cursor+tempText : (p.view[1] ? DisplayPanel.df.format(p.getM()) : "???"));
			valueWidth = g2d.getFontMetrics().stringWidth(value);
			//g2d.setColor(selected == 1 && hov ? Color.GRAY : Color.BLACK);
			g2d.drawString(value, x+WIDTH-valueWidth-2, y+2*SPACE);
			//g2d.setColor(Color.BLACK);
			g2d.drawString("Radius:", x+2, y+3*SPACE);
			value = (selected == 2 && hov ? cursor+tempText : (p.view[2] ? DisplayPanel.df.format(p.getR()/25) : "???"));
			valueWidth = g2d.getFontMetrics().stringWidth(value);
			//g2d.setColor(selected == 2 && hov ? Color.GRAY : Color.BLACK);
			g2d.drawString(value, x+WIDTH-valueWidth-2, y+3*SPACE);
			//g2d.setColor(Color.BLACK);
			if(hov) {
				g2d.drawString("Bearing:", x+2, y+4*SPACE);
				value = (selected == 3 ? cursor+tempText : (p.view[3] ? DisplayPanel.df.format(p.getBearing())+"°" : "???"));
				valueWidth = g2d.getFontMetrics().stringWidth(value);
				//g2d.setColor(selected == 3 ? Color.GRAY : Color.BLACK);
				g2d.drawString(value, x+WIDTH-valueWidth-2, y+4*SPACE);

				g2d.drawString("x Velocity:", x+2, y+5*SPACE);
				value = (selected == 4 ? cursor+tempText : (p.view[4] ? DisplayPanel.df.format(3*p.getVx()/5) : "???"));
				valueWidth = g2d.getFontMetrics().stringWidth(value);
				//g2d.setColor(selected == 3 ? Color.GRAY : Color.BLACK);
				g2d.drawString(value, x+WIDTH-valueWidth-2, y+5*SPACE);

				g2d.drawString("y Velocity:", x+2, y+6*SPACE);
				value = (selected == 5 ? cursor+tempText : (p.view[5] ? DisplayPanel.df.format(-3*(p.getVy())/5) : "???"));
				valueWidth = g2d.getFontMetrics().stringWidth(value);
				//g2d.setColor(selected == 3 ? Color.GRAY : Color.BLACK);
				g2d.drawString(value, x+WIDTH-valueWidth-2, y+6*SPACE);
			}
		}
	}

	public boolean hover(int x, int y) {
		if(selected != -1) return true;
		if(x > 50 && y > Control.height-(Control.selected == null ? 50 : 150)) {
			int i = (int) (x-50)/(WIDTH+PADDING); 
			if(i < Control.model.getAtoms().size()) {
				Particle p = Control.model.getAtoms().get(i);
				Control.selected = p;
				return true;
			}
		}
		return false;
	}

	@Override
	public void click(Point p, boolean isClickDown) {
		if(isClickDown && Control.selected != null) {
			int i = (p.y-2)/SPACE;
			if(i < 8) { 
				boolean value = (2*(p.x-50)/(WIDTH+PADDING))%2==1;
				if(value) {
					selected = i;
					cursor = ">";
				} else Control.selected.view[i] = !Control.selected.view[i];

			}
		}
	}
	
	public static void toggleCursor()
	{
		if(cursor.length() > 0) cursor = "";
		else cursor = ">";
	}

	public void saveValue(Particle p) {
		if(tempText.isEmpty()) return;
		double value = Double.parseDouble(tempText);
		switch(selected) {
		case 0: p.setVelocity(5*value/3); break;
		case 1: p.setM(value); break;
		case 2: p.setR(25*value); p.updateImage(); break;
		case 3: p.setBearing(value); break;
		case 4: p.setVelocity(5*value/3, p.getVy()); break;
		case 5: p.setVelocity(p.getVx(), -5*value/3); break;
		}
		selected = -1;
		tempText = "";
	}
}