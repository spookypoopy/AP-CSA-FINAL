import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Main class for the console food search program.
 *
 * <p>The user types a food name, and this program prints matching foods from the USDA API.</p>
 */
public class FoodSearchApp {
    /** API key used to call the USDA FoodData Central service. */
    private static final String API_KEY = "Q1UShxczUX0Ri9gRRVDe2gPyoCB4KomIKQYzytAZ";

    /** URL for the USDA food search endpoint. */
    private static final String BASE_URL = "https://api.nal.usda.gov/fdc/v1/foods/search";

    /** How many results to request from the API each time. */
    private static final int API_PAGE_SIZE = 20;

    /** How many results to display per page in the terminal. */
    private static final int DISPLAY_PAGE_SIZE = 10;

    /** Object that sends HTTP requests to the API. */
    private final FoodApiClient apiClient;

    /** Object that converts JSON text into FoodResult objects. */
    private final FoodParser parser;

    /**
     * Builds the app and sets up helper objects.
     */
    public FoodSearchApp() {
        apiClient = new FoodApiClient(API_KEY, BASE_URL, API_PAGE_SIZE);
        parser = new FoodParser();
    }

    /**
     * Searches for foods and applies a simple relevance filter.
     *
     * @param query food name typed by the user
     * @return list of matching foods with duplicates removed
     * @throws IOException if the API request fails
     */
    public List<FoodResult> searchFoods(String query) throws IOException {
        String jsonResponse = apiClient.fetchFoodsJson(query);
        List<FoodResult> results = parser.parseFoods(jsonResponse);
        return removeDuplicates(results);
    }

    /**
     * Removes duplicate foods with identical descriptions.
     *
     * @param foods list that may contain duplicates
     * @return filtered list with only first occurrence of each description
     */
    private List<FoodResult> removeDuplicates(List<FoodResult> foods) {
        Set<String> seen = new HashSet<>();
        List<FoodResult> unique = new ArrayList<>();
        for (FoodResult food : foods) {
            String desc = food.getDescription().toLowerCase();
            if (seen.add(desc)) {
                unique.add(food);
            }
        }
        return unique;
    }

    /**
     * Gets nutrition values for one selected food.
     *
     * @param fdcId USDA food ID
     * @return nutrition values for this food
     * @throws IOException if the API request fails
     */
    public NutritionInfo getNutritionForFood(int fdcId) throws IOException {
        String jsonResponse = apiClient.fetchFoodDetailsJson(fdcId);
        return parser.parseNutritionInfo(jsonResponse);
    }

    /**
     * Displays one page of search results and lets user pick a food or navigate pages.
     *
     * @param allResults all foods found by the API
     * @param scanner for reading user input
     * @return selected FoodResult, or null if user skipped
     * @throws IOException if fetching nutrition details fails
     */
    private FoodResult displayResultsWithPagination(List<FoodResult> allResults, Scanner scanner) throws IOException {
        int totalPages = (int) Math.ceil((double) allResults.size() / DISPLAY_PAGE_SIZE);
        int currentPage = 1;

        while (true) {
            // Calculate which results to show on this page
            int startIndex = (currentPage - 1) * DISPLAY_PAGE_SIZE;
            int endIndex = Math.min(startIndex + DISPLAY_PAGE_SIZE, allResults.size());

            // Print the current page
            System.out.println("\n--- Page " + currentPage + " of " + totalPages + " (" + allResults.size() + " results) ---");
            for (int i = startIndex; i < endIndex; i++) {
                FoodResult food = allResults.get(i);
                int displayNumber = i + 1;
                System.out.println(displayNumber + ". " + food.getDescription());
            }

            // Prompt user
            System.out.println("\n(Enter food number, 'n' for next page, 'p' for previous page, or '0' to skip)");
            System.out.print("Choice: ");
            if (!scanner.hasNextLine()) {
                return null;
            }
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("0")) {
                return null;
            } else if (input.equals("n") && currentPage < totalPages) {
                currentPage++;
            } else if (input.equals("p") && currentPage > 1) {
                currentPage--;
            } else {
                try {
                    int choice = Integer.parseInt(input);
                    if (choice > 0 && choice <= allResults.size()) {
                        return allResults.get(choice - 1);
                    } else {
                        System.out.println("Please enter a valid food number.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number, 'n', 'p', or '0'.");
                }
            }
        }
    }

