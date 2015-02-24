package ramsden.ryan;

import java.util.ArrayList;

import ramsden.ryan.GUI.DisplayPanel;

public class Engine {

	public static final int MAX_ATOMS = 20;
	private static final int MIN_RADIUS = 50;
	private ArrayList<Particle> atoms;


	private Vector p1 = new Vector(); // position
	private Vector p2 = new Vector();
	private Vector v1 = new Vector(); // velocity 
	private Vector v2 = new Vector();
	private Vector n  = new Vector(); // normal vector
	private Vector un = new Vector(); // unit normal
	private Vector ut = new Vector(); // unit tangent

	public int collisions;

	/** Construct a container of atoms. */
	public Engine() {
		atoms = new ArrayList<Particle>(MAX_ATOMS);
	}
	
	@SuppressWarnings("unchecked")
	public Engine(Engine copy) {
		setState(copy);
	}
	
	public void setState(Engine copy) {
		atoms = new ArrayList<Particle>(MAX_ATOMS);
		collisions = 0;
		for(Particle p : copy.atoms) atoms.add(new Particle(p));
	}
	
	
	public void updateImages() {
		for (Particle atom : getAtoms()) {atom.updateImage(); atom.updateDrawPosition();}
	}

	public void updateGame() {
		for (Particle atom : getAtoms()) iterate(atom);
		for (Particle atom : getAtoms()) checkCollisions(atom);


		//		for (int i = 0; i < getAtoms().size(); i++) {
		//			Particle p = getAtoms().get(i);
		//			Vector p1 = p.getPosition();
		//			if(p1.subtract(new Vector(10, 10)).norm() < p.getR()) getAtoms().remove(p);
		//			if(p1.subtract(new Vector(10, 0)).norm() < p.getR()) getAtoms().remove(p);
		//			if(p1.subtract(new Vector(Control.width-10, 0)).norm() < p.getR()) getAtoms().remove(p);
		//			if(p1.subtract(new Vector(Control.width-10, 0)).norm() < p.getR()) getAtoms().remove(p);
		//		}
	}

	/** Advance each atom. */
	public void iterate(Particle atom1) {
		if(this == Control.model) atom1.updateDrawPosition();
		if(DisplayPanel.currentDrag == atom1) return;
		p1 = atom1.getPosition();
		v1 = atom1.getVelocity();
		p1.add(v1);
		atom1.setPosition(p1);
		
	}



	public void checkCollisions(Particle atom1)
	{
		ArrayList<Particle> collisions = new ArrayList<Particle>();
		for (int i = atoms.indexOf(atom1); i < atoms.size(); i++) {
			Particle atom2 = atoms.get(i);
			if(atom1 == atom2) continue;
			if(willCollide(atom1, atom2)) collisions.add(atom2); 
		}
		for(Particle atom2 : collisions)
		{
			collideAtoms(atom1, atom2);			
			//			atom1.updateDrawPosition();
			//			atom2.updateDrawPosition();
		}
		collideWalls(atom1);
	}

	public boolean willCollide(Particle a1, Particle a2) {
		double radius = a1.getR() + a2.getR();
		p1 = a1.getPosition(new Vector());
		p2 = a2.getPosition(new Vector());
		n = n.set(p1).subtract(p2);
		double norm = n.norm();
		return (norm < radius);
	}

	private void collideAtoms(Particle a1, Particle a2) {

		collisions++;
		
		if(Control.stepMode) Control.playing = false;
		

		double radius = a1.getR() + a2.getR();
		//Vector momentumVectorBefore = a1.getVelocity().scale(a1.getM()).add(a2.getVelocity().scale(a2.getM()));
		//		// Move to start of collision

		double divideBy = Math.max(a1.getVelocity().norm(), a2.getVelocity().norm());
		if(divideBy == 0) divideBy = 5;
		for(int i = 0;i < Math.ceil(divideBy); i++) {
			//for (Particle atom : getAtoms()) uniterate(atom, divideBy);
			a1.setPosition(a1.getPosition().subtract(a1.getVelocity().divide(divideBy)));
			a2.setPosition(a2.getPosition().subtract(a2.getVelocity().divide(divideBy)));
			if(!willCollide(a1, a2)) break;
		}
		//if(i > 0) {
		
		p1 = a1.getPosition(new Vector());
		p2 = a2.getPosition(new Vector());
		n = n.set(p1).subtract(p2); //Relative Vector
		
		if(willCollide(a1, a2)) {
			double dr = (radius - n.norm()) / 2;
			un = un.set(n).unitVector();
			p1.add(un.scale(dr));
			un = un.set(n).unitVector();
			p2.add(un.scale(-dr));
			a1.setPosition(p1);
			a2.setPosition(p2);
		} else {
			a1.setPosition(a1.getPosition().add(a1.getVelocity().divide(divideBy)));
			a2.setPosition(a2.getPosition().add(a2.getVelocity().divide(divideBy)));
			//}
		}

		p1 = a1.getPosition(new Vector());
		p2 = a2.getPosition(new Vector());

		// Find normal and tangential components of v1/v2
		n = n.set(p1).subtract(p2);
		un = un.set(n).unitVector();
		ut = ut.set(-un.y, un.x);
		v1 = a1.getVelocity(v1);
		v2 = a2.getVelocity(v2);            
		double v1n = un.dot(v1);
		double v1t = ut.dot(v1);
		double v2n = un.dot(v2);
		double v2t = ut.dot(v2);
		// Calculate new v1/v2 in normal direction
		double m1 = a1.getM();
		double m2 = a2.getM();
		double v1nNew = Control.efficiency*(v1n * (m1 - m2) + 2d * m2 * v2n) / (m1 + m2);
		double v2nNew = Control.efficiency*(v2n * (m2 - m1) + 2d * m1 * v1n) / (m1 + m2);
		// Update velocities with sum of normal & tangential components
		v1 = v1.set(un).scale(v1nNew);
		v2 = v2.set(ut).scale(v1t);
		a1.setVelocity(v1.add(v2));
		v1 = v1.set(un).scale(v2nNew);
		v2 = v2.set(ut).scale(v2t);
		a2.setVelocity(v1.add(v2));
	}

	public boolean willCollideWhenPlaced(Particle atom1)
	{
		for (int i = 0; i < getAtoms().size(); i++) {
			Particle atom2 = getAtoms().get(i);
			if(atom1 == atom2) continue;
			if(willCollide(atom1, atom2)) return true;
		}
		return false;
	}
	
	// Check for collision with wall
	private void collideWalls(Particle atom) {
		double radius = atom.getR();
		p1 = atom.getPosition(p1);
		v1 = atom.getVelocity(v1);
		if (p1.x < radius) {
			p1.x = radius;
			v1.x = -v1.x;
		}
		if (p1.y < radius) {
			p1.y = radius;
			v1.y = -v1.y;
		}
		if (p1.x > Control.width - radius) {
			p1.x = Control.width - radius;
			v1.x = -v1.x;
		}
		if (p1.y > Control.height - radius) {
			p1.y = Control.height - radius;
			v1.y = -v1.y;
		}
		atom.setPosition(p1);
		atom.setVelocity(v1);
	}

	public void clear()
	{
		atoms.clear();
	}

	public ArrayList<Particle> getAtoms() { return atoms; }

	public void addAtom(Particle p) {
		atoms.add(p);
	}

	public Particle getNewAtom()
	{
		Particle p = new Particle();
		p.setR(50);
		p.updateImage();

		return p;
	}


}
