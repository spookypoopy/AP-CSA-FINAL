import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recipe created by a user.
 */
public class Recipe {
    /** Name of the recipe. */
    private String recipeName;

    /** List of ingredients in the recipe. */
    private List<String> ingredients;

    /** List of instructions for making the recipe. */
    private List<String> instructions;

    /** Estimated calories for the recipe. */
    private double caloriesEstimate;
    /** Aggregated calories from searched ingredients. */
    private double totalCalories;

    /** Aggregated protein in grams from searched ingredients. */
    private double totalProtein;

    /** Aggregated carbs in grams from searched ingredients. */
    private double totalCarbs;

    /** Aggregated fat in grams from searched ingredients. */
    private double totalFat;

    /**
     * Creates a new recipe with the given name.
     *
     * @param recipeName the name of the recipe
     */
    public Recipe(String recipeName) {
        this.recipeName = recipeName;
        this.ingredients = new ArrayList<>();
        this.instructions = new ArrayList<>();
        this.caloriesEstimate = 0;
        this.totalCalories = 0;
        this.totalProtein = 0;
        this.totalCarbs = 0;
        this.totalFat = 0;
    }

    /**
     * Adds nutrition values to the recipe totals (for searched ingredients).
     *
     * @param info nutrition info to add
     * @param multiplier multiplier (e.g., number of servings)
     */
    public void addNutrition(NutritionInfo info, double multiplier) {
        if (info == null) return;
        double m = multiplier <= 0 ? 1.0 : multiplier;
        if (info.getCalories() >= 0) this.totalCalories += info.getCalories() * m;
        if (info.getProtein() >= 0) this.totalProtein += info.getProtein() * m;
        if (info.getCarbs() >= 0) this.totalCarbs += info.getCarbs() * m;
        if (info.getFat() >= 0) this.totalFat += info.getFat() * m;
    }

    public double getTotalCalories() { return totalCalories; }
    public double getTotalProtein() { return totalProtein; }
    public double getTotalCarbs() { return totalCarbs; }
    public double getTotalFat() { return totalFat; }

    /**
     * Gets the recipe name.
     *
     * @return recipe name
     */
    public String getRecipeName() {
        return recipeName;
    }

    /**
     * Adds an ingredient to the recipe.
     *
     * @param ingredient ingredient to add
     */
    public void addIngredient(String ingredient) {
        ingredients.add(ingredient);
    }

    /**
     * Gets all ingredients in the recipe.
     *
     * @return list of ingredients
     */
    public List<String> getIngredients() {
        return ingredients;
    }

    /**
     * Adds an instruction step to the recipe.
     *
     * @param instruction instruction step to add
     */
    public void addInstruction(String instruction) {
        instructions.add(instruction);
    }

    /**
     * Gets all instruction steps.
     *
     * @return list of instruction steps
     */
    public List<String> getInstructions() {
        return instructions;
    }

    /**
     * Sets the estimated calorie count for the recipe.
     *
     * @param calories estimated calories
     */
    public void setCaloriesEstimate(double calories) {
        this.caloriesEstimate = calories;
    }

    /**
     * Gets the estimated calories.
     *
     * @return estimated calories
     */
    public double getCaloriesEstimate() {
        return caloriesEstimate;
    }

    /**
     * Converts the recipe to a readable string format for display or saving.
     *
     * @return formatted recipe string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Recipe: ").append(recipeName).append("\n");
        sb.append("------- Ingredients -------\n");
        for (String ingredient : ingredients) {
            sb.append("- ").append(ingredient).append("\n");
        }
        sb.append("------- Instructions -------\n");
        for (int i = 0; i < instructions.size(); i++) {
            sb.append((i + 1)).append(". ").append(instructions.get(i)).append("\n");
        }
        sb.append("Estimated Calories: ").append(caloriesEstimate).append(" kcal\n");
        if (totalCalories > 0 || totalProtein > 0 || totalCarbs > 0 || totalFat > 0) {
            sb.append("--- Aggregated Nutrition (from searched ingredients) ---\n");
            sb.append(String.format("Calories: %.1f kcal\n", totalCalories));
            sb.append(String.format("Protein:  %.1f g\n", totalProtein));
            sb.append(String.format("Carbs:    %.1f g\n", totalCarbs));
            sb.append(String.format("Fat:      %.1f g\n", totalFat));
        }
        return sb.toString();
    }
}
