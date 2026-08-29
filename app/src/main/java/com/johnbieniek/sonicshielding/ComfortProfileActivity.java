package com.johnbieniek.sonicshielding;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public final class ComfortProfileActivity extends Activity {
    private static final int NAVY = Color.rgb(7, 27, 53);
    private static final int CARD = Color.rgb(13, 41, 71);
    private static final int INNER_CARD = Color.rgb(10, 35, 62);
    private static final int BORDER = Color.rgb(32, 71, 100);
    private static final int TEXT = Color.rgb(238, 252, 255);
    private static final int MUTED = Color.rgb(167, 194, 209);
    private static final int AQUA = Color.rgb(54, 215, 202);
    private static final int GOLD = Color.rgb(241, 198, 107);

    private final TonePlayer tonePlayer = new TonePlayer();
    private LinearLayout bandsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(NAVY);
        setContentView(buildContent());
    }

    private ScrollView buildContent() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(NAVY);
        LinearLayout page = column();
        page.setPadding(dp(18), dp(24), dp(18), dp(28));
        scroll.addView(page, matchWrap());

        LinearLayout brand = row();
        brand.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.logo_tight);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        brand.addView(logo, new LinearLayout.LayoutParams(dp(58), dp(58)));
        LinearLayout brandText = column();
        LinearLayout.LayoutParams brandParams = wrapWrap();
        brandParams.setMargins(dp(12), 0, 0, 0);
        brand.addView(brandText, brandParams);
        brandText.addView(text("Sonic Shielding", 24, Typeface.BOLD, TEXT));
        brandText.addView(text("Comfort profile", 14, Typeface.NORMAL, MUTED));
        brandText.addView(text("Softer sound where you need it.", 14, Typeface.NORMAL, MUTED));
        page.addView(brand, matchWrap());

        LinearLayout warning = card(GOLD);
        warning.addView(text("Use care with sound.", 16, Typeface.BOLD, TEXT));
        warning.addView(body("This is a comfort tool, not a medical or hearing test. Begin with a low device volume and stop a test tone immediately if it is uncomfortable."));
        page.addView(warning, cardParams());

        page.addView(buildProtectionCard(), cardParams());
        page.addView(buildEqCard(), cardParams());

        LinearLayout about = card(BORDER);
        about.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView privacyTitle = text("Everything stays on your device.", 19, Typeface.BOLD, TEXT);
        privacyTitle.setGravity(Gravity.CENTER);
        about.addView(privacyTitle);
        TextView privacyBody = body("Your comfort profile is stored locally. Sonic Shielding does not record audio, use the microphone, or send listening data anywhere.");
        privacyBody.setGravity(Gravity.CENTER);
        about.addView(privacyBody);
        TextView partnership = text("Created in partnership with", 13, Typeface.NORMAL, MUTED);
        partnership.setGravity(Gravity.CENTER);
        about.addView(partnership, topMargin(dp(18)));
        ImageView whimsy = new ImageView(this);
        whimsy.setImageResource(R.drawable.whimsy_logo);
        whimsy.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        about.addView(whimsy, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(76)));
        LinearLayout.LayoutParams aboutParams = cardParams();
        aboutParams.setMargins(0, dp(18), 0, dp(66));
        page.addView(about, aboutParams);
        return scroll;
    }

    private LinearLayout buildProtectionCard() {
        LinearLayout card = card(BORDER);
        card.addView(text("Beep blocker", 21, Typeface.BOLD, TEXT));
        card.addView(body("Protection settings for beeps, electronic dings, squeals, abrupt sounds, speech, and alarm-like audio."));

        Switch enabled = toggle("Enable beep blocker", ShieldPreferences.isBeepBlockerEnabled(this));
        enabled.setOnCheckedChangeListener((button, value) -> {
            ShieldPreferences.setBeepBlockerEnabled(this, value);
            refresh();
        });
        card.addView(enabled, matchWrap());

        LinearLayout tonal = innerCard();
        tonal.addView(text("Tone-specific protection", 17, Typeface.BOLD, TEXT));
        tonal.addView(body("On supported devices, Sonic Shielding reduces protected upper-frequency bands while leaving lower sound intact."));

        Spinner strength = new Spinner(this);
        String[] strengths = {"Low", "Balanced", "Strong"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, strengths);
        strength.setAdapter(adapter);
        String savedStrength = ShieldPreferences.getProtectionStrength(this);
        strength.setSelection("low".equals(savedStrength) ? 0 : "balanced".equals(savedStrength) ? 1 : 2);
        tonal.addView(fieldLabel("Protection strength"));
        tonal.addView(strength, matchWrap());
        strength.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            String value = position == 0 ? "low" : position == 1 ? "balanced" : "strong";
            ShieldPreferences.setProtectionStrength(this, value);
            int sensitivity = position == 0 ? 35 : position == 1 ? 50 : 95;
            int reduction = position == 0 ? 88 : position == 1 ? 94 : 99;
            ShieldPreferences.setSensitivity(this, sensitivity);
            ShieldPreferences.setTonalReduction(this, reduction);
            refresh();
        }));

        tonal.addView(slider("Tone detection sensitivity", ShieldPreferences.getSensitivity(this), 0, 100,
                value -> value + "%", value -> ShieldPreferences.setSensitivity(this, value)));
        tonal.addView(slider("Maximum tone reduction", ShieldPreferences.getTonalReduction(this), 0, 100,
                value -> value + "% reduced", value -> { ShieldPreferences.setTonalReduction(this, value); refresh(); }));
        tonal.addView(slider("Lowest protected frequency", ShieldPreferences.getMinimumFrequency(this), 1000, 5000,
                value -> formatFrequency(value), value -> { ShieldPreferences.setMinimumFrequency(this, value); refresh(); }));
        tonal.addView(slider("Release duration", ShieldPreferences.getReleaseDuration(this), 40, 250,
                value -> value + " ms", value -> ShieldPreferences.setReleaseDuration(this, value)));

        Switch speech = toggle("Speech protection", ShieldPreferences.isSpeechProtectionEnabled(this));
        speech.setOnCheckedChangeListener((button, value) -> {
            ShieldPreferences.setSpeechProtectionEnabled(this, value);
            refresh();
        });
        tonal.addView(speech, matchWrap());
        tonal.addView(body("Preserves more of the speech range when shaping protected frequencies."));

        Switch alarm = toggle("Aggressive alarm blocker", ShieldPreferences.isAlarmBlockerEnabled(this));
        alarm.setOnCheckedChangeListener((button, value) -> {
            ShieldPreferences.setAlarmBlockerEnabled(this, value);
            refresh();
        });
        tonal.addView(alarm, matchWrap());
        tonal.addView(body("Widens upper-frequency suppression for alarm-like tones. It does not silence Android's alarm stream or replace safe alarm-volume settings."));
        card.addView(tonal, innerParams());

        LinearLayout spike = innerCard();
        spike.addView(text("Whole-sound spike protection", 17, Typeface.BOLD, TEXT));
        spike.addView(body("Sets the preferred reduction for abrupt clicks, impacts, and loud bursts when a compatible audio path can identify them."));
        spike.addView(slider("Sudden sound reduction", ShieldPreferences.getSuddenSoundReduction(this), 0, 90,
                value -> value + "% reduced", value -> ShieldPreferences.setSuddenSoundReduction(this, value)));
        card.addView(spike, innerParams());

        TextView support = body("Android compatibility note: ordinary apps cannot capture and rewrite every other app's mixed audio. The active output-mix EQ is device-dependent; adaptive detection settings are retained in your profile but only apply where Android exposes a compatible audio path.");
        support.setTextColor(GOLD);
        card.addView(support, topMargin(dp(14)));
        return card;
    }

    private LinearLayout buildEqCard() {
        LinearLayout card = card(BORDER);
        card.addView(text("Optional comfort EQ", 21, Typeface.BOLD, TEXT));
        Switch eqSwitch = toggle("Enable permanent comfort EQ", ShieldPreferences.isEqEnabled(this));
        card.addView(eqSwitch, matchWrap());
        card.addView(body("Each setting covers a frequency range, not one exact tone. Test plays a quiet representative tone from the middle of that range."));

        bandsContainer = column();
        card.addView(bandsContainer, matchWrap());
        buildBands();
        bandsContainer.setAlpha(eqSwitch.isChecked() ? 1f : 0.48f);
        eqSwitch.setOnCheckedChangeListener((button, checked) -> {
            ShieldPreferences.setEqEnabled(this, checked);
            bandsContainer.setAlpha(checked ? 1f : 0.48f);
            refresh();
        });

        Button reset = button("Reset to Sonic Shielding defaults", false);
        reset.setOnClickListener(view -> {
            ShieldPreferences.resetProfile(this);
            tonePlayer.stop();
            recreate();
            refresh();
        });
        card.addView(reset, topMargin(dp(14)));
        return card;
    }

    private void buildBands() {
        int[] low = {45, 90, 180, 355, 710, 1400, 2800, 5700, 9800};
        int[] high = {90, 180, 355, 710, 1400, 2800, 5700, 9800, 16000};
        int[] values = ShieldPreferences.getReductions(this);
        for (int index = 0; index < values.length; index++) {
            final int bandIndex = index;
            int testFrequency = ShieldPreferences.FREQUENCIES[index];
            LinearLayout rangeRow = row();
            rangeRow.setGravity(Gravity.CENTER_VERTICAL);
            TextView range = text(formatFrequency(low[index]) + "–" + formatFrequency(high[index]), 16, Typeface.BOLD, TEXT);
            rangeRow.addView(range, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            Button test = button("Test", true);
            test.setContentDescription("Test " + range.getText() + " range at " + formatFrequency(testFrequency));
            test.setOnClickListener(view -> tonePlayer.play(testFrequency));
            rangeRow.addView(test, new LinearLayout.LayoutParams(dp(82), dp(44)));
            bandsContainer.addView(rangeRow, topMargin(dp(12)));

            TextView value = text(values[index] + "% reduced", 13, Typeface.NORMAL, MUTED);
            SeekBar slider = new SeekBar(this);
            slider.setMax(100);
            slider.setProgress(values[index]);
            slider.setProgressTintList(android.content.res.ColorStateList.valueOf(AQUA));
            slider.setThumbTintList(android.content.res.ColorStateList.valueOf(AQUA));
            bandsContainer.addView(slider, matchWrap());
            bandsContainer.addView(value, matchWrap());
            slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    value.setText(getString(R.string.reduction_value, progress));
                    if (fromUser) ShieldPreferences.setReduction(ComfortProfileActivity.this, bandIndex, progress);
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) { refresh(); }
            });
        }
    }

    private LinearLayout slider(String label, int value, int minimum, int maximum, Formatter formatter, Saver saver) {
        LinearLayout group = column();
        TextView title = fieldLabel(label);
        TextView output = text(formatter.format(value), 13, Typeface.NORMAL, MUTED);
        group.addView(title, topMargin(dp(12)));
        SeekBar slider = new SeekBar(this);
        slider.setMax(maximum - minimum);
        slider.setProgress(value - minimum);
        slider.setProgressTintList(android.content.res.ColorStateList.valueOf(AQUA));
        slider.setThumbTintList(android.content.res.ColorStateList.valueOf(AQUA));
        group.addView(slider, matchWrap());
        group.addView(output, matchWrap());
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int actual = progress + minimum;
                output.setText(formatter.format(actual));
                if (fromUser) saver.save(actual);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) { refresh(); }
        });
        return group;
    }

    private void refresh() { ShieldController.refreshProfile(this); }

    private LinearLayout card(int accent) {
        LinearLayout view = column();
        view.setPadding(dp(17), dp(17), dp(17), dp(17));
        view.setBackground(rounded(CARD, accent, dp(16)));
        return view;
    }

    private LinearLayout innerCard() {
        LinearLayout view = column();
        view.setPadding(dp(14), dp(14), dp(14), dp(14));
        view.setBackground(rounded(INNER_CARD, Color.rgb(40, 86, 117), dp(13)));
        return view;
    }

    private GradientDrawable rounded(int fill, int stroke, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(fill);
        shape.setCornerRadius(radius);
        shape.setStroke(dp(1), stroke);
        return shape;
    }

    private Switch toggle(String label, boolean checked) {
        Switch view = new Switch(this);
        view.setText(label);
        view.setTextColor(TEXT);
        view.setTextSize(16);
        view.setChecked(checked);
        view.setButtonTintList(android.content.res.ColorStateList.valueOf(AQUA));
        return view;
    }

    private Button button(String label, boolean compact) {
        Button view = new Button(this);
        view.setText(label);
        view.setTextColor(Color.rgb(5, 32, 43));
        view.setTextSize(compact ? 13 : 15);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setAllCaps(false);
        view.setPadding(dp(10), 0, dp(10), 0);
        view.setBackground(rounded(AQUA, AQUA, dp(10)));
        return view;
    }

    private TextView body(String value) {
        TextView view = text(value, 14, Typeface.NORMAL, MUTED);
        view.setLineSpacing(0, 1.18f);
        return view;
    }

    private TextView fieldLabel(String value) { return text(value, 15, Typeface.BOLD, TEXT); }

    private TextView text(String value, int size, int style, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, style);
        return view;
    }

    private LinearLayout column() { LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.VERTICAL); return view; }
    private LinearLayout row() { LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.HORIZONTAL); return view; }
    private LinearLayout.LayoutParams matchWrap() { return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); }
    private LinearLayout.LayoutParams wrapWrap() { return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); }
    private LinearLayout.LayoutParams topMargin(int margin) { LinearLayout.LayoutParams params = matchWrap(); params.setMargins(0, margin, 0, 0); return params; }
    private LinearLayout.LayoutParams cardParams() { LinearLayout.LayoutParams params = matchWrap(); params.setMargins(0, dp(18), 0, 0); return params; }
    private LinearLayout.LayoutParams innerParams() { LinearLayout.LayoutParams params = matchWrap(); params.setMargins(0, dp(14), 0, 0); return params; }
    private String formatFrequency(int hz) { return hz >= 1000 ? (hz % 1000 == 0 ? (hz / 1000) + " kHz" : String.format(java.util.Locale.US, "%.1f kHz", hz / 1000f)) : hz + " Hz"; }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override protected void onDestroy() { tonePlayer.stop(); super.onDestroy(); }

    private interface Formatter { String format(int value); }
    private interface Saver { void save(int value); }
}
