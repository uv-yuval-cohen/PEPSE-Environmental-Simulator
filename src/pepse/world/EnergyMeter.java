package pepse.world;

import danogl.GameObject;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

import java.awt.*;
import java.util.function.Supplier;

/**
 * Represents an energy meter in the game. Displays a visual bar to indicate the current energy level,
 * along with a numeric text representation of the energy value.
 */
public class EnergyMeter extends GameObject {

    // =======================
    //   CONSTANTS
    // =======================
    private static final Color ENERGY_COLOR = Color.GREEN; // Initial color for the energy bar
    private static final float MAX_WIDTH = 200; // Maximum width of the energy bar
    private static final float HEIGHT = 20; // Height of the energy bar
    private static final int TEXT_OFFSET = 10; // Offset between the energy bar and the text
    private static final Color TEXT_COLOR = Color.BLACK; // Color of the energy text
    private static final String DEFAULT_ENERGY_TEXT = "0"; // Default initial text for energy
    private static final float TEXT_WIDTH = 50; // Width of the energy text box
    private static final float GREEN_TO_YELLOW_FACTOR = 3.33f; // Speed of green-to-yellow transition
    private static final float YELLOW_TO_RED_FACTOR = 2.5f; // Speed of yellow-to-red transition
    private static final float YELLOW_THRESHOLD = 0.7f; // Threshold for yellow transition
    private static final float RED_THRESHOLD = 0.3f; // Threshold for red transition
    private static final int MAX_RGB_VALUE = 255; // Maximum intensity for RGB color components

    // =======================
    //   FIELDS
    // =======================
    private final Supplier<Float> energySupplier; // Supplies the current energy level
    private final float maxEnergy; // Maximum energy level
    private final GameObject energyTextObject; // GameObject for the energy text
    private final TextRenderable textRenderable; // Renderable for the numeric energy text

    // =======================
    //   CONSTRUCTOR
    // =======================
    /**
     * Constructs an EnergyMeter instance.
     *
     * @param topLeftCorner  The top-left corner of the energy meter.
     * @param energySupplier A supplier providing the current energy level.
     * @param maxEnergy      The maximum energy level.
     */
    public EnergyMeter(Vector2 topLeftCorner, Supplier<Float> energySupplier, float maxEnergy) {
        super(topLeftCorner, new Vector2(MAX_WIDTH, HEIGHT), new RectangleRenderable(ENERGY_COLOR));
        this.energySupplier = energySupplier;
        this.maxEnergy = maxEnergy;

        // Create the text renderable
        this.textRenderable = new TextRenderable(DEFAULT_ENERGY_TEXT);
        this.textRenderable.setColor(TEXT_COLOR);

        // Create the text object and position it relative to the energy bar
        this.energyTextObject = new GameObject(
                topLeftCorner.add(new Vector2(MAX_WIDTH + TEXT_OFFSET, 0)), // Position to the right of
                // the bar
                new Vector2(TEXT_WIDTH, HEIGHT), // Size of the text object
                this.textRenderable
        );
    }

    // =======================
    //   PUBLIC METHODS
    // =======================
    /**
     * Updates the energy meter, adjusting its dimensions, color, and text based on the current energy level.
     *
     * @param deltaTime Time elapsed since the last frame (in seconds).
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        // Update the energy bar width and position
        float currentEnergy = updateEnergyMeter();

        // Update the energy bar color based on the current energy level
        updateBarColor(currentEnergy);
    }

    /**
     * Returns the GameObject representing the energy text.
     *
     * @return The GameObject displaying the numeric energy value.
     */
    public GameObject getEnergyTextObject() {
        return this.energyTextObject;
    }

    // =======================
    //   PRIVATE METHODS
    // =======================
    /**
     * Updates the width and position of the energy meter based on the current energy level.
     * Also updates the numeric energy text.
     *
     * @return The current energy level.
     */
    private float updateEnergyMeter() {
        // Get the current energy from the supplier
        float currentEnergy = energySupplier.get();

        // Calculate the width of the energy bar based on the current energy
        float width = (currentEnergy / maxEnergy) * MAX_WIDTH;
        setDimensions(new Vector2(width, HEIGHT));

        // Update the position of the text relative to the energy bar
        energyTextObject.setTopLeftCorner(getTopLeftCorner().add(new Vector2(width + TEXT_OFFSET, 0)));

        // Update the text with the current energy (rounded down)
        this.textRenderable.setString(String.valueOf((int) Math.floor(currentEnergy)));
        return currentEnergy;
    }

    /**
     * Updates the color of the energy meter based on the current energy level.
     *
     * @param currentEnergy The current energy level.
     */
    private void updateBarColor(float currentEnergy) {
        // Calculate the ratio of current energy to maximum energy
        float energyRatio = currentEnergy / maxEnergy;

        // Determine the color of the bar based on the energy ratio
        int red, green;
        if (energyRatio > YELLOW_THRESHOLD) {
            // Transition from green to yellow faster
            red = (int) ((1 - energyRatio) * GREEN_TO_YELLOW_FACTOR * MAX_RGB_VALUE);
            green = MAX_RGB_VALUE;
        } else if (energyRatio > RED_THRESHOLD) {
            // Transition from yellow to red faster
            red = MAX_RGB_VALUE;
            green = (int) ((energyRatio - RED_THRESHOLD) * YELLOW_TO_RED_FACTOR * MAX_RGB_VALUE);
        } else {
            // Low energy: red only
            red = MAX_RGB_VALUE;
            green = 0;
        }

        // Update the bar's color
        renderer().setRenderable(new RectangleRenderable(new Color(red, green, 0)));
    }
}