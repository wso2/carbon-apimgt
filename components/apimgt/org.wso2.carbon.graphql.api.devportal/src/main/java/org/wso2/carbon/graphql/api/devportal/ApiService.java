package org.wso2.carbon.graphql.api.devportal;

import org.wso2.carbon.graphql.api.devportal.data.*;
import org.wso2.carbon.graphql.api.devportal.modules.APIEndpointURLsDTO;
import org.wso2.carbon.graphql.api.devportal.modules.Api;
import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;
import org.wso2.carbon.graphql.api.devportal.data.*;
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
            return tierData.getTierData(api.getId());
        };
    }



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
