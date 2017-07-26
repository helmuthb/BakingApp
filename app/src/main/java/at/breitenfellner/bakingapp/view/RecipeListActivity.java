package at.breitenfellner.bakingapp.view;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import at.breitenfellner.bakingapp.R;
import at.breitenfellner.bakingapp.model.Recipe;
import at.breitenfellner.bakingapp.model.RecipeList;
import at.breitenfellner.bakingapp.viewmodel.RecipeListViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeListActivity extends AppCompatActivity
        implements LifecycleRegistryOwner, RecipeAdapter.RecipeClickListener {

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    RecipeListViewModel viewModel;

    @Override
    @NonNull
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @BindView(R.id.recipe_list_recyclerview)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        ButterKnife.bind(this);

        // connect with ViewModel
        viewModel = ViewModelProviders.of(this).get(RecipeListViewModel.class);

        // listen for updates
        viewModel.getRecipes().observe(this, new Observer<RecipeList>() {
            @Override
            public void onChanged(@Nullable RecipeList recipeList) {
                RecipeAdapter adapter = new RecipeAdapter(recipeList.recipes, RecipeListActivity.this);
                recyclerView.setAdapter(adapter);
                // show toast if there is an error
                if (recipeList != null && recipeList.error) {
                    Toast toast;
                    if (recipeList.recipes != null && recipeList.recipes.size() > 0) {
                        toast = Toast.makeText(
                                RecipeListActivity.this,
                                R.string.error_network_update,
                                Toast.LENGTH_SHORT);
                    }
                    else {
                        toast = Toast.makeText(
                                RecipeListActivity.this,
                                R.string.error_network_load,
                                Toast.LENGTH_LONG);
                    }
                    toast.show();
                }
            }
        });
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra(Recipe.class.getName(), recipe.id);
        startActivity(intent);
    }
}
