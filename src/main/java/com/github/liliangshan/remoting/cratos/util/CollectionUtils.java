package com.github.liliangshan.remoting.cratos.util;

import java.util.Collection;

/**
 * CollectionUtils .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public final class CollectionUtils {

    private CollectionUtils() {
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }


}
