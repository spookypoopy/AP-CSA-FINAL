import java.util.ArrayList;
import java.util.List;

/**
 * Parses FoodData Central JSON into application-friendly objects.
 */
public class FoodParser {
    /**
     * Parses JSON text and builds a list of food results.
     *
     * @param json raw API response text
     * @return list of parsed food items
     */
    public List<FoodResult> parseFoods(String json) {
        List<FoodResult> results = new ArrayList<>();

        int foodsStart = json.indexOf("\"foods\":[");
        if (foodsStart == -1) {
            return results;
        }

        int arrayStart = json.indexOf('[', foodsStart);
        int arrayEnd = findMatchingBracket(json, arrayStart);
        if (arrayStart == -1 || arrayEnd == -1) {
            return results;
        }

        String foodsArray = json.substring(arrayStart + 1, arrayEnd);
        List<String> foodObjects = splitTopLevelObjects(foodsArray);

        for (String object : foodObjects) {
            String description = extractStringField(object, "description");
            int fdcId = extractIntField(object, "fdcId");

            if (description != null && !description.isEmpty() && fdcId != -1) {
                results.add(new FoodResult(fdcId, description));
            }
        }

        return results;
    }

    /**
     * Finds the matching closing bracket for an opening square bracket.
     *
     * @param text text to scan
     * @param openIndex index of an opening bracket
     * @return matching closing bracket index, or -1 if missing
     */
    private int findMatchingBracket(String text, int openIndex) {
        if (openIndex < 0 || openIndex >= text.length() || text.charAt(openIndex) != '[') {
            return -1;
        }

        int depth = 0;
        for (int i = openIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Splits an array body into top-level JSON object strings.
     *
     * @param arrayBody content inside JSON array brackets
     * @return list of JSON object text blocks
     */
    private List<String> splitTopLevelObjects(String arrayBody) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int objectStart = -1;

        for (int i = 0; i < arrayBody.length(); i++) {
            char c = arrayBody.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    objectStart = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objectStart != -1) {
                    objects.add(arrayBody.substring(objectStart, i + 1));
                    objectStart = -1;
                }
            }
        }

        return objects;
    }

    /**
     * Extracts a string value for a named JSON field.
     *
     * @param jsonObject JSON object text
     * @param fieldName key to extract
     * @return string field value, or null if not found
     */
    private String extractStringField(String jsonObject, String fieldName) {
        String key = "\"" + fieldName + "\":\"";
        int start = jsonObject.indexOf(key);
        if (start == -1) {
            return null;
        }

        start += key.length();
        StringBuilder value = new StringBuilder();
        boolean escaped = false;

        for (int i = start; i < jsonObject.length(); i++) {
            char c = jsonObject.charAt(i);
            if (escaped) {
                value.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                return value.toString();
            } else {
                value.append(c);
            }
        }

        return null;
    }

    /**
     * Extracts an integer value for a named JSON field.
     *
     * @param jsonObject JSON object text
     * @param fieldName key to extract
     * @return integer field value, or -1 if not found/invalid
     */
    private int extractIntField(String jsonObject, String fieldName) {
        String key = "\"" + fieldName + "\":";
        int start = jsonObject.indexOf(key);
        if (start == -1) {
            return -1;
        }

        start += key.length();
        int end = start;
        while (end < jsonObject.length() && Character.isDigit(jsonObject.charAt(end))) {
            end++;
        }

        if (end == start) {
            return -1;
        }

        try {
            return Integer.parseInt(jsonObject.substring(start, end));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}