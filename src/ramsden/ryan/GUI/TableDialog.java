package ramsden.ryan.GUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

public class TableDialog extends Interface {

	ArrayList<String[]> rowArray = new ArrayList<String[]>();
	public int columns;
	public double scroll;
	public int rowHeight;
	public int[] columnWidth = {0, 130, 175, 250};
	public Rectangle tablePosition;
	public static Font font = new Font("Verdana", Font.PLAIN, 12);
	public ResultsViewer parent;

	public static final Color fg = Color.BLACK;
	public static final Color bd = new Color(0xb2b2b2);
	public static final Color hl = new Color(161, 162, 206);
	
	final int borderSize = 4;
	private int start = 0;
	public int selectedRow = 0;

	public TableDialog(Interface i, Rectangle r, int col, int offsetX, int offsetY) {
		super(i, r);
		setColor(new Color(0x4F5A69));
		columns = col;
		rowHeight = 20;
		tablePosition = new Rectangle(offsetX, offsetY, columnWidth[3], r.height-offsetY-350);
		//columnWidth = tablePosition.width/columns;
		//System.out.println("CW: "+columnWidth);
	}

	@Override
	public void drawInterface(Graphics2D g2d, int offsetX, int offsetY) {
		Rectangle r = getContainer();
		
		g2d.setColor(bd);
		g2d.fillRect(r.x+offsetX, r.y+offsetY, r.width, r.height);
		g2d.setColor(getColor());
		g2d.fillRect(r.x+offsetX+borderSize, r.y+offsetY+borderSize, r.width-borderSize*2, r.height-borderSize*2);
		g2d.setColor(bd);
		g2d.fillRect(r.x+offsetX+tablePosition.x, r.y+offsetY+tablePosition.y, tablePosition.width, tablePosition.height);

		if(selectedRow >= start && selectedRow < start+tablePosition.height/rowHeight) {
			g2d.setColor(hl);
			g2d.fillRect(r.x+offsetX+tablePosition.x, r.y+offsetY+tablePosition.y+(selectedRow-start)*rowHeight, tablePosition.width, rowHeight);
		}
		g2d.setColor(Color.gray);

		for(int y = 0; y < tablePosition.height/rowHeight +1; y++) g2d.drawLine(r.x+offsetX+tablePosition.x, r.y+offsetY+tablePosition.y+rowHeight*y, r.x+offsetX+tablePosition.x+tablePosition.width-1, r.y+offsetY+tablePosition.y+rowHeight*y);
		for(int x = 0; x < columns+1; x++) g2d.drawLine(r.x+offsetX+tablePosition.x+columnWidth[x], r.y+offsetY+tablePosition.y, r.x+offsetX+tablePosition.x+columnWidth[x], r.y+offsetY+tablePosition.y+tablePosition.height);
		drawChildren(g2d, offsetX, offsetY);

		if(!rowArray.isEmpty()) {
			g2d.setColor(fg);
			g2d.setFont(font);
			start = (int) (scroll*(rowArray.size()-tablePosition.height/rowHeight));
			if(start < 0) start = 0;
			//if(start + tablePosition.height/rowHeight >= rows) start = rows - tablePosition.height/rowHeight;
			for(int y = 0; y < tablePosition.height/rowHeight; y++) {
				if(y >= rowArray.size()) break;
				for(int x = 0; x < columns; x++) 
					if(getText(x, start+y) != null)
						g2d.drawString(getText(x, start+y), r.x+offsetX+tablePosition.x+columnWidth[x]+2, (int) (tablePosition.y+r.y+offsetY+(y+1)*rowHeight-4));
			}
		}
	}

	@Override
	public void click(Point p, boolean onClickDown) {
		if(!onClickDown && tablePosition.contains(p)) {
			p.translate(-tablePosition.x, -tablePosition.y);
			selectedRow = start + p.y/rowHeight;
			if(getText(1, selectedRow) != null) parent.viewAnswers(getText(0, selectedRow), Integer.parseInt(getText(1, selectedRow)));
		}
	}
	
	public void clear()
	{
		rowArray.clear();
	}

	public void addRow(String[] row) {
		rowArray.add(row);
	}

	public void setText(int column, int row, String text)
	{
		rowArray.get(row)[column] = text;
	}

	public String getText(int column, int row)
	{
		if(row >= rowArray.size() || column >= columns) return null;
		return rowArray.get(row)[column];
	}

	public String getText(int row)
	{
		if(row >= rowArray.size()) return null;
		return rowArray.get(row)[0];
	}

}
