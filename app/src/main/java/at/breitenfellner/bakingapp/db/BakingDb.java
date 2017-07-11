package at.breitenfellner.bakingapp.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.content.ReceiverCallNotAllowedException;

import at.breitenfellner.bakingapp.model.Ingredient;
import at.breitenfellner.bakingapp.model.Recipe;
import at.breitenfellner.bakingapp.model.Step;
import at.breitenfellner.bakingapp.model.WidgetRecipe;

/**
 * Database definition for Room persistence
 */

@Database(entities = {Recipe.class, Ingredient.class, Step.class, WidgetRecipe.class}, version = 1)
public abstract class BakingDb extends RoomDatabase {
    public abstract BakingDao getDao();
}
