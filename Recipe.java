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
    }

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
        return sb.toString();
    }
}
