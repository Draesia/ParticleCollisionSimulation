package ramsden.ryan.GUI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public class ScrollBar extends Interface {

	int scrollSize;
	double scrollAmount;
	double clickPoint;
	ResultsViewer father;
	public ScrollBar(Interface i, Rectangle r, int scrollSize, ResultsViewer rv) {
		super(i, r);
		this.father = (ResultsViewer) rv;
		this.scrollSize = scrollSize;
	}

	public int getMax()
	{
		return getContainer().height-scrollSize;
	}
	
	public double getScroll()
	{
		return scrollAmount/getMax();
	}
	
	@Override
	public void drawInterface(Graphics2D g2d, int offsetX, int offsetY) {
		g2d.setColor(Color.gray);
		g2d.fillRect(getContainer().x+offsetX, getContainer().y+offsetY, getContainer().width, getContainer().height);
		g2d.setColor(getColor());
		g2d.fillRect(getContainer().x+offsetX, (int) (getContainer().y+offsetY+scrollAmount), getContainer().width, scrollSize);
		g2d.setColor(Color.gray);
		g2d.drawRect(getContainer().x+offsetX, (int) (getContainer().y+offsetY+scrollAmount), getContainer().width, scrollSize);
	}

	@Override
	public void click(Point p, boolean isClickDown) {
		if(isClickDown && p.y > scrollAmount && p.y < scrollAmount+scrollSize)
		{
			clickPoint = p.y - scrollAmount;
			DisplayPanel.currentScrollBar = this;
		}
		
	}
	
	public void scroll(double d)
	{
		scrollAmount = scrollAmount + d;
		if(scrollAmount < 0) scrollAmount = 0;
		if(scrollAmount > getContainer().height - scrollSize) scrollAmount = getMax();
		//System.out.println("Scroll: "+getScroll()*100+"%");
		father.newScroll(getScroll());
	}
	
	public void scroll(Point mousePosition)
	{
		scrollAmount = mousePosition.y - clickPoint - getRealY();
		if(scrollAmount < 0) scrollAmount = 0;
		if(scrollAmount > getContainer().height - scrollSize) scrollAmount = getMax();
		//System.out.println("Scroll: "+getScroll()*100+"%");
		father.newScroll(getScroll());
	}
	
}
