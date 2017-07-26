package at.breitenfellner.bakingapp.view;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import at.breitenfellner.bakingapp.R;
import at.breitenfellner.bakingapp.model.Ingredient;
import at.breitenfellner.bakingapp.model.Recipe;
import at.breitenfellner.bakingapp.model.Step;
import at.breitenfellner.bakingapp.viewmodel.RecipeViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a single Recipe detail screen.
 */
public class RecipeDetailFragment extends LifecycleFragment implements StepAdapter.StepClickListener {
    RecipeViewModel viewModel;

    @BindView(R.id.recipe_detail_title)
    TextView recipeText;
    @BindView(R.id.recipe_detail_image)
    ImageView recipeImage;
    @BindView(R.id.recipe_detail_servings)
    TextView servingsText;
    @BindView(R.id.recipe_detail_ingredients)
    RecyclerView ingredientsLayout;
    @BindView(R.id.recipe_detail_steps)
    RecyclerView stepsLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // connect with ViewModel - shared with activity
        viewModel = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recipe_detail, container, false);
        ButterKnife.bind(this, rootView);

        // listen on detail changes
        viewModel.getRecipe().observe(this, new Observer<Recipe>() {
            @Override
            public void onChanged(@Nullable Recipe recipe) {
                if (recipe == null) {
                    recipeText.setText(R.string.no_recipe_description);
                    recipeImage.setVisibility(View.GONE);
                    servingsText.setText("");
                }
                else {
                    recipeText.setText(recipe.name);
                    if (!TextUtils.isEmpty(recipe.image)) {
                        recipeImage.setVisibility(View.VISIBLE);
                        Picasso.with(getContext())
                                .load(recipe.image)
                                .into(recipeImage);
                    }
                    else {
                        recipeImage.setVisibility(View.GONE);
                    }
                    String sTxt = getResources().getString(R.string.servings_text, recipe.servings);
                    servingsText.setText(sTxt);
                }
            }
        });

        // listen on ingredient changes
        viewModel.getIngredients().observe(this, new Observer<List<Ingredient>>() {
            @Override
            public void onChanged(@Nullable List<Ingredient> ingredients) {
                if (ingredients != null) {
                    IngredientAdapter ingredientAdapter = new IngredientAdapter(ingredients);
                    ingredientsLayout.setAdapter(ingredientAdapter);
                }
            }
        });

        // listen on step changes
        viewModel.getSteps().observe(this, new Observer<List<Step>>() {
            @Override
            public void onChanged(@Nullable List<Step> steps) {
                if (steps != null) {
                    StepAdapter stepAdapter = new StepAdapter(steps, RecipeDetailFragment.this);
                    stepsLayout.setAdapter(stepAdapter);
                }
            }
        });

        return rootView;
    }

    public void onStepClick(Step step) {
        if (step != null) {
            viewModel.loadStepByNr(step.nr);
        }
    }
}
