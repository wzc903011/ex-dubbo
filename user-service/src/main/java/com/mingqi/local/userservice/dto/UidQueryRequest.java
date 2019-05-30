package com.mingqi.local.userservice.dto;


import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class UidQueryRequest implements Serializable {
    private long uid;
    private int mallid;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getMallid() {
        return mallid;
    }

    public void setMallid(int mallid) {
        this.mallid = mallid;
    }

    @Override
    public String toString() {
        return "UidQueryRequest: uid=" + this.uid +
                ", mallId=" + this.mallid;
    }
}
