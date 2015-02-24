package ramsden.ryan.GUI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public class Dialog extends Interface {

	public Color border = new Color(0xb2b2b2);
	public Color inside = new Color(0x4F5A69);
	public int borderSize = 4;
	
	public Dialog(Interface i, Rectangle r) {
		super(i, r);
		setColor(inside);
	}

	@Override
	public void drawInterface(Graphics2D g2d, int offsetX, int offsetY) {
		Rectangle r = getContainer();
		g2d.setColor(border);
		g2d.fillRect(r.x+offsetX, r.y+offsetY, r.width, r.height);
		g2d.setColor(getColor());
		g2d.fillRect(r.x+offsetX+borderSize, r.y+offsetY+borderSize, r.width-borderSize*2, r.height-borderSize*2);
		drawChildren(g2d, offsetX, offsetY);
	}

	@Override
	public void click(Point p, boolean isClickDown) {
		//We dont need to do anything
	}

}
