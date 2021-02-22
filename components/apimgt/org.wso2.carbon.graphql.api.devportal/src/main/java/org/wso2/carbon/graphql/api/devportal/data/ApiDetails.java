package org.wso2.carbon.graphql.api.devportal.data;


import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.OASPersistenceException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.APIEndpointURLsDTO;
import org.wso2.carbon.graphql.api.devportal.modules.AdvertiseDTO;
import org.wso2.carbon.graphql.api.devportal.modules.Api;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.graphql.api.devportal.modules.BusinessInformationDTO;
import org.wso2.carbon.graphql.api.devportal.modules.IngressUrlDTO;
import org.wso2.carbon.graphql.api.devportal.modules.LabelNameDTO;
import org.wso2.carbon.graphql.api.devportal.modules.TierNameDTO;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.*;



public class ApiDetails {





    public List<Api> getAllApis(int start, int offset) throws APIPersistenceException, APIManagementException {
        SubscribeAvailableData subscribeAvailableData = new SubscribeAvailableData();
        ThrottlingPoliciesData throttlingPoliciesData = new ThrottlingPoliciesData();


        ArtifactData artifactData = new ArtifactData();


        List<Api> apiDTOList = new ArrayList<Api>();


        List<DevPortalAPI> list = artifactData.getDevportalAPIS(start,offset);
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

           // Set<String> environmentSet = devPortalAPI.getEnvironments();

            String environments = "";
            String wsdUrl = devPortalAPI.getWsdlUrl();
            String status = devPortalAPI.getStatus();

            boolean isSubscriptionAvailable = subscribeAvailableData.getSubscriptionAvailable(devPortalAPI);////

            Set<String> tiers = devPortalAPI.getAvailableTierNames();

            boolean isDefault = devPortalAPI.getIsDefaultVersion();

            String authorizationHeader = devPortalAPI.getAuthorizationHeader();

            String apiSecurity = devPortalAPI.getApiSecurity();

            boolean isMonetizationEnabled = devPortalAPI.isMonetizationEnabled();

            String throttlingPolicies = throttlingPoliciesData.getThrottlingPoliciesData(tiers);

            Set<String> categoriesSet = devPortalAPI.getApiCategories();

            String categories = "";//devPortalAPI.getGatewayLabels().toString();

            String allkeyManagers = devPortalAPI.getKeyManagers().toString();

//            BusinessInformationDTO businessInformationDTO = new BusinessInformationDTO("","","","");
//
//            BusinessInformationData businessInformationData = new BusinessInformationData();

            String businessOwner = devPortalAPI.getBusinessOwner();
            String businessOwnerEmail = devPortalAPI.getBusinessOwnerEmail();
            String technicalOwner = devPortalAPI.getTechnicalOwner();
            String technicalOwnerEmail = devPortalAPI.getTechnicalOwnerEmail();


            BusinessInformationDTO businessInformation = new BusinessInformationDTO(businessOwner,businessOwnerEmail,technicalOwner,technicalOwnerEmail);

            boolean advertised = devPortalAPI.isAdvertiseOnly();
            String originalStoreUrl = devPortalAPI.getRedirectURL();
            String apiOwner = devPortalAPI.getApiOwner();
            AdvertiseDTO advertiseInfo = new AdvertiseDTO(advertised,originalStoreUrl,apiOwner);


            //APIEndpointURLsDTO apiEndPointInformation = new APIEndpointURLsDTO();

            Set<String> environmentSet = devPortalAPI.getEnvironments();
           // String transport = devPortalAPI.getTransports();


            APIEndpointURLsData apiEndpointURLsData = new APIEndpointURLsData();
            List<APIEndpointURLsDTO> apiEndPointInformation = apiEndpointURLsData.apiEndpointURLsDTO(devPortalAPI);


            TierData tierData = new TierData();
            List<TierNameDTO>  tierInformation = tierData.getTierName(devPortalAPI);

            LabelData labelData = new LabelData();
            List<LabelNameDTO> labelNameDTO = labelData.getLabelNames(id);


            IngressUrlsData ingressUrlsData = new IngressUrlsData();
            List<IngressUrlDTO> ingressUrlDTOS = ingressUrlsData.getIngressUrlData(devPortalAPI);

            Api api = new Api(id,name,description,context,version,provider,type,transport,hasthumbnail,environments,wsdUrl,status,isSubscriptionAvailable,isDefault,authorizationHeader,apiSecurity,isMonetizationEnabled,throttlingPolicies,thumbnailUrl,categories,allkeyManagers,businessInformation,advertiseInfo,apiEndPointInformation,tierInformation,labelNameDTO,ingressUrlDTOS);
            apiDTOList.add(api);


        }

