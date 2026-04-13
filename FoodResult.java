/**
 * Represents one food returned by a FoodData Central search.
 */
public class FoodResult {
    /** Unique FoodData Central identifier for this food. */
    private final int fdcId;

    /** Human-readable description of the food. */
    private final String description;

    /**
     * Constructs a food result.
     *
     * @param fdcId the FoodData Central ID
     * @param description the food description text
     */
    public FoodResult(int fdcId, String description) {
        this.fdcId = fdcId;
        this.description = description;
    }

    /**
     * Gets the FoodData Central ID.
     *
     * @return the FDC ID
     */
    public int getFdcId() {
        return fdcId;
    }

    /**
     * Gets the food description.
     *
     * @return the description text
     */
    public String getDescription() {
        return description;
    }

    /**
     * Builds a readable display string for console output.
     *
     * @return formatted result string
     */
    @Override
    public String toString() {
        return description + " (FDC ID: " + fdcId + ")";
    }
}