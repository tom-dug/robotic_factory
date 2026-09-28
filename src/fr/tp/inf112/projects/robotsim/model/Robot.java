package fr.tp.inf112.projects.robotsim.model;

import java.awt.DisplayMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.canvas.model.impl.RGBColor;
import fr.tp.inf112.projects.robotsim.model.motion.Motion;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class Robot extends Component {

	private static final long serialVersionUID = -1218857231970296747L;

	private static final Style STYLE = new ComponentStyle(RGBColor.GREEN, RGBColor.BLACK, 3.0f, null);

	private static final Style BLOCKED_STYLE = new ComponentStyle(RGBColor.RED, RGBColor.BLACK, 3.0f,
			new float[] { 4.0f });

	private final Battery battery;

	private int speed;

	private List<Component> targetComponents;

	private transient Iterator<Component> targetComponentsIterator;

	private Component currTargetComponent;

	private transient Iterator<Position> currentPathPositionsIter;

	private transient boolean blocked;

	private Position blockedTargetPosition;

	private FactoryPathFinder pathFinder;

	private Position nextPosition;

	public Robot(final Factory factory,
			final FactoryPathFinder pathFinder,
			final CircularShape shape,
			final Battery battery,
			final String name) {
		super(factory, shape, name);

		this.pathFinder = pathFinder;

		this.battery = battery;

		targetComponents = new ArrayList<>();
		currTargetComponent = null;
		currentPathPositionsIter = null;
		speed = 5;
		blocked = false;
		blockedTargetPosition = null;
	}

	@Override
	public String toString() {
		return super.toString() + " battery=" + battery + "]";
	}

	protected int getSpeed() {
		return speed;
	}

	protected void setSpeed(final int speed) {
		this.speed = speed;
	}

	public Position getBlockedTargetPosition() {
		return blockedTargetPosition;
	}

	private List<Component> getTargetComponents() {
		if (targetComponents == null) {
			targetComponents = new ArrayList<>();
		}

		return targetComponents;
	}

	public boolean addTargetComponent(final Component targetComponent) {
		return getTargetComponents().add(targetComponent);
	}

	public boolean removeTargetComponent(final Component targetComponent) {
		return getTargetComponents().remove(targetComponent);
	}

	@Override
	public boolean isMobile() {
		return true;
	}

	@Override
	public boolean behave() {
		if (getTargetComponents().isEmpty()) {
			return false;
		}

		if (currTargetComponent == null || hasReachedCurrentTarget()) {
			currTargetComponent = nextTargetComponentToVisit();

			computePathToCurrentTargetComponent();
		}

		return moveToNextPathPosition() != 0;
	}

	private Component nextTargetComponentToVisit() {
		if (targetComponentsIterator == null || !targetComponentsIterator.hasNext()) {
			targetComponentsIterator = getTargetComponents().iterator();
		}

		return targetComponentsIterator.hasNext() ? targetComponentsIterator.next() : null;
	}

	private int moveToNextPathPosition() {
		final int resolution = getFactory().getPathResolution();
		int displacement = 0;

		final Position targetPosition = getTargetPosition();
		if (targetPosition == null) {
			return 0;
		}

		final ReentrantLock targetLock = getFactory().getLocksMap()[targetPosition.getyCoordinate()
				/ resolution][targetPosition.getxCoordinate() / resolution];

		targetLock.lock();

		try {
			final Motion motion = computeMotion(targetPosition);
			displacement = motion == null ? 0 : motion.moveToTarget();

			if (displacement != 0) {
				notifyObservers();
			}

			else if (isLivelyLocked()) {
				final Position freeNeighbouringPosition = findFreeNeighbouringPosition();

				if (freeNeighbouringPosition != null) {
					this.nextPosition = freeNeighbouringPosition;
					displacement = moveToNextPathPosition();
					computePathToCurrentTargetComponent();
				}
			}
		} finally {
			targetLock.unlock();
		}

		return displacement;
	}

	private Position findFreeNeighbouringPosition() {
		int x_position = getPosition().getxCoordinate();
		int y_position = getPosition().getyCoordinate();
		int step = getFactory().getPathResolution();

		final Component otherComponent = getFactory().getMobileComponentAt(blockedTargetPosition,
				this);

		if (otherComponent == null) {
			return null;
		}

		if (blockedTargetPosition == null) {
			return null;
		}

		int x_blocked = otherComponent.getPosition().getxCoordinate();

		Position[] positions = new Position[2];

		if (x_position != x_blocked) {
			positions[0] = new Position(x_position, y_position + step);
			positions[1] = new Position(x_position, y_position - step);
		} else {
			positions[0] = new Position(x_position + step, y_position);
			positions[1] = new Position(x_position - step, y_position);
		}

		for (int i = 0; i < positions.length; i++) {
			final Position targetPosition = positions[i];
			final PositionedShape shape = new RectangularShape(targetPosition.getxCoordinate(),
					targetPosition.getyCoordinate(),
					2,
					2);
			if (!getFactory().hasObstacleAt(shape) && !getFactory().hasMobileComponentAt(shape, this)) {
				return positions[i];
			}
		}

		return null;

	}

	private void computePathToCurrentTargetComponent() {
		final List<Position> currentPathPositions = pathFinder.findPath(this, currTargetComponent);
		currentPathPositionsIter = currentPathPositions.iterator();
	}

	private Motion computeMotion(Position targetPosition) {
		if (targetPosition == null) {
			blocked = true;
			return null;
		}

		final PositionedShape shape = new RectangularShape(targetPosition.getxCoordinate(),
				targetPosition.getyCoordinate(),
				2,
				2);

		// If there is another robot, memorize the blocked target position for the next
		// run
		if (getFactory().hasMobileComponentAt(shape, this)) {
			this.blockedTargetPosition = targetPosition;

			return null;
		}

		// Reset the memorized position
		this.blockedTargetPosition = null;

		return new Motion(getPosition(), targetPosition);
	}

	private Position getTargetPosition() {
		// If a target position was memorized, it means that the robot was blocked
		// during the last iteration
		// so it waited for another robot to pass. So try to move to this memorized
		// position otherwise move to
		// the next position from the path
		if (this.nextPosition != null) {
			Position temp_nextPosition = this.nextPosition;
			this.nextPosition = null;
			return temp_nextPosition;
		}
		
		if (this.blockedTargetPosition != null) {
			return this.blockedTargetPosition;
		}

		if (this.currentPathPositionsIter != null && this.currentPathPositionsIter.hasNext()) {
			return this.currentPathPositionsIter.next();
		}

		return null;
	}

	public boolean isLivelyLocked() {
		if (blockedTargetPosition == null) {
			return false;
		}

		final Component otherComponent = getFactory().getMobileComponentAt(blockedTargetPosition,
				this);

		if (otherComponent instanceof Robot) {
			return getPosition().equals(((Robot) otherComponent).getBlockedTargetPosition());
		}

		return false;
	}

	private boolean hasReachedCurrentTarget() {
		return getPositionedShape().overlays(currTargetComponent.getPositionedShape());
	}

	@Override
	public boolean canBeOverlayed(final PositionedShape shape) {
		return true;
	}

	@Override
	public Style getStyle() {
		return blocked ? BLOCKED_STYLE : STYLE;
	}
}
