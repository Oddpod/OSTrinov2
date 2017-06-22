package com.example.odd.ostrino.Listeners;

import com.example.odd.ostrino.Ost;

import java.util.List;

public interface PlayerListener {
    void updateCurrentlyPlaying(int newId);
    void next();
    void previous();
    void shuffle(List<Ost> osts);
}
