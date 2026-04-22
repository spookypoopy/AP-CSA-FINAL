import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Sends requests to the USDA FoodData Central API.
 */
public class FoodApiClient {
    /** API key required by the USDA service. */
    private final String apiKey;

    /** Search endpoint URL. */
    private final String baseUrl;

    /** Number of foods to ask for in one request. */
    private final int pageSize;

    /**
     * Creates an API client with settings.
     *
     * @param apiKey key for API access
     * @param baseUrl search URL
     * @param pageSize number of results to request
     */
    public FoodApiClient(String apiKey, String baseUrl, int pageSize) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.pageSize = pageSize;
    }

    /**
     * Calls the API and returns raw JSON text.
     *
     * @param query food name to search for
     * @return raw JSON response from the API
     * @throws IOException if the request fails
     */
    public String fetchFoodsJson(String query) throws IOException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String urlWithParams = baseUrl
                + "?query=" + encodedQuery
                + "&pageSize=" + pageSize
                + "&api_key=" + apiKey;

        URL url = URI.create(urlWithParams).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        int status = connection.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            throw new IOException("API request failed with status " + status);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } finally {
            connection.disconnect();
        }

        return response.toString();
    }
}