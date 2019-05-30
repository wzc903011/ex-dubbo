package com.mingqi.local.userservice.dto;

public class UidQueryExtendRequest extends UidQueryRequest {

    public Integer getQueryType() {
        return queryType;
    }

    public void setQueryType(Integer queryType) {
        this.queryType = queryType;
    }

    private Integer queryType;
}