    // Filtering removed — search returns de-duplicated API results directly.



    /**
     * Displays a nutrition summary for the selected food.
     *
     * @param food selected food item
     * @param nutrition nutrition values for that food
     */
    private void displayNutritionInfo(FoodResult food, NutritionInfo nutrition) {
        System.out.println("\n=== Nutrition Information ===");
        System.out.println("Food: " + food.getDescription());
        System.out.println("Calories: " + formatNutrient(nutrition.getCalories()) + " kcal");
        System.out.println("Protein:  " + formatNutrient(nutrition.getProtein()) + " g");
        System.out.println("Carbs:    " + formatNutrient(nutrition.getCarbs()) + " g");
        System.out.println("Fat:      " + formatNutrient(nutrition.getFat()) + " g");
        System.out.println("=============================\n");
    }


    /**
     * Displays a ingredients list for the selected food.
     *
     * @param food selected food item
     * @param nutrition ingredients values for that food
     */
    private void displayIngredientsInfo(FoodResult food, NutritionInfo nutrition) {
        System.out.println("\n=== Ingredients Information ===");
        System.out.println("Food: " + food.getDescription());
        String ingredients = nutrition.getIngredients();
        if (ingredients != null && !ingredients.isEmpty()) {
            System.out.println("Ingredients:");
            System.out.println(ingredients);
            writeIngredientsToFile(food, ingredients);
        } else {
            System.out.println("Ingredients: N/A");
        }
        System.out.println("=============================\n");
    }

    /**
     * Writes the ingredients for the selected food to a text file.
     *
     * @param food selected food item
     * @param ingredients ingredient text from the API
     */
    private void writeIngredientsToFile(FoodResult food, String ingredients) {
        String fileName = food.getDescription().replaceAll("[^a-zA-Z0-9._-]", "_") + "_ingredients.txt";
        Path filePath = Paths.get(fileName);
        List<String> lines = Arrays.asList(
                "Food: " + food.getDescription(),
                "",
                "Ingredients:",
                ingredients
        );

        try {
            Files.write(filePath, lines);
            System.out.println("Ingredients saved to " + fileName);
        } catch (IOException e) {
            System.out.println("Could not save ingredients to file: " + e.getMessage());
        }
    }

    /**
     * Formats one nutrient value for display.
     *
     * @param value numeric nutrient value
     * @return one decimal place text, or "N/A" when missing
     */
    private String formatNutrient(double value) {
        if (value < 0) {
            return "N/A";
        }

        return String.format(Locale.US, "%.1f", value);
    }

