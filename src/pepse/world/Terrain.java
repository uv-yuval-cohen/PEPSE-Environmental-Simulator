package pepse.world;

import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.util.NoiseGenerator;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static pepse.constants.TagConstants.GROUND_TAG;

/**
 * Represents the terrain generation system. It calculates ground height
 * and creates blocks to form the terrain within a specified range.
 */
public class Terrain {

    // =======================
    //   CONSTANTS
    // =======================
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);
    private static final int TERRAIN_DEPTH_BLOCKS = 20; // Number of vertical blocks in the terrain
    private static final int NOISE_SCALE_FACTOR = 7; // Noise scale factor
    private static final float GROUND_HEIGHT_RATIO = 2.0f / 3.0f; // Ground height is 2/3 of window height


    // =======================
    //   FIELDS
    // =======================
    private final NoiseGenerator noiseGenerator;
    /**
     * The ground height at x0.
     */
    public static float groundHeightAtX0;

    // =======================
    //   CONSTRUCTOR
    // =======================
    /**
     * Constructs a Terrain instance responsible for ground height and terrain block creation.
     *
     * @param windowDimensions The dimensions of the game window.
     * @param seed             The seed for the noise generator.
     */
    public Terrain(Vector2 windowDimensions, int seed) {
        this.groundHeightAtX0 = windowDimensions.y() * GROUND_HEIGHT_RATIO;
        this.noiseGenerator = new NoiseGenerator(seed, (int)Terrain.groundHeightAtX0);


    }

    // =======================
    //   PUBLIC METHODS
    // =======================
    /**
     * Calculates the ground height at a given x-coordinate.
     *
     * @param x The x-coordinate.
     * @return The ground height at the given x-coordinate.
     */
    public float groundHeightAt(float x) {
       return (float) Math.floor(groundWithNoiseHeightAt(x) / Block.SIZE) * Block.SIZE;
    }


    /**
     * Creates terrain blocks in a specified range of x-coordinates.
     *
     * @param minX The minimum x-coordinate of the range.
     * @param maxX The maximum x-coordinate of the range.
     * @return A list of blocks forming the terrain in the specified range.
     */
    public List<Block> createInRange(int minX, int maxX) {
        List<Block> blocks = new ArrayList<>();
        int startX = (minX / Block.SIZE) * Block.SIZE;
        int endX = (maxX / Block.SIZE) * Block.SIZE;

        for (int x = startX - Block.SIZE; x <= endX +Block.SIZE; x += Block.SIZE) {
            float groundHeight = (float) Math.floor(groundHeightAt(x) / Block.SIZE) * Block.SIZE;


            for (int i = 0; i < TERRAIN_DEPTH_BLOCKS; i++) {
                Vector2 blockPosition = new Vector2(x, groundHeight + i * Block.SIZE);
                Block block = new Block(blockPosition,
                        new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR)));
                blocks.add(block);
                block.setTag(GROUND_TAG);
            }
        }
        return blocks;
    }

    // =======================
    //   PRIVATE METHODS
    // =======================
    /**
     * Calculates the ground height with noise added at a given x-coordinate.
     *
     * @param x The x-coordinate.
     * @return The ground height with noise.
     */
    private float groundWithNoiseHeightAt(float x) {
        float noise = (float) noiseGenerator.noise(x, Block.SIZE * NOISE_SCALE_FACTOR);
        return groundHeightAtX0 + noise;
    }



}
