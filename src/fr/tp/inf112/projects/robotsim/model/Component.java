package fr.tp.inf112.projects.robotsim.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.canvas.model.Shape;

public abstract class Component implements Figure, Serializable, Runnable {

	private static final long serialVersionUID = -5960950869184030220L;

	private String id;

	private final Factory factory;

	private final PositionedShape positionedShape;

	private final String name;

	protected Component(final Factory factory,
			final PositionedShape shape,
			final String name) {
		this.factory = factory;
		this.positionedShape = shape;
		this.name = name;

		if (factory != null) {
			factory.addComponent(this);
		}
	}

	protected Component() {
		this(null, null, null);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public PositionedShape getPositionedShape() {
		return positionedShape;
	}

	@JsonIgnore
	public Position getPosition() {
		PositionedShape shape = getPositionedShape();
		return shape == null ? null : shape.getPosition();
	}

	protected Factory getFactory() {
		return factory;
	}

	@JsonIgnore
	@Override
	public int getxCoordinate() {
		PositionedShape shape = getPositionedShape();
		return shape == null ? -1 : shape.getxCoordinate();
	}

	protected boolean setxCoordinate(int xCoordinate) {
		PositionedShape shape = getPositionedShape();
		if (shape == null) {
			return false;
		}
		
		if (shape.setxCoordinate(xCoordinate)) {
			notifyObservers();

			return true;
		}

		return false;
	}

	@JsonIgnore
	@Override
	public int getyCoordinate() {
		PositionedShape shape = getPositionedShape();
		return shape == null ? -1 : shape.getyCoordinate();
	}

	protected boolean setyCoordinate(final int yCoordinate) {
		PositionedShape shape = getPositionedShape();
		if (shape == null) {
			return false;
		}

		if (shape.setyCoordinate(yCoordinate)) {
			notifyObservers();

			return true;
		}

		return false;
	}

	protected void notifyObservers() {
		Factory factory = getFactory();
		if (factory == null) {
			return;
		}
		
		factory.notifyObservers();
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [name=" + name + " xCoordinate=" + getxCoordinate() + ", yCoordinate="
				+ getyCoordinate()
				+ ", shape=" + getPositionedShape();
	}

	@JsonIgnore
	public int getWidth() {
		PositionedShape posShape = getPositionedShape();
		return posShape == null ? 0 : posShape.getWidth();
	}

	@JsonIgnore
	public int getHeight() {
		PositionedShape posShape = getPositionedShape();
		return posShape == null ? 0 : posShape.getHeight();
	}

	public boolean behave() {
		return false;
	}

	@JsonIgnore
	public boolean isMobile() {
		return false;
	}

	public boolean overlays(final Component component) {
		return overlays(component.getPositionedShape());
	}

	public boolean overlays(final PositionedShape shape) {
		PositionedShape posShape = getPositionedShape();
		return posShape == null ? false : posShape.overlays(shape);
	}

	public boolean canBeOverlayed(final PositionedShape shape) {
		return false;
	}

	@JsonIgnore
	@Override
	public Style getStyle() {
		return ComponentStyle.DEFAULT;
	}

	@JsonIgnore
	@Override
	public Shape getShape() {
		return getPositionedShape();
	}

	@JsonIgnore
	public boolean isSimulationStarted() {
		Factory factory = getFactory();
		return factory == null ? false : factory.isSimulationStarted();
	}

	@Override
	public void run() {
		while (isSimulationStarted()) {
			behave();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
	}
}
