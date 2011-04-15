package org.galaxy;

public class Ship {
	Planet source = null;
	Planet dest =  null;
	
	float x;
	float y;
	float speed;
	
	float fuzzyness = 5f;
	
	public Ship(Planet source, Planet dest, float speed) {
		this.source = source;
		this.dest = dest;
		this.speed = speed;
		x = source.getX();
		y = source.getY();
		
		Vector delta = dest.getVector().sub(getVector()); 
		delta = delta.normalize();
		Vector move = delta.multiply(source.getSize());
//		
//		float dx = dest.getX() - x;
//		float dy = dest.getY() - y;
//		float length = (float) Math.sqrt(dx*dx+dy*dy);
//		dx /= length;
//		dy /= length;
//		dx *= source.getSize();
//		dy *= source.getSize();
		
		x += move.getX();
		y += move.getY();		
	}
		
	public void move(float period) {		
		float dx = dest.getX() - x;
		float dy = dest.getY() - y;
		float length = (float) Math.sqrt(dx*dx+dy*dy);
		dx /= length;
		dy /= length;
		dx *= speed * period;
		dy *= speed * period;
		
		dx += (Math.random() * fuzzyness) - fuzzyness / 2.f;
		dy += (Math.random() * fuzzyness) - fuzzyness / 2.f;
		
		x += dx;
		y += dy;
		
	}

	public Planet getSource() {
		return source;
	}

	public Planet getDest() {
		return dest;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}	
	
	public Vector getVector() {
		return new Vector(x, y);
	}
}