package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * A utility class for creating the night effect in the game world.
 * The night effect gradually increases and decreases its opacity
 * to simulate a day-night cycle.
 */
public class Night {

    // =======================
    //       CONSTANTS
    // =======================
    private static final float MIDNIGHT_OPACITY = 0.5f;      // Maximum opacity at midnight
    private static final float INITIAL_OPACITY = 0f;        // Initial opacity at daytime
    private static final Color NIGHT_COLOR = Color.BLACK;   // Color of the night overlay
    private static final String NIGHT_TAG = "night";        // Tag for the night GameObject

    // =======================
    //    PUBLIC METHODS
    // =======================

    /**
     * Creates a night effect object that transitions its opacity over a given cycle length.
     *
     * @param windowDimensions The dimensions of the game window (used for the night overlay).
     * @param cycleLength      Length of the day-night cycle in seconds.
     * @return A GameObject representing the night effect.
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength) {
        // Create the night overlay
        GameObject night = new GameObject(
                new Vector2(Vector2.ZERO),
                windowDimensions,
                new RectangleRenderable(NIGHT_COLOR)
        );
        night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        night.setTag(NIGHT_TAG);

        // Add a transition to change the opacity of the night effect
        new Transition<>(
                night,
                night.renderer()::setOpaqueness,
                INITIAL_OPACITY,
                MIDNIGHT_OPACITY,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );

        return night;
    }
}
