package at.breitenfellner.bakingapp.view;

import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import at.breitenfellner.bakingapp.R;
import at.breitenfellner.bakingapp.model.Recipe;
import at.breitenfellner.bakingapp.model.RecipeList;
import at.breitenfellner.bakingapp.viewmodel.IngredientsWidgetViewModel;
import at.breitenfellner.bakingapp.widget.IngredientsWidget;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * The configuration screen for the {@link IngredientsWidget IngredientsWidget} AppWidget.
 */
public class IngredientsWidgetConfigureActivity extends LifecycleActivity
implements IngredientsWidgetConfigRecipeAdapter.RecipeSelectListener {
    int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    IngredientsWidgetViewModel viewModel;
    @BindView(R.id.ingredients_widget_config_cancel)
    Button cancel;
    @BindView(R.id.ingredients_widget_config_recipe_list)
    RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.ingredients_widget_configure);
        ButterKnife.bind(this);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        // connect with ViewModel
        viewModel = ViewModelProviders.of(this).get(IngredientsWidgetViewModel.class);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        viewModel.getRecipes().observe(this, new Observer<RecipeList>() {
            @Override
            public void onChanged(@Nullable RecipeList recipeList) {
                IngredientsWidgetConfigRecipeAdapter adapter =
                        new IngredientsWidgetConfigRecipeAdapter(
                                recipeList.recipes,
                                IngredientsWidgetConfigureActivity.this);
                recyclerView.setAdapter(adapter);
            }
        });
    }

    /**
     * Called when the recipe has been selected for the widget.
     * It saves the recipe id into the database.
     * @param recipe the selected recipe
     */
    @Override
    public void onRecipeSelect(Recipe recipe) {
        // write the selected recipe into the database
        viewModel.setWidgetRecipe(widgetId, recipe.id);
        // and set the result to OK
        setResult(RESULT_OK);
        // and finish!
        finish();
    }
}

