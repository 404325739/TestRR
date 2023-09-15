package com.jancar.bluetooth.utils;

public class NoDoubleClickUtils {
        private static long lastClickTime = 0;
        private final static int SPACE_TIME = 150;
        private final static int CLICK_LIMIT_TIME = 400;

        public synchronized static boolean isDoubleClick() {
            long currentTime = System.currentTimeMillis();
            boolean isClick2;
            if (Math.abs(currentTime - lastClickTime) >
                    SPACE_TIME) {
                isClick2 = false;
            } else {
                isClick2 = true;
            }
            lastClickTime = currentTime;
            return isClick2;
        }

    public synchronized static boolean isIllegalClick() {
        long currentTime = System.currentTimeMillis();
        boolean isClick2;
        if (Math.abs(currentTime - lastClickTime) < CLICK_LIMIT_TIME) {
            isClick2 = true;
        } else {
            isClick2 = false;
            lastClickTime = currentTime;
        }
        return isClick2;
    }
    }