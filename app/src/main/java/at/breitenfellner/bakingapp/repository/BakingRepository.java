package at.breitenfellner.bakingapp.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.AnyThread;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import at.breitenfellner.bakingapp.db.BakingDao;
import at.breitenfellner.bakingapp.db.BakingDb;
import at.breitenfellner.bakingapp.model.Ingredient;
import at.breitenfellner.bakingapp.model.JsonRecipe;
import at.breitenfellner.bakingapp.model.Recipe;
import at.breitenfellner.bakingapp.model.RecipeList;
import at.breitenfellner.bakingapp.model.Step;
import at.breitenfellner.bakingapp.service.BakingService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * This class is the interface between any application logic and UI on the one side and
 * the data (through API, or the database) on the other side.
 */

public class BakingRepository {
    private static final long JSON_LOAD_FREQUENCY = 3600*1000;
    private final BakingService service;
    private final HashMap<Integer, MutableLiveData<Recipe>> recipeCache;
    private final HashMap<Integer, MutableLiveData<List<Ingredient>>> ingredientListCache;
    private final HashMap<Integer, MutableLiveData<List<Step>>> stepListCache;
    private final MediatorLiveData<RecipeList> allRecipes;
    private final MutableLiveData<RecipeList> jsonRecipes;
    private MutableLiveData<RecipeList> dbRecipes;
    private final BakingDb db;
    private final BakingDao dao;
    private long lastLoaded = 0;
    static private BakingRepository theRepository = null;

