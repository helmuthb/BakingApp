package at.breitenfellner.bakingapp;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import at.breitenfellner.bakingapp.db.BakingDb;
import at.breitenfellner.bakingapp.db.BakingDao;
import at.breitenfellner.bakingapp.model.Ingredient;
import at.breitenfellner.bakingapp.model.Recipe;
import at.breitenfellner.bakingapp.model.Step;

import static org.junit.Assert.*;

/**
 * Unit tests for BakingDb and BakingDao and the Entity classes
 */

@RunWith(AndroidJUnit4.class)
public class BakingDbTest {
    private BakingDb db;
    private BakingDao dao;

    @Before
    public void createTestDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        db = Room.inMemoryDatabaseBuilder(context, BakingDb.class).build();
        dao = db.getDao();
    }

    @After
    public void closeTestDb() {
        db.close();
    }

    @Test
    public void insertRecipeAndCount() throws Exception {
        int count1 = dao.getRecipesCount();
        Recipe recipe = new Recipe();
        recipe.id = 1;
        recipe.name = "Test Baking Recipe";
        recipe.servings = 9;
        recipe.image = "";
        dao.insertRecipe(recipe);
        int count2 = dao.getRecipesCount();
        assertEquals("The recipe count increased by 1", count1+1, count2);
    }

    @Test
    public void insertRecipeAndQuery() throws Exception {
        Recipe recipe1 = new Recipe();
        recipe1.id = 1;
        recipe1.name = "Test Recipe";
        recipe1.servings = 2;
        recipe1.image = null;
        int recipeId = (int)dao.insertRecipe(recipe1);
        Recipe recipe2 = dao.getRecipeById(recipeId);
        assertEquals("The same recipe name was retrieved", recipe1.name, recipe2.name);
    }

    @Test
    public void insertReceipeAndSteps() throws Exception {
        Recipe recipe = new Recipe();
        recipe.id = 1;
        recipe.name = "Test";
        recipe.servings = 5;
        recipe.image = "";
        int recipeId = (int)dao.insertRecipe(recipe);
        List<Step> stepList = new ArrayList<>();
        Step step = new Step();
        step.recipeId = recipeId;
        step.nr = 1;
        step.name = "Mix together";
        step.description = "Thoroughly mixing of everything";
        stepList.add(step);
        step = new Step();
        step.recipeId = recipeId;
        step.name = "Bake";
        step.description = "Put into the oven and bake";
        step.nr = 2;
        stepList.add(step);
        dao.insertSteps(stepList);
        assertEquals("Two steps were inserted", 2, dao.getStepsCount(recipeId));
    }

    @Test
    public void insertRecipeAndIngredients() throws Exception {
        Recipe recipe = new Recipe();
        recipe.id = 1;
        recipe.name = "Test";
        recipe.servings = 5;
        recipe.image = "";
        int recipeId = (int)dao.insertRecipe(recipe);
        List<Ingredient> ingredientList = new ArrayList<>();
        Ingredient ingredient = new Ingredient();
        ingredient.recipeId = recipeId;
        ingredient.name = "Sugar";
        ingredientList.add(ingredient);
        ingredient = new Ingredient();
        ingredient.recipeId = recipeId;
        ingredient.name = "Milk";
        ingredientList.add(ingredient);
        dao.insertIngredients(ingredientList);
        assertEquals("Two ingredients were inserted", 2, dao.getIngredientsCount(recipeId));
    }

    @Test
    public void insertStepsAndDelete() throws Exception {
        Recipe recipe = new Recipe();
        recipe.id = 1;
        recipe.name = "Test";
        recipe.servings = 5;
        recipe.image = "";
        int recipeId = (int)dao.insertRecipe(recipe);
        List<Step> stepList = new ArrayList<>();
        Step step = new Step();
        step.recipeId = recipeId;
        step.name = "Sugar";
        stepList.add(step);
        step = new Step();
        step.recipeId = recipeId;
        step.name = "Milk";
        stepList.add(step);
        dao.insertSteps(stepList);
        // delete all steps for this recipe
        dao.deleteSteps(recipeId);
        assertEquals("All steps were deleted", 0, dao.getStepsCount(recipeId));
    }

    @Test
    public void insertIngredientsAndDelete() throws Exception {
        Recipe recipe = new Recipe();
        recipe.id = 1;
        recipe.name = "Test";
        recipe.servings = 5;
        recipe.image = "";
        int recipeId = (int)dao.insertRecipe(recipe);
        List<Ingredient> ingredientList = new ArrayList<>();
        Ingredient ingredient = new Ingredient();
        ingredient.recipeId = recipeId;
        ingredient.name = "Sugar";
        ingredientList.add(ingredient);
        ingredient = new Ingredient();
        ingredient.recipeId = recipeId;
        ingredient.name = "Milk";
        ingredientList.add(ingredient);
        dao.insertIngredients(ingredientList);
        // delete all ingredients for this recipe
        dao.deleteIngredients(recipeId);
        assertEquals("All ingredients were deleted", 0, dao.getIngredientsCount(recipeId));
    }

    @Test
    public void deleteAllButTwoRecipes() throws Exception {
        Recipe recipe = new Recipe();
        recipe.id = 1;
        recipe.name = "Test 1";
        dao.insertRecipe(recipe);
        recipe = new Recipe();
        recipe.id = 2;
        recipe.name = "Test 2";
        dao.insertRecipe(recipe);
        recipe = new Recipe();
        recipe.id = 3;
        recipe.name = "Test 3";
        dao.insertRecipe(recipe);
        recipe = new Recipe();
        recipe.id = 4;
        recipe.name = "Test 4";
        dao.insertRecipe(recipe);
        recipe = new Recipe();
        recipe.id = 5;
        recipe.name = "Test 5";
        dao.insertRecipe(recipe);
        // delete all but number 4 and 5
        List<Integer> idList = new ArrayList<>();
        idList.add(4);
        idList.add(5);
        dao.deleteOtherRecipes(idList);
        assertEquals("Only two elements remain", 2, dao.getRecipesCount());
        assertEquals("Recipe #1 was deleted", null, dao.getRecipeById(1));
        assertEquals("Receipe #4 is still here", "Test 4", dao.getRecipeById(4).name);
    }
}
