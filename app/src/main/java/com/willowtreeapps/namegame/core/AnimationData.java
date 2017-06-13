package com.willowtreeapps.namegame.core;

import android.view.View;

/**
 * The animation data class contains information about an animation action.
 * @author Keegan Smith
 * @since 6/5/2017
 */

public class AnimationData {
    private boolean selectionCorrect; //Indicates what animation will be used.
    private View selectedView; //The view that will be animated

    public AnimationData(boolean selectionCorrect, View selectedView){
        this.selectedView = selectedView;
        this.selectionCorrect = selectionCorrect;
    }

    public boolean isSelectionCorrect() {
        return selectionCorrect;
    }

    public void setSelectionCorrect(boolean selectionCorrect) {
        this.selectionCorrect = selectionCorrect;
    }

    public View getSelectedView() {
        return selectedView;
    }

    public void setSelectedView(View selectedView) {
        this.selectedView = selectedView;
    }
}
