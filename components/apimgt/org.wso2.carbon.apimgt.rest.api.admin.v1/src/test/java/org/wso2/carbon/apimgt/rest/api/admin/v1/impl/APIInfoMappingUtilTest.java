/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApiResultDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.APIInfoMappingUtil;

public class APIInfoMappingUtilTest {
    private API api1;
    private API api2;
    private API api3;
    private API api1tenant;
    private API api2tenant;
    private API api3tenant;
    private APIIdentifier apiId;

    @Before
    public void setUp() {
        apiId = new APIIdentifier("apicreator", "GoogleAPI", "1.0.0");
        api1 = new API(apiId);
        api1.setUuid("b95ab4d5-c83e-4895-afb8-e1853602e88f");

        apiId = new APIIdentifier("admin", "TwitterAPI", "1.0.0");
        api2 = new API(apiId);
        api2.setUuid("16d5b419-cf5b-478a-ba05-1973264ffedf");

        apiId = new APIIdentifier("admin", "FacebookAPI", "1.0.0");
        api3 = new API(apiId);
        api3.setUuid("0db65e3c-ef12-43e2-8626-4fa4a663ae60");

        apiId = new APIIdentifier("apicreator-AT-pizzashack.com", "GoogleAPI", "1.0.0");
        api1tenant = new API(apiId);
        api1tenant.setUuid("b95ab4d5-c83e-4895-afb8-e1853602e88f");

        apiId = new APIIdentifier("admin-AT-pizzashack.com", "TwitterAPI", "1.0.0");
        api2tenant = new API(apiId);
        api2tenant.setUuid("16d5b419-cf5b-478a-ba05-1973264ffedf");

        apiId = new APIIdentifier("admin-AT-pizzashack.com", "FacebookAPI", "1.0.0");
        api3tenant = new API(apiId);
        api3tenant.setUuid("0db65e3c-ef12-43e2-8626-4fa4a663ae60");
    }

    @Test
    public void testFromAPIToAPIResultDTO() {
        ApiResultDTO apiResultDTO1 = APIInfoMappingUtil.fromAPIToAPIResultDTO(api1);
        ApiResultDTO apiResultDTO2 = APIInfoMappingUtil.fromAPIToAPIResultDTO(api2);
        ApiResultDTO apiResultDTO3 = APIInfoMappingUtil.fromAPIToAPIResultDTO(api3);

        assert(apiResultDTO1.getId().equals("b95ab4d5-c83e-4895-afb8-e1853602e88f"));
        assert(apiResultDTO1.getName().equals("GoogleAPI"));
        assert(apiResultDTO1.getVersion().equals("1.0.0"));
        assert(apiResultDTO1.getProvider().equals("apicreator"));

        assert(apiResultDTO2.getId().equals("16d5b419-cf5b-478a-ba05-1973264ffedf"));
        assert(apiResultDTO2.getName().equals("TwitterAPI"));
        assert(apiResultDTO2.getVersion().equals("1.0.0"));
        assert(apiResultDTO2.getProvider().equals("admin"));

        assert(apiResultDTO3.getId().equals("0db65e3c-ef12-43e2-8626-4fa4a663ae60"));
        assert(apiResultDTO3.getName().equals("FacebookAPI"));
        assert(apiResultDTO3.getVersion().equals("1.0.0"));
        assert(apiResultDTO3.getProvider().equals("admin"));

        ApiResultDTO apiResultDTO1tenant = APIInfoMappingUtil.fromAPIToAPIResultDTO(api1tenant);
        ApiResultDTO apiResultDTO2tenant = APIInfoMappingUtil.fromAPIToAPIResultDTO(api2tenant);
        ApiResultDTO apiResultDTO3tenant = APIInfoMappingUtil.fromAPIToAPIResultDTO(api3tenant);

        assert(apiResultDTO1tenant.getId().equals("b95ab4d5-c83e-4895-afb8-e1853602e88f"));
        assert(apiResultDTO1tenant.getName().equals("GoogleAPI"));
        assert(apiResultDTO1tenant.getVersion().equals("1.0.0"));
        assert(apiResultDTO1tenant.getProvider().equals("apicreator@pizzashack.com"));

        assert(apiResultDTO2tenant.getId().equals("16d5b419-cf5b-478a-ba05-1973264ffedf"));
        assert(apiResultDTO2tenant.getName().equals("TwitterAPI"));
        assert(apiResultDTO2tenant.getVersion().equals("1.0.0"));
        assert(apiResultDTO2tenant.getProvider().equals("admin@pizzashack.com"));

        assert(apiResultDTO3tenant.getId().equals("0db65e3c-ef12-43e2-8626-4fa4a663ae60"));
        assert(apiResultDTO3tenant.getName().equals("FacebookAPI"));
        assert(apiResultDTO3tenant.getVersion().equals("1.0.0"));
        assert(apiResultDTO3tenant.getProvider().equals("admin@pizzashack.com"));
    }
}
