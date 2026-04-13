import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles communication with the USDA FoodData Central search endpoint.
 */
public class FoodApiClient {
    /** API key used to authenticate search requests. */
    private final String apiKey;

    /** Base URL for the food search endpoint. */
    private final String baseUrl;

    /** Number of items requested per search call. */
    private final int pageSize;

    /**
     * Constructs an API client with configuration values.
     *
     * @param apiKey API key for FoodData Central
     * @param baseUrl endpoint for search requests
     * @param pageSize number of results per request
     */
    public FoodApiClient(String apiKey, String baseUrl, int pageSize) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.pageSize = pageSize;
    }

    /**
     * Requests foods matching the given query and returns raw JSON.
     *
     * @param query user-entered search text
     * @return raw JSON response text
     * @throws IOException if the API call fails
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