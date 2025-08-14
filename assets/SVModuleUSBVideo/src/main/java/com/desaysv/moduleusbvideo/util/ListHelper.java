package com.desaysv.moduleusbvideo.util;

import java.util.Collections;
import java.util.List;

/**
 */
public class ListHelper {

    public static <T extends Comparable<T>> boolean compare(List<T> a, List<T> b) {
        if (a == null && b == null) {
            return true;
        }

        if (a == null || b == null) {
            return false;
        }

        if (a.size() != b.size()) {
            return false;
        }
        if(a.contains(null) || b.contains(null)){
            return false;
        }
        Collections.sort(a);
        Collections.sort(b);
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i) == null || !a.get(i).equals(b.get(i))) {
                return false;
            }
        }
        return true;
    }
}
