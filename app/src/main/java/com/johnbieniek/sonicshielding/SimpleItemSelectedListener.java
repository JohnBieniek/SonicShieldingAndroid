package com.johnbieniek.sonicshielding;

import android.view.View;
import android.widget.AdapterView;

final class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {
    interface Selection { void select(int position); }
    private final Selection selection;
    SimpleItemSelectedListener(Selection selection) { this.selection = selection; }
    @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { selection.select(position); }
    @Override public void onNothingSelected(AdapterView<?> parent) {}
}
