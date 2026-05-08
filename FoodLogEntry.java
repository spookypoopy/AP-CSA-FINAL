import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents one logged food or recipe entry for a daily plan.
 */
public class FoodLogEntry {
    /** Formatter used for saving and displaying timestamps. */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Timestamp when the entry was logged. */
    private final LocalDateTime timestamp;

    /** Name of the logged food or recipe. */
    private final String itemName;

    /** Meal slot for the entry, such as breakfast or lunch. */
    private final String mealSlot;

    /** Calories in the logged entry. */
    private final double calories;

    /** Protein in grams. */
    private final double protein;

    /** Carbs in grams. */
    private final double carbs;

    /** Fat in grams. */
    private final double fat;

    /**
     * Creates a new food log entry.
     *
     * @param itemName logged item name
     * @param calories calories for the entry
     * @param protein protein for the entry
     * @param carbs carbs for the entry
     * @param fat fat for the entry
     */
    public FoodLogEntry(String mealSlot, String itemName, double calories, double protein, double carbs, double fat) {
        this(LocalDateTime.now(), mealSlot, itemName, calories, protein, carbs, fat);
    }

    /**
     * Creates a new food log entry without a meal slot.
     *
     * @param itemName logged item name
     * @param calories calories for the entry
     * @param protein protein for the entry
     * @param carbs carbs for the entry
     * @param fat fat for the entry
     */
    public FoodLogEntry(String itemName, double calories, double protein, double carbs, double fat) {
        this("Unspecified", itemName, calories, protein, carbs, fat);
    }

    /**
     * Creates a food log entry with a specific timestamp.
     *
     * @param timestamp timestamp for the entry
     * @param itemName logged item name
     * @param calories calories for the entry
     * @param protein protein for the entry
     * @param carbs carbs for the entry
     * @param fat fat for the entry
     */
    public FoodLogEntry(LocalDateTime timestamp, String mealSlot, String itemName, double calories, double protein, double carbs, double fat) {
        this.timestamp = timestamp;
        this.mealSlot = mealSlot;
        this.itemName = itemName;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    /**
     * Returns the timestamp.
     *
     * @return entry timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the item name.
     *
     * @return item name
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Returns the meal slot.
     *
     * @return meal slot label
     */
    public String getMealSlot() {
        return mealSlot;
    }

    /**
     * Returns the calories.
     *
     * @return calories
     */
    public double getCalories() {
        return calories;
    }

    /**
     * Returns the protein.
     *
     * @return protein
     */
    public double getProtein() {
        return protein;
    }

    /**
     * Returns the carbs.
     *
     * @return carbs
     */
    public double getCarbs() {
        return carbs;
    }

    /**
     * Returns the fat.
     *
     * @return fat
     */
    public double getFat() {
        return fat;
    }

    /**
     * Formats the entry for saving to disk.
     *
     * @return serialized entry text
     */
    public String toStorageLine() {
        return "Entry: " + timestamp.format(FORMATTER) + "|" + mealSlot + "|" + itemName + "|" + calories + "|" + protein + "|" + carbs + "|" + fat;
    }

    /**
     * Creates an entry from a stored line.
     *
     * @param line saved line starting with "Entry: "
     * @return parsed food log entry
     */
    public static FoodLogEntry fromStorageLine(String line) {
        String payload = line.substring("Entry: ".length()).trim();
        String[] parts = payload.split("\\|", -1);
        if (parts.length != 6 && parts.length != 7) {
            throw new IllegalArgumentException("Invalid food log entry line: " + line);
        }

        LocalDateTime timestamp = LocalDateTime.parse(parts[0].trim(), FORMATTER);
        if (parts.length == 6) {
            String itemName = parts[1].trim();
            double calories = Double.parseDouble(parts[2].trim());
            double protein = Double.parseDouble(parts[3].trim());
            double carbs = Double.parseDouble(parts[4].trim());
            double fat = Double.parseDouble(parts[5].trim());
            return new FoodLogEntry(timestamp, "Unspecified", itemName, calories, protein, carbs, fat);
        }

        String mealSlot = parts[1].trim();
        String itemName = parts[2].trim();
        double calories = Double.parseDouble(parts[3].trim());
        double protein = Double.parseDouble(parts[4].trim());
        double carbs = Double.parseDouble(parts[5].trim());
        double fat = Double.parseDouble(parts[6].trim());

        return new FoodLogEntry(timestamp, mealSlot, itemName, calories, protein, carbs, fat);
    }

    /**
     * Returns a readable display string.
     *
     * @return formatted entry text
     */
    @Override
    public String toString() {
        return timestamp.format(FORMATTER) + " - " + mealSlot + ": " + itemName + String.format(
                " (%.1f kcal, P %.1f g, C %.1f g, F %.1f g)",
                calories, protein, carbs, fat);
    }
}