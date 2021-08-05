package com.dspread.rn.utils;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.util.Hashtable;

/**
 *
 */
public class ReactUtils {
    private ReactUtils() {
    }

    public static WritableMap write(Hashtable<String, ?> in, WritableMap result) {
        for (String key : in.keySet()) {
            final Object value = in.get(key);
            if (value instanceof Hashtable) {
                write((Hashtable) value, result);
            } else {
                result.putString(key, in.get(key).toString());
            }
        }
        return result;
    }

    public static ReadableMap convert(Hashtable<String, ?> in) {
        if (in == null) {
            return new WritableNativeMap();
        }
        final WritableMap result = new WritableNativeMap();
        for (String key : in.keySet()) {
            result.putString(key, in.get(key).toString());
        }
        return result;
    }
}
