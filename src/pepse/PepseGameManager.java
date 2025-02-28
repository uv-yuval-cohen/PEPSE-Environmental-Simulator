package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;

import danogl.components.Component;
import danogl.components.CoordinateSpace;

import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.world.*;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Flora;


import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

import static pepse.constants.TagConstants.AVATAR_TAG;


/**
 * Manages the Pepse game world, including the terrain, avatar, flora, and day-night cycle.
 */
public class PepseGameManager extends GameManager {

    // =======================
    //       CONSTANTS
    // =======================

    /**
     * The seed used for random terrain and flora generation.
     * This ensures consistent world generation across sessions
     * when the same seed is provided.
     */
    public static final int SEED = (int) System.currentTimeMillis(); // Random seed for terrain and
    // flora generation
    private static final float INITIAL_LAST_AVATAR_X = 0f;            // Initial X-coordinate for
    // avatar tracking
    private static final float MAX_ENERGY = 100f;
    final float WINDOW_PADDING = 300f; // Extra space around the window for terrain generation
    final float CAMERA_CENTER_OFFSET = 0.5f; // Centering factor for the camera
    final int COLLISION_LAYER_FRUIT = -98; // Layer for terrain collisions
    final int COLLISION_LAYER_DEFAULT = 0;   // Default collision layer
    final float BLOCK_UPDATE_THRESHOLD = Block.SIZE * 3; // Threshold for updating terrain
    final float DAY_NIGHT_CYCLE_LENGTH = 30f; // Duration of the day-night cycle in seconds
    final float NIGHT_CYCLE_LENGTH = 15f; // Duration of the night cycle in seconds
    final float AVATAR_VERTICAL_OFFSET = 50f; // Offset to place the avatar above the ground
    final Vector2 ENERGY_METER_POSITION = new Vector2(10, 10); // Top-left corner of the energy meter
    final int FLORA_RANGE_PADDING = 300;    // Padding for flora creation range
    final float FRUIT_ENERGY_REWARD = 10f; // Energy added when a fruit is collected
    final Color WHITE_COLOR = new Color(255, 255, 255); // Base color for the cloud
    final Vector2 CLOUD_DIMENSIONS = new Vector2(140, 100); // Dimensions of the cloud



    // =======================
    //    INSTANCE FIELDS
    // =======================
    private WindowController windowController;
    private final HashMap<Vector2, Block> activeBlocks = new HashMap<>();
    private Avatar avatar;
    private Terrain terrain;
    private Flora flora;
    private float lastAvatarX = INITIAL_LAST_AVATAR_X;
    private boolean isInitialized = false;
    private float minLimit;
    private float maxLimit;


    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader,
                               UserInputListener inputListener, WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        this.windowController = windowController;
        float windowWidth = windowController.getWindowDimensions().x();
        minLimit = -WINDOW_PADDING;
        maxLimit = windowWidth + WINDOW_PADDING;
        createSky();
        Terrain terrain = new Terrain(windowController.getWindowDimensions(), SEED);
        this.terrain = terrain;
        // Create blocks for the terrain in the defined range
        addNewBlocksInRange((int)minLimit, (int)maxLimit);
        GameObject night = createNight();
        GameObject sun = createSun();
        GameObject sunHalo = createSunHalo(sun);
        Avatar avatar = createAvatar(imageReader, inputListener, terrain);
        this.avatar = avatar;
        this.lastAvatarX = avatar.getTopLeftCorner().x();
        createEnergyMeter(avatar);
        gameObjects().layers().shouldLayersCollide(COLLISION_LAYER_FRUIT, COLLISION_LAYER_DEFAULT,
                true);
        createFlora(terrain, avatar);
        Cloud cloud = createCloud();
        // Store the listener in a variable to ensure reference consistency
        Runnable cloudRainListener = cloud::createRain;
        // Add the listener to the Avatar
        avatar.addAvatarEventListener(cloudRainListener);
        
