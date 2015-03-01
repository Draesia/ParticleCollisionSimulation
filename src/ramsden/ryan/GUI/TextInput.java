package ramsden.ryan.GUI;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import ramsden.ryan.Control;

public class TextInput extends Interface {

	public String text;
	public Font font = new Font("Verdana", Font.PLAIN, 12);
	public FontMetrics fm;
	public int caratPosition, caratX;
	
	public TextInput(Interface i, String text, Rectangle r) {
		super(i, r);
		setText(text);
		setColor(new Color(0xe2e2e2));
		fm = new Canvas().getFontMetrics(font);
	}

	
	@Override
	public void drawInterface(Graphics2D g2d, int offsetX, int offsetY) {
		g2d.setColor(getColor());
		g2d.setFont(font);
		Rectangle r = getContainer();
		g2d.fillRect(r.x+offsetX, r.y+offsetY, r.width, r.height);
		g2d.setColor(Color.BLACK);
		g2d.drawString(getText(), r.x+2+offsetX, 8+r.y+fm.getHeight()/2+offsetY);
		//Not too happy with this.
		if(Control.selectedTextBox == this && System.currentTimeMillis()%1000<700) 
			g2d.drawLine(caratX+offsetX+r.x+2, r.y+offsetY+3, caratX+offsetX+r.x+2, r.y+offsetY+r.height-4);
		
	}

	
	
	@Override
	public void click(Point p, boolean onClickDown) {
		Control.selectedTextBox = this;
		p.translate(-4, 0); //This is so we dont have to round to the nearest letter.
		int caratPosition = 0;
		while(fm.stringWidth(getText().substring(0, caratPosition)) < p.getX()) 
			if(++caratPosition >= getText().length()) break;
		updateCarat(caratPosition);
	}
	
	public void updateCarat(int position)
	{
		caratPosition = Math.max(Math.min(position, getText().length()),0);
		caratX = fm.stringWidth(getText().substring(0, caratPosition));
	}
	public void enter()
	{
		System.out.println("Enter");
	}

	public void setText(String text)
	{
		if(text == null) text = "";
		this.text = text;
		if(text.length() > caratPosition) updateCarat(text.length());
	}

	public void setFont(Font font)
	{
		this.font = font;
	}

	public void updateContainer()
	{
		int hgt = fm.getHeight();
		int adv = fm.stringWidth(getText());
		getContainer().setSize(adv+2 , hgt-2);
	}

	public String getText() {
		if(text == null) return "";
		return text;
	}

	public boolean validate(char c)
	{
		return Character.isAlphabetic(c) || Character.isDigit(c);
	}
	
	public void addCharacter(int i, char c) {
		if(i == KeyEvent.VK_LEFT) updateCarat(caratPosition-1);
		else if(i == KeyEvent.VK_RIGHT) updateCarat(caratPosition+1);
		else if(i == 8 && text.length() > 0 && caratPosition > 0) {
			text = text.substring(0, caratPosition-1) + text.substring(caratPosition);
			updateCarat(caratPosition-1);
		} else if(i == 127 && text.length() > caratPosition) {
			text = text.substring(0, caratPosition) + text.substring(caratPosition+1);
		} else if(i == 10) {
			enter();
		} else if(validate(c)) {
			text = text.substring(0, caratPosition) + c + text.substring(caratPosition);
			updateCarat(caratPosition+1);
		} 
		
		
	}
}
