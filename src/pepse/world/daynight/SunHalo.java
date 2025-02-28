package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Utility class for creating a halo effect around the sun in the game world.
 */
public class SunHalo {

    // =======================
    //       CONSTANTS
    // =======================
    private static final Color HALO_COLOR = new Color(255, 255, 0, 20); // Yellow with transparency
    private static final float HALO_SCALE_FACTOR = 2.0f;                // Multiplier for halo size

    // =======================
    //    PUBLIC METHODS
    // =======================

    /**
     * Creates a halo effect for the given sun object.
     *
     * @param sun The sun GameObject around which the halo is created.
     * @return A GameObject representing the sun halo.
     */
    public static GameObject create(GameObject sun) {
        // Create the renderable for the halo
        OvalRenderable haloRenderer = new OvalRenderable(HALO_COLOR);

        // Calculate halo dimensions and position
        Vector2 sunDimensions = sun.getDimensions();
        Vector2 haloDimensions = sunDimensions.mult(HALO_SCALE_FACTOR);
        Vector2 haloCenter = sun.getCenter();

        // Create and configure the halo GameObject
        GameObject halo = new GameObject(haloCenter, haloDimensions, haloRenderer);
        halo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);

        return halo;
    }
}
