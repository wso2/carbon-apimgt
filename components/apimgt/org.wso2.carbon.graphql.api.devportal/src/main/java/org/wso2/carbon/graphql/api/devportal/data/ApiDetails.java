package org.wso2.carbon.graphql.api.devportal.data;


import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.OASPersistenceException;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.Api;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.*;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getTiers;
//import static org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil.getAPICategoriesFromAPIGovernanceArtifact;


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
            boolean isDefault = devPortalAPI.getIsDefaultVersion();

            String authorizationHeader = devPortalAPI.getAuthorizationHeader();

            String apiSecurity = devPortalAPI.getApiSecurity();

            boolean isMonetizationEnabled = devPortalAPI.isMonetizationEnabled();

            String throttlingPolicies = throttlingPoliciesData.getThrottlingPoliciesData(id);

            Set<String> categoriesSet = devPortalAPI.getApiCategories();

            String categories = "";//devPortalAPI.getGatewayLabels().toString();

            String allkeyManagers = devPortalAPI.getKeyManagers().toString();

            Api api1 = new Api(id,name,description,context,version,provider,type,transport,hasthumbnail,environments,wsdUrl,status,isSubscriptionAvailable,monetizationLabel,isDefault,authorizationHeader,apiSecurity,isMonetizationEnabled,throttlingPolicies,thumbnailUrl,categories,allkeyManagers);
            apiDTOList.add(api1);





        }
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

    public String getCatogories(Set<String> catogoriesSet){
        String catogories = null;

        if (catogoriesSet!=null){
            catogories = String.join(",",catogoriesSet);
        }
        return catogories;
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
        String transport = devPortalAPI.getTransports();

        String thumbnailUrl = devPortalAPI.getThumbnail();
        boolean hasthumbnail = HasThumbnail(thumbnailUrl);

        Set<String> environmentSet = devPortalAPI.getEnvironments();
        String environments = environmentSet.toString();
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
