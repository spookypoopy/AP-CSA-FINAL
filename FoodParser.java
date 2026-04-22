import java.util.ArrayList;
import java.util.List;

/**
 * Reads JSON text from the API and builds FoodResult objects.
 */
public class FoodParser {
    /** Text that appears before each food ID value in JSON. */
    private static final String FDC_ID_KEY = "\"fdcId\":";

    /** Text that appears before each food description in JSON. */
    private static final String DESCRIPTION_KEY = "\"description\":\"";

    /**
     * Builds a list of foods by scanning the JSON string.
     *
     * @param json raw JSON from the API
     * @return list of foods found in the JSON
     */
    public List<FoodResult> parseFoods(String json) {
        List<FoodResult> results = new ArrayList<>();

        int searchIndex = 0;
        while (true) {
            int idKeyIndex = json.indexOf(FDC_ID_KEY, searchIndex);
            if (idKeyIndex == -1) {
                break;
            }

            int idStart = idKeyIndex + FDC_ID_KEY.length();
            int idEnd = idStart;
            while (idEnd < json.length() && Character.isDigit(json.charAt(idEnd))) {
                idEnd++;
            }

            if (idEnd == idStart) {
                searchIndex = idStart;
                continue;
            }

            int descriptionKeyIndex = json.indexOf(DESCRIPTION_KEY, idEnd);
            if (descriptionKeyIndex == -1) {
                break;
            }

            int descriptionStart = descriptionKeyIndex + DESCRIPTION_KEY.length();
            int descriptionEnd = findStringEnd(json, descriptionStart);
            if (descriptionEnd == -1) {
                break;
            }

            int fdcId = Integer.parseInt(json.substring(idStart, idEnd));
            String description = unescape(json.substring(descriptionStart, descriptionEnd));

            if (!description.isEmpty()) {
                results.add(new FoodResult(fdcId, description));
            }

            searchIndex = descriptionEnd + 1;
        }

        return results;
    }

    /**
        * Finds where a JSON string value ends.
     *
        * @param text full JSON text
        * @param start index where the string value starts
        * @return index of the ending quote, or -1 if not found
     */
    private int findStringEnd(String text, int start) {
        boolean escaped = false;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                return i;
            }
        }

        return -1;
    }

    /**
        * Converts common escaped JSON characters into normal text.
     *
        * @param text raw text from JSON
        * @return cleaned text for display
     */
    private String unescape(String text) {
        return text
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", " ")
                .replace("\\t", " ");
    }
}