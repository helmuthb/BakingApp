package at.breitenfellner.bakingapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import java.util.List;

import at.breitenfellner.bakingapp.R;
import at.breitenfellner.bakingapp.model.Ingredient;
import at.breitenfellner.bakingapp.model.Recipe;
import at.breitenfellner.bakingapp.repository.BakingRepository;
import at.breitenfellner.bakingapp.view.RecipeActivity;

/**
 * Implementation of App Widget functionality.
 */
public class IngredientsWidget extends AppWidgetProvider {

    BakingRepository repository;

    void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager,
                         final int appWidgetId) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ingredients_widget);
        repository.getWidgetRecipe(appWidgetId).observeForever(new Observer<Recipe>() {
            @Override
            public void onChanged(@Nullable Recipe recipe) {
                views.setTextViewText(R.id.appwidget_title, recipe.name);
                Intent intent = new Intent(context, RecipeActivity.class);
                intent.putExtra(Recipe.class.getName(), recipe.id);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                views.setOnClickPendingIntent(R.id.appwidget_layout, pendingIntent);
                repository.getWidgetIngredients(appWidgetId).observeForever(new Observer<List<Ingredient>>() {
                    @Override
                    public void onChanged(@Nullable List<Ingredient> ingredients) {
                        String ingredientsText = "";
                        for (Ingredient i : ingredients) {
                            ingredientsText = ingredientsText +
                                    i.quantity +
                                    " " +
                                    i.measure +
                                    " " +
                                    i.name +
                                    "\n";
                        }
                        views.setTextViewText(R.id.appwidget_ingredients, ingredientsText);
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }
                });
            }
        });
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        // We are using LiveData - the widget will be updated automatically, no need for refresh
        // once initialized
        if (repository == null) {
            repository = BakingRepository.getInstance(context);
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }
}

