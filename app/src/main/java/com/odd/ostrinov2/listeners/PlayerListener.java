package com.odd.ostrinov2.listeners;

import com.odd.ostrinov2.Ost;

import java.util.List;

public interface PlayerListener {
    void updateCurrentlyPlaying(int newId);
    void next();
    void previous();
    void shuffle(long seed);
    void unShuffle(List<Ost> unShuffledList);
}
