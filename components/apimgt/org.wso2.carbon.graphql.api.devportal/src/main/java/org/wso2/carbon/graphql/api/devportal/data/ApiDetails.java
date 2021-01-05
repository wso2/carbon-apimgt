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
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.exceptions.OASPersistenceException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
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



    public List<Api> getAllApis() throws UserStoreException, RegistryException, OASPersistenceException, APIManagementException {
        SubscribeAvailableData subscribeAvailableData = new SubscribeAvailableData();
        MonetizationLabelData monetizationLabelData = new MonetizationLabelData();
        ThrottlingPoliciesData throttlingPoliciesData = new ThrottlingPoliciesData();


        //ArtifactData artifactData = new ArtifactData();

        //ApisGet apisGet = new ApisGet();
        //List<Object> allMatchedApis = apisGet.getAllapiData();
        List<Api> apiDTOList = new ArrayList<Api>();

        for (int i =0;i<getApiCount();i++){

            ArtifactData artifactData = new ArtifactData();
            //String Id = artifactData.getTotalApisCount().

            //API api = (API) allMatchedApis.get(i);
            //APIIdentifier Id = api.getId();
            List<String> allIds = StringListtoArray();

            String Id = allIds.get(i);
            String id = artifactData.getDevportalApis(Id).getId();;



            String name = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_NAME);
            String description = artifactData.getDevportalApis(id).getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION); //
            String context = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_CONTEXT);
            String version = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_VERSION);
            String provider = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_PROVIDER);



            apiPersistenceInstance = PersistenceManager.getPersistenceInstance("wso2.anonymous.user");
            String TenantDomain = RestApiUtil.getRequestedTenantDomain(null);
            Organization org = new Organization(TenantDomain);
            String apiDefinition = apiPersistenceInstance.getOASDefinition(org, id); //

            String type = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_TYPE);
            String transport = artifactData.getDevportalApis(id).getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS); //



            String thumbnailUrl  = artifactData.getDevportalApis(id).getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL);
            boolean hasthumbnail = HasThumbnail(thumbnailUrl);//

            //Map<String, String> additionalProperties = ""; //


            String environments = getEnvironmentList(id);////

            String wsdUrl   = artifactData.getDevportalApis(id).getAttribute(APIConstants.API_OVERVIEW_WSDL);;
            String status  = getLcStateFromArtifact(artifactData.getDevportalApis(Id));




            boolean isSubscriptionAvailable = subscribeAvailableData.getSubscriptionAvailable(id);////

            String  tiers = artifactData.getDevportalApis(id).getAttribute(APIConstants.API_OVERVIEW_TIER);
            String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(provider));
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomainName);
            Map<String, Tier> definedTiers = getTiers(tenantId);
            String monetizationLabel = monetizationLabelData.getMonetizationLabelData(definedTiers,tiers,name);


            //boolean isDefault = api.isDefaultVersion();
            boolean isDefault = Boolean.parseBoolean(artifactData.getDevportalApis(id).getAttribute(
                    APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION));

            String authorizationHeader = artifactData.getDevportalApis(id).getAttribute(APIConstants.API_OVERVIEW_AUTHORIZATION_HEADER);;
            String apiSecurity = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_API_SECURITY);

            //String tags = ""; //
            Registry registry = artifactData.getRegistry();
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifactData.getDevportalApis(id).getId());

            Set<String> tagSet = new HashSet<String>();
            org.wso2.carbon.registry.core.Tag[] tag = registry.getTags(artifactPath);
            for (Tag tag1 : tag) {
                tagSet.add(tag1.getTagName());
            }
            String tags = getTags(tagSet);


            boolean isMonetizationEnabled = Boolean.parseBoolean(artifactData.getDevportalApis(id).getAttribute
                    (APIConstants.Monetization.API_MONETIZATION_STATUS));

            //Float rating = api.getRating();
//            APIIdentifier apiIdentifier = new APIIdentifier(provider, name, version);
//            int apiId = ApiMgtDAO.getInstance().getAPIID(apiIdentifier, null);
//            Float rating  = ApiMgtDAO.getInstance().getAverageRating(apiId);//APIUtil.getAverageRating(apiId);


            String throttlingPolicies = throttlingPoliciesData.getThrottlingPoliciesData(definedTiers,tiers,name);



            String categories = getCatogories(getAPICategoriesFromAPIGovernanceArtifact(artifactData.getDevportalApis(id)));


//            String categories = "";
//            String keyManagers = "";
            String keyManagers = artifactData.getDevportalApis(id).getAttribute(APIConstants.API_OVERVIEW_KEY_MANAGERS);
            List<String> keyManagersList = null;
            if (StringUtils.isNotEmpty(keyManagers)) {
                keyManagersList =  new Gson().fromJson(keyManagers, List.class);
            } else {
                keyManagersList = Arrays.asList(APIConstants.API_LEVEL_ALL_KEY_MANAGERS);
            }
            String allkeyManagers = getKeymanagers(keyManagersList);



