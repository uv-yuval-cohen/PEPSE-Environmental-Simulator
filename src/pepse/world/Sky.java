package pepse.world;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;

import static pepse.constants.TagConstants.SKY_TAG;

/**
 * Represents the sky in the game. Responsible for creating a sky background
 * that spans the entire game window.
 */
public class Sky {

    // =======================
    //   CONSTANTS
    // =======================
    private static final Color BASIC_SKY_COLOR = Color.decode("#80C6E5"); // Default sky color
    // =======================
    //   PUBLIC METHODS
    // =======================
    /**
     * Creates a sky GameObject that spans the entire window dimensions.
     *
     * @param windowDimensions The dimensions of the game window.
     * @return A GameObject representing the sky.
     */
    public static GameObject create(Vector2 windowDimensions) {
        GameObject sky = new GameObject(
                Vector2.ZERO,
                windowDimensions,
                new RectangleRenderable(BASIC_SKY_COLOR));
        sky.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sky.setTag(SKY_TAG);
        return sky;
    }
}
