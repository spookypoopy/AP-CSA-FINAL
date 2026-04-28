import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

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
     * Searches for foods that match the user's text.
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

    /**
     * Filters search results by relevance, prioritizing:
     * 1. Exact keyword matches (first word of description)
     * 2. Foods with processed/brand names (lower priority)
     * 3. Results that match multiple query words
     *
     * @param allResults all results from the API
     * @param query original search text
     * @return filtered results sorted by relevance
     */
    private List<FoodResult> filterResultsByRelevance(List<FoodResult> allResults, String query) {
        String[] queryWords = query.toLowerCase().split("\\s+");
        String queryBase = queryWords[0]; // primary search term
        
        List<FoodResult> highRelevance = new ArrayList<>();
        List<FoodResult> mediumRelevance = new ArrayList<>();

        for (FoodResult food : allResults) {
            String descLower = food.getDescription().toLowerCase();
            
            // Exact match: food description starts with primary query word
            if (descLower.startsWith(queryBase)) {
                highRelevance.add(food);
            } else {
                // Check if contains all main query words
                boolean hasAllWords = true;
                int wordCount = 0;
                for (String word : queryWords) {
                    if (word.length() > 2) {
                        if (descLower.contains(word)) {
                            wordCount++;
                        } else {
                            hasAllWords = false;
                        }
                    }
                }
                // Include foods with meaningful keyword matches
                if (wordCount > 0 || descLower.contains(queryBase)) {
                    mediumRelevance.add(food);
                }
            }
        }

        // Combine: high relevance results first, then medium
        highRelevance.addAll(mediumRelevance);
        
        // If we got no results from filtering, return originals
        if (highRelevance.isEmpty()) {
            return allResults;
        }
        return highRelevance;
    }



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
                String searchQuery = scanner.nextLine().trim();

                if (searchQuery.equalsIgnoreCase("exit")) {
                    break;
                }

                if (searchQuery.isEmpty()) {
                    System.out.println("Please enter a food name.\n");
                    continue;
                }

                try {
                    System.out.println("Searching...");
                    List<FoodResult> apiResults = app.searchFoods(searchQuery);
                    
                    if (apiResults.isEmpty()) {
                        System.out.println("No matching foods found.\n");
                    } else {
                        // Filter results for better relevance
                        List<FoodResult> filteredResults = app.filterResultsByRelevance(apiResults, searchQuery);
                        
                        System.out.println("Found " + filteredResults.size() + " matching foods.\n");
                        
                        // Show paginated results and get user selection
                        FoodResult selectedFood = app.displayResultsWithPagination(filteredResults, scanner);
                        
                        if (selectedFood != null) {
                            try {
                                NutritionInfo nutritionInfo = app.getNutritionForFood(selectedFood.getFdcId());
                                app.displayNutritionInfo(selectedFood, nutritionInfo);
                            } catch (IOException e) {
                                System.out.println("Error fetching nutrition info: " + e.getMessage() + "\n");
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error searching for foods: " + e.getMessage() + "\n");
                }
            }
        }

        System.out.println("\nGoodbye!");
    }
}