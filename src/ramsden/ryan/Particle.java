package ramsden.ryan;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;

import ramsden.ryan.GUI.GradientImage;

public class Particle extends Object {

	private double radius = 50;
	private double mass = 0;
	private Color color = new Color(0, true);
	private Image image = null;
	private Vector p = new Vector();
	private Vector v = new Vector();
	private Vector d = new Vector();
	
	private Ellipse2D ellipse = new Ellipse2D.Double();

	public static Random r = new Random();
	
	public boolean[] view = new boolean[8];
	private double bearing;
	
	/**
	 * Construct a dimensionless, massless, invisible,
	 * stationary particle at the origin.
	 */
	public Particle() {
		newColor();
		for(int i = 0; i < 8; i++) view[i] = true;
		mass = 1;
		radius = 100;
	}

	public Particle(Particle p) {
		this.radius = p.radius;
		this.mass = p.mass;
		this.color = p.color;
		this.p = new Vector(p.p);
		this.v = new Vector(p.v);
		this.view = p.view.clone();
	}

	/** Update this particle's Image. */
	public void updateImage() {
		setImage(GradientImage.createImage((int) radius+1, color));
	}

	public void newColor()
	{
		this.color = new Color(r.nextInt(200)+56, r.nextInt(200)+56, r.nextInt(200)+56);
	}
	
	public Shape getShape(Vector drawVector)
	{
		double radius = getR();
		double diameter = 2 * radius;
		ellipse.setFrame(drawVector.x, drawVector.y, diameter, diameter);
		return ellipse;
	}
	
	public static Vector getSumMomentum(Particle[] particles)
	{
		Vector v = new Vector();
		for(Particle p : particles)
		{
			v.add(p.getVelocity().scale(p.getM()));
		}
		return v;
	}

	public static Vector centroid(Particle[] particles)  {
		double centroidX = 0, centroidY = 0;
		for(Particle p : particles) {
			centroidX += p.getX();
			centroidY += p.getY();
		}
		return new Vector(centroidX / particles.length, centroidY / particles.length);
	}
	
	//public 
	
	public Vector getDrawPosition(float interpolation)
	{
		if(!Control.isPlaying()) return new Vector(p.x - radius, p.y - radius);
		int drawX = (int) ((p.x - d.x) * interpolation + d.x - radius);
		int drawY = (int) ((p.y - d.y) * interpolation + d.y - radius);
        return new Vector(drawX, drawY);
	}

	/** Return a new Vector with this particle's position. */
	public Vector getPosition() {
		return new Vector(p);
	}

	/** Return the given Vector set to this particle's position. */
	public Vector getPosition(Vector p) {
		p.x = this.p.x; p.y = this.p.y;
		return p;
	}

	/** Return this particle's x position. */
	public double getX() { return this.p.x; }

	/** Return this particle's y position. */
	public double getY() { return this.p.y; }

	/** Set this particle's position to the given Vector. */
	public void setPosition(Vector p) {
		this.p.x = p.x;
		this.p.y = p.y;
		
	}

	/** Set this particle's x, y position. */
	public void setPosition(double x, double y) {
		this.p.x = x;
		this.p.y = y;
	}

	/** Return a new Vector with this particle's velocity. */
	public Vector getVelocity() {
		return new Vector(v);
	}


	
	/** Return the given Vector set to this particle's velocity. */
	public Vector getVelocity(Vector v) {
		v.x = this.v.x; v.y = this.v.y;
		return v;
	}

	/** Return this particle's x velocity component. */
	public double getVx() { return this.v.x; }

	/** Return this particle's y velocity component. */
	public double getVy() { return this.v.y; }

	/** Return this particle's velocity magnitude. */
	public double getVNorm() {
		return v.norm();
	}

	/** Set this particle's velocity to the given Vector. */
	public void setVelocity(Vector v) {
		this.v.x = v.x; this.v.y = v.y;
	}

	/** Set this particle's x, y velocity components. */
	public void setVelocity(double vx, double vy) {
		this.v.x = vx; this.v.y = vy;
	}

	/** Set this particle's x, y velocity components. */
	public void setVelocity(double v) {
		if(getVx() == 0 && getVy() == 0 && v != 0) {
			double radians = Math.toRadians(bearing);
			this.v.x = Math.sin(radians);
			this.v.y = -Math.cos(radians);
		} else if (v == 0) setBearing(getBearing());
		this.setVelocity(this.v.unitVector().scale(v));
		
	}
	
	/** Set this particle's radius and imputed mass. */
	public void setR(double radius) {
		this.radius = radius;
	}

	/** Get this particle's radius. */
	public double getR() { return this.radius; }

	/** Set this particle's imputed mass. */
	public double getM() { return this.mass; }

	/** Get this particle's Color. */
	public Color getColor() { return this.color; }

	/** Set this particle's Color. */
	public void setColor(Color color) { this.color = color; updateImage(); }

	/** Set this particle's Image. */
	public Image getImage() { return image; }

	/** Set this particle's Image. */
	public void setImage(Image image) { this.image = image; }

	public void updateDrawPosition() {
		this.d.set(p);
	}
	public void setM(double value) {
		this.mass = value;
	}
	
	public double getBearing()
	{
		if(getVx() == 0 && getVy() == 0) return bearing;
		return (360+Math.toDegrees(Math.atan2(v.x, -v.y)))%360;
	}
	
	public void setBearing(double value)
	{
		bearing = (360+value)%360;
		double radians = Math.toRadians(value);
		double velocity = getVNorm();
		this.v.x = velocity*Math.sin(radians);
		this.v.y = -velocity*Math.cos(radians);
	}
	
	/** Get converted units */
	
	public double getConvertedVelocity()
	{
		return 3*getVNorm()/5;
	}
	
	public double getConvertedVelocityX()
	{
		return 3*getVx()/5;
	}
	
	public double getConvertedVelocityY()
	{
		return 3*getVy()/5;
	}
	
	public double getConvertedKE()
	{
		return getConvertedVelocity()*getConvertedVelocity()*getM()/2;
	}
	
	public double getConvertedMomentumX()
	{
		return getConvertedVelocityX()*getM();
	}
	
	public double getConvertedMomentumY()
	{
		return getConvertedVelocityY()*getM();
	}
	
	public double getConvertedMass()
	{
		return getM();
	}
	
	public double getConvertedRadius()
	{
		return getR()/50;
	}
	
	
	
	
	
	

}
