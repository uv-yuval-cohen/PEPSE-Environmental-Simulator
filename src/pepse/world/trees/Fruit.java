package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import pepse.world.Avatar;

import java.awt.*;
import java.util.Random;

/**
 * Represents a fruit in the game world.
 * A fruit has a random color and can be collected by the avatar.
 */
public class Fruit extends GameObject {

    // =======================
    //       CONSTANTS
    // =======================
    private static final String AVATAR_TAG = "avatar";      // Tag for the avatar
    private static final float MIN_RED_VALUE = 0.5f;        // Minimum red value for the fruit color
    private static final float RED_RANGE = 0.5f;           // Range of red value
    private static final float MAX_GREEN_BLUE_VALUE = 0.5f; // Maximum green and blue value
    private static final String NULL_RUNNABLE_ERROR = "onCollect callback is already set.";

    // =======================
    //     INSTANCE FIELDS
    // =======================
    private Runnable onCollect;

    // =======================
    //     CONSTRUCTOR
    // =======================

    /**
     * Constructs a new Fruit object.
     *
     * @param position   The position of the fruit in the game world.
     * @param dimensions The dimensions of the fruit.
     * @param random     A random number generator for generating colors.
     */
    public Fruit(Vector2 position, Vector2 dimensions, Random random) {
        super(position, dimensions, createRenderable(random));
        this.onCollect = null;
    }

    // =======================
    //    PUBLIC METHODS
    // =======================

    /**
     * Handles collision with other game objects.
     *
     * @param other     The other game object involved in the collision.
     * @param collision The collision data.
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        if (other.getTag().equals(AVATAR_TAG)) {
            if (onCollect != null) {
                onCollect.run();
            }
        }
    }

    /**
     * Sets the callback to be executed when the fruit is collected.
     *
     * @param onCollect A Runnable representing the callback.
     */
    public void setRunnable(Runnable onCollect) {
        if (this.onCollect != null) {
            throw new IllegalStateException(NULL_RUNNABLE_ERROR);
        }
        this.onCollect = onCollect;
    }

    // =======================
    //    PRIVATE METHODS
    // =======================

    /**
     * Creates a renderable object for the fruit with a random color.
     *
     * @param random A random number generator for generating the color.
     * @return An OvalRenderable object representing the fruit.
     */
    private static OvalRenderable createRenderable(Random random) {
        // Generate random color components
        float red = MIN_RED_VALUE + random.nextFloat() * RED_RANGE; // Shades of red
        float green = random.nextFloat() * MAX_GREEN_BLUE_VALUE;    // Low green value
        float blue = random.nextFloat() * MAX_GREEN_BLUE_VALUE;     // Low blue value
        Color fruitColor = new Color(red, green, blue);

        return new OvalRenderable(fruitColor);
    }
}
