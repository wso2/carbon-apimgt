package org.wso2.carbon.graphql.api.devportal;

import org.wso2.carbon.graphql.api.devportal.data.*;
import org.wso2.carbon.graphql.api.devportal.modules.APIEndpointURLsDTO;
import org.wso2.carbon.graphql.api.devportal.modules.Api;
import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;
import org.wso2.carbon.graphql.api.devportal.data.*;
import org.wso2.carbon.graphql.api.devportal.modules.TierDTO;
import org.wso2.carbon.graphql.api.devportal.modules.TierNameDTO;
//import org.wso2.carbon.apimgt.graphql.api.devportal.data.*;


@Component
public class ApiService {

    ApiDetails apiDetails = new ApiDetails();
    BusinessInformationData dummyBusinessInformation = new BusinessInformationData();
    APIEndpointURLsData apiEndpointURLData = new APIEndpointURLsData();
    APIUrlsData dummyAPIUrlsDTO = new APIUrlsData();
    DefaultAPIURLsData defaultAPIURLsData = new DefaultAPIURLsData();
    PaginationData paginationData = new PaginationData();
    ApiData apiData = new ApiData();
    TierData tierData = new TierData();
    AdvertiseData advertiseData = new AdvertiseData();
    ScopesData scopesData = new ScopesData();
    OperationData operationData = new OperationData();

    IngressUrlsData ingressUrlsData  = new IngressUrlsData();

    LabelData labelData = new LabelData();

    APIDTOData apidtoData = new APIDTOData();


//    public DataFetcher getApiDetails(){
//        return env->{
//            Api api = env.getSource();
//            return apidtoData.getApiData(api.getId());
//
//        };
//    }
    public DataFetcher getApiName(){
        return env->{
            Api api = env.getSource();
            return apidtoData.getApiName(api.getId());

        };
    }
    public DataFetcher getApiContext(){
        return env->{
            Api api = env.getSource();
            return apidtoData.getApiContext(api.getId());

        };
    }
    public DataFetcher getApiVersion(){
        return env->{
            Api api = env.getSource();
            return apidtoData.getApiVersion(api.getId());

        };
    }
    public DataFetcher getApiProvider(){
        return env->{
            Api api = env.getSource();
            return apidtoData.getApiProvider(api.getId());

        };
    }
    public DataFetcher getApiType(){
        return env->{
            Api api = env.getSource();
            return apidtoData.getApiType(api.getId());

        };
    }
    public DataFetcher getApiCreatedTime(){
        return env->{
            Api api = env.getSource();
            return apidtoData.getApiCreatedTime(api.getId());

        };
    }
    public DataFetcher getApiUpdatedTime(){
        return env->{
            Api api = env.getSource();
            return apidtoData.getApiLastUpdateTime(api.getId());

        };
    }

    public DataFetcher getAdditionalProperties(){
        return env->{
            Api api = env.getSource();
            return apidtoData.getAdditionalProperties(api.getId());

        };
    }
    public DataFetcher getApis(){


        return env-> apiDetails.getAllApis();

    }

    public DataFetcher getApi(){
        return env->{
            String Id = env.getArgument("id");
            return apiDetails.getApi(Id);

        };
    }
    public DataFetcher getApiCount(){
        return env-> apiDetails.getApiCount();
    }

    public DataFetcher  getApiRating(){
        return env->{
            Api api = env.getSource();
            return apiDetails.getApiRating(api.getId());
        };
    }

    public DataFetcher getTierInformation(){
        return env->{
            Api api = env.getSource();
            return tierData.getTierName(api.getId());
        };
    }

    public DataFetcher getTiers(){
        return env->{
            TierNameDTO tierNameDTO = env.getSource();
            return tierData.getTierData(tierNameDTO.getApiId(),tierNameDTO.getName());
        };
    }

//    public DataFetcher getTierName(){
//        return env->{
//
//            TierDTO tierDTO = env.getSource();
//            return tierData.getTierName(tierDTO.setApiId("b9cb1f47-f450-4ff6-bba9-3b51ba28433c"));
//        };
//    }



    public DataFetcher getOperationInformation(){
        return env->{
            Api api = env.getSource();
            return operationData.getOperationData(api.getId());

        };
    }
    public DataFetcher getBusinessInformation(){

        return env-> {
            Api api = env.getSource();
            return dummyBusinessInformation.getBusinessInformations(api.getId());
        };
    }

    public DataFetcher getLabelInformation(){
        return env->{
          Api api = env.getSource();
          return labelData.getLabeldata(api.getId());
        };
    }
    public DataFetcher getScopeInformation(){
        return env->{
            Api api = env.getSource();
            return scopesData.getScopesData(api.getId());
        };
    }

    public DataFetcher getAdvertiseInformation(){
        return env->{
            Api api = env.getSource();
            return advertiseData.getAdvertiseInformation(api.getId());
        };
    }

//    public DataFetcher getUrl(){
//        return env->{
//            Api api = env.getSource();
//            return dummyAPIUrlsDTO.getUrl(api.getId());
//        };
//    }

    public DataFetcher getApiUrlsEndPoint(){
        return env->{
            Api api = env.getSource();
            return apiEndpointURLData.apiEndpointURLsDTO(api.getId());

        };
    }
    public DataFetcher getApiUrlsDTO(){
        return env ->{
            //Api api =  env.getSource();
            APIEndpointURLsDTO apiEndpointURLsDTO = env.getSource();
            //apiEndpointURLsDTO.setApiId(api.getId());
            return dummyAPIUrlsDTO.apiURLsDTO(apiEndpointURLsDTO.getApiId());

        };
    }

    public DataFetcher getDefaultApiUrlsDTO(){
        return env->{
            APIEndpointURLsDTO apiEndpointURLsDTO = env.getSource();
            return defaultAPIURLsData.getDefaultAPIURLsData(apiEndpointURLsDTO.getApiId());
        };

    }

    public DataFetcher getPagination(){
        return env->paginationData.getPaginationData();
    }

    public DataFetcher getDeploymentEnvironmentName(){
        return env->{
            Api api = env.getSource();
            return ingressUrlsData.getDeplymentEnvironmentName(api.getId());
        };
    }
    public DataFetcher getDeploymentClusterInformation(){
        return env ->{
            Api api = env.getSource();
            return ingressUrlsData.getDeploymentClusterData(api.getId());
        };
    }



}
