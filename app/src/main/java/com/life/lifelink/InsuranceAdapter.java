package com.life.lifelink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class InsuranceAdapter extends RecyclerView.Adapter<InsuranceAdapter.InsuranceViewHolder> {
    private List<InsuranceDTO> insuranceList;
    private OnInsuranceClickListener listener;

    public interface OnInsuranceClickListener {
        void onInsuranceClick(InsuranceDTO insurance);
    }

    public InsuranceAdapter(List<InsuranceDTO> insuranceList, OnInsuranceClickListener listener) {
        this.insuranceList = insuranceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InsuranceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_insurance_card, parent, false);
        return new InsuranceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InsuranceViewHolder holder, int position) {
        InsuranceDTO insurance = insuranceList.get(position);
        holder.bind(insurance);
    }

    @Override
    public int getItemCount() {
        return insuranceList.size();
    }

    public void updateInsuranceList(List<InsuranceDTO> newList) {
        this.insuranceList = newList;
        notifyDataSetChanged();
    }

    class InsuranceViewHolder extends RecyclerView.ViewHolder {
        private TextView providerName;
        private TextView policyNumber;
        private TextView policyHolder;
        private MaterialButton viewDetailsButton;

        InsuranceViewHolder(@NonNull View itemView) {
            super(itemView);
            providerName = itemView.findViewById(R.id.providerName);
            policyNumber = itemView.findViewById(R.id.policyNumber);
            policyHolder = itemView.findViewById(R.id.policyHolder);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }

        void bind(InsuranceDTO insurance) {
            providerName.setText(insurance.getProviderName());
            policyNumber.setText("Policy: " + insurance.getPolicyNumber());
            policyHolder.setText("Holder: " + insurance.getPolicyHolderName());

            viewDetailsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInsuranceClick(insurance);
                }
            });

            // Add animation
            itemView.setAlpha(0f);
            itemView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }
}