    @UiThread
    private BakingRepository(Context ctx) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://go.udacity.com")
                .addConverterFactory(MoshiConverterFactory.create())
                .build();
        service = retrofit.create(BakingService.class);
        recipeCache = new HashMap<>();
        ingredientListCache = new HashMap<>();
        stepListCache = new HashMap<>();
        allRecipes = new MediatorLiveData<>();
        jsonRecipes = new MutableLiveData<>();
        dbRecipes = null;
        db = Room.databaseBuilder(ctx.getApplicationContext(), BakingDb.class, "baking.db").build();
        dao = db.getDao();
        // merge in changes from Json
        allRecipes.addSource(jsonRecipes, new Observer<RecipeList>() {
            @Override
            @UiThread
            public void onChanged(@Nullable RecipeList recipeList) {
                if (recipeList != null) {
                    if (!recipeList.error || (recipeList.error && allRecipes.getValue() == null)) {
                        allRecipes.setValue(recipeList);
                    }
                }
            }
        });
    }

    @UiThread
    public static BakingRepository getInstance(Context ctx) {
        if (theRepository == null) {
            theRepository = new BakingRepository(ctx);
        }
        return theRepository;
    }

    @AnyThread
    private void updateRecipeInCache(Recipe recipe, List<Ingredient> ingredients, List<Step> steps) {
        synchronized (recipeCache) {
            MutableLiveData<Recipe> liveRecipe = recipeCache.get(recipe.id);
            if (liveRecipe == null) {
                liveRecipe = new MutableLiveData<>();
                recipeCache.put(recipe.id, liveRecipe);
            }
            liveRecipe.postValue(recipe);
        }
        synchronized (ingredientListCache) {
            MutableLiveData<List<Ingredient>> liveIngredients = ingredientListCache.get(recipe.id);
            if (liveIngredients == null) {
                liveIngredients = new MutableLiveData<>();
                ingredientListCache.put(recipe.id, liveIngredients);
            }
            liveIngredients.postValue(ingredients);
        }
        synchronized (stepListCache) {
            MutableLiveData<List<Step>> liveSteps = stepListCache.get(recipe.id);
            if (liveSteps == null) {
                liveSteps = new MutableLiveData<>();
                stepListCache.put(recipe.id, liveSteps);
            }
            liveSteps.postValue(steps);
        }
    }

    @UiThread
    private void readJson(final List<JsonRecipe> recipes){
        if (recipes != null) {
            // disable / stop reading from DB
            if (dbRecipes != null) {
                allRecipes.removeSource(dbRecipes);
                dbRecipes = null;
            }
            // go into a background thread
            new Thread(new Runnable() {
                @Override
                @WorkerThread
                public void run() {
                    // get list of all recipes
                    List<Integer> recipeIds = new ArrayList<Integer>(recipes.size());
                    for (JsonRecipe r : recipes) {
                        recipeIds.add(r.id);
                        dao.insertRecipe(r);
                        for (Ingredient i : r.ingredients) {
                            // set parent recipe-id
                            i.recipeId = r.id;
                        }
                        // delete old recipe-ingredients
                        dao.deleteIngredients(r.id);
                        // insert all recipe-ingredients in one transaction
                        dao.insertIngredients(r.ingredients);
                        for (Step s : r.steps) {
                            // set parent recipe-id
                            s.recipeId = r.id;
                        }
                        // delete old recipe-steps
                        dao.deleteSteps(r.id);
                        // insert all recipe-steps in one transaction
                        dao.insertSteps(r.steps);
                        // update cache for this recipe
                        updateRecipeInCache(r, r.ingredients, r.steps);
                    }
                    // delete all other recipes
                    dao.deleteOtherRecipes(recipeIds);
                }
            }).start();
        }
    }

    @UiThread
    private void loadJsonFromNet() {
        // load only once an hour
        if (lastLoaded + JSON_LOAD_FREQUENCY < System.currentTimeMillis()) {
            lastLoaded = System.currentTimeMillis();
            Call<List<JsonRecipe>> recipesCall = service.getBakingJson();
            recipesCall.enqueue(new Callback<List<JsonRecipe>>() {
                @Override
                @UiThread
                public void onResponse(Call<List<JsonRecipe>> call, Response<List<JsonRecipe>> response) {
                    // we received a json
                    List<JsonRecipe> recipes = response.body();
                    // store Json-recipes in LiveData
                    RecipeList rl = new RecipeList();
                    rl.recipes = new ArrayList<Recipe>(recipes.size());
                    for (Recipe r : recipes) {
                        rl.recipes.add(r);
                    }
                    rl.error = false;
                    jsonRecipes.setValue(rl);
                    // read Json, put into DB & LiveData (in background)
                    readJson(recipes);
                }

                @Override
                @UiThread
                public void onFailure(Call<List<JsonRecipe>> call, Throwable t) {
                    // set an error state in the json recipe list
                    RecipeList error = new RecipeList();
                    error.error = true;
                    error.recipes = null;
                    jsonRecipes.setValue(error);
                    // reset timer to try again
                    lastLoaded = 0;
                }
            });
        }
    }

    @UiThread
    public LiveData<RecipeList> getRecipes() {
        // load recipes from DB
        if (dbRecipes == null) {
            dbRecipes = new MutableLiveData<>();
            allRecipes.addSource(dbRecipes, new Observer<RecipeList>() {
                @Override
                @UiThread
                public void onChanged(@Nullable RecipeList recipeList) {
                    allRecipes.setValue(recipeList);
                }
            });
            new Thread(new Runnable() {
                @Override
                @WorkerThread
                public void run() {
                    RecipeList rl = new RecipeList();
                    rl.recipes = dao.getAllRecipes();
                    rl.error = (rl.recipes == null);
                    MutableLiveData<RecipeList> dbr = dbRecipes;
                    if (dbr != null) {
                        dbr.postValue(rl);
                    }
                }
            }).start();
        }
        // initiate loading from net - if needed
        loadJsonFromNet();
        return allRecipes;
    }

    @UiThread
    public LiveData<Recipe> getRecipe(final int recipeId) {
        // is it in cache?
        final MutableLiveData<Recipe> liveRecipe;
        synchronized (recipeCache) {
            if (recipeCache.containsKey(recipeId)) {
                return recipeCache.get(recipeId);
            }
            else {
                liveRecipe = new MutableLiveData<>();
                recipeCache.put(recipeId, liveRecipe);
            }
        }
        // read recipe from DB
        new Thread(new Runnable() {
            @Override
            @WorkerThread
            public void run() {
                Recipe recipe = dao.getRecipeById(recipeId);
                liveRecipe.postValue(recipe);
            }
        }).start();
        // initiate reading from Net - if needed
        loadJsonFromNet();
        return liveRecipe;
    }

    @UiThread
    public LiveData<List<Ingredient>> getIngredients(final int recipeId) {
        // is it in cache?
        final MutableLiveData<List<Ingredient>> liveIngredients;
        synchronized (ingredientListCache) {
            if (ingredientListCache.containsKey(recipeId)) {
                return ingredientListCache.get(recipeId);
            }
            else {
                liveIngredients = new MutableLiveData<>();
                ingredientListCache.put(recipeId, liveIngredients);
            }
        }
        // read recipe from DB
        new Thread(new Runnable() {
            @Override
            @WorkerThread
            public void run() {
                List<Ingredient> ingredients = dao.getIngredients(recipeId);
                liveIngredients.postValue(ingredients);
            }
        }).start();
        // initiate reading from Net - if needed
        loadJsonFromNet();
        return liveIngredients;
    }

    @UiThread
    public LiveData<List<Step>> getSteps(final int recipeId) {
        // is it in cache?
        final MutableLiveData<List<Step>> liveSteps;
        synchronized (stepListCache) {
            if (stepListCache.containsKey(recipeId)) {
                return stepListCache.get(recipeId);
            }
            else {
                liveSteps = new MutableLiveData<>();
                stepListCache.put(recipeId, liveSteps);
            }
        }
        // read recipe from DB
        new Thread(new Runnable() {
            @Override
            @WorkerThread
            public void run() {
                List<Step> steps = dao.getSteps(recipeId);
                liveSteps.postValue(steps);
            }
        }).start();
        // initiate reading from Net - if needed
        loadJsonFromNet();
        return liveSteps;
    }
}
