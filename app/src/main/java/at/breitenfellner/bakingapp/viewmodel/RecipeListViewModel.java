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
 * This ViewModel contains the data needed to be displayed on the RecipeList activity.
 */

@UiThread
public class RecipeListViewModel extends AndroidViewModel {
    private final BakingRepository repository;
    private final LiveData<RecipeList> liveRecipeList;

    public RecipeListViewModel(Application application) {
        super(application);
        repository = BakingRepository.getInstance(application);
        liveRecipeList = repository.getRecipes();
    }

    public LiveData<RecipeList> getRecipes() {
        return liveRecipeList;
    }
}
