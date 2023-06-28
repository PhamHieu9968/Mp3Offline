package com.hieu.mp3offline;

import android.widget.SeekBar;

public interface OnSeekBarChange extends SeekBar.OnSeekBarChangeListener {

    @Override
    default void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    default void onStartTrackingTouch(SeekBar seekBar) {

    }


}
