import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages user profiles and recipes, including saving/loading from text files.
 */
public class UserProfile {
    /** Username for this profile. */
    private String username;

    /** List of recipes created by this user. */
    private List<Recipe> recipes;

    /** Directory where user profile files are stored. */
    private static final String PROFILES_DIR = "user_profiles";

    /**
     * Creates a new user profile with the given username.
     *
     * @param username the username for this profile
     */
    public UserProfile(String username) {
        this.username = username;
        this.recipes = new ArrayList<>();
        ensureProfilesDirectoryExists();
    }

    /**
     * Gets the username.
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets all recipes for this user.
     *
     * @return list of recipes
     */
    public List<Recipe> getRecipes() {
        return recipes;
    }

    /**
     * Adds a recipe to the profile.
     *
     * @param recipe recipe to add
     */
    public void addRecipe(Recipe recipe) {
        recipes.add(recipe);
    }

    /**
     * Removes a recipe by name.
     *
     * @param recipeName name of recipe to remove
     * @return true if recipe was removed, false if not found
     */
    public boolean removeRecipe(String recipeName) {
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).getRecipeName().equalsIgnoreCase(recipeName)) {
                recipes.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a recipe by name.
     *
     * @param recipeName name to search for
     * @return the recipe or null if not found
     */
    public Recipe findRecipe(String recipeName) {
        for (Recipe recipe : recipes) {
            if (recipe.getRecipeName().equalsIgnoreCase(recipeName)) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Ensures the profiles directory exists.
     */
    private void ensureProfilesDirectoryExists() {
        File dir = new File(PROFILES_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    /**
     * Gets the filename for this user's profile.
     *
     * @return filename (e.g., "username_recipes.txt")
     */
    private String getProfileFilename() {
        String cleanedUsername = username.replaceAll("[^a-zA-Z0-9._-]", "_");
        return PROFILES_DIR + File.separator + cleanedUsername + "_recipes.txt";
    }

    /**
     * Saves the user profile and recipes to a text file.
     *
     * @throws IOException if save fails
     */
    public void saveProfile() throws IOException {
        String filename = getProfileFilename();
        List<String> lines = new ArrayList<>();

        lines.add("=== USER PROFILE ===");
        lines.add("Username: " + username);
        lines.add("Number of Recipes: " + recipes.size());
        lines.add("");

        for (Recipe recipe : recipes) {
            lines.add("====================");
            lines.add("Recipe Name: " + recipe.getRecipeName());
            lines.add("");
            
            lines.add("Ingredients:");
            if (recipe.getIngredients().isEmpty()) {
                lines.add("  (none)");
            } else {
                for (String ingredient : recipe.getIngredients()) {
                    lines.add("  - " + ingredient);
                }
            }
            lines.add("");

            lines.add("Instructions:");
            if (recipe.getInstructions().isEmpty()) {
                lines.add("  (none)");
            } else {
                for (int i = 0; i < recipe.getInstructions().size(); i++) {
                    lines.add("  " + (i + 1) + ". " + recipe.getInstructions().get(i));
                }
            }
            lines.add("");

            lines.add("Estimated Calories: " + recipe.getCaloriesEstimate() + " kcal");
            lines.add("");
        }

        Path filePath = Paths.get(filename);
        Files.write(filePath, lines);
        System.out.println("Profile saved to " + filename);
    }

    /**
     * Loads a user profile from a text file.
     *
     * @param username username to load
     * @return UserProfile if found, or null if file doesn't exist
     * @throws IOException if load fails
     */
    public static UserProfile loadProfile(String username) throws IOException {
        UserProfile profile = new UserProfile(username);
        String filename = profile.getProfileFilename();
        Path filePath = Paths.get(filename);

        if (!Files.exists(filePath)) {
            return profile; // Return empty profile if file doesn't exist
        }

        List<String> lines = Files.readAllLines(filePath);
        Recipe currentRecipe = null;
        boolean readingIngredients = false;
        boolean readingInstructions = false;

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("===") || line.startsWith("--")) {
                readingIngredients = false;
                readingInstructions = false;
                continue;
            }

            if (line.startsWith("Recipe Name:")) {
                if (currentRecipe != null) {
                    profile.addRecipe(currentRecipe);
                }
                String recipeName = line.substring("Recipe Name:".length()).trim();
                currentRecipe = new Recipe(recipeName);
                readingIngredients = false;
                readingInstructions = false;
            } else if (line.equals("Ingredients:")) {
                readingIngredients = true;
                readingInstructions = false;
            } else if (line.equals("Instructions:")) {
                readingInstructions = true;
                readingIngredients = false;
            } else if (line.startsWith("Estimated Calories:")) {
                if (currentRecipe != null) {
                    String caloriesStr = line.substring("Estimated Calories:".length()).trim();
                    caloriesStr = caloriesStr.replace(" kcal", "").trim();
                    try {
                        double calories = Double.parseDouble(caloriesStr);
                        currentRecipe.setCaloriesEstimate(calories);
                    } catch (NumberFormatException e) {
                        // Skip invalid calorie values
                    }
                }
            } else if (readingIngredients && line.startsWith("-")) {
                if (currentRecipe != null) {
                    String ingredient = line.substring(1).trim();
                    if (!ingredient.equals("(none)")) {
                        currentRecipe.addIngredient(ingredient);
                    }
                }
            } else if (readingInstructions && line.matches("^\\d+\\..*")) {
                if (currentRecipe != null) {
                    // Remove the number and period from the start
                    String instruction = line.replaceFirst("^\\d+\\.\\s*", "");
                    if (!instruction.equals("(none)")) {
                        currentRecipe.addInstruction(instruction);
                    }
                }
            }
        }

        if (currentRecipe != null) {
            profile.addRecipe(currentRecipe);
        }

        return profile;
    }

    /**
     * Displays all recipes in the profile.
     */
    public void displayRecipes() {
        if (recipes.isEmpty()) {
            System.out.println("You have no recipes yet.");
            return;
        }

        System.out.println("\n=== Your Recipes ===");
        for (int i = 0; i < recipes.size(); i++) {
            System.out.println((i + 1) + ". " + recipes.get(i).getRecipeName());
        }
    }
}
