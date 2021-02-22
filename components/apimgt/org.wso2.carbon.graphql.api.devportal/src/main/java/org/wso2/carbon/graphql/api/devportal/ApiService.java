package org.wso2.carbon.graphql.api.devportal;

import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.graphql.api.devportal.data.*;
import org.wso2.carbon.graphql.api.devportal.modules.*;
import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Component
public class ApiService{

    ApiDetails apiDetails = new ApiDetails();
    BusinessInformationData dummyBusinessInformation = new BusinessInformationData();
    APIEndpointURLsData apiEndpointURLData = new APIEndpointURLsData();
    APIUrlsData dummyAPIUrlsDTO = new APIUrlsData();
    DefaultAPIURLsData defaultAPIURLsData = new DefaultAPIURLsData();
    PaginationData paginationData = new PaginationData();

    TierData tierData = new TierData();
    MonetizationLabelData monetizationLabelData = new MonetizationLabelData();
    AdvertiseData advertiseData = new AdvertiseData();
    ScopesData scopesData = new ScopesData();
    OperationData operationData = new OperationData();

    IngressUrlsData ingressUrlsData  = new IngressUrlsData();

    LabelData labelData = new LabelData();

    APIDTOData apidtoData = new APIDTOData();

    ArtifactData artifactData = new ArtifactData();



    ApiListingData apiListingData = new ApiListingData();




    public DataFetcher getApiListing(){
        return env-> {
            int start = env.getArgument("start");
            int offset = env.getArgument("offset");
            return apiListingData.getApiListing(start,offset);
        };
    }

//    public DataFetcher getApiCreatedTime(){
//        return env->{
//            Api api = env.getSource();
//            return apidtoData.getApiCreatedTime(api.getId());
//
//        };
//    }
//    public DataFetcher getApiUpdatedTime(){
//        return env->{
//            Api api = env.getSource();
//            return apidtoData.getApiLastUpdateTime(api.getId());
//
//        };
//    }

    public DataFetcher getApiDefinition(){
        return env->{
            Api api = env.getSource();
            return apidtoData.getApiDefinition(api.getId());

        };
    }

    public DataFetcher getApiFromArtifact(){
        return env->{
            String Id = env.getArgument("id");
            return apiDetails.getApi(Id);

        };
    }
    public DataFetcher getApiTimeDetails(){
        return env->{
            Api api = env.getSource();

            DataLoaderRegistry dataLoaderRegistry = env.getContext();
            DataLoader<String , Object> timeDetailsLoader = dataLoaderRegistry.getDataLoader("times");

            return timeDetailsLoader.load(api.getId());

        };
    }
    public BatchLoader<String, Object> timeBatchLoader = new BatchLoader<String, Object>() {
        @Override
        public CompletionStage<List<Object>> load(List<String> Ids)  {
            return  CompletableFuture.supplyAsync(()-> {
                List<Object> timeDetails = new ArrayList<>();

                for(int i = 0;i<Ids.size();i++){
                    try {
                        timeDetails.add(apidtoData.getApiTimeDetails(Ids.get(i)));
                    } catch (APIManagementException e) {
                        e.printStackTrace();
                    }
                }

                return timeDetails;
            });
        }

    };


    public DataFetcher  getApiRating(){
        return env->{
            Api api = env.getSource();
            return apiDetails.getApiRating(api.getId());
        };
    }

//    public DataFetcher getTierNames(){
//        return env->{
//            Api api = env.getSource();
//           return tierData.getTierName(api.getId());
//        };
//    }


    public DataFetcher getTierDetails(){
        return env->{
            TierNameDTO tierNameDTO = env.getSource();

            return tierData.getTierData(tierNameDTO.getApiId(),tierNameDTO.getName());
        };
    }

//    public BatchLoader<String , TierNameDTO> tierBatchLoader = new BatchLoader<String, TierNameDTO>() {
//        @Override
//        public CompletionStage<List<TierNameDTO>> load(List<String> tierIds)  {
//            return  CompletableFuture.supplyAsync(()-> {
//                try {
//                    return tierData.getTierName(tierIds.get(0));
//                } catch (APIPersistenceException e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            });
//        }
//    };


    public DataFetcher getMonetizationLabel(){
        return env->{
            Api api = env.getSource();
            return monetizationLabelData.getMonetizationLabelData(api.getId());
        };
    }



    public DataFetcher getOperationInformation(){
        return env->{
            Api api = env.getSource();
            return operationData.getOperationData(api.getId());

        };
    }
//    public DataFetcher getBusinessInformation(){
//
//        return env-> {
//            Api api = env.getSource();
//            return dummyBusinessInformation.getBusinessInformations(env);
//        };
//    }

//    public DataFetcher getLabelInformation(){
//        return env->{
////            DataLoader<String,Object> dataLoader = env.getDataLoader("")
//          Api api = env.getSource();
//          return labelData.getLabelNames(api.getId());
//        };
//    }
    public DataFetcher getLabelsDetails(){
        return env->{
            LabelNameDTO labelNameDTO = env.getSource();
            return labelData.getLabeldata(labelNameDTO.getName());
        };
    }
    public DataFetcher getScopeInformation(){
        return env->{
            Api api = env.getSource();
            return scopesData.getScopesData(api.getId());
        };
    }

//    public DataFetcher getAdvertiseInformation(){
//        return env->{
//            Api api = env.getSource();
//            return advertiseData.getAdvertiseInformation(api.getId());
//        };
//    }

//    public DataFetcher getApiUrlsEndPoint(){
//        return env->{
//            Api api = env.getSource();
//            return apiEndpointURLData.apiEndpointURLsDTO(api.getId());
//
//        };
//    }
//    public DataFetcher getApiUrlsDTO(){
//        return env ->{
//            APIEndpointURLsDTO apiEndpointURLsDTO = env.getSource();
//            return dummyAPIUrlsDTO.apiURLsDTO(apiEndpointURLsDTO.getApiId());
//
//        };
//    }
//
//    public DataFetcher getDefaultApiUrlsDTO(){
//        return env->{
//            APIEndpointURLsDTO apiEndpointURLsDTO = env.getSource();
//            return defaultAPIURLsData.getDefaultAPIURLsData(apiEndpointURLsDTO.getApiId());
//        };
//
//    }


//    public DataFetcher getDeploymentEnvironmentName(){
//        return env->{
//            Api api = env.getSource();
//            return ingressUrlsData.getDeplymentEnvironmentName(api.getId());
//        };
//    }
//    public DataFetcher getDeploymentClusterInformation(){
//        return env ->{
//            Api api = env.getSource();
//            return ingressUrlsData.getDeploymentClusterData(api.getId());
//        };
//    }







}
