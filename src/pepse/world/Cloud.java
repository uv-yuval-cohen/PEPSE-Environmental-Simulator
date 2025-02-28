package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static pepse.constants.TagConstants.CLOUD_BLOCK_TAG;

/**
 * Represents a cloud in the game. The cloud consists of a pattern of blocks
 * and can create raindrops. Clouds move across the screen at a specified velocity,
 * and their appearance is determined by a defined pattern.
 */
public class Cloud  extends GameObject {


    // =======================
    //   CONSTANTS
    // =======================
    private static final float DROPS_VELOCITY = 50;
    private static final int BLOCK_PRESENT_INDICATOR = 1; // Indicates presence of a block in the pattern
    private static final int MIN_NUMBER_DROPS = 1; // Minimum number of raindrops
    private static final int MAX_NUMBER_DROPS = 15; // Maximum number of raindrops
    private static final int FIRST_POSITION_IN_ARRAY = 0; // Index for the first position in an array
    private static final int SECOND_POSITION_IN_ARRAY = 1; // Index for the second position in an array
    private static final int THIRD_POSITION_IN_ARRAY = 2; // Index for the third position in an array
    private static final int CLOUD_MOVING_TIME = 15;
    private static final int DROP_SIZE = 10; // Size of each raindrop
    private static final int DROP_ACCELERATION = 300; // Acceleration of raindrops
    // (in pixels per second squared)
    private static final float INITIAL_DROP_TRANSPARENCY = 1f; // Initial transparency of raindrops
    private static final float FINAL_DROP_TRANSPARENCY = 0f; // Final transparency of raindrops
    private static final float DROP_FALLING_TIME = 1f; // Time (in seconds) for a drop to fall


    // =======================
    //   FIELDS
    // =======================
    private final Vector2 topLeftCorner; // Top-left corner of the cloud
    private final List<List<Integer>> cloudPattern; // Pattern defining the cloud's shape
    private final Vector2 windowDimensions; // Dimensions of the game window
    private final Renderable renderer; // Renderable for the cloud's appearance
    private final List<List<Block>> cloudBlocks; // Blocks representing the cloud
    private final GameObjectCollection gameObjects; // Collection of all game objects
    private final List<GameObject> raindrops = new ArrayList<>(); // List of raindrop GameObjects
    private Vector2 cloudDimensions; // Dimensions of the cloud

    private Runnable onRemoveCallback; // handle removal from avatars listeners list



    // =======================
    //   CONSTRUCTOR
    // =======================
    /**
     * Constructs a cloud object at the specified position and dimensions.
     * Initializes the cloud's pattern and blocks and sets its horizontal velocity.
     *
     * @param topLeftCorner   The top-left corner of the cloud's position.
     * @param dimensions      The dimensions of the cloud.
     * @param renderable      The renderable defining the appearance of the cloud.
     * @param objectsCollection The collection of all game objects.
     * @param windowDimensions The dimensions of the game window.
     */
    public Cloud(Vector2 topLeftCorner, Vector2 dimensions, Renderable renderable,
                 GameObjectCollection objectsCollection, Vector2 windowDimensions) {
        super(topLeftCorner, Vector2.ZERO, null);
        this.topLeftCorner = topLeftCorner;
        this.windowDimensions = windowDimensions;
        this.renderer = renderable;
        this.gameObjects = objectsCollection;
        this.cloudDimensions = dimensions;
        this.cloudPattern = initializeCloudPattern();
        this.cloudBlocks = initializeCloudFromBlocks();
        //set cloud position change
        new Transition<>(
                this,
                (Float delta) -> this.setTopLeftCorner(topLeftCorner.add(new Vector2(delta,0))),
                0f,
                windowDimensions.x(),
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                CLOUD_MOVING_TIME,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );


    }


    // =======================
    //   PUBLIC METHODS
    // =======================


    /**
     * Creates the cloud blocks based on the defined cloud pattern.
     * Each block is added to the game object collection and assigned velocity.
     *
     * @return A 2D list representing the cloud blocks.
     */
    public List<List<Block>> initializeCloudFromBlocks() {
        int relativePositionX;
        int relativePositionY = 0;
        List<List<Block>> cloudBlocks = new ArrayList<>();

        for (int i = 0; i < cloudPattern.size(); i++) {
            // Create a new list of blocks for each row
            List<Block> blocks = new ArrayList<>();
            relativePositionX = 0; // Reset X position for each row

            for (int j = 0; j < cloudPattern.get(i).size(); j++) {
                if (cloudPattern.get(i).get(j) == BLOCK_PRESENT_INDICATOR) {
                    Vector2 position = new Vector2(
                            this.topLeftCorner.x() + relativePositionX,
                            this.topLeftCorner.y() + relativePositionY
                    );
                    Block cloudBlock = new Block(
                           position,
                            this.renderer
                    );
                    cloudBlock.setTag(CLOUD_BLOCK_TAG);
                    blocks.add(cloudBlock); // Add block to the current row
                    gameObjects.addGameObject(cloudBlock, Layer.BACKGROUND);
                    //set cloud block position change
                    new Transition<>(
                            cloudBlock,
                            (Float delta) -> cloudBlock.setTopLeftCorner(position.add(new Vector2(delta,0))),
                            0f,
                            windowDimensions.x(),
                            Transition.LINEAR_INTERPOLATOR_FLOAT,
                            CLOUD_MOVING_TIME,
                            Transition.TransitionType.TRANSITION_LOOP,
                            null
                    );
                    cloudBlock.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
                }
                relativePositionX += Block.SIZE;
            }
            cloudBlocks.add(blocks); // Add the row to the cloud structure
            relativePositionY += Block.SIZE;
        }
        return cloudBlocks;
    }

