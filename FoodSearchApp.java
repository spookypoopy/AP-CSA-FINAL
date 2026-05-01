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
     * Starts the console loop for user input.
     *
     * @param args command-line arguments (not used in this app)
     */
    public static void main(String[] args) {
        FoodSearchApp app = new FoodSearchApp();
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("=== FOOD SEARCH ===");
            System.out.println("Type 'exit' to quit. Search any food by name.\n");

            while (true) {
                System.out.print("Search for a food: ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String searchQuery = scanner.nextLine().trim();

                // Exit command
                if (searchQuery.equalsIgnoreCase("exit")) {
                    break;
                }

                // Delegate the search handling to a small helper to keep main readable
                app.handleSearchRequest(searchQuery, scanner);
            }
        }

        System.out.println("\nGoodbye!");
    }
}