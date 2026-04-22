# AP-CSA-FINAL

Console-based USDA food search app for AP CSA.

## AP CSA Requirements Checklist

- Multiple interacting classes: `FoodSearchApp`, `FoodApiClient`, `FoodParser`, `FoodResult`
- Encapsulation: private fields used in all core classes with getters in `FoodResult`
- Array or ArrayList: `ArrayList<FoodResult>` in parser output
- 2D array: `String[][]` table in `FoodSearchApp` for structured result display
- Working driver program: `main` method in `FoodSearchApp`

## Class Diagram

```mermaid
classDiagram
	class FoodSearchApp {
		-String API_KEY
		-String BASE_URL
		-int PAGE_SIZE
		-FoodApiClient apiClient
		-FoodParser parser
		+FoodSearchApp()
		+searchFoods(query) List~FoodResult~
		-buildResultsTable(results) String[][]
		-printResultsTable(table) void
		+main(args) void
	}

	class FoodApiClient {
		-String apiKey
		-String baseUrl
		-int pageSize
		+FoodApiClient(apiKey, baseUrl, pageSize)
		+fetchFoodsJson(query) String
	}

	class FoodParser {
		-String FDC_ID_KEY
		-String DESCRIPTION_KEY
		+parseFoods(json) List~FoodResult~
		-findStringEnd(text, start) int
		-unescape(text) String
	}

	class FoodResult {
		-int fdcId
		-String description
		+FoodResult(fdcId, description)
		+getFdcId() int
		+getDescription() String
		+toString() String
	}

	FoodSearchApp --> FoodApiClient : uses
	FoodSearchApp --> FoodParser : uses
	FoodParser --> FoodResult : creates
	FoodSearchApp --> FoodResult : displays
```