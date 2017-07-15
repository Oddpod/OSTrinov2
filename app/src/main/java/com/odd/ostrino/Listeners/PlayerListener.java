package com.odd.ostrino.Listeners;

import com.odd.ostrino.Ost;

import java.util.List;

public interface PlayerListener {
    void updateCurrentlyPlaying(int newId);
    void next();
    void previous();
    void shuffle(List<Ost> osts);
}
