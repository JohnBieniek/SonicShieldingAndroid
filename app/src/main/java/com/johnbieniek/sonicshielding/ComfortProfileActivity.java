package com.johnbieniek.sonicshielding;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public final class ComfortProfileActivity extends Activity {
    private LinearLayout bandsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.comfort_profile);
        setContentView(buildContent());
    }

    private ScrollView buildContent() {
        int spacing = dp(20);
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(spacing, spacing, spacing, spacing);
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = text(getString(R.string.profile_title), 28, Typeface.BOLD);
        content.addView(title);

        TextView intro = text(getString(R.string.profile_intro), 16, Typeface.NORMAL);
        LinearLayout.LayoutParams introParams = matchWrap();
        introParams.setMargins(0, dp(8), 0, dp(14));
        content.addView(intro, introParams);

        Switch eqSwitch = new Switch(this);
        eqSwitch.setText(R.string.permanent_comfort_eq);
        eqSwitch.setTextSize(18);
        eqSwitch.setChecked(ShieldPreferences.isEqEnabled(this));
        content.addView(eqSwitch, matchWrap());

        bandsContainer = new LinearLayout(this);
        bandsContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams bandParams = matchWrap();
        bandParams.setMargins(0, dp(12), 0, 0);
        content.addView(bandsContainer, bandParams);
        buildBands();
        bandsContainer.setAlpha(eqSwitch.isChecked() ? 1f : 0.42f);

        eqSwitch.setOnCheckedChangeListener((button, checked) -> {
            ShieldPreferences.setEqEnabled(this, checked);
            bandsContainer.setAlpha(checked ? 1f : 0.42f);
            ShieldController.refreshProfile(this);
        });

        Button reset = new Button(this);
        reset.setText(R.string.reset_profile);
        LinearLayout.LayoutParams resetParams = matchWrap();
        resetParams.setMargins(0, dp(12), 0, 0);
        content.addView(reset, resetParams);
        reset.setOnClickListener(view -> {
            ShieldPreferences.resetProfile(this);
            recreate();
            ShieldController.refreshProfile(this);
        });

        TextView note = text(getString(R.string.device_note), 14, Typeface.NORMAL);
        LinearLayout.LayoutParams noteParams = matchWrap();
        noteParams.setMargins(0, dp(18), 0, dp(8));
        content.addView(note, noteParams);
        return scroll;
    }

    private void buildBands() {
        int[] values = ShieldPreferences.getReductions(this);
        for (int i = 0; i < ShieldPreferences.FREQUENCIES.length; i++) {
            final int index = i;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, dp(4), 0, dp(8));

            LinearLayout labels = new LinearLayout(this);
            labels.setGravity(Gravity.CENTER_VERTICAL);
            TextView frequency = text(formatFrequency(ShieldPreferences.FREQUENCIES[i]), 16, Typeface.BOLD);
            TextView value = text(getString(R.string.reduction_value, values[i]), 15, Typeface.NORMAL);
            value.setGravity(Gravity.END);
            labels.addView(frequency, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            labels.addView(value, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            row.addView(labels, matchWrap());

            SeekBar slider = new SeekBar(this);
            slider.setMax(100);
            slider.setProgress(values[i]);
            row.addView(slider, matchWrap());
            slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    value.setText(getString(R.string.reduction_value, progress));
                    if (fromUser) {
                        ShieldPreferences.setReduction(ComfortProfileActivity.this, index, progress);
                    }
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    ShieldController.refreshProfile(ComfortProfileActivity.this);
                }
            });
            bandsContainer.addView(row, matchWrap());
        }
    }

    private String formatFrequency(int frequency) {
        return frequency >= 1000 ? (frequency / 1000) + " kHz" : frequency + " Hz";
    }

    private TextView text(String value, int size, int style) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTypeface(Typeface.DEFAULT, style);
        view.setLineSpacing(0, 1.16f);
        return view;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
