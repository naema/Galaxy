package org.galaxy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class GalaxyScreen extends View implements View.OnTouchListener {

	List<Planet> planets = new CopyOnWriteArrayList<Planet>();
	List<Ship> ships = new CopyOnWriteArrayList<Ship>();

	Set<Planet> dragFrom = new CopyOnWriteArraySet<Planet>();
	Planet possibleTarget = null;

	Party player = null;
	Party computer = null;
	Party neutral = null;

	float fps = 10;

	public GalaxyScreen(Context context) {
		super(context);
		init();
	}

	public GalaxyScreen(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public GalaxyScreen(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		float boarder = 30;

		Paint playerPaint = new Paint();
		playerPaint.setColor(0xff00a000);
		Paint computerPaint = new Paint();
		computerPaint.setColor(0xffa00000);
		Paint neutralPaint = new Paint();
		neutralPaint.setColor(0xffa0a0a0);

		player = new Party("player", playerPaint);
		computer = new Party("computer", computerPaint);
		neutral = new Party("", neutralPaint);

		for (int i = 0; i < 6; i++) {
			float size = 20;
			float sr = (float) Math.random();
			if (sr > 0.4)
				size = 25;
			if (sr > 0.7)
				size = 30;

			float x = 0;
			float y = 0;
			while (true) {
				x = (float) (boarder + Math.random() * (320.f - 2.f * boarder));
				y = (float) (boarder + Math.random() * (400.f - 2.f * boarder));

				boolean collision = false;
				for (Planet p : planets) {
					Vector vp = p.getVector();
					Vector dist = vp.sub(new Vector(x, y));
					if (dist.length() < p.getSize() + size + 10.f)
						collision = true;
				}

				if (!collision)
					break;
			}

			Party party = neutral;
			if (i == 0)
				party = player;
			if (i == 1)
				party = computer;

			planets.add(new Planet(party, x, y, size, 5));
		}

		Thread thread = new Thread(new Runnable() {
			public void run() {
				cycle();
			}
		});
		thread.start();

		setOnTouchListener(this);
	}

	/**
	 * main thread loop
	 */
	private void cycle() {
		try {
			while (true) {

				Thread.sleep((int) (1000.f / fps));

				for (Planet planet : planets) {
					planet.grow((1.0f / fps));
				}

				List<Ship> deadShips = new CopyOnWriteArrayList<Ship>();
				for (Ship ship : ships) {
					ship.move((1.0f / fps));

					for (Planet planet : planets) {
						if (planet.detectCollision(ship)
								&& planet != ship.getSource()) {
							planet.collide(ship);
							deadShips.add(ship);
						}
					}
				}
				for (Ship ship : deadShips) {
					ships.remove(ship);
				}

				((Activity) this.getContext()).runOnUiThread(new Runnable() {
					public void run() {
						invalidate();
					}
				});
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void draw(Canvas canvas) {
		Paint paintText = new Paint();
		paintText.setColor(0xffffffff);
		paintText.setTextAlign(Align.CENTER);

		Paint paintPlanetSelect = new Paint();
		paintPlanetSelect.setColor(0x50ffffff);
		for (Planet planet : planets) {

			canvas.drawCircle(planet.getX(), planet.getY(), planet.getSize(),
					planet.getParty().getPaint());
			canvas.drawText("" + planet.getEnergy(), planet.getX(),
					planet.getY() + 3, paintText);
			
		  if (dragFrom.contains(planet) || planet == possibleTarget) {
			  canvas.drawCircle(planet.getX(), planet.getY(),
					  planet.getSize()+15, paintPlanetSelect); 
		  }
			 
		}
		Paint spp = new Paint();
		spp.setColor(0xffa00000);
		for (Ship ship : ships) {
			canvas.drawCircle(ship.getX(), ship.getY(), 2, ship.getParty().getPaint());
			canvas.drawCircle(ship.getSp1().getX(), ship.getSp1().getY(), 2, spp);
			canvas.drawCircle(ship.getSp2().getX(), ship.getSp2().getY(), 2, spp);
		}

		super.draw(canvas);
	}

	private Planet findPlanet(float x, float y, Party party) {
		for (Planet planet : planets) {
			if ((party == null || planet.getParty() == party)
					&& planet.isHit(x, y, 30))
				return planet;
		}
		return null;
	}

	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Planet planetDragFrom = findPlanet(event.getX(), event.getY(),
					player);
			if (planetDragFrom != null)
				dragFrom.add(planetDragFrom);

		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			possibleTarget = null;
			if (dragFrom.size() > 0) {
				Planet planetDragFrom = findPlanet(event.getX(), event.getY(),
						player);
				if (planetDragFrom != null)
					dragFrom.add(planetDragFrom);

				possibleTarget = findPlanet(event.getX(), event.getY(), null);
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			Planet dragTo = findPlanet(event.getX(), event.getY(), null);
			if (dragTo != null) {
				for (Planet planetDragFrom : dragFrom) {
					ships.addAll(planetDragFrom.launch(dragTo));
				}
			}
			dragFrom.clear();
			possibleTarget = null;
		}

		return true;
	}

}