//            String createdTime = api.getCreatedTime();
//            String lastUpdate = String.valueOf(api.getLastUpdated());
            String createdTime = String.valueOf(registry.get(artifactPath).getCreatedTime().getTime());

            String lastUpdate = String.valueOf(registry.get(artifactPath).getLastModified());

            String additionalPropertiesString = getResourceProperties(registry,artifactPath);


            Api api1 = new Api(id,name,description, context,version,provider,apiDefinition,type,transport,hasthumbnail,additionalPropertiesString,environments,wsdUrl,status,isSubscriptionAvailable,monetizationLabel,isDefault,authorizationHeader,apiSecurity,tags,isMonetizationEnabled,throttlingPolicies,thumbnailUrl,categories,allkeyManagers,createdTime,lastUpdate);
            apiDTOList.add(api1);
        }


        return apiDTOList;
    }
    public String getTags(Set<String> tagset){

        //Set<String> tagset = apiTypeWrapper.getApi().getTags();
        String tags = null;
        if (tagset!=null){
            List<String> tagList = new ArrayList<>(tagset);
            tags = String.join(",",tagList);
        }
        return tags;
    }
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
    public String getResourceProperties(Registry registry, String artifactPath) throws RegistryException {

        Map<String,String> additionalProperties = new HashMap<>();


        Resource apiResource = registry.get(artifactPath);
        Properties properties = apiResource.getProperties();

        if (properties != null) {
            Enumeration propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                if (propertyName.startsWith(APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX)) {
                    additionalProperties.put(propertyName.substring(APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX.length()),
                            apiResource.getProperty(propertyName));
                }
            }
        }

        return getAdditionalProperties(additionalProperties);

    }

    public String getAdditionalProperties(Map<String, String> additionalProperties){
        //Map<String, String> additionalProperties = apiTypeWrapper.getApi().getAdditionalProperties();

        List<String> additionalPropertiesList = new ArrayList<>();

        String alladditionalProperties= null;

        for (String key : additionalProperties.keySet()) {
            String properties = key + ":" + additionalProperties.get(key);
            additionalPropertiesList.add(properties);
        }
        if(additionalPropertiesList!=null){
            alladditionalProperties= String.join(",",additionalPropertiesList);

        }
        return alladditionalProperties;
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

    public String getEnvironmentList(String Id) throws GovernanceException {

        ArtifactData artifactData = new ArtifactData();
        Set<String> environmentset = getEnvironments(artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS));
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

    public String getCatogories(List<APICategory> catogories){


        //List<APICategory> catogories = apiTypeWrapper.getApi().getApiCategories();
        List<String> apiCatogoryNameList = new ArrayList<>();

        String apiCatogoryName = null;
        if(catogories!=null){
            for (int i = 0;i< catogories.size();i++){
                String name  = catogories.get(i).getName();
                apiCatogoryNameList.add(name);

            }
            apiCatogoryName = String.join(",",apiCatogoryNameList);
            return apiCatogoryName;
        }
        else{
            return apiCatogoryName;
        }

    }
    public List<APICategory> getAPICategoriesFromAPIGovernanceArtifact(GovernanceArtifact artifact) throws GovernanceException{

        String[] categoriesOfAPI = artifact.getAttributes(APIConstants.API_CATEGORIES_CATEGORY_NAME);

        List<APICategory> categoryList = new ArrayList<>();

        if (ArrayUtils.isNotEmpty(categoriesOfAPI)) {
            for (String categoryName : categoriesOfAPI) {
                APICategory category = new APICategory();
                category.setName(categoryName);
                categoryList.add(category);
            }
        }
        return categoryList;
    }


    public int getApiCount() throws UserStoreException, RegistryException, APIManagementException {

       ArtifactData artifactData = new ArtifactData();
//
//        List<GovernanceArtifact> governanceArtifacts = artifactData.searchdDevPortalAPIs();
//
//        return governanceArtifacts.size();
//        ApisGet apisGet = new ApisGet();
//        List<Object> allMatchedApis = apisGet.getAllapiData();
//
//        return allMatchedApis.size();

        return artifactData.getTotalApisCount().length;
    }

    public List<String> StringListtoArray() throws UserStoreException, RegistryException, APIManagementException{

        ArtifactData artifactData = new ArtifactData();
        String[] allIds = artifactData.getTotalApisCount();

        List<String> IdList = Arrays.asList(allIds);

        return IdList;
    }

    public Float getApiRating(String Id) throws GovernanceException, APIManagementException {
        ArtifactData artifactData = new ArtifactData();

        String name = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_NAME);

        String version = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_VERSION);
        String provider = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_PROVIDER);

        APIIdentifier apiIdentifier = new APIIdentifier(provider, name, version);
        int apiId = ApiMgtDAO.getInstance().getAPIID(apiIdentifier, null);
        Float rating  = ApiMgtDAO.getInstance().getAverageRating(apiId);//APIUtil.getAverageRating(apiId);

        return rating;
    }


    public Api getApi(String Id) throws APIManagementException, RegistryException, OASPersistenceException, UserStoreException {
        SubscribeAvailableData subscribeAvailableData = new SubscribeAvailableData();
        MonetizationLabelData monetizationLabelData = new MonetizationLabelData();
        ThrottlingPoliciesData throttlingPoliciesData = new ThrottlingPoliciesData();


        //for new Implementation
        ArtifactData artifactData = new ArtifactData();


        String id = artifactData.getDevportalApis(Id).getId();
        String name = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_NAME);

        String description = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION);
        String context = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_CONTEXT);

        String version = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_VERSION);
        String provider = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_PROVIDER);




        apiPersistenceInstance = PersistenceManager.getPersistenceInstance("wso2.anonymous.user");
        String TenantDomain = RestApiUtil.getRequestedTenantDomain(null);
        Organization org = new Organization(TenantDomain);
        String apiDefinition = apiPersistenceInstance.getOASDefinition(org, id);

        //String apiDefinition = apiTypeWrapper.getApi().getSwaggerDefinition();


        String type = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_TYPE);
        String transport = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS);


        String thumbnailUrl = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL);
        boolean hasthumbnail = HasThumbnail(thumbnailUrl);

        //not completed
        //String additionalPropertiesString = "";
        //

        String environments = getEnvironmentList(Id);

        String wsdUrl = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_WSDL);
        String status  = getLcStateFromArtifact(artifactData.getDevportalApis(Id));


        boolean isSubscriptionAvailable = subscribeAvailableData.getSubscriptionAvailable(Id);


        //for monetizationLabel
        String  tiers = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_TIER);
        String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(provider));
        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                .getTenantId(tenantDomainName);
        Map<String, Tier> definedTiers = getTiers(tenantId);
        String monetizationLabel = monetizationLabelData.getMonetizationLabelData(definedTiers,tiers,name);


        boolean isDefault = Boolean.parseBoolean(artifactData.getDevportalApis(Id).getAttribute(
                APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION));


        String authorizationHeader = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_AUTHORIZATION_HEADER);
        String apiSecurity = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_API_SECURITY);

        //

        boolean isMonetizationEnabled = Boolean.parseBoolean(artifactData.getDevportalApis(Id).getAttribute
                (APIConstants.Monetization.API_MONETIZATION_STATUS));


        //
