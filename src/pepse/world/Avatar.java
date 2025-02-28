package pepse.world;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;
import java.awt.event.KeyEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

import static pepse.constants.TagConstants.GROUND_TAG;
import static pepse.constants.TagConstants.TRUNK_TAG;

/**
 * Represents the main avatar in the game. The avatar has animations for idle, running,
 * and jumping states. The avatar also manages energy
 * consumption and recovery.
 */
public class Avatar extends GameObject {
    // =======================
    //   CONSTANTS
    // =======================
    private static final float GRAVITY = 600; // Acceleration due to gravity
    private static final float VELOCITY_X = 300; // Horizontal velocity
    private static final float JUMP_VELOCITY = -450; // Jumping velocity (negative for upward motion)
    private static final Vector2 DEFAULT_SIZE = Vector2.ONES.mult(50); // Default size for the avatar
    private static final float ENERGY_RECOVERY = 1; // Energy recovery rate
    private static final float ENERGY_CONSUMPTION_RUN = 0.5f; // Energy consumption for running
    private static final float ENERGY_CONSUMPTION_JUMP = 10; // Energy consumption for jumping
    private static final float MAX_ENERGY = 100; // Maximum energy level
    private static final float IDLE_ANIMATION_FRAME_DURATION = 0.2f;
    private static final float RUN_ANIMATION_FRAME_DURATION = 0.1f;
    private static final float JUMP_ANIMATION_FRAME_DURATION = 0.15f;
    private static final String[] IDLE_ANIMATION_FRAMES = {
            "assets/idle_0.png", "assets/idle_1.png", "assets/idle_2.png", "assets/idle_3.png"
    };
    private static final String[] RUN_ANIMATION_FRAMES = {
            "assets/run_0.png", "assets/run_1.png", "assets/run_2.png", "assets/run_3.png"
    };
    private static final String[] JUMP_ANIMATION_FRAMES = {
            "assets/jump_0.png", "assets/jump_1.png", "assets/jump_2.png", "assets/jump_3.png"
    };
    private static final String IMAGE_PATH = "assets/idle_0.png"; // Path to the default image



    // =======================
    //   FIELDS
    // =======================
    private AnimationRenderable idleAnimation; // Animation for idle state
    private AnimationRenderable runAnimation; // Animation for running state
    private AnimationRenderable jumpAnimation; // Animation for jumping state

    private float energy; // Current energy level of the avatar
    private UserInputListener inputListener; // Listener for user inputs

    private final List<Runnable> listeners = new ArrayList<>(); // List of event listeners for the avatar


    // =======================
    //   CONSTRUCTOR
    // =======================

    /**
     * Constructs the avatar at a specific position with input and image reader dependencies.
     *
     * @param topLeftCorner The initial position of the avatar in the game world.
     * @param inputListener The listener for user inputs to control the avatar.
     * @param imageReader   The image reader used to load animations for the avatar.
     */
    public Avatar(Vector2 topLeftCorner, UserInputListener inputListener, ImageReader imageReader) {
        super(topLeftCorner, DEFAULT_SIZE, imageReader.readImage(IMAGE_PATH, true));
        this.inputListener = inputListener;
        initializeAnimations(imageReader);
        this.energy = MAX_ENERGY; // Start with full energy
        // Apply gravity to the avatar
        this.transform().setAccelerationY(GRAVITY);

        // Prevent intersections from any direction
        this.physics().preventIntersectionsFromDirection(Vector2.ZERO);
    }

    // =======================
    //   PUBLIC METHODS
    // =======================