        // set camera
        setCamera(new Camera(avatar,
                windowController.getWindowDimensions().mult(CAMERA_CENTER_OFFSET).
                        subtract(avatar.getTopLeftCorner()),
                windowController.getWindowDimensions(),
                windowController.getWindowDimensions()));
        isInitialized = true;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);


        float avatarX = avatar.getTopLeftCorner().x();

        if ((Math.abs(avatarX - lastAvatarX) >= BLOCK_UPDATE_THRESHOLD) && this.isInitialized) {
            float moveChange = avatarX - lastAvatarX;

            if(moveChange<0){ //moved left
                addNewBlocksInRange((int)(minLimit + moveChange), (int)minLimit);
                removeBlocksOutsideRange((int)(maxLimit+moveChange), (int)maxLimit);
                flora.createInRange((int)(minLimit + moveChange),  (int)minLimit);
                flora.removeTreesOutsideRange((int)(maxLimit+moveChange), (int)minLimit);

            }
            else{ //moved right
                addNewBlocksInRange((int)maxLimit+Block.SIZE, (int)(maxLimit +Block.SIZE + moveChange));
                removeBlocksOutsideRange((int)minLimit, (int)(minLimit+moveChange));
                flora.createInRange((int)maxLimit+Block.SIZE,  (int)(maxLimit+Block.SIZE + moveChange));
                flora.removeTreesOutsideRange((int)minLimit, (int)(minLimit + moveChange));
            }

            lastAvatarX = avatarX;
            minLimit = minLimit + moveChange;
            maxLimit = maxLimit +moveChange;


        }
    }

    private void addNewBlocksInRange(int minX, int maxX) {
        List<Block> newBlocks = terrain.createInRange(minX, maxX);
        for (Block block : newBlocks) {
            Vector2 blockPosition  = block.getTopLeftCorner();
            if (!activeBlocks.containsKey(blockPosition)) {
                gameObjects().addGameObject(block, Layer.STATIC_OBJECTS);
                activeBlocks.put(blockPosition, block);
            }


        }
    }

    /**
     * Removes blocks outside the specified range from the active blocks map
     * and the game world.
     *
     * @param minX The minimum X-coordinate of the range.
     * @param maxX The maximum X-coordinate of the range.
     */
    private void removeBlocksOutsideRange(int minX, int maxX) {
        List<Block> blocksToRemove = new ArrayList<>();

        // Iterate over the keys in activeBlocks to find blocks outside the range
        for (Vector2 position : activeBlocks.keySet()) {
            if (position.x() > minX && position.x() < maxX) {
                Block block = activeBlocks.get(position);
                blocksToRemove.add(block);
            }
        }

        // Remove blocks that are outside the range
        for (GameObject block : blocksToRemove) {
            Vector2 blockPosition = block.getTopLeftCorner();
            activeBlocks.remove(blockPosition);
            gameObjects().removeGameObject(block, Layer.STATIC_OBJECTS);
        }
    }

    private void createSky() {
        GameObject sky = Sky.create(windowController.getWindowDimensions());
        gameObjects().addGameObject(sky, Layer.BACKGROUND);
    }
    private GameObject createNight(){
        GameObject night = Night.create(this.windowController.getWindowDimensions(),NIGHT_CYCLE_LENGTH);
        gameObjects().addGameObject(night, Layer.BACKGROUND);
        return night;
    }
    private GameObject createSunHalo(GameObject sun){
        GameObject sunHalo = SunHalo.create(sun);
        gameObjects().addGameObject(sunHalo, Layer.STATIC_OBJECTS); // picked this layer
        // because it is in front of the sun as required
        Component component = deltaTime -> sunHalo.setCenter(sun.getCenter());
        sunHalo.addComponent(component);
        return sunHalo;
    }

    private GameObject createSun(){
        GameObject sun = Sun.create(new Vector2(windowController.getWindowDimensions().x()
                ,windowController.getWindowDimensions().y()),DAY_NIGHT_CYCLE_LENGTH);
        gameObjects().addGameObject(sun, Layer.BACKGROUND);
        return sun;
    }

    private Avatar createAvatar(ImageReader imageReader, UserInputListener inputListener, Terrain terrain) {
        float windowWidth = windowController.getWindowDimensions().x();
        Vector2 avatarStartPosition = new Vector2(windowWidth / 2f,
                terrain.groundHeightAt(windowWidth / 2f) - AVATAR_VERTICAL_OFFSET);
        Avatar avatar = new Avatar(avatarStartPosition, inputListener, imageReader);
        gameObjects().addGameObject(avatar, Layer.DEFAULT);
        avatar.setTag(AVATAR_TAG);
        return avatar;
    }

    private void createEnergyMeter(GameObject avatar) {
        Supplier<Float> energySupplier = () -> ((Avatar) avatar).getEnergy();
        EnergyMeter energyMeter = new EnergyMeter(
                ENERGY_METER_POSITION, // Position of the energy meter
                energySupplier,
                MAX_ENERGY // Max energy value
        );
        gameObjects().addGameObject(energyMeter, Layer.UI);
        gameObjects().addGameObject(energyMeter.getEnergyTextObject(), Layer.UI);
        energyMeter.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        energyMeter.getEnergyTextObject().setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
    }

    private void createFlora(Terrain terrain, Avatar avatar) {
        // Define the range for creating trees
        int minX = -FLORA_RANGE_PADDING;
        int maxX = (int) (windowController.getWindowDimensions().x() + FLORA_RANGE_PADDING);

        // Callback for adding energy when a fruit is collected
        Runnable fruitCallback = () -> avatar.addEnergy(FRUIT_ENERGY_REWARD);

        // Create Flora instance
        Flora flora = new Flora(terrain, gameObjects(), fruitCallback);

        // Create trees in the defined range
        flora.createInRange(minX, maxX);
        this.flora = flora;

    }
    private Cloud createCloud(){
        Renderable cloudBlockRenderable = new RectangleRenderable(ColorSupplier.approximateMonoColor(
                WHITE_COLOR));
        Cloud cloud = new Cloud(Vector2.ZERO, CLOUD_DIMENSIONS,cloudBlockRenderable,
                gameObjects(), windowController.getWindowDimensions());
        gameObjects().addGameObject(cloud, Layer.BACKGROUND);
        cloud.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        return cloud;

    }


    /**
     * The entry point for the Pepse game application.
     * Starts the game manager.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        new PepseGameManager().run();
    }
}