//        APIIdentifier apiIdentifier = new APIIdentifier(provider, name, version);
//        int apiId = ApiMgtDAO.getInstance().getAPIID(apiIdentifier, null);
//        Float rating  = ApiMgtDAO.getInstance().getAverageRating(apiId);//APIUtil.getAverageRating(apiId);


        String throttlingPolicies = throttlingPoliciesData.getThrottlingPoliciesData(definedTiers,tiers,name);

        //String thumbnailUrl = apiTypeWrapper.getApi().getThumbnailUrl();


        String categories = getCatogories(getAPICategoriesFromAPIGovernanceArtifact(artifactData.getDevportalApis(Id)));


        String keyManagers = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_KEY_MANAGERS);
        List<String> keyManagersList = null;
        if (StringUtils.isNotEmpty(keyManagers)) {
            keyManagersList =  new Gson().fromJson(keyManagers, List.class);
        } else {
            keyManagersList = Arrays.asList(APIConstants.API_LEVEL_ALL_KEY_MANAGERS);
        }
        String allkeyManagers = getKeymanagers(keyManagersList);

        Registry registry = artifactData.getRegistry();

        String artifactPath = GovernanceUtils.getArtifactPath(registry, artifactData.getDevportalApis(Id).getId());
        String createdTime = String.valueOf(registry.get(artifactPath).getCreatedTime().getTime());

        String lastUpdate = String.valueOf(registry.get(artifactPath).getLastModified());

        Set<String> tagSet = new HashSet<String>();
        org.wso2.carbon.registry.core.Tag[] tag = registry.getTags(artifactPath);
        for (Tag tag1 : tag) {
            tagSet.add(tag1.getTagName());
        }
        String tags = getTags(tagSet);

        String additionalPropertiesString = getResourceProperties(registry,artifactPath);


        return new Api(id,name,description, context,version,provider,apiDefinition,type,transport,hasthumbnail,additionalPropertiesString,environments,wsdUrl,status,isSubscriptionAvailable,monetizationLabel,isDefault,authorizationHeader,apiSecurity,tags,isMonetizationEnabled,throttlingPolicies,thumbnailUrl,categories,allkeyManagers,createdTime,lastUpdate);
    }




}
