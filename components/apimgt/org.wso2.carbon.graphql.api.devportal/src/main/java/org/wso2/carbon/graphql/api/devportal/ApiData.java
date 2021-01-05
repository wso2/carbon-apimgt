package org.wso2.carbon.graphql.api.devportal;

import org.wso2.carbon.graphql.api.devportal.data.ApiDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
//import org.wso2.carbon.apimgt.impl.utils.APIUtil;

//mport javax.xml.ws.Response;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;

@RestController
public class ApiData {

    @GetMapping("/apis")
//    @Consumes({ "application/json" })
//    @Produces({ "application/json" })
//    @ApiOperation(value = "Retrieve/Search APIs ", notes = "This operation provides you a list of available APIs qualifying under a given search condition.  Each retrieved API is represented with a minimal amount of attributes. If you want to get complete details of an API, you need to use **Get details of an API** operation.  This operation supports retrieving APIs of other tenants. The required tenant domain need to be specified as a header `X-WSO2-Tenant`. If not specified super tenant's APIs will be retrieved. If you used an Authorization header, the user's tenant associated with the access token will be used.  **NOTE:** * By default, this operation retrieves Published APIs. In order to retrieve Prototyped APIs, you need to use **query** parameter and specify **status:PROTOTYPED**. * This operation does not require an Authorization header by default. But if it is provided, it will be validated and checked for permissions of the user, hence you may be able to see APIs which are restricted for special permissions/roles. ", response = Api.class, authorizations = {
//            @Authorization(value = "OAuth2Security", scopes = {
//
//            })
//    }, tags={ "APIs",  })
//    @ApiResponses(value  ={
//            @ApiResponse(code = 200, message = "OK. List of qualifying APIs is returned. ", response = Api.class)
//    })
    //@Produces({MediaType.TEXT_PLAIN})
    public Response ApiGet()  {
        int size = 0;
        ArrayList<Object> allMatchedApis = null;
        Object id=null;
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(null);
        ApiDetails dummyApi = new ApiDetails();

        //String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain("X-WSO2-Tenant");
        //APIListDTO apiListDTO = new APIListDTO();
        try {
            APIConsumer apiConsumer = RestApiUtil.getConsumer("wso2.anonymous.user");
            String newSearchQuery = "name=*&enableStore=(true OR null)&lcState=(PUBLISHED OR PROTOTYPED)";
            //String requestedTenantDomain = "carbon.super";

            Map allMatchedApisMap = apiConsumer
                    .searchPaginatedAPIsNew(newSearchQuery, requestedTenantDomain, 0, 25);
            Set<Object> sortedSet = (Set<Object>) allMatchedApisMap.get("apis"); // This is a SortedSet
            allMatchedApis = new ArrayList<>(sortedSet);
            size = allMatchedApis.size()+1;
            id = allMatchedApis.get(0);



            //return Response.ok().entity(allMatchedApis).build();
        }catch (APIManagementException e){



        }

        return Response.ok().entity(allMatchedApis).build();

    }
}
