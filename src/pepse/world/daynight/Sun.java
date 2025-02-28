package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import pepse.world.Terrain;

import java.awt.*;

import static pepse.constants.TagConstants.SUN_TAG;

/**
 * A utility class for creating a sun object in the game world.
 * The sun moves in a circular path to simulate a day-night cycle.
 */
public class Sun {

    // =======================
    //       CONSTANTS
    // =======================
    private static final float MAX_CIRCLE_ANGLE = 360f;        // Full circle in degrees
    private static final int SUN_RADIUS = 90;                 // Radius of the sun
    private static final Color SUN_COLOR = Color.YELLOW;      // Color of the sun


    // =======================
    //    PUBLIC METHODS
    // =======================

    /**
     * Creates a sun object that moves in a circular path to simulate the day-night cycle.
     *
     * @param windowDimensions Dimensions of the window (used as sky dimensions).
     * @param cycleLength      Length of one complete day-night cycle in seconds.
     * @return A GameObject representing the sun.
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength) {
        // Create the renderable for the sun
        OvalRenderable yellowSunCircle = new OvalRenderable(SUN_COLOR);

        // Initial position of the sun (middle of the sky)
        Vector2 initialSunCenter = new Vector2(windowDimensions.x() / 2, Terrain.groundHeightAtX0 / 2);

        // Create the sun GameObject
        GameObject sun = new GameObject(
                initialSunCenter,
                new Vector2(SUN_RADIUS, SUN_RADIUS),
                yellowSunCircle
        );
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sun.setTag(SUN_TAG);

        // Calculate the cycle center (middle of the ground surface)
        Vector2 cycleCenter = new Vector2(windowDimensions.x() / 2, Terrain.groundHeightAtX0);

        // Create a transition for the sun's circular movement
        new Transition<>(
                sun,
                (Float angle) -> sun.setCenter(
                        initialSunCenter.subtract(cycleCenter)
                                .rotated(angle)
                                .add(cycleCenter)
                ),
                0f,
                MAX_CIRCLE_ANGLE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );

        return sun;
    }
}
