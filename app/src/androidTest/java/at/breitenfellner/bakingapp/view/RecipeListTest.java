package at.breitenfellner.bakingapp.view;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import at.breitenfellner.bakingapp.R;
import at.breitenfellner.bakingapp.model.Recipe;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;

/**
 * Espresso test for the list of recipes
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RecipeListTest {

    @Rule
    public IntentsTestRule<RecipeListActivity> intentsTestRule = new IntentsTestRule<>(RecipeListActivity.class);

    @Test
    public void showsRecipes() {
        // we click on "Nutella Pie" ...
        onView(withText("Nutella Pie"))
                .perform(click());

        // and expect an intent sent out for RecipeActivity
        intended(allOf(
                toPackage("at.breitenfellner.bakingapp"),
                hasExtraWithKey(Recipe.class.getName())));

        // and click there on the Step #0
        onView(withText("0"))
                .perform(scrollTo(), click());

        // expect a visible video player
        onView(allOf(
                anyOf(
                        withId(R.id.recipe_step_video),
                        withId(R.id.activity_video_fullscreen)),
                withClassName(endsWith("SimpleExoPlayerView")),
                withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                .check(matches(isDisplayed()));
    }
}
