package pepse.world;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * Represents a single block in the game world.
 * Blocks are static objects with a fixed size and immovable physics.
 */
public class Block extends GameObject {

    // =======================
    //   CONSTANTS
    // =======================
    /**
     * Fixed size of each block (width and height)
     */
    public static final int SIZE = 30;

    // =======================
    //   CONSTRUCTOR
    // =======================
    /**
     * Constructs a Block at the specified position with the given renderable.
     * Blocks have a fixed size and are immovable.
     *
     * @param topLeftCorner The top-left corner of the block's position.
     * @param renderable    The renderable defining the block's appearance.
     */
    public Block(Vector2 topLeftCorner, Renderable renderable) {
        super(topLeftCorner, Vector2.ONES.mult(SIZE), renderable);

        // Prevent intersections with other objects
        physics().preventIntersectionsFromDirection(Vector2.ZERO);

        // Set the block's mass to immovable
        physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
    }
}