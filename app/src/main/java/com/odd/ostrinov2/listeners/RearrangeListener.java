package com.odd.ostrinov2.listeners;

public interface RearrangeListener {
    void onGrab(int index);
    boolean onRearrangeRequested(int fromIndex, int toIndex);
    void onDrop();
}