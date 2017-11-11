package org.wso2.carbon.apimgt.gateway.threatprotection.pool;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class AnalyzerPool <T> extends GenericObjectPool<T> {
    public AnalyzerPool(PooledObjectFactory<T> factory) {
        super(factory);
    }
    public AnalyzerPool(PooledObjectFactory<T> factory, GenericObjectPoolConfig config) {
        super(factory, config);
    }

}