    /**
     * Updates the avatar's state based on user input, energy level, and physics.
     *
     * @param deltaTime Time elapsed since the last update (in seconds).
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        // Common checks
        boolean isOnGround = transform().getVelocity().y() == 0;
        boolean left  = inputListener.isKeyPressed(KeyEvent.VK_LEFT);
        boolean right = inputListener.isKeyPressed(KeyEvent.VK_RIGHT);
        boolean space = inputListener.isKeyPressed(KeyEvent.VK_SPACE);
        // Determine direction: -1 (left), 0 (none), 1 (right)
        int dir = (left ^ right) ? (left ? -1 : 1) : 0;
        // Can we run on the ground?
        boolean canRun = (isOnGround && energy >= ENERGY_CONSUMPTION_RUN);
        // If we can run or we are in the air, apply horizontal velocity
        float xVel = (canRun || !isOnGround) ? dir * VELOCITY_X : 0;
        // Mark as moving only if we have a direction on ground with energy
        boolean isMoving = (dir != 0 && canRun);
        if (isMoving) {
            energy -= ENERGY_CONSUMPTION_RUN; // Consume energy for running
        }
        // Handle jump input
        if (space && isOnGround && energy >= ENERGY_CONSUMPTION_JUMP) {
            transform().setVelocityY(JUMP_VELOCITY);
            isOnGround=false;
            energy -= ENERGY_CONSUMPTION_JUMP; // Consume energy for jumping

            // Notify listeners about the jump
            notifyAvatarJump();
        }
        // Update horizontal velocity in transform
        transform().setVelocityX(xVel);
        // Recover energy if not moving and on the ground
        if (!isMoving && isOnGround && energy < MAX_ENERGY) {
            energy = Math.min(energy + ENERGY_RECOVERY, MAX_ENERGY);
        }
        updateAnimationState();
    }

    /**
     * Handles collision events with other game objects.
     *
     * @param other     The other game object involved in the collision.
     * @param collision The collision data.
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        if(other.getTag().equals(GROUND_TAG)){
            if (collision.getNormal().y() > 0) {
                this.transform().setVelocityY(0);
            }

            if (collision.getNormal().x() != 0) {
                this.transform().setVelocityX(0);
            }
        }
        if (other.getTag().equals(TRUNK_TAG)) {
            this.transform().setVelocityX(0);

        }
    }

    /**
     * Initializes the avatar animations (idle, run, jump).
     *
     * @param imageReader The image reader used to load animation frames.
     */
    public void initializeAnimations(ImageReader imageReader) {
        // Load idle animation
        idleAnimation = new AnimationRenderable(
                IDLE_ANIMATION_FRAMES,
                imageReader,
                false,
                IDLE_ANIMATION_FRAME_DURATION
        );

        // Load run animation
        runAnimation = new AnimationRenderable(
                RUN_ANIMATION_FRAMES,
                imageReader,
                false,
                RUN_ANIMATION_FRAME_DURATION
        );

        // Load jump animation
        jumpAnimation = new AnimationRenderable(
                JUMP_ANIMATION_FRAMES,
                imageReader,
                false,
                JUMP_ANIMATION_FRAME_DURATION
        );

        // Set default animation
        renderer().setRenderable(idleAnimation);
    }

    /**
     * Gets the current energy of the avatar.
     *
     * @return The current energy.
     */
    public float getEnergy() {
        return energy;
    }

    /**
     * Adds energy to the avatar.
     *
     * @param amount The amount of energy to add.
     */
    public void addEnergy(float amount) {
        this.energy = Math.min(this.energy + amount, MAX_ENERGY); // Ensure energy does not exceed max
    }

    /**
     * Adds a listener for avatar events.
     *
     * @param listener The listener to add.
     */
    public void addAvatarEventListener(Runnable listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener for avatar events.
     *
     * @param listener The listener to remove.
     */
    public void removeAvatarEventListener(Runnable listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all listeners that the avatar has jumped.
     */
    public void notifyAvatarJump() {
        for (Runnable listener : listeners) {
            listener.run();
        }

    }


    private void updateAnimationState() {
        // Determine animation state
        if (transform().getVelocity().y() != 0) {
            renderer().setRenderable(jumpAnimation);
            renderer().setIsFlippedHorizontally(transform().getVelocity().x() < 0);
        } else if (transform().getVelocity().x() != 0) {
            renderer().setRenderable(runAnimation);
            renderer().setIsFlippedHorizontally(transform().getVelocity().x() < 0);
        } else {
            renderer().setRenderable(idleAnimation);
        }
    }



}

