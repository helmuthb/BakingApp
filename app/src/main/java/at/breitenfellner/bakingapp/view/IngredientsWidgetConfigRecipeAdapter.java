package at.breitenfellner.bakingapp.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import at.breitenfellner.bakingapp.R;
import at.breitenfellner.bakingapp.model.Recipe;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter to show a recipe in a list
 */

@UiThread
public class IngredientsWidgetConfigRecipeAdapter extends RecyclerView.Adapter<IngredientsWidgetConfigRecipeAdapter.RecipeViewHolder> {
    private final RecipeSelectListener clickListener;
    private final List<Recipe> recipeList;

    public IngredientsWidgetConfigRecipeAdapter(List<Recipe> recipeList, RecipeSelectListener clickListener) {
        this.recipeList = recipeList;
        this.clickListener = clickListener;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(
                R.layout.ingredients_widget_config_recipe_list_item,
                parent,
                false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (recipeList == null) {
            return 0;
        }
        return recipeList.size();
    }


    class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Recipe recipe;

        @BindView(R.id.ingredients_widget_config_recipe_list_item_name)
        TextView nameView;
        @BindView(R.id.ingredients_widget_config_recipe_list_item_select_button)
        Button selectButton;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onRecipeSelect(recipe);
        }

        void bind(int index) {
            recipe = recipeList.get(index);
            nameView.setText(recipe.name);
            selectButton.setOnClickListener(this);
        }
    }

    public interface RecipeSelectListener {
        void onRecipeSelect(Recipe recipe);
    }
}
