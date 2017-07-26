package at.breitenfellner.bakingapp.view;

import android.app.Activity;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;


import at.breitenfellner.bakingapp.R;
import at.breitenfellner.bakingapp.model.Recipe;
import at.breitenfellner.bakingapp.util.ExoPlayerVideoHandler;
import at.breitenfellner.bakingapp.viewmodel.RecipeViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;

import static at.breitenfellner.bakingapp.viewmodel.RecipeViewModel.STATE_DETAIL;
import static at.breitenfellner.bakingapp.viewmodel.RecipeViewModel.STATE_INVALID;
import static at.breitenfellner.bakingapp.viewmodel.RecipeViewModel.STATE_STEP;


/**
 * An activity representing a list of Recipes. It is using Fragments
 * to display different UI for tablets and phones.
 */
@UiThread
public class RecipeActivity extends AppCompatActivity
        implements LifecycleRegistryOwner {

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    @Nullable
    @BindView(R.id.recipe_activity_multipane)
    View multipane;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recipe_detail_container)
    View detailContainer;
    @BindView(R.id.recipe_step_container)
    View stepContainer;
    RecipeViewModel viewModel;
    RecipeDetailFragment detailFragment;
    RecipeStepFragment stepFragment;
    int oldState;

    @Override
    @NonNull
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    public void onBackPressed() {
        if (oldState == STATE_STEP) {
            viewModel.resetStep();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideFragmentIf(FragmentTransaction ft, Fragment f, boolean c) {
        if (c && f != null) {
            ft.hide(f);
        }
    }

    private void showFragmentIf(FragmentTransaction ft, Fragment f, boolean c) {
        if (c && f != null) {
            ft.show(f);
        }
    }

    private void showFragments(int state) {
        if (state == oldState) {
            return;
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        // Show the Up button in the action bar - if we are after list and one-pane
        if (multipane == null) {
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                String title = getResources().getString(R.string.app_name);
                try {
                    if (state == RecipeViewModel.STATE_STEP) {
                        title = title + ": " + viewModel.getRecipe().getValue().name;
                    }
                } catch (NullPointerException e) {
                    // we are catching cases when the recipe is not retrieved yet
                }
                actionBar.setTitle(title);
            }
        }
        // direction of transition - upwards or downwards
        ft.setTransition(state > oldState ?
                FragmentTransaction.TRANSIT_FRAGMENT_OPEN :
                FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        // 1. hide all fragments not needed
        if (multipane != null) {
            hideFragmentIf(ft, stepFragment, state != STATE_STEP);
        }
        else {
            hideFragmentIf(ft, detailFragment, state != STATE_DETAIL);
            hideFragmentIf(ft, stepFragment, state != STATE_STEP);
        }
        // 2. show all fragments needed
        if (multipane != null) {
            ft.show(detailFragment);
            showFragmentIf(ft, stepFragment, state == STATE_STEP);
        }
        else {
            showFragmentIf(ft, detailFragment, state == STATE_DETAIL);
            showFragmentIf(ft, stepFragment, state == STATE_STEP);
        }
        ft.commit();
        oldState = state;

        // stop playback if needed, and reset views
        if (state != STATE_STEP) {
            ExoPlayerVideoHandler.getInstance().goToBackground();
            if (stepFragment.hadFullscreenVideo) {
                stepFragment.hadFullscreenVideo = false;
                recreate();
            }
        }
    }

    private void assignFragment(Fragment f) {
        if (f != null) {
            if (f instanceof RecipeDetailFragment) {
                detailFragment = (RecipeDetailFragment) f;
            } else if (f instanceof RecipeStepFragment) {
                stepFragment = (RecipeStepFragment) f;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // connect with ViewModel
        viewModel = ViewModelProviders.of(this).get(RecipeViewModel.class);
        // set status to an invalid start state
        oldState = STATE_INVALID;
        // identify all fragments
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        assignFragment(fm.findFragmentById(R.id.recipe_detail_container));
        assignFragment(fm.findFragmentById(R.id.recipe_step_container));
        // create & add all fragments
        if (detailFragment == null) {
            detailFragment = new RecipeDetailFragment();
            ft.add(R.id.recipe_detail_container, detailFragment);
        }
        if (stepFragment == null) {
            stepFragment = new RecipeStepFragment();
            ft.add(R.id.recipe_step_container, stepFragment);
        }
        ft.commit();
        // check if Extra data was provided to go to a specific recipe
        Intent myIntent = getIntent();
        if (myIntent.hasExtra(Recipe.class.getName())) {
            // we got a recipe ID passed in
            int recipeId = myIntent.getIntExtra(Recipe.class.getName(), 0);
            if (recipeId > 0) {
                viewModel.loadRecipe(recipeId);
            }
        }
        // watch for updates from the viewmodel
        viewModel.getState().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer state) {
                Log.d(getClass().getName(), "onChanged: " + state);
                int stateInt = state == null ? 0 : state;
                showFragments(stateInt);
            }
        });
    }
}
