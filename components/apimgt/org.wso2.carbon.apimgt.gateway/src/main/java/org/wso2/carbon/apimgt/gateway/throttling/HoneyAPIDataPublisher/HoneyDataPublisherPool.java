package org.wso2.carbon.apimgt.gateway.throttling.HoneyAPIDataPublisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;

public class HoneyDataPublisherPool {
    private static final Log log = LogFactory.getLog(HoneyDataPublisherPool.class);

    private ObjectPool clientPool;

    private HoneyDataPublisherPool() {
        //Using stack object pool to handle high concurrency scenarios without dropping any messages.
        //Tuning this pool is mandatory according to use cases.
        //A finite number of "sleeping" or idle instances is enforced, but when the pool is empty, new instances
        // are created to support the new load. Hence this following data stricture places no limit on the number of "
        // active" instance created by the pool, but is quite useful for re-using Objects without introducing
        // artificial limits.
        //Proper tuning is mandatory for good performance according to system load.
        ThrottleProperties.DataPublisherPool dataPublisherPoolConfiguration = ServiceReferenceHolder
                .getInstance().getThrottleProperties().getDataPublisherPool();

        clientPool = new StackObjectPool(new BasePoolableObjectFactory() {
            @Override
            public Object makeObject() {
                if(log.isDebugEnabled()) {
                    log.debug("Initializing new HoneyPotAPIDataPublisher instance");
                }
                return new HoneyAPIDataProcessAndPublishAgent();
            }
        }, dataPublisherPoolConfiguration.getMaxIdle(), dataPublisherPoolConfiguration.getInitIdleCapacity());
    }

    private static class HoneyAPIDataPublisherPoolHolder {
        private static final HoneyDataPublisherPool INSTANCE = new HoneyDataPublisherPool();

        private HoneyAPIDataPublisherPoolHolder(){}
    }

    public static HoneyDataPublisherPool getInstance() {
        return HoneyDataPublisherPool.HoneyAPIDataPublisherPoolHolder.INSTANCE;
    }

    public HoneyAPIDataProcessAndPublishAgent get() throws Exception {
        return (HoneyAPIDataProcessAndPublishAgent) clientPool.borrowObject();
    }

    void release(HoneyAPIDataProcessAndPublishAgent client) throws Exception {
        //We must clean data references as it can caused to pass old data to global policy server.
        client.clearDataReference();
        clientPool.returnObject(client);
    }

    public void cleanup() {
        try {
            clientPool.close();
        } catch (Exception e) {
            log.warn("Error while cleaning up the object pool", e);
        }
    }
}
