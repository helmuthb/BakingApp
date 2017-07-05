package at.breitenfellner.bakingapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonQualifier;


/**
 * Model class for an ingredient of a recipe
 */

@Entity(tableName = Ingredient.TABLE_NAME,
        foreignKeys = @ForeignKey(entity = Recipe.class,
                childColumns = Ingredient.COLUMN_RECIPE_ID,
                parentColumns = Recipe.COLUMN_ID,
                onDelete = ForeignKey.CASCADE))
public class Ingredient {
    public static final String TABLE_NAME = "ingredient";
    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_RECIPE_ID = "recipe_id";
    public static final String COLUMN_MEASURE = "measure";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_NAME = "name";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    @Json(name = "db_id")
    public int id;

    @ColumnInfo(name = COLUMN_RECIPE_ID, index = true)
    public int recipeId;

    @ColumnInfo(name = COLUMN_QUANTITY)
    public double quantity;

    @ColumnInfo(name = COLUMN_MEASURE)
    public String measure;

    @ColumnInfo(name = COLUMN_NAME)
    @Json(name = "ingredient")
    public String name;
}