     return apiDTOList;


    }
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



//        APIIdentifier apiIdentifier1 = ApiMgtDAO.getInstance().getAPIIdentifierFromUUID(Id);
//        int apiId = ApiMgtDAO.getInstance().getAPIID(apiIdentifier1, null);
//        Float rating  = ApiMgtDAO.getInstance().getAverageRating(apiId);
//
//        return rating;


        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);


        Float rating  = apiConsumer.getRatingFromDAO(Id);
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

        boolean isSubscriptionAvailable = subscribeAvailableData.getSubscriptionAvailable(devPortalAPI);////

        Set<String> tiers = devPortalAPI.getAvailableTierNames();

        boolean isDefault =devPortalAPI.getIsDefaultVersion();

        String authorizationHeader = devPortalAPI.getAuthorizationHeader();

        String apiSecurity = devPortalAPI.getApiSecurity();

        boolean isMonetizationEnabled = devPortalAPI.isMonetizationEnabled();

        String throttlingPolicies = throttlingPoliciesData.getThrottlingPoliciesData(tiers);;

        Set<String> categoriesSet = devPortalAPI.getApiCategories();

        String categories = getCatogories(categoriesSet);

        List<String> allkeymangersList = devPortalAPI.getKeyManagers();
        String allkeyManagers = getKeymanagers(allkeymangersList);

       //BusinessInformationDTO businessInformationDTO = new BusinessInformationDTO("","","","");

        String businessOwner = devPortalAPI.getBusinessOwner();
        String businessOwnerEmail = devPortalAPI.getBusinessOwnerEmail();
        String technicalOwner = devPortalAPI.getTechnicalOwner();
        String technicalOwnerEmail = devPortalAPI.getTechnicalOwnerEmail();

        BusinessInformationDTO businessInformation = new BusinessInformationDTO(businessOwner,businessOwnerEmail,technicalOwner,technicalOwnerEmail);


        boolean advertised = devPortalAPI.isAdvertiseOnly();
        String originalStoreUrl = devPortalAPI.getRedirectURL();
        String apiOwner = devPortalAPI.getApiOwner();
        AdvertiseDTO advertiseInfo = new AdvertiseDTO(advertised,originalStoreUrl,apiOwner);

        APIEndpointURLsData apiEndpointURLsData = new APIEndpointURLsData();
        List<APIEndpointURLsDTO> apiEndPointInformation = apiEndpointURLsData.apiEndpointURLsDTO(devPortalAPI);

        TierData tierData = new TierData();
        List<TierNameDTO>  tierInformation = tierData.getTierName(devPortalAPI);

        LabelData labelData = new LabelData();
        List<LabelNameDTO> labelNameDTO = labelData.getLabelNames(id);

        IngressUrlsData ingressUrlsData = new IngressUrlsData();
        List<IngressUrlDTO> ingressUrlDTOS = ingressUrlsData.getIngressUrlData(devPortalAPI);

        return new Api(id,name,description,context,version,provider,type,transport,hasthumbnail,environments,wsdUrl,status,isSubscriptionAvailable,isDefault,authorizationHeader,apiSecurity,isMonetizationEnabled,throttlingPolicies,thumbnailUrl,categories,allkeyManagers,businessInformation,advertiseInfo,apiEndPointInformation,tierInformation,labelNameDTO,ingressUrlDTOS);
    }




}
