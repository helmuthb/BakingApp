package at.breitenfellner.bakingapp.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.support.annotation.WorkerThread;

import java.util.List;

import at.breitenfellner.bakingapp.model.Ingredient;
import at.breitenfellner.bakingapp.model.Recipe;
import at.breitenfellner.bakingapp.model.Step;

/**
 * Data Access Object class for recipes
 */

@Dao
@WorkerThread
public interface BakingDao {
    @Query("select * from " + Recipe.TABLE_NAME)
    List<Recipe> getAllRecipes();

    @Query("select count(*) from " + Recipe.TABLE_NAME)
    int getRecipesCount();

    @Query("select * from " + Recipe.TABLE_NAME +
            " where " + Recipe.COLUMN_ID + " = :id")
    Recipe getRecipeById(int id);

    @Query("select * from " + Ingredient.TABLE_NAME +
            " where " + Ingredient.COLUMN_RECIPE_ID + " = :recipeId")
    List<Ingredient> getIngredients(int recipeId);

    @Query("select count(*) from " + Ingredient.TABLE_NAME +
            " where " + Ingredient.COLUMN_RECIPE_ID + " = :recipeId")
    int getIngredientsCount(int recipeId);

    @Query("select * from " + Step.TABLE_NAME +
            " where " + Step.COLUMN_RECIPE_ID + " = :recipeId order by " + Step.COLUMN_NR)
    List<Step> getSteps(int recipeId);

    @Query("select count(*) from " + Step.TABLE_NAME +
            " where " + Step.COLUMN_RECIPE_ID + " = :recipeId")
    int getStepsCount(int recipeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRecipe(Recipe recipe);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSteps(List<Step> steps);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIngredients(List<Ingredient> ingredients);

    @Query("delete from " + Step.TABLE_NAME +
            " where " + Step.COLUMN_RECIPE_ID + " = :recipeId")
    void deleteSteps(int recipeId);

    @Query("delete from " + Ingredient.TABLE_NAME +
            " where " + Ingredient.COLUMN_RECIPE_ID + " = :recipeId")
    void deleteIngredients(int recipeId);

    @Query("delete from " + Recipe.TABLE_NAME +
            " where " + Recipe.COLUMN_ID + " not in (:recipeIds)")
    void deleteOtherRecipes(List<Integer> recipeIds);
}
