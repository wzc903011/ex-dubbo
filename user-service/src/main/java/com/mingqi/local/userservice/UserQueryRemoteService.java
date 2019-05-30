package com.mingqi.local.userservice;

import com.mingqi.local.userservice.dto.UidListQueryRequest;
import com.mingqi.local.userservice.dto.UidQueryExtendRequest;
import com.mingqi.local.userservice.dto.UidQueryRequest;

public interface UserQueryRemoteService {

    /**
     * 基础类型作为入参，第一个参数   success；  失败过，要确认一致性hash算法原理
     */
    String queryWithPrimitiveType(long uid, int mallId);

    /**
     * 基础类型作为入参，第二个参数 success
     */
    String queryWithPrimitiveType2(long mallId, int uid);

    /**
     * UidQueryRequest  success ；    失败过，要确认一致性hash算法原理
     */
    String queryByUidRequest(UidQueryRequest request);

    /**
     * UidQueryRequest的子类 success
     */
    String queryBySonUidRequest(UidQueryExtendRequest request);

    /**
     * 其他请求对象, 也使用自定义路由  success
     */
    String queryByOtherRequest(UidListQueryRequest request);

}
