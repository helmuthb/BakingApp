package at.breitenfellner.bakingapp.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private final RecipeClickListener clickListener;
    private final List<Recipe> recipeList;

    public RecipeAdapter(List<Recipe> recipeList, RecipeClickListener clickListener) {
        this.recipeList = recipeList;
        this.clickListener = clickListener;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.recipe_list_item, parent, false);
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

        @BindView(R.id.recipe_list_item_name)
        TextView contentView;
        @BindView(R.id.recipe_list_item_image)
        ImageView imageView;
        @BindView(R.id.recipe_list_item_button)
        Button moreButton;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onRecipeClick(recipe);
        }

        void bind(int index) {
            recipe = recipeList.get(index);
            contentView.setText(recipe.name);
            if (!TextUtils.isEmpty(recipe.image)) {
                Picasso.with(imageView.getContext())
                        .load(recipe.image)
                        .error(R.drawable.baking_placeholder)
                        .into(imageView);
            }
            else {
                Picasso.with(imageView.getContext())
                        .load(R.drawable.baking_placeholder)
                        .into(imageView);
            }
            moreButton.setOnClickListener(this);
        }
    }

    public interface RecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }
}
