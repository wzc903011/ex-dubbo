package com.mingqi.local.userservice.cache;

import java.util.Map;

public class CaffeineSwitchBean {
    private boolean closeAll;

    /**
     * key: 使用caffeine缓存的对象标识 （==》缓存key的前缀）
     * value: 是否使用
     */
    private Map<String, Boolean> switchMap;

    public boolean isCloseAll() {
        return closeAll;
    }

    public void setCloseAll(boolean closeAll) {
        this.closeAll = closeAll;
    }

    public Map<String, Boolean> getSwitchMap() {
        return switchMap;
    }

    public void setSwitchMap(Map<String, Boolean> switchMap) {
        this.switchMap = switchMap;
    }
}
