package org.wso2.carbon.graphql.api.devportal.data;


import com.google.gson.Gson;
import org.apache.commons.lang3.ArrayUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.PersistenceManager;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.OASPersistenceException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.graphql.api.devportal.ApisGet;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.Api;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.*;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getTiers;
import static org.wso2.carbon.apimgt.persistence.utils.PersistenceUtil.replaceEmailDomainBack;
//import static org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil.getAPICategoriesFromAPIGovernanceArtifact;
import static org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil.getLcStateFromArtifact;


public class ApiDetails {

    APIPersistence apiPersistenceInstance;



    public List<Api> getAllApis() throws UserStoreException, RegistryException, OASPersistenceException, APIManagementException, APIPersistenceException {
        SubscribeAvailableData subscribeAvailableData = new SubscribeAvailableData();
        MonetizationLabelData monetizationLabelData = new MonetizationLabelData();
        ThrottlingPoliciesData throttlingPoliciesData = new ThrottlingPoliciesData();


        ArtifactData artifactData = new ArtifactData();

       // GenericArtifact[] artifacts = artifactData.getAllApis();;

        List<Api> apiDTOList = new ArrayList<Api>();


        List<DevPortalAPI> list = artifactData.getDevportalAPIS();
        for (DevPortalAPI devPortalAPI: list){
            String id = devPortalAPI.getId();
            String name = devPortalAPI.getApiName();
            String description = devPortalAPI.getDescription();
            String context = devPortalAPI.getContext();
            String version = devPortalAPI.getVersion();
            String type = devPortalAPI.getType();
            String provider = devPortalAPI.getProviderName();
            String transport = devPortalAPI.getTransports();

            String thumbnailUrl = devPortalAPI.getThumbnail();
            boolean hasthumbnail = HasThumbnail(thumbnailUrl);

            Set<String> environmentSet = devPortalAPI.getEnvironments();
            String environments = "";
            String wsdUrl = devPortalAPI.getWsdlUrl();
            String status = devPortalAPI.getStatus();

            boolean isSubscriptionAvailable = subscribeAvailableData.getSubscriptionAvailable(id);////

            Set<String> tiers = devPortalAPI.getAvailableTierNames();

            String monetizationLabel = "";//monetizationLabelData.getMonetizationLabelData(definedTiers,tiers,name);
            boolean isDefault = false ; //devPortalAPI.getIsDefaultVersion();

            String authorizationHeader = devPortalAPI.getAuthorizationHeader();

            String apiSecurity = devPortalAPI.getApiSecurity();

            boolean isMonetizationEnabled = devPortalAPI.isMonetizationEnabled();

            String throttlingPolicies = throttlingPoliciesData.getThrottlingPoliciesData(id);

            Set<String> categoriesSet = devPortalAPI.getApiCategories();

            String categories = "";//devPortalAPI.getGatewayLabels().toString();

            String allkeyManagers = "";

            Api api1 = new Api(id,name,description,context,version,provider,type,transport,hasthumbnail,environments,wsdUrl,status,isSubscriptionAvailable,monetizationLabel,isDefault,authorizationHeader,apiSecurity,isMonetizationEnabled,throttlingPolicies,thumbnailUrl,categories,allkeyManagers);
            apiDTOList.add(api1);





        }

//        for (GenericArtifact artifact : artifacts) {
//
//            String id = artifact.getId();
//            String name = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
//            String description = artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION); //
//
//            String provider = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
//
//            String transport = artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS); //
//            String thumbnailUrl  = artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL);
//            boolean hasthumbnail = HasThumbnail(thumbnailUrl);
//            String environments = getEnvironmentList(id);////
//
//            String wsdUrl   = artifact.getAttribute(APIConstants.API_OVERVIEW_WSDL);;
//            String status  = getLcStateFromArtifact(artifact);
//
//            boolean isSubscriptionAvailable = subscribeAvailableData.getSubscriptionAvailable(id);////
//
//            String  tiers = artifact.getAttribute(APIConstants.API_OVERVIEW_TIER);
//            String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(provider));
//            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
//                    .getTenantId(tenantDomainName);
//            Map<String, Tier> definedTiers = getTiers(tenantId);
//            String monetizationLabel = monetizationLabelData.getMonetizationLabelData(definedTiers,tiers,name);
//
//            boolean isDefault = Boolean.parseBoolean(artifactData.getDevportalApis(id).getAttribute(
//                    APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION));
//
//            String authorizationHeader = artifact.getAttribute(APIConstants.API_OVERVIEW_AUTHORIZATION_HEADER);;
//            String apiSecurity = artifact.getAttribute(APIConstants.API_OVERVIEW_API_SECURITY);
//
//            //String tags = ""; //
////            Registry registry = artifactData.getRegistry();
////            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifactData.getDevportalApis(id).getId());
////
////            Set<String> tagSet = new HashSet<String>();
////            org.wso2.carbon.registry.core.Tag[] tag = registry.getTags(artifactPath);
////            for (Tag tag1 : tag) {
////                tagSet.add(tag1.getTagName());
////            }
////            String tags = getTags(tagSet);
//
//            boolean isMonetizationEnabled = Boolean.parseBoolean(artifactData.getDevportalApis(id).getAttribute
//                    (APIConstants.Monetization.API_MONETIZATION_STATUS));
//
//            String throttlingPolicies = throttlingPoliciesData.getThrottlingPoliciesData(definedTiers,tiers,name);
//
//            String categories = getCatogories(getAPICategoriesFromAPIGovernanceArtifact(artifact));
//            String keyManagers = artifact.getAttribute(APIConstants.API_OVERVIEW_KEY_MANAGERS);
//            List<String> keyManagersList = null;
//            if (StringUtils.isNotEmpty(keyManagers)) {
//                keyManagersList =  new Gson().fromJson(keyManagers, List.class);
//            } else {
//                keyManagersList = Arrays.asList(APIConstants.API_LEVEL_ALL_KEY_MANAGERS);
//            }
//            String allkeyManagers = getKeymanagers(keyManagersList);
//
//            Api api1 = new Api(id,description,transport,hasthumbnail,environments,wsdUrl,status,isSubscriptionAvailable,monetizationLabel,isDefault,authorizationHeader,apiSecurity,isMonetizationEnabled,throttlingPolicies,thumbnailUrl,categories,allkeyManagers);
//            apiDTOList.add(api1);
//        }
        return apiDTOList;
    }
//    public String getTags(Set<String> tagset){
//
//        String tags = null;
//        if (tagset!=null){
//            List<String> tagList = new ArrayList<>(tagset);
//            tags = String.join(",",tagList);
//        }
//        return tags;
//    }
    public String getKeymanagers(List<String> keyManagersList){

        //List<String> keyManagersList = apiTypeWrapper.getApi().getKeyManagers();
        String keyManagers = null;
        if(keyManagersList!=null){
            keyManagers = String.join(",",keyManagersList);
        }
        else{
            keyManagers = null;
        }

        return keyManagers;

    }

