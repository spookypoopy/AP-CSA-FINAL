/**
 * Stores one food item from the search results.
 */
public class FoodResult {
    /** USDA FoodData Central ID for this food. */
    private final int fdcId;

    /** Food name/description shown to the user. */
    private final String description;

    /**
     * Creates one food result object.
     *
     * @param fdcId USDA ID for the food
     * @param description name/description of the food
     */
    public FoodResult(int fdcId, String description) {
        this.fdcId = fdcId;
        this.description = description;
    }

    /**
     * Returns the USDA ID.
     *
     * @return food ID number
     */
    public int getFdcId() {
        return fdcId;
    }

    /**
     * Returns the food description.
     *
     * @return food name text
     */
    public String getDescription() {
        return description;
    }

    /**
     * Creates the line shown in console results.
     *
     * @return formatted food result text
     */
    @Override
    public String toString() {
        return description + " (FDC ID: " + fdcId + ")";
    }
}