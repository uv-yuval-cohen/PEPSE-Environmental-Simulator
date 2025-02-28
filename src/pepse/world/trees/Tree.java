package pepse.world.trees;

import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.components.GameObjectPhysics;
import danogl.components.ScheduledTask;
import danogl.util.Vector2;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static pepse.PepseGameManager.SEED;
import static pepse.constants.TagConstants.FRUIT_TAG;
import static pepse.constants.TagConstants.TRUNK_TAG;

/**
 * Manages the creation of a single tree, including trunk, leaves, and fruit.
 */
public class Tree {

    // =======================
    //       CONSTANTS
    // =======================
    private static final float SPACE_BETWEEN_LEAVES = 3f;
    private static final float TRIANGLE_SPACE_BETWEEN_LEAVES = 18f;
    private static final int FRUIT_LAYER = Layer.STATIC_OBJECTS+2;

    private static final float TRUNK_WIDTH = 30f;      // Width of the trunk
    private static final float MIN_TRUNK_HEIGHT = 95f; // Height of each trunk segment
    private static final float MAX_TREE_HEIGHT = 150f; // Maximum height of the tree

    private static final float LEAF_SIZE = 30f;
    private static final int TRIANGLE_BASE_WIDTH = 4; // The base width of the triangle

    private static final float TRIANGLE_START_OFFSET_Y = 8f;  // Vertical shift before drawing triangle

    // -- Square leaves --
    private static final int SQUARE_NUM_LEAVES_PER_ROW = 4;
    private static final int SQUARE_NUM_ROWS = 4;
    private static final float SQUARE_OFFSET_X_FACTOR = 2f;   // For shifting leaves in X
    private static final float SQUARE_OFFSET_Y_FACTOR = 2.5f; // For shifting leaves in Y

    // -- Diamond leaves --
    private static final int DIAMOND_UPPER_TRIANGLE_ROWS = 2;
    private static final int DIAMOND_LOWER_TRIANGLE_ROWS = 3;


    private static final float FRUIT_SIZE = 20f;
    private static final float FRUIT_SPAWN_INTERVAL = 30f;
    private static final float FRUIT_CREATION_PROBABILITY = 0.2f;
    private static final Vector2 FRUIT_OFFSET = new Vector2(4, 4);
    private static final long MY_SEED = 20;
    private static final float LEAF_ROW_SPACING = 3f;

    private final Random random;
    private static final String SHAPE_SQUARE = "square";
    private static final String SHAPE_TRIANGLE = "triangle";
    private static final String SHAPE_DIAMOND = "diamond";
    private static final String[] SHAPES = {SHAPE_DIAMOND, SHAPE_TRIANGLE, SHAPE_SQUARE};



    // =======================
    //        FIELDS
    // =======================
    private Trunk trunk;
    private final Map<Vector2, LeafAndFruit> leavesMap = new HashMap<>();
    private Runnable fruitCallback;
    private final GameObjectCollection gameObjects; // Collection to manage game objects
    private final Vector2 position;                // Starting position of the tree

    // =======================
    //     CONSTRUCTOR
    // =======================
    /**
     * Creates a new Tree object.
     *
     * @param gameObjects   The collection of game objects to which this tree's parts will be added.
     * @param position      The starting position (x,y) of the tree.
     * @param fruitCallback A callback to invoke when fruit is collected.
     */
    public Tree(GameObjectCollection gameObjects, Vector2 position, Runnable fruitCallback, Random random) {
        this.gameObjects = gameObjects;
        this.position = position;
        this.fruitCallback = fruitCallback;
        this.random = random;
    }

    // =======================
    //    PUBLIC METHODS
    // =======================

    /**
     * Builds the trunk, leaves, and fruits for this tree instance.
     */



    public void buildTree() {
        // Randomize the height of the tree
        float treeHeight =(float) Math.ceil( MIN_TRUNK_HEIGHT + random.nextFloat() *
                (MAX_TREE_HEIGHT - MIN_TRUNK_HEIGHT) );
        createTrunk(treeHeight);
        createLeaves(treeHeight);
        addFruits();
        scheduleNewFruits();
    }