    public boolean HasThumbnail(String thumbnailUrl){

        boolean hasthumbnail;
        if (!StringUtils.isBlank(thumbnailUrl) ){
            hasthumbnail = true;
        }
        else{
            hasthumbnail = false;
        }
        return hasthumbnail;
    }

    public String getEnvironmentList(String Id) throws RegistryException, APIPersistenceException, UserStoreException {

        ArtifactData artifactData = new ArtifactData();
        Set<String> environmentset = artifactData.getApiFromUUID(Id).getEnvironments();//getEnvironments(artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS));
        String environments = null;
        if(environmentset!=null){
            List<String> environmentList = new ArrayList<>(environmentset);
            environments = String.join(",",environmentList);
        }else{
            environments = null;
        }
        return environments;


    }
    public static Set<String> getEnvironments(String environments) {
        if(environments != null) {
            String[] publishEnvironmentArray = environments.split(",");
            return new HashSet<String>(Arrays.asList(publishEnvironmentArray));
        }
        return null;
    }

//    public String getCatogories(List<APICategory> catogories){
//
//
//        //List<APICategory> catogories = apiTypeWrapper.getApi().getApiCategories();
//        List<String> apiCatogoryNameList = new ArrayList<>();
//
//        String apiCatogoryName = null;
//        if(catogories!=null){
//            for (int i = 0;i< catogories.size();i++){
//                String name  = catogories.get(i).getName();
//                apiCatogoryNameList.add(name);
//
//            }
//            apiCatogoryName = String.join(",",apiCatogoryNameList);
//            return apiCatogoryName;
//        }
//        else{
//            return apiCatogoryName;
//        }
//
//    }
//    public List<APICategory> getAPICategoriesFromAPIGovernanceArtifact(GovernanceArtifact artifact) throws GovernanceException{
//
//        String[] categoriesOfAPI = artifact.getAttributes(APIConstants.API_CATEGORIES_CATEGORY_NAME);
//
//        List<APICategory> categoryList = new ArrayList<>();
//
//        if (ArrayUtils.isNotEmpty(categoriesOfAPI)) {
//            for (String categoryName : categoriesOfAPI) {
//                APICategory category = new APICategory();
//                category.setName(categoryName);
//                categoryList.add(category);
//            }
//        }
//        return categoryList;
//    }

    public String getCatogories(Set<String> catogoriesSet){
        String catogories = null;

        if (catogoriesSet!=null){
            catogories = String.join(",",catogoriesSet);
        }
        return catogories;
    }


    public int getApiCount() throws UserStoreException, RegistryException, APIManagementException, APIPersistenceException {

       ArtifactData artifactData = new ArtifactData();
       //return artifactData.getAllApis().length;
        return  2;
    }

    public Float getApiRating(String Id) throws APIManagementException {



        APIIdentifier apiIdentifier1 = ApiMgtDAO.getInstance().getAPIIdentifierFromUUID(Id);
        int apiId = ApiMgtDAO.getInstance().getAPIID(apiIdentifier1, null);
        Float rating  = ApiMgtDAO.getInstance().getAverageRating(apiId);

        return rating;
    }



