package at.breitenfellner.bakingapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

import com.squareup.moshi.Json;

/**
 * Model class for a recipe step
 */

@Entity(tableName = Step.TABLE_NAME,
        foreignKeys = @ForeignKey(entity = Recipe.class,
                childColumns = Step.COLUMN_RECIPE_ID,
                parentColumns = Recipe.COLUMN_ID,
                onDelete = ForeignKey.CASCADE))
public class Step {
    public static final String TABLE_NAME = "step";
    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_RECIPE_ID = "recipe_id";
    public static final String COLUMN_NR = "nr";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_VIDEO = "video";
    public static final String COLUMN_THUMBNAIL = "thumbnail";


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    @Json(name = "db_id")
    public int id;

    @ColumnInfo(name = COLUMN_RECIPE_ID, index = true)
    public int recipeId;

    @ColumnInfo(name = COLUMN_NR)
    @Json(name = "id")
    public int nr;

    @ColumnInfo(name = COLUMN_NAME)
    @Json(name="shortDescription")
    public String name;

    @ColumnInfo(name = COLUMN_DESCRIPTION)
    public String description;

    @ColumnInfo(name = COLUMN_VIDEO)
    public String videoURL;

    @ColumnInfo(name = COLUMN_THUMBNAIL)
    public String thumbnailURL;
}
