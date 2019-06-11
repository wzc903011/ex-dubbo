package com.mingqi.local.userservice.dto;


import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
public class UidQueryRequest implements Serializable {
    private long uid;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }
    @Override
    public String toString() {
        return "UidQueryRequest: uid=" + this.uid;
    }
}