    public Api getApi(String Id) throws APIManagementException, RegistryException, OASPersistenceException, UserStoreException, APIPersistenceException {
        SubscribeAvailableData subscribeAvailableData = new SubscribeAvailableData();
        MonetizationLabelData monetizationLabelData = new MonetizationLabelData();
        ThrottlingPoliciesData throttlingPoliciesData = new ThrottlingPoliciesData();

        ArtifactData artifactData = new ArtifactData();

        DevPortalAPI devPortalAPI = artifactData.getApiFromUUID(Id);

        //GenericArtifact apiArtifact = artifactData.getDevportalApis(Id);

        String id = devPortalAPI.getId();


        String name = devPortalAPI.getApiName();//apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);

        String description = devPortalAPI.getDescription();//apiArtifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION);

        String context = devPortalAPI.getContext();
        String version = devPortalAPI.getVersion();
        String type = devPortalAPI.getType();
        String provider = devPortalAPI.getProviderName();

//        String provider = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
//
//        String transport = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS);
//
//
//        String thumbnailUrl = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL);
//        boolean hasthumbnail = HasThumbnail(thumbnailUrl);
//
//
//        String environments = getEnvironmentList(Id);
//
//        String wsdUrl = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_WSDL);
//        String status  = getLcStateFromArtifact(artifactData.getDevportalApis(Id));
//
//
//        boolean isSubscriptionAvailable = subscribeAvailableData.getSubscriptionAvailable(Id);
//
//
//        //for monetizationLabel
//        String  tiers = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_TIER);
//        String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(provider));
//        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
//                .getTenantId(tenantDomainName);
//        Map<String, Tier> definedTiers = getTiers(tenantId);
//        String monetizationLabel = monetizationLabelData.getMonetizationLabelData(definedTiers,tiers,name);
//
//
//        boolean isDefault = Boolean.parseBoolean(artifactData.getDevportalApis(Id).getAttribute(
//                APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION));
//
//
//        String authorizationHeader = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_AUTHORIZATION_HEADER);
//        String apiSecurity = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_API_SECURITY);
//
//        //
//
//        boolean isMonetizationEnabled = Boolean.parseBoolean(artifactData.getDevportalApis(Id).getAttribute
//                (APIConstants.Monetization.API_MONETIZATION_STATUS));
//        String throttlingPolicies = throttlingPoliciesData.getThrottlingPoliciesData(definedTiers,tiers,name);
//
//        String categories = getCatogories(getAPICategoriesFromAPIGovernanceArtifact(artifactData.getDevportalApis(Id)));
//
//
//        String keyManagers = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_KEY_MANAGERS);
//        List<String> keyManagersList = null;
//        if (StringUtils.isNotEmpty(keyManagers)) {
//            keyManagersList =  new Gson().fromJson(keyManagers, List.class);
//        } else {
//            keyManagersList = Arrays.asList(APIConstants.API_LEVEL_ALL_KEY_MANAGERS);
//        }
//        String allkeyManagers = getKeymanagers(keyManagersList);

//        Registry registry = artifactData.getRegistry();
//
//        String artifactPath = GovernanceUtils.getArtifactPath(registry, artifactData.getDevportalApis(Id).getId());
//
//        Set<String> tagSet = new HashSet<String>();
//        org.wso2.carbon.registry.core.Tag[] tag = registry.getTags(artifactPath);
//        for (Tag tag1 : tag) {
//            tagSet.add(tag1.getTagName());
//        }
//        String tags = getTags(tagSet);
        //String provider = devPortalAPI.getProviderName();
        String transport = devPortalAPI.getTransports();

        String thumbnailUrl = devPortalAPI.getThumbnail();
        boolean hasthumbnail = HasThumbnail(thumbnailUrl);

        Set<String> environmentSet = devPortalAPI.getEnvironments();
        String environments = "";
        String wsdUrl = devPortalAPI.getWsdlUrl();
        String status = devPortalAPI.getStatus();

        boolean isSubscriptionAvailable = subscribeAvailableData.getSubscriptionAvailable(id);////

        Set<String> tiers = devPortalAPI.getAvailableTierNames();

        String monetizationLabel = "";//monetizationLabelData.getMonetizationLabelData(definedTiers,tiers,name);
        boolean isDefault =devPortalAPI.getIsDefaultVersion();

        String authorizationHeader = devPortalAPI.getAuthorizationHeader();

        String apiSecurity = devPortalAPI.getApiSecurity();

        boolean isMonetizationEnabled = devPortalAPI.isMonetizationEnabled();

        String throttlingPolicies = throttlingPoliciesData.getThrottlingPoliciesData(id);;

        Set<String> categoriesSet = devPortalAPI.getApiCategories();

        String categories = getCatogories(categoriesSet);

        List<String> allkeymangersList = devPortalAPI.getKeyManagers();
        String allkeyManagers = getKeymanagers(allkeymangersList);

        return new Api(id,name,description,context,version,provider,type,transport,hasthumbnail,environments,wsdUrl,status,isSubscriptionAvailable,monetizationLabel,isDefault,authorizationHeader,apiSecurity,isMonetizationEnabled,throttlingPolicies,thumbnailUrl,categories,allkeyManagers);
    }




}
