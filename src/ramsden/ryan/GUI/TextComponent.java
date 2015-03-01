package ramsden.ryan.GUI;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public class TextComponent extends Interface {

	public String text;
	public Font font = new Font("Verdana", Font.PLAIN, 12);
	
	public TextComponent(Interface i, String text, Point p) {
		super(i, new Rectangle(p.x, p.y, 0, 0));
		setText(text);
		setStyle(Font.BOLD);
		if(i instanceof Dialog) setColor(Color.LIGHT_GRAY);
		else setColor(Color.BLACK);
		click = false;
	}
	
	public TextComponent(Interface i, String text, Point p, int size) {
		this(i, text, p);
		setSize(size);
	}
	
	public TextComponent(Interface i, String text, Point p, int size, int style) {
		this(i, text, p, size);
		setStyle(style);
	}
	
	public TextComponent(Interface i, String text, Point p, int size, int style, Color c) {
		this(i, text, p, size, style);
		setColor(c);
	}

	@Override
	public void drawInterface(Graphics2D g2d, int offsetX, int offsetY) {
		g2d.setColor(getColor());
		g2d.setFont(font);
		Rectangle r = getContainer();
		g2d.drawString(text, r.x+offsetX, r.y+offsetY+r.height);
	}

	@Override
	public void click(Point p, boolean onClickDown) {}

	public void setText(String text)
	{
		this.text = text;
		updateContainer();
	}

	public void setFont(Font font)
	{
		this.font = font;
		
	}
	
	public void setStyle(int style)
	{
		setFont(font.deriveFont(style, font.getSize()));
	}
	
	public void setSize(int px)
	{
		setFont(font.deriveFont(font.getStyle(), px));
	}

	public void updateContainer()
	{
		FontMetrics fm = new Canvas().getFontMetrics(font);
		int hgt = fm.getHeight();
		int adv = fm.stringWidth(text);
		getContainer().setSize(adv+2 , hgt-2);
	}

	public String getText() {
		return text;
	}
}