    /**
     * Removes the tree from the game.
     */
    public void removeTree() {
        // Remove the trunk
        if (trunk != null) {
            gameObjects.removeGameObject(trunk, Layer.STATIC_OBJECTS);
        }

        // Remove all leaves and fruits
        for (LeafAndFruit leafAndFruit : leavesMap.values()) {
            // Remove the leaf
            if (leafAndFruit.getLeaf() != null) {
                gameObjects.removeGameObject(leafAndFruit.getLeaf(), Layer.STATIC_OBJECTS + 1);
            }
            // Remove the fruit (if it exists)
            if (leafAndFruit.getFruit() != null) {
                gameObjects.removeGameObject(leafAndFruit.getFruit(), FRUIT_LAYER);
            }
        }

        // Clear the map of leaves and fruits
        leavesMap.clear();
    }

    // =======================
    //   PRIVATE METHODS
    // =======================

    private void scheduleNewFruits() {
        new ScheduledTask(
                this.trunk,
                FRUIT_SPAWN_INTERVAL,
                true,
                this::addNewFruits
        );
    }

    private void addNewFruits() {
        for (LeafAndFruit leafAndFruit : leavesMap.values()) {
            if (leafAndFruit.isFruitGotEaten() && leafAndFruit.getFruit() != null) {
                // Re-add the fruit to the game if it was eaten
                gameObjects.addGameObject(leafAndFruit.getFruit(), FRUIT_LAYER);
                leafAndFruit.setFruitGotEaten(false);
            }
        }
    }

    private void createTrunk(float height) {

        // Calculate the position for each trunk segment
        Vector2 segmentPosition = new Vector2(position.x(), position.y() - height);

        // Random shade factor for each segment
        float shadeFactor = random.nextFloat();
        // Create a trunk segment and add it to the game
        Trunk trunk = new Trunk(segmentPosition, new Vector2(TRUNK_WIDTH, height), shadeFactor);
        trunk.setTag(TRUNK_TAG); // Tag the trunk for collision handling
        trunk.physics().preventIntersectionsFromDirection(Vector2.ZERO);
        trunk.physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
        gameObjects.addGameObject(trunk, Layer.STATIC_OBJECTS);
        this.trunk = trunk;

    }

    private void createLeaves(float trunkHeight) {
        String shape = getRandomShape();

        switch (shape) {
            case SHAPE_SQUARE:
                createSquareLeaves(trunkHeight);
                break;
            case SHAPE_TRIANGLE:
                createTriangleLeaves(trunkHeight, TRIANGLE_BASE_WIDTH, true);
                break;
            default: // SHAPE_DIAMOND
                createDiamondLeaves(trunkHeight);
                break;
        }
    }

    private String getRandomShape() {
        int randomIndex = (int) (random.nextFloat() * SHAPES.length);
        return SHAPES[randomIndex];
    }

    private void createSquareLeaves(float trunkHeight) {
        float startX = position.x() + TRUNK_WIDTH/2 - SQUARE_OFFSET_X_FACTOR*
                (LEAF_SIZE + SPACE_BETWEEN_LEAVES) ;
        float startY = position.y() - trunkHeight - SQUARE_OFFSET_Y_FACTOR*
                (LEAF_SIZE + SPACE_BETWEEN_LEAVES) ;

        float leafSpacingX = LEAF_SIZE + SPACE_BETWEEN_LEAVES;
        float leafSpacingY = LEAF_SIZE + SPACE_BETWEEN_LEAVES;

        for (int row = 0; row < SQUARE_NUM_ROWS; row++) {
            for (int col = 0; col < SQUARE_NUM_LEAVES_PER_ROW; col++) {
                float x = startX + col * leafSpacingX;
                float y = startY + row * leafSpacingY;

                addLeaf(new Vector2(x, y));
            }
        }
    }

