package at.breitenfellner.bakingapp.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import at.breitenfellner.bakingapp.R;
import at.breitenfellner.bakingapp.model.Step;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter to show a step of a recipe.
 */

@UiThread
public class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepViewHolder> {
    private final StepClickListener clickListener;
    private final List<Step> stepList;

    public StepAdapter(List<Step> stepList, StepClickListener clickListener) {
        this.stepList = stepList;
        this.clickListener = clickListener;
    }

    @Override
    public StepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.step_list_item, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StepViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (stepList == null) {
            return 0;
        }
        return stepList.size();
    }

    class StepViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Step step;

        @BindView(R.id.recipe_step_nr)
        TextView nrView;
        @BindView(R.id.recipe_step_instruction)
        TextView instructionView;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onStepClick(step);
        }

        void bind(int index) {
            step = stepList.get(index);
            nrView.setText(Integer.toString(step.nr));
            instructionView.setText(step.name);
        }
    }

    public interface StepClickListener {
        void onStepClick(Step step);
    }
}
