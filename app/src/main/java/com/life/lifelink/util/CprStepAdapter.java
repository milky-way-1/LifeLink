package com.life.lifelink.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.life.lifelink.R;
import com.life.lifelink.model.CprStep;

import java.util.List;

public class CprStepAdapter extends RecyclerView.Adapter<CprStepAdapter.StepViewHolder> {
    private List<CprStep> steps;

    public CprStepAdapter(List<CprStep> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(com.life.lifelink.R.layout.item_cpr_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        CprStep step = steps.get(position);
        holder.bind(step);
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        ImageView stepImage;
        TextView stepNumber;
        TextView stepTitle;
        TextView stepDescription;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            stepImage = itemView.findViewById(R.id.stepImage);
            stepNumber = itemView.findViewById(R.id.stepNumber);
            stepTitle = itemView.findViewById(R.id.stepTitle);
            stepDescription = itemView.findViewById(R.id.stepDescription);
        }

        void bind(CprStep step) {
            stepImage.setImageResource(step.getImageResId());
            stepNumber.setText("Step " + step.getStepNumber());
            stepTitle.setText(step.getTitle());
            stepDescription.setText(step.getDescription());
        }
    }
}
