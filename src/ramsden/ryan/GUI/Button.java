package ramsden.ryan.GUI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import ramsden.ryan.Control;

public class Button extends Interface {

	private boolean clicked;
	BufferedImage image = null;
	
	public Button(Interface i, Rectangle r, String text) {
		super(i, r);
		setColor(new Color(0xb2b2b2));
		if(text.startsWith("image:")) {
			setImage(getImage(text));
			text = "";
		}
		new TextComponent(this, text, new Point(0, 0));
	}

	@Override
	public void drawInterface(Graphics2D g2d, int offsetX, int offsetY) {
		Rectangle r = getContainer();
		g2d.setColor(getColor());
		if(image != null) g2d.drawImage(image, r.x+offsetX, r.y+offsetY, r.width, r.height, null);
		else g2d.fillRect(r.x+offsetX, r.y+offsetY, r.width, r.height);
		drawChildren(g2d, offsetX, offsetY);
		centerText();
	}

	@Override
	public void click(Point p, boolean onClickDown) {
		clicked = onClickDown;
	}

	@Override
	public Color getColor()
	{
		return (clicked ? super.getColor().darker() : super.getColor());
	}

	public String getText()
	{
		return getTextComponent().getText();
	}

	public static BufferedImage getImage(String s) {
		if(s.startsWith("image:")) {
			BufferedImage image = DisplayPanel.images.get(s);
			if(image == null) {
				try {
					image = ImageIO.read(Control.class.getResourceAsStream("/img/"+s.split("image:")[1]));
					DisplayPanel.images.put(s, image);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return image;
		}
		return null;
	}

	public void setImage(BufferedImage img)
	{
		image = img;
	}

	public void setText(String s)
	{
		if(s.startsWith("image:")) {
			setImage(getImage(s));
			s = "";
		}
		getTextComponent().setText(s);
		if(getTextComponent().container.width+8 > container.width) 
			setSize(getTextComponent().container.width+8, container.height);
		centerText();
	}

	public TextComponent getTextComponent()
	{
		return (TextComponent) children.get(0);
	}

	public void centerText()
	{
		int width = getTextComponent().getContainer().width;
		int height = getTextComponent().getContainer().height;
		getTextComponent().moveContainer((int)getContainer().width/2 - width/2, (int)getContainer().height/2 - height/2);
	}

}
