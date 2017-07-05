package at.breitenfellner.bakingapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

import com.squareup.moshi.Json;

import java.util.List;

/**
 * Model class for a baking recipe
 */

@Entity(tableName = Recipe.TABLE_NAME)
public class Recipe {
    public static final String TABLE_NAME = "recipe";
    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SERVINGS = "servings";
    public static final String COLUMN_IMAGE = "image";

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = COLUMN_ID)
    public int id;

    @ColumnInfo(name = COLUMN_NAME)
    public String name;

    @ColumnInfo(name = COLUMN_SERVINGS)
    public int servings;

    @ColumnInfo(name = COLUMN_IMAGE)
    public String image;
}
