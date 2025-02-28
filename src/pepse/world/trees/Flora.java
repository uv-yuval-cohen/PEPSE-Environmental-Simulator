package pepse.world.trees;

import danogl.collisions.GameObjectCollection;
import danogl.util.Vector2;
import pepse.world.Terrain;

import java.util.*;

import static pepse.PepseGameManager.SEED;

/**
 * Manages the creation and removal of trees in the game world. Handles tree placement,
 * ensuring alignment to a grid, and maintains active trees within a specified range.
 */
public class Flora {

    // =======================
    //   CONSTANTS
    // =======================
    private static final int TREE_SPACING = 210; // Distance between trees
    private static final int ALIGNMENT_FACTOR = 210; // Alignment factor for tree positioning
    private static final String ERROR_MINX_GREATER_THAN_MAXX = "minX must be smaller than maxX.";

    // =======================
    //   FIELDS
    // =======================
    private final Terrain terrain;
    private final GameObjectCollection gameObjects;
    private final Runnable fruitCallback;
    private final HashMap<Vector2, Tree> activeTrees = new HashMap<>();

    // =======================
    //   CONSTRUCTOR
    // =======================
    /**
     * Constructs a Flora instance responsible for managing trees in the game world.
     *
     * @param terrain      The terrain object, used to determine ground height.
     * @param gameObjects  The collection of game objects.
     * @param fruitCallback A callback function for handling tree fruit-related actions.
     */
    public Flora(Terrain terrain, GameObjectCollection gameObjects, Runnable fruitCallback) {
        this.terrain = terrain;
        this.gameObjects = gameObjects;
        this.fruitCallback = fruitCallback;
    }

    // =======================
    //   PUBLIC METHODS
    // =======================
    /**
     * Creates trees in a specified range of x-coordinates.
     *
     * @param minX The minimum x-coordinate of the range.
     * @param maxX The maximum x-coordinate of the range.
     * @return A list of created Tree objects.
     */
    public List<Tree> createInRange(int minX, int maxX) {
        if (minX >= maxX) {
            throw new IllegalArgumentException(ERROR_MINX_GREATER_THAN_MAXX);
        }

        int startX = alignToFactor(minX, true);
        int endX = alignToFactor(maxX, false);
        List<Tree> trees = new ArrayList<>();

        for (int x = startX; x <= endX; x += TREE_SPACING) {
            float groundHeight = terrain.groundHeightAt(x);
            Vector2 treePosition = new Vector2(x, groundHeight);

            if (!activeTrees.containsKey(treePosition)) {
                Tree tree = new Tree(
                        gameObjects,
                        treePosition,
                        fruitCallback,
                        new Random(Objects.hash(treePosition.x(), SEED))
                );
                tree.buildTree();
                trees.add(tree);
                activeTrees.put(treePosition, tree);
            }
        }

        return trees;
    }

    /**
     * Removes trees outside the specified range of x-coordinates.
     *
     * @param minX The minimum x-coordinate of the range.
     * @param maxX The maximum x-coordinate of the range.
     */
    public void removeTreesOutsideRange(int minX, int maxX) {
        List<Vector2> treesToRemove = new ArrayList<>();

        for (Vector2 treePosition : activeTrees.keySet()) {
            if (treePosition.x() > minX && treePosition.x() < maxX) {
                treesToRemove.add(treePosition);
            }
        }

        for (Vector2 treePosition : treesToRemove) {
            Tree tree = activeTrees.remove(treePosition);
            tree.removeTree(); // Removes all parts of the tree
        }
    }

    // =======================
    //   PRIVATE METHODS
    // =======================
    /**
     * Aligns a value to the nearest multiple of the alignment factor.
     *
     * @param value   The value to align.
     * @param roundUp True to round up, false to round down.
     * @return The aligned value.
     */
    private int alignToFactor(int value, boolean roundUp) {
        int remainder = value % ALIGNMENT_FACTOR;
        if (remainder == 0) {
            return value;
        }
        if (value < 0) {
            remainder = ALIGNMENT_FACTOR + remainder;
        }
        return roundUp ? value + (ALIGNMENT_FACTOR - remainder) : value - remainder;
    }
}