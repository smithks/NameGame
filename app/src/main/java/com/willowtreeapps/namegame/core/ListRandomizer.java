package com.willowtreeapps.namegame.core;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ListRandomizer {

    @NonNull
    private final Random random;

    public ListRandomizer(@NonNull Random random) {
        this.random = random;
    }

    /**
     * Used to pick a random element out of a given list.
     * @param list the list to pick an element from.
     * @return a random element from that list.
     */
    @NonNull
    public <T> T pickOne(@NonNull List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    /**
     * Returns N random elements from the given list.
     * @param list The list to choose elements from.
     * @param n The number of elements to pick.
     * @return A list of random elements of length N.
     */
    @NonNull
    public <T> List<T> pickN(@NonNull List<T> list, int n) {
        if (list.size() == n) return list;
        if (n == 0) return Collections.emptyList();
        List<T> pickFrom = new ArrayList<>(list);
        List<T> picks = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            picks.add(pickFrom.remove(random.nextInt(pickFrom.size())));
        }
        return picks;
    }
}
