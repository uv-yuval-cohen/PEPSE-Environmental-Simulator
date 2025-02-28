package pepse.world.trees;

import danogl.GameObject;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Represents a trunk in the game world.
 * A trunk is a rectangular object with a brownish color,
 * the shade of which can vary based on the given shade factor.
 */
public class Trunk extends GameObject {

    // =======================
    //       CONSTANTS
    // =======================
    private static final int BASE_RED_VALUE = 100;
    private static final int RED_SHADE_MULTIPLIER = 50;
    private static final int BASE_GREEN_VALUE = 50;
    private static final int GREEN_SHADE_MULTIPLIER = 25;
    private static final int BASE_BLUE_VALUE = 20;
    private static final int BLUE_SHADE_MULTIPLIER = 10;

    // =======================
    //    CONSTRUCTOR
    // =======================

    /**
     * Constructs a new Trunk object.
     *
     * @param topLeftCorner The top-left corner position of the trunk.
     * @param dimensions    The dimensions of the trunk.
     * @param shadeFactor   The factor determining the shade of the trunk color.
     */
    public Trunk(Vector2 topLeftCorner, Vector2 dimensions, float shadeFactor) {
        super(topLeftCorner, dimensions, createRenderable(shadeFactor));
    }

    // =======================
    //    PRIVATE METHODS
    // =======================

    /**
     * Creates a renderable object for the trunk with a color determined by the shade factor.
     *
     * @param shadeFactor The factor determining the shade of the trunk color.
     * @return A Renderable object with a brownish color.
     */
    private static Renderable createRenderable(float shadeFactor) {
        int redValue = BASE_RED_VALUE + (int) (shadeFactor * RED_SHADE_MULTIPLIER);
        int greenValue = BASE_GREEN_VALUE + (int) (shadeFactor * GREEN_SHADE_MULTIPLIER);
        int blueValue = BASE_BLUE_VALUE + (int) (shadeFactor * BLUE_SHADE_MULTIPLIER);
        return new RectangleRenderable(new Color(redValue, greenValue, blueValue));
    }
}
