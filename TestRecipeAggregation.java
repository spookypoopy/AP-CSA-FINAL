public class TestRecipeAggregation {
    public static void main(String[] args) {
        boolean ok = true;

        Recipe r = new Recipe("TestRecipe");
        NutritionInfo n = new NutritionInfo(100.0, 10.0, 20.0, 5.0, null, 3.0, 5.0);

        // Add with multiplier 2.0 => totals should double
        r.addNutrition(n, 2.0);
        if (Math.abs(r.getTotalCalories() - 200.0) > 0.0001) {
            System.out.println("FAIL: calories expected 200.0 got " + r.getTotalCalories());
            ok = false;
        }
        if (Math.abs(r.getTotalProtein() - 20.0) > 0.0001) {
            System.out.println("FAIL: protein expected 20.0 got " + r.getTotalProtein());
            ok = false;
        }
        if (Math.abs(r.getTotalCarbs() - 40.0) > 0.0001) {
            System.out.println("FAIL: carbs expected 40.0 got " + r.getTotalCarbs());
            ok = false;
        }
        if (Math.abs(r.getTotalFat() - 10.0) > 0.0001) {
            System.out.println("FAIL: fat expected 10.0 got " + r.getTotalFat());
            ok = false;
        }

        // Add a "missing" nutrition (all -1) should not change totals
        NutritionInfo missing = new NutritionInfo(-1, -1, -1, -1, null, -1, -1);
        r.addNutrition(missing, 1.0);
        if (Math.abs(r.getTotalCalories() - 200.0) > 0.0001) {
            System.out.println("FAIL: calories changed after missing nutrition");
            ok = false;
        }

        if (ok) {
            System.out.println("All tests passed.");
            System.exit(0);
        } else {
            System.out.println("Some tests failed.");
            System.exit(2);
        }
    }
}