    /**
     * Handle a single search request. Kept simple for AP CSA readability.
     * This method performs the search, shows results, and fetches nutrition
     * information for the selected item.
     */
    private void handleSearchRequest(String searchQuery, Scanner scanner) {
        if (searchQuery.isEmpty()) {
            System.out.println("Please enter a food name.\n");
            return;
        }

        try {
            System.out.println("Searching...");
            List<FoodResult> apiResults = searchFoods(searchQuery);

            if (apiResults.isEmpty()) {
                System.out.println("No matching foods found.\n");
                return;
            }

            System.out.println("Found " + apiResults.size() + " matching foods.\n");

            FoodResult selectedFood = displayResultsWithPagination(apiResults, scanner);
            if (selectedFood == null) {
                return;
            }

            try {
                NutritionInfo nutritionInfo = getNutritionForFood(selectedFood.getFdcId());
                displayNutritionInfo(selectedFood, nutritionInfo);
                displayIngredientsInfo(selectedFood, nutritionInfo);
            } catch (IOException e) {
                System.out.println("Error fetching nutrition info: " + e.getMessage() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error searching for foods: " + e.getMessage() + "\n");
        }
    }

    /**
     * Displays the main menu for the app.
     */
    private void displayMainMenu() {
        System.out.println("\n========== MAIN MENU ==========");
        System.out.println("1. Search Foods");
        System.out.println("2. Create Recipe");
        System.out.println("3. View My Recipes");
        System.out.println("4. Exit");
        System.out.println("==============================");
    }

    /**
     * Handles recipe creation from user input.
     *
     * @param scanner for reading user input
     * @param userProfile user profile to add recipe to
     */
    private void handleRecipeCreation(Scanner scanner, UserProfile userProfile) {
        System.out.println("\n=== CREATE RECIPE ===");
        System.out.print("Recipe name: ");
        String recipeName = scanner.nextLine().trim();

        if (recipeName.isEmpty()) {
            System.out.println("Recipe name cannot be empty.\n");
            return;
        }

        if (userProfile.findRecipe(recipeName) != null) {
            System.out.println("A recipe with that name already exists.\n");
            return;
        }

        Recipe recipe = new Recipe(recipeName);

        // Choose ingredient input method
        System.out.println("Choose ingredient input method:");
        System.out.println("1. Enter custom ingredients manually");
        System.out.println("2. Search foods and add ingredients with nutrition aggregation");
        System.out.print("Choice (1 or 2): ");
        String methodChoice = scanner.nextLine().trim();

        if (methodChoice.equals("2")) {
            // Search-based ingredient adding
            System.out.println("(Type empty line to finish adding searched ingredients)");
            while (true) {
                System.out.print("Search for a food to add (or press Enter to finish): ");
                String query = scanner.nextLine().trim();
                if (query.isEmpty()) break;

                try {
                    List<FoodResult> apiResults = searchFoods(query);
                    if (apiResults.isEmpty()) {
                        System.out.println("No results found for: " + query);
                        continue;
                    }

                    FoodResult selectedFood = displayResultsWithPagination(apiResults, scanner);
                    if (selectedFood == null) continue;

                    NutritionInfo nutrition = null;
                    try {
                        nutrition = getNutritionForFood(selectedFood.getFdcId());
                        displayNutritionInfo(selectedFood, nutrition);
                    } catch (IOException e) {
                        System.out.println("Error fetching nutrition info: " + e.getMessage());
                        continue;
                    }

                    // Ask for multiplier / servings
                    System.out.print("Enter number of servings (e.g. 1, 0.5): ");
                    String servStr = scanner.nextLine().trim();
                    double servings = 1.0;
                    try {
                        if (!servStr.isEmpty()) servings = Double.parseDouble(servStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number, using 1 serving.");
                        servings = 1.0;
                    }

                    String ingredientLine = servings + " x " + selectedFood.getDescription();
                    recipe.addIngredient(ingredientLine);
                    recipe.addNutrition(nutrition, servings);
                    System.out.println("Added: " + ingredientLine + " (nutrition aggregated)");

                } catch (IOException e) {
                    System.out.println("Error searching for foods: " + e.getMessage());
                }
            }
        } else {
            // Default to manual ingredient entry
            System.out.println("Add ingredients (enter empty line when done):");
            while (true) {
                System.out.print("Ingredient: ");
                String ingredient = scanner.nextLine().trim();
                if (ingredient.isEmpty()) {
                    break;
                }
                recipe.addIngredient(ingredient);
            }
        }

        // Add instructions
        System.out.println("Add cooking instructions (enter empty line when done):");
        int stepNum = 1;
        while (true) {
            System.out.print("Step " + stepNum + ": ");
            String instruction = scanner.nextLine().trim();
            if (instruction.isEmpty()) {
                break;
            }
            recipe.addInstruction(instruction);
            stepNum++;
        }

        // If nutrition was aggregated from searches, use that as the estimate when available
        if (recipe.getTotalCalories() > 0) {
            recipe.setCaloriesEstimate(recipe.getTotalCalories());
            System.out.println("Estimated calories set from aggregated nutrition: " + recipe.getTotalCalories());
        } else {
            // Add calorie estimate
            System.out.print("Estimated calories (0 if unknown): ");
            try {
                String calorieInput = scanner.nextLine().trim();
                if (!calorieInput.isEmpty()) {
                    double calories = Double.parseDouble(calorieInput);
                    recipe.setCaloriesEstimate(calories);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid calorie input, set to 0.");
                recipe.setCaloriesEstimate(0);
            }
        }

        userProfile.addRecipe(recipe);
        System.out.println("Recipe '" + recipeName + "' created successfully!");

        try {
            userProfile.saveProfile();
        } catch (IOException e) {
            System.out.println("Error saving profile: " + e.getMessage());
        }
    }

    /**
     * Displays all user recipes with the option to view details.
     *
     * @param scanner for reading user input
     * @param userProfile user profile to display recipes from
     */
    private void displayUserRecipes(Scanner scanner, UserProfile userProfile) {
        if (userProfile.getRecipes().isEmpty()) {
            System.out.println("\nYou have no recipes yet. Create one to get started!\n");
            return;
        }

        while (true) {
            userProfile.displayRecipes();
            System.out.print("Enter recipe number to view, or '0' to return to menu: ");
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                break;
            }

            try {
                int choice = Integer.parseInt(input);
                if (choice > 0 && choice <= userProfile.getRecipes().size()) {
                    Recipe recipe = userProfile.getRecipes().get(choice - 1);
                    System.out.println("\n" + recipe.toString());

                    System.out.print("Delete this recipe? (yes/no): ");
                    String deleteChoice = scanner.nextLine().trim().toLowerCase();
                    if (deleteChoice.equals("yes")) {
                        userProfile.removeRecipe(recipe.getRecipeName());
                        try {
                            userProfile.saveProfile();
                            System.out.println("Recipe deleted.\n");
                        } catch (IOException e) {
                            System.out.println("Error saving profile: " + e.getMessage());
                        }
                    }
                } else {
                    System.out.println("Invalid recipe number.\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.\n");
            }
        }
    }

    /**
     * Runs the interactive search mode where users search for foods.
     *
     * @param scanner for reading user input
     * @return true if user wants to return to main menu, false if exiting
     */
    private boolean runSearchMode(Scanner scanner) {
        System.out.println("\n=== FOOD SEARCH ===");
        System.out.println("(Type 'menu' to return to main menu)\n");

        while (true) {
            System.out.print("Search for a food: ");
            if (!scanner.hasNextLine()) {
                return false;
            }
            String searchQuery = scanner.nextLine().trim();

            if (searchQuery.equalsIgnoreCase("menu")) {
                return true;
            }

            if (searchQuery.isEmpty()) {
                System.out.println("Please enter a food name.\n");
                continue;
            }

            handleSearchRequest(searchQuery, scanner);
        }
    }

    /**
     * Gets username from user input at startup.
     *
     * @param scanner for reading user input
     * @return username entered by user
     */
    private String getUsernameInput(Scanner scanner) {
        System.out.println("=== WELCOME TO FOOD SEARCH APP ===\n");
        System.out.print("Enter your name: ");
        String username = scanner.nextLine().trim();

        while (username.isEmpty()) {
            System.out.print("Name cannot be empty. Please enter your name: ");
            username = scanner.nextLine().trim();
        }

        return username;
    }

    /**
     * Starts the console loop for user input with menu system.
     *
     * @param args command-line arguments (not used in this app)
     */
    public static void main(String[] args) {
        FoodSearchApp app = new FoodSearchApp();
        try (Scanner scanner = new Scanner(System.in)) {
            // Get username at startup
            String username = app.getUsernameInput(scanner);

            // Load or create user profile
            UserProfile userProfile = null;
            try {
                userProfile = UserProfile.loadProfile(username);
                System.out.println("Welcome back, " + username + "!\n");
            } catch (IOException e) {
                System.out.println("Error loading profile: " + e.getMessage());
                System.out.println("Creating new profile for " + username + ".\n");
                userProfile = new UserProfile(username);
            }

            // Main menu loop
            boolean running = true;
            while (running) {
                app.displayMainMenu();
                System.out.print("Choice: ");

                if (!scanner.hasNextLine()) {
                    break;
                }
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        boolean returnFromSearch = app.runSearchMode(scanner);
                        if (!returnFromSearch) {
                            running = false;
                        }
                        break;
                    case "2":
                        app.handleRecipeCreation(scanner, userProfile);
                        break;
                    case "3":
                        app.displayUserRecipes(scanner, userProfile);
                        break;
                    case "4":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter 1, 2, 3, or 4.\n");
                }
            }

            // Final save before exit
            try {
                userProfile.saveProfile();
            } catch (IOException e) {
                System.out.println("Error saving profile: " + e.getMessage());
            }
        }

        System.out.println("\nGoodbye!");
    }
}