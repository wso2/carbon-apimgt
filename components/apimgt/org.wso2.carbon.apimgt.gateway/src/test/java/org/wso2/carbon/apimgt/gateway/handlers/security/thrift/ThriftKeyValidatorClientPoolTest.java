/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.thrift;

import org.apache.commons.pool.impl.StackObjectPool;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

/**
 * ThriftAuthClient test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ThriftKeyValidatorClientPool.class})
public class ThriftKeyValidatorClientPoolTest {

    @Test
    public void testBorrowingThriftValidatorClientFromPool() throws Exception {
        StackObjectPool clientPool = Mockito.mock(StackObjectPool.class);
        ThriftKeyValidatorClient thriftKeyValidatorClient = Mockito.mock(ThriftKeyValidatorClient.class);
        PowerMockito.whenNew(StackObjectPool.class).withAnyArguments().thenReturn(clientPool);
        PowerMockito.when(clientPool.borrowObject()).thenReturn(thriftKeyValidatorClient);
        ThriftKeyValidatorClientPool thriftKeyValidatorClientPool = ThriftKeyValidatorClientPool.getInstance();
        try {
            thriftKeyValidatorClientPool.get();
        } catch (Exception e) {
            Assert.fail("Unexpected exception occurred while borrowing ThriftKeyValidatorClient from pool");
        }
    }

    @Test
    public void testReleasingThriftValidatorClientFromPool() throws Exception {
        StackObjectPool clientPool = Mockito.mock(StackObjectPool.class);
        ThriftKeyValidatorClient thriftKeyValidatorClient = Mockito.mock(ThriftKeyValidatorClient.class);
        PowerMockito.whenNew(StackObjectPool.class).withAnyArguments().thenReturn(clientPool);
        PowerMockito.doNothing().when(clientPool).returnObject(Mockito.anyObject());
        ThriftKeyValidatorClientPool thriftKeyValidatorClientPool = ThriftKeyValidatorClientPool.getInstance();

        try{
            thriftKeyValidatorClientPool.release(thriftKeyValidatorClient);
        } catch (Exception e){
            Assert.fail("Unexpected exception occurred while releasing ThriftKeyValidatorClient from pool");
        }
    }

    @Test
    public void testThriftClientPoolCleanUp() throws Exception {
        StackObjectPool clientPool = Mockito.mock(StackObjectPool.class);
        PowerMockito.whenNew(StackObjectPool.class).withAnyArguments().thenReturn(clientPool);
        PowerMockito.doNothing().when(clientPool).close();
        ThriftKeyValidatorClientPool thriftKeyValidatorClientPool = ThriftKeyValidatorClientPool.getInstance();

        try{
            thriftKeyValidatorClientPool.cleanup();
        } catch (Exception e){
            Assert.fail("Unexpected exception occurred while cleaning up ThriftKeyValidatorClient pool");
        }
    }

    @Test
    public void testThriftClientPoolCleanUpFailure() throws Exception {

        StackObjectPool clientPool = Mockito.mock(StackObjectPool.class);
        PowerMockito.whenNew(StackObjectPool.class).withAnyArguments().thenReturn(clientPool);
        PowerMockito.doThrow(new Exception()).when(clientPool).close();
        ThriftKeyValidatorClientPool thriftKeyValidatorClientPool = ThriftKeyValidatorClientPool.getInstance();

        try{
            thriftKeyValidatorClientPool.cleanup();
        } catch (Exception e){
            Assert.fail("Exception should not throw when ThriftKeyValidatorClient pool clean up failed");
        }
    }
}

