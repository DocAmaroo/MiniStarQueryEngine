package qengine.program.utils;

import java.util.ArrayList;

public class Utils {

    /**
     * Display all the values contains in an index give
     */
    public static void displayIndex(ArrayList<ArrayList<Integer>> index) {
        for (ArrayList<Integer> arr : index) {
            System.out.println(arr);
        }
    }

    /**
     * Return the current time in millisecond
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
