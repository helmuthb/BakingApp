package at.breitenfellner.bakingapp.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.util.List;

import at.breitenfellner.bakingapp.model.Ingredient;
import at.breitenfellner.bakingapp.model.Recipe;
import at.breitenfellner.bakingapp.model.RecipeList;
import at.breitenfellner.bakingapp.model.Step;
import at.breitenfellner.bakingapp.repository.BakingRepository;

/**
 * This ViewModel contains the data needed to be displayed on the activity.
 * It also gives information about the state: are we in the list of recipes,
 * at a specific recipe, or at a specific step of a recipe?
 */

@UiThread
public class RecipeViewModel extends AndroidViewModel {
    public final static int STATE_LIST = 0;
    public final static int STATE_DETAIL = 1;
    public final static int STATE_STEP = 2;
    private final BakingRepository repository;
    private final LiveData<RecipeList> liveRecipeList;
    private final MediatorLiveData<Recipe> liveRecipe;
    private LiveData<Recipe> currentRecipe;
    private final MediatorLiveData<List<Ingredient>> liveIngredients;
    private LiveData<List<Ingredient>> currentIngredients;
    private final MediatorLiveData<List<Step>> liveSteps;
    private LiveData<List<Step>> currentSteps;
    private final MutableLiveData<Step> liveStep;
    private Integer currentRecipeId;
    private Integer currentStepNr;
    private final MutableLiveData<Integer> liveState;

    public RecipeViewModel(Application application) {
        super(application);
        repository = BakingRepository.getInstance(application);
        liveRecipeList = repository.getRecipes();
        liveRecipe = new MediatorLiveData<>();
        currentRecipe = null;
        liveIngredients = new MediatorLiveData<>();
        currentIngredients = null;
        liveSteps = new MediatorLiveData<>();
        currentSteps = null;
        liveStep = new MutableLiveData<>();
        currentRecipeId = null;
        currentStepNr = null;
        liveState = new MutableLiveData<>();
        liveState.setValue(STATE_LIST);
    }

    public void loadRecipe(int recipeId) {
        if (liveState.getValue() != STATE_DETAIL) {
            liveState.setValue(STATE_DETAIL);
        }
        // in any case we will reset the step
        currentStepNr = null;
        liveStep.setValue(null);
        if (currentRecipeId != null && recipeId == currentRecipeId) {
            // we are already there
            return;
        }
        currentRecipeId = recipeId;
        if (currentRecipe != null) {
            liveRecipe.removeSource(currentRecipe);
        }
        currentRecipe = repository.getRecipe(recipeId);
        liveRecipe.addSource(currentRecipe, new Observer<Recipe>() {
            @Override
            public void onChanged(@Nullable Recipe recipe) {
                liveRecipe.setValue(recipe);
            }
        });
        if (currentIngredients != null) {
            liveIngredients.removeSource(currentIngredients);
        }
        currentIngredients = repository.getIngredients(recipeId);
        liveIngredients.addSource(currentIngredients, new Observer<List<Ingredient>>() {
            @Override
            public void onChanged(@Nullable List<Ingredient> ingredients) {
                liveIngredients.setValue(ingredients);
            }
        });
        if (currentSteps != null) {
            liveSteps.removeSource(currentSteps);
        }
        currentSteps = repository.getSteps(recipeId);
        liveSteps.addSource(currentSteps, new Observer<List<Step>>() {
            @Override
            public void onChanged(@Nullable List<Step> steps) {
                liveSteps.setValue(steps);
                // load current step if needed
                if (currentStepNr != null && steps != null) {
                    // we will set the liveStep
                    for (Step step : steps) {
                        if (step.nr == currentStepNr) {
                            liveStep.setValue(step);
                            return;
                        }
                    }
                }
                // still here? set liveStep to null
                if (liveStep.getValue() != null) {
                    liveStep.setValue(null);
                }
            }
        });
    }

    public void resetRecipe() {
        if (liveState.getValue() != STATE_LIST) {
            liveState.setValue(STATE_LIST);
        }
        currentRecipeId = null;
        currentStepNr = null;
        liveStep.setValue(null);
        if (currentRecipe != null) {
            liveRecipe.removeSource(currentRecipe);
            currentRecipe = null;
        }
        if (currentIngredients != null) {
            liveIngredients.removeSource(currentIngredients);
            currentIngredients = null;
        }
        if (currentSteps != null) {
            liveSteps.removeSource(currentSteps);
            currentSteps = null;
        }
        liveRecipe.setValue(null);
        liveIngredients.setValue(null);
        liveSteps.setValue(null);
    }

    public void loadStepByNr(int stepNr) {
        if (liveState.getValue() != STATE_STEP) {
            liveState.setValue(STATE_STEP);
        }
        if (currentStepNr != null && stepNr == currentStepNr) {
            // we are already there
            return;
        }
        currentStepNr = stepNr;
        // is the step already loaded?
        if (liveSteps.getValue() != null) {
            List<Step> steps = liveSteps.getValue();
            for (Step step : steps) {
                if (step.nr == stepNr) {
                    liveStep.setValue(step);
                    return;
                }
            }
        }
        // not loaded; we reset it to null
        liveStep.setValue(null);
        // once the step list arrives the step data will be loaded
    }

    public void resetStep() {
        if (liveState.getValue() == STATE_STEP) {
            currentStepNr = null;
            liveState.setValue(STATE_DETAIL);
            liveStep.setValue(null);
        }
    }

    public LiveData<RecipeList> getRecipes() {
        return liveRecipeList;
    }

    public LiveData<Recipe> getRecipe() {
        return liveRecipe;
    }

    public LiveData<List<Ingredient>> getIngredients() {
        return liveIngredients;
    }

    public LiveData<List<Step>> getSteps() {
        return liveSteps;
    }

    public LiveData<Step> getStep() { return liveStep; }

    public LiveData<Integer> getState() { return liveState; }
}
