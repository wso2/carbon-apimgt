package org.wso2.carbon.graphql.api.devportal;

import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.wso2.carbon.graphql.api.devportal.data.*;
import org.wso2.carbon.graphql.api.devportal.modules.*;
import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
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


    ///////////////////////
    public static class Context {

        final DataLoaderRegistry dataLoaderRegistry;

        public Context() {
            this.dataLoaderRegistry = new DataLoaderRegistry();
            dataLoaderRegistry.register("characters", newCharacterDataLoader());
        }

        public DataLoaderRegistry getDataLoaderRegistry() {
            return dataLoaderRegistry;
        }

        public DataLoader<String, Object> getCharacterDataLoader() {
            return dataLoaderRegistry.getDataLoader("characters");
        }
    }


    private static List<Object> getCharacterDataViaBatchHTTPApi(List<String> keys) {
        return keys.stream().map(TierData::getCharacterData).collect(Collectors.toList());
    }

    // a batch loader function that will be called with N or more keys for batch loading
    private static BatchLoader<String,Object> characterBatchLoader = keys -> {

        //
        // we are using multi threading here.  Imagine if getCharacterDataViaBatchHTTPApi was
        // actually a HTTP call - its not here - but it could be done asynchronously as
        // a batch API call say
        //
        //
        // direct return of values
        //CompletableFuture.completedFuture(getCharacterDataViaBatchHTTPApi(keys))
        //
        // or
        //
        // async supply of values

        return CompletableFuture.supplyAsync(() -> getCharacterDataViaBatchHTTPApi(keys));
    };
    private static DataLoader<String, Object> newCharacterDataLoader() {
        return new DataLoader<>(characterBatchLoader);
    }

    ///////////////////////



    public DataFetcher getApiListing(){
        return env-> {
            int start = env.getArgument("start");
            int offset = env.getArgument("offset");
            return apiListingData.getApiListing(start,offset);
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


    public DataFetcher  getApiRating(){
        return env->{
            Api api = env.getSource();
            return apiDetails.getApiRating(api.getId());
        };
    }

    public DataFetcher getTierNames(){
        return env->{
            Api api = env.getSource();
            //List<TierNameDTO> tierNames = tierData.getTierName(api.getId());

            Context ctx = env.getContext();
            return ctx.getCharacterDataLoader().load("1000");
           //return tierData.getTierName(api.getId());
        };
    }


    public DataFetcher getTierDetails(){
        return env->{
            TierNameDTO tierNameDTO = env.getSource();
            return tierData.getTierData(tierNameDTO.getApiId(),tierNameDTO.getName());
        };
    }

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
    public DataFetcher getBusinessInformation(){

        return env-> {
            Api api = env.getSource();
            return dummyBusinessInformation.getBusinessInformations(api.getId());
        };
    }

    public DataFetcher getLabelInformation(){
        return env->{
//            DataLoader<String,Object> dataLoader = env.getDataLoader("")
          Api api = env.getSource();
          return labelData.getLabelNames(api.getId());
        };
    }
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

    public DataFetcher getAdvertiseInformation(){
        return env->{
            Api api = env.getSource();
            return advertiseData.getAdvertiseInformation(api.getId());
        };
    }

    public DataFetcher getApiUrlsEndPoint(){
        return env->{
            Api api = env.getSource();
            return apiEndpointURLData.apiEndpointURLsDTO(api.getId());

        };
    }
    public DataFetcher getApiUrlsDTO(){
        return env ->{
            APIEndpointURLsDTO apiEndpointURLsDTO = env.getSource();
            return dummyAPIUrlsDTO.apiURLsDTO(apiEndpointURLsDTO.getApiId());

        };
    }

    public DataFetcher getDefaultApiUrlsDTO(){
        return env->{
            APIEndpointURLsDTO apiEndpointURLsDTO = env.getSource();
            return defaultAPIURLsData.getDefaultAPIURLsData(apiEndpointURLsDTO.getApiId());
        };

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
