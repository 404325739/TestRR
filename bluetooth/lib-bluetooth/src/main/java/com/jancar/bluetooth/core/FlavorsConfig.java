package com.jancar.bluetooth.core;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;

import com.jancar.bluetooth.ui.adapter.ContactsAdapter;
import com.jancar.bluetooth.ui.agent.BannerViewAgent;
import com.jancar.bluetooth.ui.agent.ContactViewAgent;
import com.jancar.bluetooth.ui.agent.IncomingViewAgent;
import com.jancar.bluetooth.ui.agent.KeypadViewAgent;
import com.jancar.bluetooth.ui.agent.TalkingViewAgent;
import com.jancar.bluetooth.ui.agent.ViewAgent;
import com.jancar.bluetooth.utils.ObjectUtil;
import com.jancar.utils.ObjectFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 各渠道相关的配置，单一实例
 */

public class FlavorsConfig {
    public static final String R_LAYOUT_PHONE_CALL = "R.layout.phone_call";
    public static final String R_LAYOUT_ITEM_CONTACT = "R.layout.item_contact";
    public static final String R_LAYOUT_ITEM_CONTACT_NO_CLICKABLE = "R.layout.item_contact_no_clickable";

    public static final String R_DRAWABLE_BT_MUTE_SELECTOR = "R.drawable.btn_mute_selector";
    public static final String R_DRAWABLE_BT_MUTE_MIC_SELECTOR = "R.drawable.btn_mute_mic_selector";
    public static final String R_DRAWABLE_BT_MUTE_HALF_SELECTOR = "R.drawable.btn_mute_half_selector";
    public static final String R_DRAWABLE_BT_MUTE_MIC_HALF_SELECTOR = "R.drawable.btn_mute_mic_half_selector";

    public static final String CLASS_BANNER_VIEW_AGENT = "BannerViewAgent.class";
    public static final String CLASS_CONTACT_VIEW_AGENT = "ContactViewAgent.class";
    public static final String CLASS_INCOMING_VIEW_AGENT = "IncomingViewAgent.class";
    public static final String CLASS_KEYPAD_VIEW_AGENT = "KeypadViewAgent.class";
    public static final String CLASS_TALKING_VIEW_AGENT = "TalkingViewAgent.class";

    public static final String CLASS_CONTACTS_ADAPTER = "ContactsAdapter.class";
    public static final String CLASS_CALL_HISTORY_ADAPTER = "CallHistoryAdapter.class";
    public static final String CLASS_KEYBOARD_CONTACTS_ADAPTER = "KeyboardContactsAdapter.class";

    public static final String CLASS_CONTACT_FRAGMENT = "ContactFragment.class";
    public static final String CLASS_FAVORITE_FRAGMENT = "FavoriteFragment.class";
    public static final String CLASS_HISTORY_FRAGEMENT = "HistoryFragment.class";
    public static final String CLASS_KEYBOARD_FRAGMENT = "KeyboardFragment.class";

    static FlavorsConfig sDefaultInstance;

    private HashMap<String, Integer> mAssetsIds;
    private HashMap<String, String> mClassMap;

    /**
     * 返回默认实例
     */
    public static FlavorsConfig getDefault() {
        if (sDefaultInstance == null) {
            synchronized (FlavorsConfig.class) {
                if (sDefaultInstance == null) {
                    sDefaultInstance = new FlavorsConfig();
                }
            }
        }
        return sDefaultInstance;
    }

    public static FlavorsConfigBuilder builder() {
        return new FlavorsConfigBuilder();
    }

    FlavorsConfig() {
        initLayoutIds();
        initClassMap();
        // Empty
    }

    FlavorsConfig(FlavorsConfigBuilder builder) {
        initLayoutIds();
        initClassMap();

        if (null != builder.mAssetsIds) {
            for (Map.Entry<String, Integer> entry : builder.mAssetsIds.entrySet()) {
                mAssetsIds.put(entry.getKey(), entry.getValue()); // 更新layout Id
            }
        }

        if (null != builder.mClassMap) {
            for (Map.Entry<String, String> entry : builder.mClassMap.entrySet()) {
                mClassMap.put(entry.getKey(), entry.getValue()); // 更新class map
            }
        }
    }

    /**
     * 返回Layout ID
     * @param name
     * @return 保证返回一个数值 默认值-1
     */
    public int getLayoutId(final String name) {
        return mAssetsIds.get(name);
    }

    /**
     * 返回Drawable ID
     * @param name
     * @return 保证返回一个数值 默认值-1
     */
    public int getDrawableId(final String name) {
        return mAssetsIds.get(name);
    }

    /**
     * 通过类名获取对象
     * @param name
     * @return
     */
    public Object classForName(final String name) {
        return ObjectFactory.obtain(mClassMap.get(name));
    }

    public <T extends ViewAgent> T getViewAgent(final String name, Context context, ViewGroup root) {
        T viewAgent = (T) ObjectFactory.obtain(mClassMap.get(name));
        if (viewAgent != null) {
            viewAgent.init(context, root);
        }
        return viewAgent;
    }

    public <T extends Fragment> T getFragment(final String name) {
        return (T) ObjectFactory.obtain(mClassMap.get(name));
    }

    public <T> T getAdapter(final String name, Context context) {
        return (T) ObjectUtil.newObject(mClassMap.get(name), context);
    }

    private void initLayoutIds() {
        mAssetsIds = new HashMap<String, Integer>() {{ // 预置layout ids
            put(R_LAYOUT_PHONE_CALL, -1);
            put(R_LAYOUT_ITEM_CONTACT, -1);
            put(R_LAYOUT_ITEM_CONTACT_NO_CLICKABLE, -1);
            put(R_DRAWABLE_BT_MUTE_SELECTOR, -1);
            put(R_DRAWABLE_BT_MUTE_MIC_SELECTOR, -1);
            put(R_DRAWABLE_BT_MUTE_HALF_SELECTOR, -1);
            put(R_DRAWABLE_BT_MUTE_MIC_HALF_SELECTOR, -1);
        }};
    }

    private void initClassMap() {
        mClassMap = new HashMap<String, String>() {{ // 预置class map
            put(CLASS_BANNER_VIEW_AGENT, BannerViewAgent.class.getName());
            put(CLASS_CONTACT_VIEW_AGENT, ContactViewAgent.class.getName());
            put(CLASS_INCOMING_VIEW_AGENT, IncomingViewAgent.class.getName());
            put(CLASS_KEYPAD_VIEW_AGENT, KeypadViewAgent.class.getName());
            put(CLASS_TALKING_VIEW_AGENT, TalkingViewAgent.class.getName());
            put(CLASS_CONTACTS_ADAPTER, ContactsAdapter.class.getName());
        }};
    }
}
