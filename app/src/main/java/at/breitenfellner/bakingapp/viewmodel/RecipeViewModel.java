package at.breitenfellner.bakingapp.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

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
    public final static int STATE_INVALID = 0;
    public final static int STATE_DETAIL = 1;
    public final static int STATE_STEP = 2;
    private final BakingRepository repository;
    private final MediatorLiveData<Recipe> liveRecipe;
    private LiveData<Recipe> currentRecipe;
    private final MediatorLiveData<List<Ingredient>> liveIngredients;
    private LiveData<List<Ingredient>> currentIngredients;
    private final MediatorLiveData<List<Step>> liveSteps;
    private LiveData<List<Step>> currentSteps;
    private final MutableLiveData<Step> liveStep;
    private Integer currentRecipeId;
    private Integer currentStepNr;
    private int currentStepCount;
    private final MutableLiveData<Integer> liveState;

    public RecipeViewModel(Application application) {
        super(application);
        repository = BakingRepository.getInstance(application);
        liveRecipe = new MediatorLiveData<>();
        currentRecipe = null;
        liveIngredients = new MediatorLiveData<>();
        currentIngredients = null;
        liveSteps = new MediatorLiveData<>();
        currentSteps = null;
        liveStep = new MutableLiveData<>();
        currentRecipeId = null;
        currentStepNr = null;
        currentStepCount = 0;
        liveState = new MutableLiveData<>();
        liveState.setValue(STATE_INVALID);
    }

    public void loadRecipe(int recipeId) {
        if (currentRecipeId != null && recipeId == currentRecipeId) {
            // we are already there
            return;
        }
        // otherwise we will reset the step
        liveState.setValue(STATE_DETAIL);
        currentStepNr = null;
        currentStepCount = 0;
        liveStep.setValue(null);
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
                // get number of steps
                currentStepCount = steps.size();
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

    public void loadStepByNr(int stepNr) {
        if (currentStepNr != null && stepNr == currentStepNr) {
            // we are already there
            return;
        }
        liveState.setValue(STATE_STEP);
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

    public void loadNextStep() {
        if (currentStepNr == null) {
            return;
        }
        loadStepByNr(currentStepNr + 1);
    }

    public void loadPreviousStep() {
        if (currentStepNr == null || currentStepNr < 1) {
            return;
        }
        loadStepByNr(currentStepNr - 1);
    }

    public boolean hasNextStep() {
        if (currentStepNr == null) {
            return false;
        }
        return currentStepNr < currentStepCount - 1;
    }

    public boolean hasPreviousStep() {
        if (currentStepNr == null) {
            return false;
        }
        return currentStepNr > 0;
    }

    public void resetStep() {
        if (liveState.getValue() == STATE_STEP) {
            currentStepNr = null;
            liveState.setValue(STATE_DETAIL);
            liveStep.setValue(null);
        }
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
