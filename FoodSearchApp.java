import java.io.IOException;
import java.util.List;
import java.util.Scanner;

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
    private static final int PAGE_SIZE = 20;

    /** Object that sends HTTP requests to the API. */
    private final FoodApiClient apiClient;

    /** Object that converts JSON text into FoodResult objects. */
    private final FoodParser parser;

    /**
     * Builds the app and sets up helper objects.
     */
    public FoodSearchApp() {
        apiClient = new FoodApiClient(API_KEY, BASE_URL, PAGE_SIZE);
        parser = new FoodParser();
    }

    /**
     * Searches for foods that match the user's text.
     *
     * @param query food name typed by the user
     * @return list of matching foods
     * @throws IOException if the API request fails
     */
    public List<FoodResult> searchFoods(String query) throws IOException {
        String jsonResponse = apiClient.fetchFoodsJson(query);
        return parser.parseFoods(jsonResponse);
    }

    /**
     * Builds a 2D table of result data for clean console output.
     *
     * <p>Rows represent foods and columns represent: index, FDC ID, and description.</p>
     *
     * @param results foods found by the API
     * @return 2D array of display data
     */
    private String[][] buildResultsTable(List<FoodResult> results) {
        String[][] table = new String[results.size()][3];

        for (int i = 0; i < results.size(); i++) {
            FoodResult food = results.get(i);
            table[i][0] = String.valueOf(i + 1);
            table[i][1] = String.valueOf(food.getFdcId());
            table[i][2] = food.getDescription();
        }

        return table;
    }

    /**
     * Prints rows from the 2D results table.
     *
     * @param table result table with index, FDC ID, and description columns
     */
    private void printResultsTable(String[][] table) {
        for (int i = 0; i < table.length; i++) {
            System.out.println(table[i][0] + ". " + table[i][2] + " (FDC ID: " + table[i][1] + ")");
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
            System.out.println("Food Search (type 'exit' to quit)");
            while (true) {
                System.out.print("Search for a food: ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {
                    break;
                }

                if (input.isEmpty()) {
                    System.out.println("Please enter a food name.");
                    continue;
                }

                try {
                    List<FoodResult> results = app.searchFoods(input);
                    if (results.isEmpty()) {
                        System.out.println("No matching foods found.");
                    } else {
                        System.out.println("Matches:");
                        String[][] resultTable = app.buildResultsTable(results);
                        app.printResultsTable(resultTable);
                    }
                } catch (IOException e) {
                    System.out.println("Error while searching foods: " + e.getMessage());
                }

                System.out.println();
            }
        }

        System.out.println("Goodbye!");
    }
}