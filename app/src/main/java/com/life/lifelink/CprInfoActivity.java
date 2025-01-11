package com.life.lifelink;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.life.lifelink.model.CprStep;
import com.life.lifelink.util.CprStepAdapter;

import java.util.ArrayList;
import java.util.List;

public class CprInfoActivity extends AppCompatActivity {
    private RecyclerView stepsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpr_info);

        // Initialize RecyclerView
        stepsRecyclerView = findViewById(R.id.stepsRecyclerView);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        List<CprStep> steps = new ArrayList<>();

        steps.add(new CprStep(1,
                "Check the Scene and the Person",
                "• Make sure the scene is safe\n" +
                        "• Tap and shout to get response\n" +
                        "• Check if breathing is normal\n" +
                        "• Look for severe bleeding",
                R.drawable.check_scene));

        steps.add(new CprStep(2,
                "Position for CPR",
                "• Person should be lying on their back on a firm, flat surface\n" +
                        "• Kneel beside the person's neck and shoulders\n" +
                        "• Remove any bulky clothing",
                R.drawable.position_cpr));

        steps.add(new CprStep(3,
                "Give 30 Chest Compressions",
                "• Place the heel of one hand on the center of the chest\n" +
                        "• Place your other hand on top and lace your fingers together\n" +
                        "• Keep your arms straight and position shoulders directly over hands\n" +
                        "• Push hard and fast:\n" +
                        "  - At least 2 inches deep\n" +
                        "  - At a rate of 100-120 compressions per minute\n" +
                        "• Let the chest fully recoil between compressions",
                R.drawable.chest_compression));

        steps.add(new CprStep(4,
                "Give 2 Rescue Breaths",
                "• Tilt the head back and lift the chin up\n" +
                        "• Pinch the nose shut\n" +
                        "• Make a complete seal over the person's mouth\n" +
                        "• Blow in for about 1 second to make the chest rise\n" +
                        "• Give second breath\n" +
                        "• If chest doesn't rise, reposition head and try again",
                R.drawable.rescue_breaths));

        steps.add(new CprStep(5,
                "Continue CPR",
                "• Keep giving sets of 30 chest compressions and 2 breaths\n" +
                        "• Continue until:\n" +
                        "  - You see obvious signs of life\n" +
                        "  - An AED is ready to use\n" +
                        "  - EMS or trained help takes over\n" +
                        "  - You're too exhausted to continue\n" +
                        "  - The scene becomes unsafe",
                R.drawable.continue_cpr));

        CprStepAdapter adapter = new CprStepAdapter(steps);
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stepsRecyclerView.setAdapter(adapter);
    }
}