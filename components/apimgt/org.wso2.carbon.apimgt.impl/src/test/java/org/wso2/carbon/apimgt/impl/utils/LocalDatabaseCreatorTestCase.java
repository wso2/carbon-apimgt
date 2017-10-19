/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;
import javax.sql.DataSource;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DatabaseCreator.class, LocalDatabaseCreator.class})
public class LocalDatabaseCreatorTestCase {

    private LocalDatabaseCreator localDatabaseCreator;
    private File file = Mockito.mock(File.class);

    @Before
    public void setup() throws Exception {
        System.setProperty(CARBON_HOME, "");
        localDatabaseCreator = new LocalDatabaseCreator(Mockito.mock(DataSource.class));
        PowerMockito.mockStatic(DatabaseCreator.class);
        PowerMockito.when(DatabaseCreator.getDatabaseType(Matchers.any(Connection.class))).thenReturn("mysql");
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(file);

    }

    @Test
    public void testCreateRegistryDatabaseWhenFileExists() throws Exception {

        Mockito.when(file.exists()).thenReturn(true);
        Method method = PowerMockito.method(DatabaseCreator.class, "createRegistryDatabase");
        PowerMockito.suppress(method);
        localDatabaseCreator.createRegistryDatabase();
        Mockito.verify(file, Mockito.times(1)).exists();
    }

    @Test
    public void testCreateRegistryDatabaseWhenFileDoesNotExist() throws Exception {

        Mockito.when(file.exists()).thenReturn(false);
        Method method = PowerMockito.method(DatabaseCreator.class, "createRegistryDatabase");
        PowerMockito.suppress(method);
        localDatabaseCreator.createRegistryDatabase();
        Mockito.verify(file, Mockito.times(1)).exists();

    }
}
