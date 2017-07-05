package at.breitenfellner.bakingapp.model;

import android.arch.persistence.room.Ignore;

import java.util.List;

/**
 * This class contains the links to its ingredients and its steps.
 * However, for DB-related access everything is flat.
 */

public class JsonRecipe extends Recipe {

    @Ignore
    public List<Ingredient> ingredients;

    @Ignore
    public List<Step> steps;
}
