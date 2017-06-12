package com.example.odd.ostrinofragnavdrawer;

import java.util.Random;

public interface PlayerListener {
    void updateCurrentlyPlaying(int newId);
    void next();
    void previous();
    void shuffle(long seed, Random rnd);
}
