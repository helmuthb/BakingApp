package at.breitenfellner.bakingapp.view;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import at.breitenfellner.bakingapp.R;
import at.breitenfellner.bakingapp.model.Recipe;
import at.breitenfellner.bakingapp.model.RecipeList;
import at.breitenfellner.bakingapp.viewmodel.RecipeViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment displaying a list of recipes.
 */

public class RecipeListFragment extends LifecycleFragment
        implements RecipeAdapter.RecipeClickListener {

    @BindView(R.id.recipe_list_fragment)
    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recipe_list, container, false);
        ButterKnife.bind(this, rootView);
        viewModel.getRecipes().observe(this, new Observer<RecipeList>() {
            @Override
            public void onChanged(@Nullable RecipeList recipeList) {
                RecipeAdapter adapter = new RecipeAdapter(recipeList.recipes, RecipeListFragment.this);
                recyclerView.setAdapter(adapter);
            }
        });
        return rootView;
    }

    RecipeViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // connect with ViewModel - shared with activity
        viewModel = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        // we tell the viewModel to load the recipe. It will inform the activity
        // about the changed status.
        viewModel.loadRecipe(recipe.id);
    }
}
