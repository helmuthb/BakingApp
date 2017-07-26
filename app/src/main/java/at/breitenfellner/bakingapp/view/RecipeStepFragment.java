package at.breitenfellner.bakingapp.view;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
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
import at.breitenfellner.bakingapp.util.ExoPlayerVideoHandler;
import at.breitenfellner.bakingapp.viewmodel.RecipeViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.android.exoplayer2.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING;

/**
 * A fragment representing a single Recipe step screen.
 *
 * Using recipe from the medium post
 * https://medium.com/tall-programmer/fullscreen-functionality-with-android-exoplayer-5fddad45509f
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
    SimpleExoPlayerView videoView;
    boolean hadFullscreenVideo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // connect with ViewModel - shared with activity
        viewModel = ViewModelProviders.of(getActivity()).get(RecipeViewModel.class);
        hadFullscreenVideo = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ExoPlayerVideoHandler.getInstance().releaseVideoPlayer();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ExoPlayerVideoHandler.getInstance().releaseVideoPlayer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ExoPlayerVideoHandler.getInstance().releaseVideoPlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        ExoPlayerVideoHandler.getInstance().goToBackground();
    }

    @Override
    public void onResume() {
        super.onResume();
        ExoPlayerVideoHandler.getInstance().goToForeground();
    }

    /**
     * Shall we show the video on full screen? This is the case when we have a phone
     * <b>and</b> when the rotation is landscape
     * @return whether the video shall be full screen
     */
    boolean videoIsFullscreen() {
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        boolean isLandscape = getResources().getBoolean(R.bool.isLandscape);
        return isLandscape && !isTablet;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_recipe_step, container, false);
        ButterKnife.bind(this, rootView);
        if (videoIsFullscreen()) {
            // videoView = stepVideoFullscreen;
            videoView =
                    (SimpleExoPlayerView)getActivity().findViewById(R.id.activity_video_fullscreen);
            videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
        else {
            videoView = stepVideo;
        }

        // listen on step changes
        viewModel.getStep().observe(this, new Observer<Step>() {

            @Override
            public void onChanged(@Nullable Step step) {
                if (step != null) {
                    stepTitle.setText(step.name);
                    stepDetails.setText(step.description);
                    if (!TextUtils.isEmpty(step.thumbnailURL)) {
                        stepImage.setVisibility(View.VISIBLE);
                        Picasso.with(rootView.getContext())
                                .load(step.thumbnailURL)
                                .into(stepImage);
                    }
                    else {
                        stepImage.setVisibility(View.GONE);
                    }
                    Uri uri = Uri.parse(step.videoURL);
                    if (TextUtils.isEmpty(step.videoURL)) {
                        uri = null;
                    }
                    if (uri != null && videoIsFullscreen()) {
                        hadFullscreenVideo = true;
                        RecipeActivity activity = (RecipeActivity) getActivity();

                        activity.findViewById(android.R.id.content).setBackgroundColor(
                                ContextCompat.getColor(getContext(), android.R.color.black));
                        // AppBarLayout app_bar = activity.findViewById(R.id.app_bar);
                        // app_bar.setVisibility(View.GONE);
                        View regularView = activity.findViewById(R.id.activity_coordinator);
                        regularView.setVisibility(View.GONE);
                        videoView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                        videoView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    }
                    ExoPlayerVideoHandler.getInstance()
                            .prepareExoPlayerForUri(
                                    getContext(),
                                    uri,
                                    videoView);
                    if (uri != null) {
                        ExoPlayerVideoHandler.getInstance().goToForeground();
                        videoView.setVisibility(View.VISIBLE);
                    }
                    else {
                        ExoPlayerVideoHandler.getInstance().goToBackground();
                        videoView.setVisibility(View.GONE);
                    }
                }
            }
        });

        return rootView;
    }
}
