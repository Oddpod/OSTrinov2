package com.example.odd.ostrinofragnavdrawer.Listeners;

import com.example.odd.ostrinofragnavdrawer.Ost;

import java.util.List;

public interface PlayerListener {
    void updateCurrentlyPlaying(int newId);
    void next();
    void previous();
    void shuffle(List<Ost> osts);
}
