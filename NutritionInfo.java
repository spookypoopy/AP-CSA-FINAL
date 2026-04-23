/**
 * Stores basic nutrition values for one food.
 */
public class NutritionInfo {
    /** Calories in kcal. */
    private final double calories;

    /** Protein in grams. */
    private final double protein;

    /** Carbohydrates in grams. */
    private final double carbs;

    /** Fat in grams. */
    private final double fat;

    /**
     * Creates a nutrition info object.
     *
     * @param calories calories in kcal
     * @param protein protein in grams
     * @param carbs carbs in grams
     * @param fat fat in grams
     */
    public NutritionInfo(double calories, double protein, double carbs, double fat) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    /**
     * Returns calories.
     *
     * @return calories in kcal
     */
    public double getCalories() {
        return calories;
    }

    /**
     * Returns protein.
     *
     * @return protein in grams
     */
    public double getProtein() {
        return protein;
    }

    /**
     * Returns carbs.
     *
     * @return carbs in grams
     */
    public double getCarbs() {
        return carbs;
    }

    /**
     * Returns fat.
     *
     * @return fat in grams
     */
    public double getFat() {
        return fat;
    }
}