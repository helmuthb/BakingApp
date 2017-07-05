package at.breitenfellner.bakingapp.view;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
public class RecipeDetailFragment extends LifecycleFragment implements View.OnClickListener {
    RecipeViewModel viewModel;

    @BindView(R.id.recipe_detail_title)
    TextView recipeText;
    @BindView(R.id.recipe_detail_image)
    ImageView recipeImage;
    @BindView(R.id.recipe_detail_servings)
    TextView servingsText;
    @BindView(R.id.recipe_detail_ingredients)
    TableLayout ingredientsLayout;
    @BindView(R.id.recipe_detail_steps)
    TableLayout stepsLayout;

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
                    if (recipe.image != null && recipe.image.length() > 0) {
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
                ingredientsLayout.removeAllViews();
                if (ingredients != null) {
                    DecimalFormat format = new DecimalFormat();
                    format.setDecimalSeparatorAlwaysShown(false);
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    for (Ingredient i : ingredients) {
                        TableRow tr = (TableRow)inflater.inflate(
                                R.layout.recipe_ingredients_template,
                                null,
                                false);
                        TextView tvCount = (TextView)tr.findViewById(R.id.recipe_ingredient_count);
                        TextView tvUnit = (TextView)tr.findViewById(R.id.recipe_ingredient_unit);
                        TextView tvText = (TextView)tr.findViewById(R.id.recipe_ingredient_text);
                        tvCount.setText(format.format(i.quantity));
                        tvUnit.setText(i.measure);
                        tvText.setText(i.name);
                        ingredientsLayout.addView(tr);
                    }
                    ingredientsLayout.setColumnShrinkable(2, true);
                }
            }
        });

        // listen on step changes
        viewModel.getSteps().observe(this, new Observer<List<Step>>() {
            @Override
            public void onChanged(@Nullable List<Step> steps) {
                stepsLayout.removeAllViews();
                if (steps != null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    for (Step s : steps) {
                        TableRow tr = (TableRow)inflater.inflate(
                                R.layout.recipe_steps_template,
                                null,
                                false);
                        TextView tvNr = (TextView)tr.findViewById(R.id.recipe_step_nr);
                        TextView tvText = (TextView)tr.findViewById(R.id.recipe_step_instruction);
                        if (s.nr > 0) {
                            tvNr.setText(Integer.toString(s.nr) + ".");
                        }
                        tvText.setText(s.name);
                        tr.setTag(s.nr);
                        tr.setOnClickListener(RecipeDetailFragment.this);
                        stepsLayout.addView(tr);
                    }
                }
            }
        });

        return rootView;
    }

    @Override
    public void onClick(View view) {
        Integer stepNr = (Integer)view.getTag();
        if (stepNr != null) {
            viewModel.loadStepByNr(stepNr);
        }
    }
}
