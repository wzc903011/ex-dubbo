package com.mingqi.local.userservice;

import com.mingqi.local.userservice.dto.UidQueryRequest;

public interface UserQueryRemoteService {

    /**
     * UidQueryRequest  success ；
     */
    String queryByUidRequest(UidQueryRequest request);

}
