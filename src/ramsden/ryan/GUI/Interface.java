package ramsden.ryan.GUI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JTextField;

import ramsden.ryan.Control;

@SuppressWarnings("unused")
public abstract class Interface {
	
	public static ArrayList<Interface> mainInterfaces = new ArrayList<Interface>();
	
	protected ArrayList<Interface> children;
	protected Rectangle container;
	protected boolean click;
	private Color color;
	private boolean hidden;
	private Image overlay;
	private Interface parent;
	private Button close;
	
	public Interface(Interface i, Rectangle r) {
		this.setContainer(r);
		if(i != null) {
			i.addChild(this);
			setParent(i);
		} else mainInterfaces.add(this);
		click = true;
	}
	
	public void setCloseable(boolean b)
	{
		if(b) {
			if(close == null) close = new Button(this, new Rectangle(container.width-28, 4, 24, 24), "image:close.png") {
				@Override
				public void click(Point p, boolean b)
				{
					this.getParent().onClose();
				}
			};
		} else close = null;
	}
	
	public void onClose() {
		this.setHidden(true);
	}
	
	public void remove() {
		if(getParent() != null) getParent().children.remove(this);
		else mainInterfaces.remove(this);
	}
	

	public void setSize(int w, int h) {
		container.setSize(w, h);
		if(close!=null)close.moveContainer(container.width-28, 4);
	}

	
	public abstract void drawInterface(Graphics2D g2d, int offsetX, int offsetY);
	public abstract void click(Point p, boolean isClickDown);
	
	public void drawChildren(Graphics2D g2d, int offsetX, int offsetY)
	{
		if(children != null && children.size() > 0) for(Interface i : children) if(i != null && !i.isHidden()) i.drawInterface(g2d, getContainer().x+offsetX, getContainer().y+offsetY);
	}
	
	public void clicked(Point p, boolean isClickDown)
	{
		
		p.translate(-getContainer().x, -getContainer().y);
		if(children != null) for(Interface c : children) if(c.canClick() && c.getContainer().contains(p)) { c.clicked(p, isClickDown); return; }
		click(p, isClickDown);
	}
	
	public int getRealX()
	{
		Interface i = this;
		int x = (int) getContainer().getX();
		while(i.getParent() != null)
		{
			i = i.getParent();
			x = (int) (x + i.getContainer().getX());
		}
		return x;
	}
	
	public int getRealY()
	{
		Interface i = this;
		int y = (int) getContainer().getY();
		while(i.getParent() != null)
		{
			i = i.getParent();
			y = (int) (y + i.getContainer().getY());
		}
		return y;
	}
	
	public boolean canClick() {
		return click;
	}

	public Color getColor()
	{
		return color;
	}
	
	public Rectangle getContainer() {
		return container;
	}

	public void setContainer(Rectangle container) {
		this.container = container;
	}
	
	public void moveContainer(int x, int y)
	{
		this.container.setLocation(x, y);
	}
	
	public void centerOnScreen()
	{
		int x = Control.width/2 - this.container.width/2;
		int y = Control.height/2 - this.container.width/2;
		this.container.setLocation(x, y);
	}
	
	public int getAmountChildren()
	{
		return children.size();
	}
	
	public Interface getChild(int i)
	{
		if(i < children.size()) return children.get(i);
		return null;
	}
	
	public void addChild(Interface child)
	{
		if(children == null) children = new ArrayList<Interface>();
		children.add(child);
	}
	
	public void removeChild(Interface child)
	{
		children.remove(child);
	}
	
	public void clearChildren()
	{
		children.clear();
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
		
	}

	public Image getImage() {
		return overlay;
	}

	public void setImage(Image overlay) {
		this.overlay = overlay;
	}
	
	public void setImage(String image) {
		
	}

	public Interface getParent() {
		return parent;
	}

	public void setParent(Interface parent) {
		this.parent = parent;
	}
	
}
