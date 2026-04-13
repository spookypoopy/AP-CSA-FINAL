import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Provides a simple console-based food search app using the USDA FoodData Central API.
 *
 * <p>This class is intentionally written with straightforward Java constructs so it stays at an
 * AP Computer Science A level. A user enters a food name, and the program prints similar matches
 * returned by the API.</p>
 */
public class FoodSearchApp {
    /** API key used to authenticate requests to the FoodData Central service. */
    private static final String API_KEY = "Q1UShxczUX0Ri9gRRVDe2gPyoCB4KomIKQYzytAZ";

    /** Base endpoint for the FoodData Central food search operation. */
    private static final String BASE_URL = "https://api.nal.usda.gov/fdc/v1/foods/search";

    /** Number of foods requested per search call. */
    private static final int PAGE_SIZE = 20;

    /** Client used to fetch raw food data from the API. */
    private final FoodApiClient apiClient;

    /** Parser used to convert JSON text into food result objects. */
    private final FoodParser parser;

    /**
     * Constructs an app with default API settings.
     */
    public FoodSearchApp() {
        apiClient = new FoodApiClient(API_KEY, BASE_URL, PAGE_SIZE);
        parser = new FoodParser();
    }

    /**
     * Searches for foods that match the user's query text.
     *
     * @param query the food text to search for
     * @return a list of matching food results
     * @throws IOException if the HTTP request fails or the API is unavailable
     */
    public List<FoodResult> searchFoods(String query) throws IOException {
        String jsonResponse = apiClient.fetchFoodsJson(query);
        return parser.parseFoods(jsonResponse);
    }

    /**
     * Runs the console app loop.
     *
     * @param args command-line arguments (not used)
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
                        for (int i = 0; i < results.size(); i++) {
                            System.out.println((i + 1) + ". " + results.get(i));
                        }
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