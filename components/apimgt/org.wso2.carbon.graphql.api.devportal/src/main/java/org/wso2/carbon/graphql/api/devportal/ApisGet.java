package org.wso2.carbon.graphql.api.devportal;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.*;

//@RestController
public class ApisGet {

    //@GetMapping("/all")
//    public ArrayList<Object> getAllapiData(){
//        //String searchQuery = "store_view_roles=(null OR system\\/wso2.anonymous.role)&name=*&enableStore=(true OR null)&lcState=(PUBLISHED OR PROTOTYPED)";
//        Object id = null;
//        ArrayList<Object> allMatchedApis = null;
//        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(null);
//        int start = 0;
//        int end = 25;
//        //Map allMatchedApisMap =null;// = new HashMap<String, Object>();
//        try{
////            String searchQuery = "name=*&enableStore=(true OR null)&lcState=(PUBLISHED OR PROTOTYPED)";
////            APIConsumer apiConsumer = RestApiUtil.getConsumer("wso2.anonymous.user");
////            Map allMatchedApisMap  = apiConsumer.searchPaginatedAPIsNew(searchQuery,requestedTenantDomain,start,end);
////            Set<Object> sortedSet = (Set<Object>) allMatchedApisMap.get("apis"); // This is a SortedSet
////            allMatchedApis = new ArrayList<>(sortedSet);
////            //id = allMatchedApis.get(0);
//
//
//
//
//
//        }catch (APIManagementException e){
//
//        }
//       // return Response.ok().entity(allMatchedApis).build();
//        return allMatchedApis;
//    }
}
