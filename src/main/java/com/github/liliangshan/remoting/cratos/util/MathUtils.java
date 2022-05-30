package com.github.liliangshan.remoting.cratos.util;

/**
 * MathUtils .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public final class MathUtils {

    private MathUtils() {

    }

    public static int getNonNegativeRange24bit(int originValue) {
        return 0x00ffffff & originValue;
    }

}
