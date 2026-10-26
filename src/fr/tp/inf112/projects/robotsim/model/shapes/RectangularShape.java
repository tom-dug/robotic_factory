package fr.tp.inf112.projects.robotsim.model.shapes;

import fr.tp.inf112.projects.canvas.model.RectangleShape;

public class RectangularShape extends PositionedShape implements RectangleShape {
	
	private static final long serialVersionUID = -6113167952556242089L;

	private final int width;

	private final int height;

	public RectangularShape(final int xCoordinate,
							final int yCoordinate,
							final int width,
							final int height) {
		super(xCoordinate, yCoordinate);
	
		this.width = width;
		this.height = height;
	}

	public RectangularShape() {
		this(-1, -1, 0, 0);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public String toString() {
		return super.toString() + " [width=" + width + ", height=" + height + "]";
	}
}
