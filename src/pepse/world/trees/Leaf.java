package pepse.world.trees;

import danogl.GameObject;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Represents a leaf in the game world.
 * A leaf can move and change its size and angle dynamically over time.
 */
public class Leaf extends GameObject {

    // =======================
    //       CONSTANTS
    // =======================
    private static final float TRANSITION_DURATION = 2f;
    private static final float MAX_RANDOM_DELAY = 5f;
    private static final float MIN_RENDERABLE_ANGLE = -5f;
    private static final float MAX_RENDERABLE_ANGLE = 5f;
    private static final float MIN_SIZE_FACTOR = 0.95f;
    private static final int BASE_GREEN_VALUE = 150;
    private static final int GREEN_SHADE_MULTIPLIER = 55;
    private static final int RED_VALUE = 50;
    private static final int BLUE_VALUE = 30;

    // =======================
    //     CONSTRUCTOR
    // =======================

    /**
     * Constructs a new Leaf object.
     *
     * @param topLeftCorner The top-left corner position of the leaf.
     * @param dimensions    The dimensions of the leaf.
     * @param shadeFactor   The factor determining the shade of the leaf's color.
     */
    public Leaf(Vector2 topLeftCorner, Vector2 dimensions, float shadeFactor) {
        super(topLeftCorner, dimensions, createRenderable(shadeFactor));
    }

    // =======================
    //     PUBLIC METHODS
    // =======================

    /**
     * Adds a scheduled movement to the leaf.
     * The movement starts after a random delay of up to {@value MAX_RANDOM_DELAY} seconds.
     */
    public void addMovement() {
        float delay = (float) Math.random() * MAX_RANDOM_DELAY;
        new ScheduledTask(
                this,
                delay,
                false,
                this::startMovement
        );
    }

    // =======================
    //    PRIVATE METHODS
    // =======================

    /**
     * Starts the movement of the leaf, including changes in angle and size.
     */
    private void startMovement() {

        // Transition for angle movement
        new Transition<>(
                this,
                renderer()::setRenderableAngle,
                MIN_RENDERABLE_ANGLE,
                MAX_RENDERABLE_ANGLE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                TRANSITION_DURATION,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );

        // Transition for size movement
        new Transition<>(
                this,
                size -> setDimensions(new Vector2(size, size)),
                getDimensions().x() * MIN_SIZE_FACTOR, // 95% of original size
                getDimensions().x(),                  // 100% of original size
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                TRANSITION_DURATION,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );
    }

    /**
     * Creates a renderable object for the leaf with a green shade based on the given factor.
     *
     * @param shadeFactor The factor determining the shade of the leaf color.
     * @return A Renderable object representing the leaf.
     */
    private static Renderable createRenderable(float shadeFactor) {
        int greenValue = BASE_GREEN_VALUE + (int) (shadeFactor * GREEN_SHADE_MULTIPLIER);
        return new RectangleRenderable(new Color(RED_VALUE, greenValue, BLUE_VALUE));
    }
}
