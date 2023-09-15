package com.jancar.bluetooth.utils;

import android.content.Context;
import android.text.TextUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 对象工厂.
 */

public class ObjectUtil {

    /**
     * 根据类名创建对象
     *
     * @param className 类名
     * @return
     */
    public static <T> T newObject(String className, Context context) {
        T ret = null;

        if (!TextUtils.isEmpty(className)) {
            try {
                Class<T> tClass = (Class<T>) Class.forName(className);
                Constructor<T> tConstructor = tClass.getConstructor(Context.class);
                ret = tConstructor.newInstance(context);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
}
