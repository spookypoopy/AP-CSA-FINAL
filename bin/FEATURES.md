# Food Search App - User Profile & Recipe System

## What's New

Your food search app now includes:

### 1. **User Profiles**
- When you start the app, you're prompted to enter your name
- Your profile is automatically saved to `user_profiles/username_recipes.txt`
- Your profile is loaded when you return with the same username
- All recipes are saved automatically after creation

### 2. **Main Menu System**
Instead of immediately searching for foods, you now get a menu with options:
```
1. Search Foods       - Search the USDA database for nutrition info
2. Create Recipe      - Build custom recipes and save them
3. View My Recipes    - See and manage your saved recipes
4. Exit               - Exit the app
```

### 3. **Recipe Creation**
Create custom recipes with:
- **Recipe Name** - Give your recipe a unique name
- **Ingredients** - Add multiple ingredients (enter blank line when done)
- **Instructions** - Add step-by-step cooking instructions
- **Estimated Calories** - Optional calorie estimate for the whole recipe

Example recipe you can create:
```
Name: Pasta Carbonara
Ingredients:
  - Eggs
  - Bacon
  - Pasta
  - Parmesan Cheese
Instructions:
  1. Cook pasta according to package directions
  2. Fry bacon until crispy
  3. Mix eggs with cheese
  4. Combine all ingredients while pasta is hot
Estimated Calories: 500 kcal
```

### 4. **View & Delete Recipes**
- View all your recipes in a numbered list
- Click on recipe number to see full details
- Delete recipes if you no longer want them
- All changes are saved automatically

### 5. **Navigation Features**
- **In Search Mode**: Type `menu` to return to main menu at any time
- **In Menu**: Choose to search, create recipes, view recipes, or exit
- All your data persists across sessions with the same username

## File Structure

```
/workspaces/AP-CSA-FINAL/
├── FoodSearchApp.java          (Updated - added menu system)
├── Recipe.java                 (NEW - recipe management)
├── UserProfile.java            (NEW - user profiles & save/load)
├── FoodApiClient.java          (Unchanged)
├── FoodParser.java             (Unchanged)
├── FoodResult.java             (Unchanged)
├── NutritionInfo.java          (Unchanged)
├── bin/                        (Compiled .class files)
└── user_profiles/              (User recipe files saved here)
    ├── John_recipes.txt        (Example user file)
    ├── Jane_recipes.txt
    └── ...other users...
```

## Recipe Data Storage

Each user's recipes are saved as plain text files, like this:

```
=== USER PROFILE ===
Username: John
Number of Recipes: 1

====================
Recipe Name: Pasta Carbonara

Ingredients:
  - Eggs
  - Bacon
  - Pasta

Instructions:
  1. Cook pasta
  2. Fry bacon
  3. Mix and serve

Estimated Calories: 500 kcal

```

## How to Use

### Starting the App
```bash
cd /workspaces/AP-CSA-FINAL
javac -d bin *.java
java -cp bin FoodSearchApp
```

### Example User Session
1. Enter your name when prompted: `John`
2. See the main menu
3. Choose option 2 to create a recipe
4. Fill in recipe details
5. Recipe is saved automatically to `user_profiles/John_recipes.txt`
6. Next time you run the app and enter `John`, your recipes load automatically!

## Features Summary

| Feature | Details |
|---------|---------|
| **User Login** | Enter name at startup, automatic profile loading |
| **Recipe Creation** | Add ingredients, instructions, and calorie estimates |
| **Recipe Storage** | Plain text files in `user_profiles/` directory |
| **Recipe Management** | View, delete, and manage your recipes |
| **Menu Navigation** | Easy access to search, create, or manage recipes |
| **Return to Menu** | Type "menu" while searching to return anytime |
| **Auto-Save** | Recipes saved automatically after changes |
| **Multi-User** | Different profiles for each username |

## Classes Created

### Recipe.java
- Stores recipe data (name, ingredients, instructions, calories)
- Converts recipes to readable format with `toString()`

### UserProfile.java
- Manages user profile and recipe collection
- Handles saving to and loading from `.txt` files
- Methods: addRecipe(), removeRecipe(), findRecipe(), saveProfile(), loadProfile()
- Maintains list of Recipe objects

### Updated FoodSearchApp.java
- New methods for menu display and recipe creation
- User profiling and data persistence
- Navigation between search mode and recipe management

Enjoy your enhanced food tracking app! 🍳
