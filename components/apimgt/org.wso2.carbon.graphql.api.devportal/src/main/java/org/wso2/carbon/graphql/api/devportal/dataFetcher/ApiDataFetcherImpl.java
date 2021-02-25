package org.wso2.carbon.graphql.api.devportal.dataFetcher;

import org.dataloader.BatchLoader;
import org.wso2.carbon.apimgt.api.APIManagementException;
//import org.wso2.carbon.graphql.api.devportal.data.*;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
public class ApiDataFetcherImpl {

    ApiRegistryService apiRegistry = new ApiRegistryService();
    TierService tierDAO = new TierService();
    ScopesService scopesDAO = new ScopesService();
    OperationService operationDAO = new OperationService();
    LabelService labelDAO = new LabelService();
    ApiService apiDAO = new ApiService();
    ApiListingMapping apiListingMapping = new ApiListingMapping();




    public DataFetcher getApiListing(){
        return env-> {
           // env.getContext();
            int start = env.getArgument("start");
            int offset = env.getArgument("offset");
            return apiListingMapping.getApiListing(start,offset);
        };
    }

    public DataFetcher getApiDefinition(){
        return env->{
            ApiDTO api = env.getSource();
            return apiRegistry.getApiDefinition(api.getId());

        };
    }

    public DataFetcher getApiFromArtifact(){
        return env->{
            String Id = env.getArgument("id");
            return apiRegistry.getApi(Id);

        };
    }
    public DataFetcher getApiTimeDetails(){
        return env->{
            ApiDTO api = env.getSource();
            //env.getVariables().put("name", 12);

//            DataLoaderRegistry dataLoaderRegistry = env.getContext();
//            DataLoader<String , Object> timeDetailsLoader = dataLoaderRegistry.getDataLoader("times");

            //return timeDetailsLoader.load(api.getId());
            return apiDAO.getApiTimeDetailsFromDAO(api.getId());

        };
    }
    public BatchLoader<String, Object> timeBatchLoader = new BatchLoader<String, Object>() {
        @Override
        public CompletionStage<List<Object>> load(List<String> Ids)  {
            return  CompletableFuture.supplyAsync(()-> {
                List<Object> timeDetails = new ArrayList<>();

                for(int i = 0;i<Ids.size();i++){
                    try {
                        timeDetails.add(apiDAO.getApiTimeDetailsFromDAO(Ids.get(i)));
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
            ApiDTO api = env.getSource();
            return apiDAO.getApiRatingFromDAO(api.getId());
        };
    }
    public DataFetcher getTierDetails(){
        return env->{
            TierNameDTO tierNameDTO = env.getSource();

            return tierDAO.getTierDetailsFromDAO(tierNameDTO.getApiId(),tierNameDTO.getName());
        };
    }
    public DataFetcher getMonetizationLabel(){
        return env->{
            ApiDTO api = env.getSource();
            return apiDAO.getMonetizationLabel(api.getId());
        };
    }

    public DataFetcher getOperationInformation(){
        return env->{
            ApiDTO api = env.getSource();
            return operationDAO.getOperationDetails(api.getId());

        };
    }
    public DataFetcher getLabelsDetails(){
        return env->{
            LabelNameDTO labelNameDTO = env.getSource();
            return labelDAO.getLabelDetails(labelNameDTO.getName());
        };
    }
    public DataFetcher getScopeInformation(){
        return env->{
            ApiDTO api = env.getSource();
            return scopesDAO.getScopesDetails(api.getId());
        };
    }






}
