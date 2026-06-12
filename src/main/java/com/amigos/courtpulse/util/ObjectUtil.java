package com.amigos.courtpulse.util;

import java.util.Objects;

public final class ObjectUtil {

    private ObjectUtil() {
    }

    public static boolean isNull(Object value) {
        return value == null;
    }

    public static boolean nonNull(Object value) {
        return value != null;
    }

    public static boolean isEqual(Object left, Object right) {
        return Objects.equals(left, right);
    }

    public static boolean isNotEqual(Object left, Object right) {
        return !Objects.equals(left, right);
    }
}
