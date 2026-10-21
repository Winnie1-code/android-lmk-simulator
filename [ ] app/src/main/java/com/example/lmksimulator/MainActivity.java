package com.example.lmksimulator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView tvUsed, tvFree, tvPressure, tvApps, tvLog;
    ProgressBar ramBar;
    Button btnIncrease, btnRun, btnReset;

    int totalRam = 4096;
    int systemRam = 400;

    String[] apps = {
            "Chrome",
            "Music Player",
            "Maps",
            "Current App"
    };

    int[] memory = {650, 300, 500, 700};

    String[] states = {
            "Cached",
            "Background",
            "Visible",
            "Foreground"
    };

    int[] scores = {900, 700, 200, 0};

    boolean[] running = {true, true, true, true};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvUsed = findViewById(R.id.tvUsed);
        tvFree = findViewById(R.id.tvFree);
        tvPressure = findViewById(R.id.tvPressure);
        tvApps = findViewById(R.id.tvApps);
        tvLog = findViewById(R.id.tvLog);

        ramBar = findViewById(R.id.ramBar);

        btnIncrease = findViewById(R.id.btnIncrease);
        btnRun = findViewById(R.id.btnRun);
        btnReset = findViewById(R.id.btnReset);

        btnIncrease.setOnClickListener(v -> increaseMemory());
        btnRun.setOnClickListener(v -> runLMK());
        btnReset.setOnClickListener(v -> resetSimulation());

        updateScreen();
    }

    private int getUsedRam() {
        int used = systemRam;

        for (int i = 0; i < apps.length; i++) {
            if (running[i]) {
                used += memory[i];
            }
        }

        return used;
    }

    private void increaseMemory() {
        if (systemRam < 1900) {
            systemRam += 500;

            if (systemRam > 1900) {
                systemRam = 1900;
            }

            int pressure = (getUsedRam() * 100) / totalRam;

            tvLog.setText(
                    "Memory usage increased.\nRAM pressure: "
                            + pressure + "%"
            );

            if (pressure >= 85) {
                Toast.makeText(
                        this,
                        "Warning! RAM pressure is high. Background apps may be terminated.",
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Toast.makeText(
                        this,
                        "RAM usage increased to " + pressure + "%.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        } else {
            Toast.makeText(
                    this,
                    "Maximum simulated memory reached! Run LMK to free RAM.",
                    Toast.LENGTH_LONG
            ).show();
        }

        updateScreen();
    }

    private void runLMK() {
        int pressure = (getUsedRam() * 100) / totalRam;

        if (pressure < 85) {
            tvLog.setText(
                    "RAM pressure is " + pressure
                            + "%. No process needs to be terminated yet."
            );

            Toast.makeText(
                    this,
                    "RAM pressure is below 85%. No termination needed.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        int selected = -1;
        int highestScore = -1;

        for (int i = 0; i < apps.length; i++) {
            if (running[i]
                    && (states[i].equals("Cached")
                    || states[i].equals("Background"))) {

                if (scores[i] > highestScore) {
                    highestScore = scores[i];
                    selected = i;
                }
            }
        }

        if (selected != -1) {
            String terminatedApp = apps[selected];
            int freedMemory = memory[selected];
            String oldState = states[selected];

            running[selected] = false;
            states[selected] = "Terminated";

            tvLog.setText(
                    "PROCESS TERMINATED\n\n"
                            + "App: " + terminatedApp
                            + "\nPrevious state: " + oldState
                            + "\nRAM pressure: " + pressure + "%"
                            + "\nMemory freed: " + freedMemory + " MB"
                            + "\n\nReason: RAM pressure is high. "
                            + "The simulator selected an eligible "
                            + "background or cached process to free memory."
            );

            Toast.makeText(
                    this,
                    terminatedApp
                            + " terminated! RAM pressure is "
                            + pressure + "%. Background memory is being freed.",
                    Toast.LENGTH_LONG
            ).show();

            updateScreen();

        } else {
            tvLog.setText(
                    "RAM pressure is high, but no eligible cached "
                            + "or background process is available."
            );

            Toast.makeText(
                    this,
                    "High RAM pressure! No eligible background process found.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void resetSimulation() {
        systemRam = 400;

        memory[0] = 650;
        memory[1] = 300;
        memory[2] = 500;
        memory[3] = 700;

        states[0] = "Cached";
        states[1] = "Background";
        states[2] = "Visible";
        states[3] = "Foreground";

        scores[0] = 900;
        scores[1] = 700;
        scores[2] = 200;
        scores[3] = 0;

        for (int i = 0; i < running.length; i++) {
            running[i] = true;
        }

        tvLog.setText("Simulation reset. All apps are running.");

        updateScreen();

        Toast.makeText(
                this,
                "Simulation reset successfully!",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void updateScreen() {
        int used = getUsedRam();
        int free = totalRam - used;
        int pressure = (used * 100) / totalRam;

        tvUsed.setText("Used RAM: " + used + " MB");
        tvFree.setText("Free RAM: " + free + " MB");
        tvPressure.setText("RAM Pressure: " + pressure + "%");

        ramBar.setMax(100);
        ramBar.setProgress(pressure);

        StringBuilder details = new StringBuilder();

        for (int i = 0; i < apps.length; i++) {
            details.append(apps[i])
                    .append("\nMemory: ")
                    .append(memory[i])
                    .append(" MB")
                    .append("\nState: ")
                    .append(states[i])
                    .append("\nTermination score: ")
                    .append(scores[i])
                    .append("\nStatus: ")
                    .append(running[i] ? "Running" : "Terminated")
                    .append("\n\n");
        }

        tvApps.setText(details.toString());
    }
}
