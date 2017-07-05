package at.breitenfellner.bakingapp.model;

import java.util.List;

/**
 * RecipeList, as communicated to the outside.
 * It contains an error status which is used to communicate network issues.
 */

public class RecipeList {
    public List<Recipe> recipes;
    public boolean error = false;
}
