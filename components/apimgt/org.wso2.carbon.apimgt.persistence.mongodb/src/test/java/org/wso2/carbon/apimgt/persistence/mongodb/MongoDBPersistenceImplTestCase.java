package org.wso2.carbon.apimgt.persistence.mongodb;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class MongoDBPersistenceImplTestCase {

    @Test
    public void testConstructor(){
        new MongoDBPersistenceImpl();
        Assert.assertTrue(true);
    }
}
