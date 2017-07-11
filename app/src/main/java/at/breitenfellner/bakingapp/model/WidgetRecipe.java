package at.breitenfellner.bakingapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

/**
 * Model class for the recipe shown in a widget
 */

@Entity(tableName = WidgetRecipe.TABLE_NAME)
public class WidgetRecipe {
    public static final String TABLE_NAME = "widget_recipe";
    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_RECIPE_ID = "recipe_id";

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = COLUMN_ID)
    public int id;

    @ColumnInfo(name = COLUMN_RECIPE_ID)
    public int recipeId;
}
