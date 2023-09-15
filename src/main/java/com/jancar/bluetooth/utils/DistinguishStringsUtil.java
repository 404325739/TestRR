package com.jancar.bluetooth.utils;


import android.content.Context;

import com.jancar.sdk.utils.CustomUtil;
import com.jancar.sdk.utils.Logcat;
import com.jancar.utils.SystemPropertiesUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 类描述：不同客户使用同一种语言 语言冲突进行动态修改字符串
 * 创建人：thx
 * 创建时间：2022/3/25 19:29
 * 修改人：thx
 * 修改时间：2022/3/25 19:29
 * 修改备注：
 */
public class DistinguishStringsUtil {

        private static final Map<String, String> ruiShiJiaStringMap;
        private static int currentCustomerID =-1;

        static {
            ruiShiJiaStringMap = new HashMap<>();
            ruiShiJiaStringMap.put("search_device", "Odkryj urządzenie");
        }

        public static String getStringByCustomerID(String stringName, Context context) {
            if (currentCustomerID<0){
                currentCustomerID =SystemPropertiesUtil.getInt(CustomUtil.PERSIST_JANCAR_CUSTOMER_ID);
            }

            Locale locale = context.getResources().getConfiguration().getLocales().get(0);
            Logcat.i("CUSTOMER_ID: " + currentCustomerID
                    + " Language: " + locale.getLanguage());
            if (CustomUtil.CUSTOMER_ID.RuiShiJia ==currentCustomerID && "pl".equals(locale.getLanguage())) {
                return ruiShiJiaStringMap.get(stringName);
            } else {
                return null;
            }
        }
}
