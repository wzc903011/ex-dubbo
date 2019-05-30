package com.mingqi.local.userservice.dto;

import java.io.Serializable;
import java.util.List;

public class UidListQueryRequest implements Serializable {
    private List<Long> uidList;

    public List<Long> getUidList() {
        return uidList;
    }

    public void setUidList(List<Long> uidList) {
        this.uidList = uidList;
    }

    @Override
    public String toString() {
        return "UidListQueryRequest: uidList=" + this.uidList;
    }
}