    private void  createTriangleLeaves(float trunkHeight, int numOfRows, boolean isUpsideDown) {
        float startX = position.x() + (TRUNK_WIDTH / 2);
        float startY = position.y() - trunkHeight - TRIANGLE_START_OFFSET_Y;
        float leafSpacing = LEAF_SIZE + TRIANGLE_SPACE_BETWEEN_LEAVES;

        for (int row = 0; row < numOfRows; row++) {
            int numLeavesInRow = isUpsideDown ? (row + 1) : (numOfRows - row);
            float rowWidth = getTriangleRowWidth(row, numOfRows, isUpsideDown);
            float rowStartX = startX - rowWidth/2;

            for (int col = 0; col < numLeavesInRow; col++) {
                float x = rowStartX + col * (leafSpacing);
                float y = startY - row * (LEAF_SIZE+LEAF_ROW_SPACING);

                addLeaf(new Vector2(x, y));
            }
        }
    }


    private void addLeaf(Vector2 leafPosition) {
        Leaf leaf = new Leaf(leafPosition, new Vector2(LEAF_SIZE, LEAF_SIZE), random.nextFloat());
        leaf.addMovement();
        gameObjects.addGameObject(leaf, Layer.STATIC_OBJECTS + 1);
        leavesMap.put(leafPosition, new LeafAndFruit(leaf));
    }

    private float getTriangleRowWidth(int row, int numOfRows, boolean isUpsideDown) {
        if (isUpsideDown) {
            return (row+1)*LEAF_SIZE + row*TRIANGLE_SPACE_BETWEEN_LEAVES;
        }
        int amountOfLeavesInRow = numOfRows - row;
        return amountOfLeavesInRow * LEAF_SIZE + (amountOfLeavesInRow-1)*TRIANGLE_SPACE_BETWEEN_LEAVES ;
    }

    private void createDiamondLeaves(float trunkHeight) {

        createTriangleLeaves(trunkHeight, DIAMOND_LOWER_TRIANGLE_ROWS, true);

        createTriangleLeaves(trunkHeight +
                DIAMOND_LOWER_TRIANGLE_ROWS * (LEAF_SIZE+DIAMOND_LOWER_TRIANGLE_ROWS) ,
                DIAMOND_UPPER_TRIANGLE_ROWS, false);
    }

    private void addFruits() {
        for (Map.Entry<Vector2, LeafAndFruit> entry : leavesMap.entrySet()) {
            if (random.nextFloat() < FRUIT_CREATION_PROBABILITY) {
                Vector2 leafPosition = entry.getKey().add(FRUIT_OFFSET);
                LeafAndFruit leafAndFruit = entry.getValue();

                Fruit fruit = new Fruit(
                        leafPosition,
                        new Vector2(FRUIT_SIZE, FRUIT_SIZE),
                        new Random(Objects.hash(leafPosition.x(), SEED))
                );

                fruit.setTag(FRUIT_TAG);

                fruit.setRunnable(() -> {
                    gameObjects.removeGameObject(fruit, FRUIT_LAYER); // Remove fruit from the game
                    leafAndFruit.setFruitGotEaten(true);
                    if (fruitCallback != null) {
                        fruitCallback.run();
                    }
                });

                gameObjects.addGameObject(fruit, FRUIT_LAYER);
                leafAndFruit.setFruit(fruit);

            }
        }
    }

    // =======================
    //   PRIVATE CLASSES
    // =======================
    private static class LeafAndFruit {
        private final Leaf leaf;

        public void setFruit(Fruit fruit) {
            this.fruit = fruit;
        }

        public void setFruitGotEaten(boolean fruitGotEaten) {
            this.fruitGotEaten = fruitGotEaten;
        }

        private Fruit fruit;
        private boolean fruitGotEaten = false;

        public LeafAndFruit(Leaf leaf) {
            this.leaf = leaf;
            this.fruit = null;
        }

        public Leaf getLeaf() {
            return leaf;
        }

        public Fruit getFruit() {
            return fruit;
        }

        public boolean isFruitGotEaten() {
            return fruitGotEaten;
        }
    }



}
