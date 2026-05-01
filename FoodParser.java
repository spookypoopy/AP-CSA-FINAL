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

    /** Nutrient number for calories. */
    private static final String CALORIES_NUMBER = "208";

    /** Nutrient number for protein. */
    private static final String PROTEIN_NUMBER = "203";

    /** Nutrient number for carbohydrates. */
    private static final String CARB_NUMBER = "205";

    /** Nutrient number for total fat. */
    private static final String FAT_NUMBER = "204";

    /** JSON key for ingredient text in food detail responses. */
    private static final String INGREDIENTS_KEY = "\"ingredients\":";

    /**
     * Parsing nutrients: Each nutrient has a USDA number (208=calories, 203=protein, etc.)
     * The parser searches for "number":"XXX" in JSON, then extracts the "amount" field.
     * Ingredients are stored as a single string and included in NutritionInfo.
     */

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

    /**
     * Extracts ingredient information from food-detail JSON.
     *
     * @param json raw JSON for one selected food
     * @return ingredient string, or null if not found
     */
    public String parseIngredients(String json) {
        int ingIndex = json.indexOf(INGREDIENTS_KEY);
        if (ingIndex != -1) {
            int start = ingIndex + INGREDIENTS_KEY.length();
            // skip possible whitespace and opening quote
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
            if (start < json.length() && json.charAt(start) == '"') {
                start++;
                int end = findStringEnd(json, start);
                if (end != -1) {
                    return unescape(json.substring(start, end));
                }
            }
        }
        return null;
    }

    /**
     * Builds nutrition values from food-detail JSON.
     *
     * @param json raw JSON for one selected food
     * @return nutrition info with calories, protein, carbs, and fat
     */
    public NutritionInfo parseNutritionInfo(String json) {
        double calories = findAmountByNutrientNumber(json, CALORIES_NUMBER);
        double protein = findAmountByNutrientNumber(json, PROTEIN_NUMBER);
        double carbs = findAmountByNutrientNumber(json, CARB_NUMBER);
        double fat = findAmountByNutrientNumber(json, FAT_NUMBER);

        // Try to extract an ingredients string when present
        String ingredients = null;
        int ingIndex = json.indexOf(INGREDIENTS_KEY);
        if (ingIndex != -1) {
            int start = ingIndex + INGREDIENTS_KEY.length();
            // skip possible whitespace and opening quote
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
            if (start < json.length() && json.charAt(start) == '"') {
                start++;
                int end = findStringEnd(json, start);
                if (end != -1) {
                    ingredients = unescape(json.substring(start, end));
                }
            }
        }

        return new NutritionInfo(calories, protein, carbs, fat, ingredients);
    }

    /**
     * Finds nutrient amount by nutrient number.
     *
     * @param json raw food-detail JSON
     * @param nutrientNumber USDA nutrient number
     * @return nutrient amount, or -1 when missing
     */
    private double findAmountByNutrientNumber(String json, String nutrientNumber) {
        String numberKey = "\"number\":\"" + nutrientNumber + "\"";
        int numberIndex = json.indexOf(numberKey);
        if (numberIndex == -1) {
            return -1;
        }

        // Search FORWARD from the nutrient number to find the "amount" field
        // This ensures we get the amount for THIS nutrient, not a different one
        int amountIndex = json.indexOf("\"amount\":", numberIndex);
        if (amountIndex == -1) {
            return -1;
        }

        int valueStart = amountIndex + "\"amount\":".length();
        int valueEnd = valueStart;

        // Skip whitespace
        while (valueEnd < json.length() && Character.isWhitespace(json.charAt(valueEnd))) {
            valueEnd++;
        }

        // Parse the numeric value
        int numStart = valueEnd;
        while (valueEnd < json.length()) {
            char c = json.charAt(valueEnd);
            if (Character.isDigit(c) || c == '.' || c == '-') {
                valueEnd++;
            } else {
                break;
            }
        }

        if (numStart == valueEnd) {
            return -1;
        }

        try {
            return Double.parseDouble(json.substring(numStart, valueEnd));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}