    /**
     * Creates a rain effect by generating raindrops under the cloud.
     * The raindrops have randomized positions and fade away as they fall.
     */
    public void createRain() {
        float[] dropsBoundaries = this.getDropsBoundaries();
        float minX = dropsBoundaries[FIRST_POSITION_IN_ARRAY];
        float maxX = dropsBoundaries[SECOND_POSITION_IN_ARRAY];
        float baseY = dropsBoundaries[THIRD_POSITION_IN_ARRAY];
        // Randomize the number of raindrops to create
        int numberOfDrops = MIN_NUMBER_DROPS + (int) (Math.random() * MAX_NUMBER_DROPS);
        for (int i = 0; i < numberOfDrops; i++) {
            float dropX = (float) (minX + Math.random() * (maxX - minX));
            Vector2 position = new Vector2(dropX, baseY);
            Vector2 size = new Vector2(DROP_SIZE, DROP_SIZE);
            // Create a new raindrop GameObject
            GameObject raindrop = new GameObject(
                    position,
                    size,
                    new RectangleRenderable(Color.BLUE)
            );
            raindrop.transform().setAccelerationY(DROP_ACCELERATION);
            new Transition<>(
                    raindrop,
                    alpha -> {
                        if (raindrop.renderer() != null) {
                            raindrop.renderer().setOpaqueness(alpha);
                        }
                    },
                    INITIAL_DROP_TRANSPARENCY,
                    FINAL_DROP_TRANSPARENCY,
                    Transition.LINEAR_INTERPOLATOR_FLOAT,
                    DROP_FALLING_TIME,
                    Transition.TransitionType.TRANSITION_ONCE,
                    () -> {
                        gameObjects.removeGameObject(raindrop);
                    }
            );
            gameObjects.addGameObject(raindrop, Layer.DEFAULT);
            raindrop.transform().setVelocityX(DROPS_VELOCITY);
            raindrops.add(raindrop); // Save the raindrop
            raindrop.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        }
    }

    /**
     * Sets a callback to be executed when the Cloud is removed.
     *
     * @param onRemoveCallback The callback to execute upon removal.
     */
    public void setOnRemoveCallback(Runnable onRemoveCallback) {
        this.onRemoveCallback = onRemoveCallback;
    }


    // =======================
    //   PRIVATE METHODS
    // =======================

    /**
     * Initializes the cloud's pattern. The pattern defines which blocks are present
     * and creates a shape by removing blocks from the edges of each row.
     *
     * @return A 2D list of integers representing the cloud's pattern.
     */
    private List<List<Integer>> initializeCloudPattern() {
        List<List<Integer>> cloudPattern = new ArrayList<>();

        int rows = (int) Math.ceil(cloudDimensions.y() / Block.SIZE);
        int columns = (int) Math.ceil(cloudDimensions.x() / Block.SIZE);

        int[] missingPerSide = new int[] {3, 2, 1, 0, 1, 2, 3};

        for (int i = 0; i < rows; i++) {
            List<Integer> row = new ArrayList<>();
            int missingBlocks = missingPerSide[i % missingPerSide.length];
            for (int j = 0; j < columns; j++) {
                if (j < missingBlocks || j >= columns - missingBlocks) {
                    row.add(0);
                } else {
                    row.add(1);
                }
            }
            cloudPattern.add(row);
        }

        return cloudPattern;
    }

    /**
     * Calculates the boundaries for raindrop creation beneath the cloud.
     * Determines the leftmost, rightmost, and base Y positions based on the cloud's blocks.
     *
     * @return An array containing the minimum X, maximum X, and base Y coordinates.
     */
    private float[] getDropsBoundaries(){
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float baseY = 0;

        List<Block> cloudBase = cloudBlocks.get(cloudBlocks.size() - 1);
        for (Block block : cloudBase) {
            float blockX = block.getTopLeftCorner().x();
            minX = Math.min(minX, blockX);
            maxX = Math.max(maxX, blockX + Block.SIZE);
            baseY = Math.max(block.getTopLeftCorner().y() + Block.SIZE, baseY);
        }
        return new float[] {minX, maxX, baseY};
    }





}
