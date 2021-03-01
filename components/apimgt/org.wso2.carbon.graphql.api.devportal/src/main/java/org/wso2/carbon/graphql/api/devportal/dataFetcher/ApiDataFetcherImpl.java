package org.wso2.carbon.graphql.api.devportal.dataFetcher;

//import org.wso2.carbon.graphql.api.devportal.data.*;
import org.wso2.carbon.graphql.api.devportal.modules.api.ContextDTO;
import org.wso2.carbon.graphql.api.devportal.service.ApiService;
import org.wso2.carbon.graphql.api.devportal.mapping.ApiListingMapping;
import org.wso2.carbon.graphql.api.devportal.service.ApiRegistryService;
import org.wso2.carbon.graphql.api.devportal.service.LabelService;
import org.wso2.carbon.graphql.api.devportal.service.OperationService;
import org.wso2.carbon.graphql.api.devportal.service.ScopesService;
import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;
import org.wso2.carbon.graphql.api.devportal.service.TierService;
import org.wso2.carbon.graphql.api.devportal.modules.api.ApiDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.LabelNameDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.TierNameDTO;

import java.util.*;

@Component
public class ApiDataFetcherImpl {

    ApiRegistryService apiRegistryService = new ApiRegistryService();
    TierService tierService = new TierService();
    ScopesService scopesService = new ScopesService();
    OperationService operationService = new OperationService();
    LabelService labelService = new LabelService();
    ApiService apiService = new ApiService();
    ApiListingMapping apiListingMapping = new ApiListingMapping();


    public DataFetcher getApiListing(){
        return env-> {
            int start = env.getArgument("start");
            int offset = env.getArgument("offset");
            return apiListingMapping.getApiListing(start,offset);
        };
    }

    public DataFetcher getApiDefinition(){
        return env->{
            ApiDTO api = env.getSource();
            return apiRegistryService.getApiDefinition(api.getUuid());
        };
    }

    public DataFetcher getApiFromArtifact(){
        return env->{
            String uuid = env.getArgument("id");
            return apiRegistryService.getApi(uuid);
        };
    }
    public DataFetcher getApiTimeDetails(){
        return env->{
            ApiDTO api = env.getSource();
            return apiService.getApiTimeDetailsFromDAO(api.getUuid());
        };
    }
//    public BatchLoader<String, ContextDTO> timeBatchLoader = new BatchLoader<String, ContextDTO>() {
//        @Override
//        public CompletionStage<List<ContextDTO>> load(List<String> Ids)  {
//            return  CompletableFuture.supplyAsync(()-> {
//                List<ContextDTO> timeDetails = new ArrayList<>();
//
//                for(int i = 0;i<Ids.size();i++){
//                   // try {
//                        String username = "wso2.anonymous.user";
//                        APIConsumer apiConsumer = null;
//                        try {
//                            apiConsumer = RestApiCommonUtil.getConsumer(username);
//                        } catch (APIManagementException e) {
//                            e.printStackTrace();
//                        }
//                        Time time = apiConsumer.getTimeDetailsFromDAO(Ids.get(i));
//                        ContextDTO test = new ContextDTO();
//                        String createTime = time.getCreatedTime();
//                        test.setCreatedTime(createTime);
//                        String lastUpdate = time.getLastUpdate();
//                        test.setLastUpdate(lastUpdate);
//                        timeDetails.add(test);
//                }
//
//                return timeDetails;
//            });
//        }
//
//    };
//    public BatchLoader<String, Object> timeBatchLoader = new BatchLoader<String, Object>() {
//        @Override
//        public CompletionStage<List<Object>> load(List<String> Ids)  {
//            return  CompletableFuture.supplyAsync(()-> {
//                List<Object> timeDetails = new ArrayList<>();
//
//                for(int i = 0;i<Ids.size();i++){
//                    try {
//                        timeDetails.add(apiDAO.getApiTimeDetailsFromDAO(Ids.get(i)));
//                    } catch (APIManagementException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                return timeDetails;
//            });
//        }
//
//    };

    public List<ContextDTO> Data()  {
        List<ContextDTO> test = apiService.getApiTimeDetails();
        return test;
    }

    public DataFetcher getCreatedTime(){
        return env->{
            ApiDTO api = env.getSource();
            List<ContextDTO> contextDTOList = env.getContext();
            String createdTime = null;
            for(int i = 0 ;i< contextDTOList.size();i++){
                if (contextDTOList.get(i).getId().equals(api.getUuid())){
                    createdTime = contextDTOList.get(i).getCreatedTime();
                }
            }
            return createdTime;
        };
    }

    public DataFetcher getLastUpdate(){
        return env->{
            ApiDTO api = env.getSource();
            List<ContextDTO> contextDTOList = env.getContext();
            String lastUpdate = null;
            for(int i = 0 ;i< contextDTOList.size();i++){
                if (contextDTOList.get(i).getId().equals(api.getUuid())){
                    lastUpdate = contextDTOList.get(i).getLastUpdate();
                }
            }
            return lastUpdate;
        };
    }
    public DataFetcher  getApiRating(){
        return env->{
            ApiDTO api = env.getSource();
            return apiService.getApiRatingFromDAO(api.getUuid());
        };
    }
    public DataFetcher getTierDetails(){
        return env->{
            TierNameDTO tierNameDTO = env.getSource();

            return tierService.getTierDetailsFromDAO(tierNameDTO.getApiId(),tierNameDTO.getName());
        };
    }
    public DataFetcher getMonetizationLabel(){
        return env->{
            ApiDTO api = env.getSource();
            return apiService.getMonetizationLabel(api.getUuid());
        };
    }

    public DataFetcher getOperationInformation(){
        return env->{
            ApiDTO api = env.getSource();
            List<ContextDTO> contextDTOList = env.getContext();
            return operationService.getOperationDetails(contextDTOList, api.getUuid());

        };
    }
    public DataFetcher getLabelsDetails(){
        return env->{
            LabelNameDTO labelNameDTO = env.getSource();
            return labelService.getLabelDetails(labelNameDTO.getName());
        };
    }
    public DataFetcher getScopeInformation(){
        return env->{
            ApiDTO api = env.getSource();
            return scopesService.getScopesDetails(api.getUuid());
        };
    }






}
