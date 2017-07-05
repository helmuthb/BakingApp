package at.breitenfellner.bakingapp.view;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import at.breitenfellner.bakingapp.R;
import at.breitenfellner.bakingapp.model.Ingredient;
import at.breitenfellner.bakingapp.model.Recipe;
import at.breitenfellner.bakingapp.model.Step;
import at.breitenfellner.bakingapp.viewmodel.RecipeViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.android.exoplayer2.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING;

/**
 * A fragment representing a single Recipe step screen.
 */
public class RecipeStepFragment extends LifecycleFragment {
    RecipeViewModel viewModel;

    @BindView(R.id.recipe_step_image)
    ImageView stepImage;
    @BindView(R.id.recipe_step_video)
    SimpleExoPlayerView stepVideo;
    @BindView(R.id.recipe_step_title)
    TextView stepTitle;
    @BindView(R.id.recipe_step_details)
    TextView stepDetails;
    SimpleExoPlayer exoPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // connect with ViewModel - shared with activity
        viewModel = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);

        // create exoplayer object
        exoPlayer = ExoPlayerFactory.newSimpleInstance(
                getContext(),
                new DefaultTrackSelector(),
                new DefaultLoadControl()
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recipe_step, container, false);
        ButterKnife.bind(this, rootView);
        stepVideo.setPlayer(exoPlayer);

        // listen on step changes
        viewModel.getStep().observe(this, new Observer<Step>() {

            @Override
            public void onChanged(@Nullable Step step) {
                if (step != null) {
                    stepTitle.setText(step.name);
                    stepDetails.setText(step.description);
                    if (step.thumbnailURL != null && step.thumbnailURL.length() > 0) {
                        stepImage.setVisibility(View.VISIBLE);
                        Picasso.with(getContext())
                                .load(step.thumbnailURL)
                                .into(stepImage);
                    }
                    else {
                        stepImage.setVisibility(View.GONE);
                    }
                    if (step.videoURL != null && step.videoURL.length() > 0) {
                        String userAgent = Util.getUserAgent(getContext(), "BakingApp");
                        MediaSource mediaSource = new ExtractorMediaSource(
                                Uri.parse(step.videoURL),
                                new DefaultDataSourceFactory(getContext(), userAgent),
                                new DefaultExtractorsFactory(),
                                null,
                                null
                        );
                        exoPlayer.prepare(mediaSource);
                        exoPlayer.setPlayWhenReady(true);
                        stepVideo.setVisibility(View.VISIBLE);
                    }
                    else {
                        stepVideo.setVisibility(View.GONE);
                    }
                }
            }
        });

        return rootView;
    }
}
