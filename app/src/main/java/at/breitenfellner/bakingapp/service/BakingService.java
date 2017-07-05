package at.breitenfellner.bakingapp.service;

import java.util.List;

import at.breitenfellner.bakingapp.model.JsonRecipe;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * This interface (the implementation will be provided by Retrofit) defines
 * a function for getting the recipe list stored in Json
 */

public interface BakingService {
    @GET("/android-baking-app-json")
    Call<List<JsonRecipe>> getBakingJson();
}
