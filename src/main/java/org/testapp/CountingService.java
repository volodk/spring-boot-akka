package org.testapp;

import javax.inject.Named;

@Named("CountingService")
public class CountingService {
    /**
     * Increment the given number by one.
     */
    public int increment(int count) {
        return count + 1;
    }
}
