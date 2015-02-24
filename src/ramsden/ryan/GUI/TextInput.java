package ramsden.ryan.GUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import ramsden.ryan.Control;

public class TextInput extends Interface {

	public String text;
	public Font font = new Font("Verdana", Font.PLAIN, 12);
	public FontMetrics fm;

	
	public TextInput(Interface i, String text, Rectangle r) {
		super(i, r);
		setText(text);
		setColor(new Color(0xe2e2e2));
	}

	
	@Override
	public void drawInterface(Graphics2D g2d, int offsetX, int offsetY) {
		g2d.setColor(getColor());
		g2d.setFont(font);
		if(fm == null) {
			fm = g2d.getFontMetrics();
			//updateContainer();
		}

		Rectangle r = getContainer();
		g2d.fillRect(r.x+offsetX, r.y+offsetY, r.width, r.height);
		g2d.setColor(Color.BLACK);
		g2d.drawString(text, r.x+2+offsetX, 8+r.y+fm.getHeight()/2+offsetY);
		//Not too happy with this.
		if(Control.selectedTextBox == this && System.currentTimeMillis()%1000<700)
			g2d.drawLine(fm.stringWidth(text)+r.x+offsetX+2, r.y+offsetY+3, fm.stringWidth(text)+r.x+offsetX+2, r.y+offsetY+r.height-4);
	}

	
	
	@Override
	public void click(Point p, boolean onClickDown) {
		Control.selectedTextBox = this;
		p.translate(-getRealX(), 0); //We dont care about the Y value
		
		
	}
	
	public void enter()
	{
		System.out.println("Enter");
	}

	public void setText(String text)
	{
		this.text = text;
		//if(fm != null) updateContainer();
	}

	public void setFont(Font font)
	{
		this.font = font;
	}

	public void updateContainer()
	{
		int hgt = fm.getHeight();
		int adv = fm.stringWidth(text);
		getContainer().setSize(adv+2 , hgt-2);
	}

	public String getText() {
		return text;
	}

	public boolean validate(char keyChar)
	{
		return true;
	}
	
	public void addCharacter(char keyChar) {
		if(Character.isLetterOrDigit(keyChar) && validate(keyChar)) {
			text = text + keyChar;
		} else if(keyChar == (char)8 && text.length() > 0) {
			text = text.substring(0, text.length()-1);
		} else if(keyChar == (char)10) {
			enter();
		}
		//updateContainer();
	}
}
