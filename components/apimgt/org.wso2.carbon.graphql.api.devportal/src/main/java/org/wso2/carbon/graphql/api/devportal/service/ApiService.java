package org.wso2.carbon.graphql.api.devportal.service;

import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.wso2.carbon.apimgt.api.APIManagementException;
//import org.wso2.carbon.graphql.api.devportal.data.*;
import org.wso2.carbon.graphql.api.devportal.impl.dao.ApiDAO;
import org.wso2.carbon.graphql.api.devportal.impl.dao.TierDAO;
import org.wso2.carbon.graphql.api.devportal.impl.ApiListing;
import org.wso2.carbon.graphql.api.devportal.impl.registry.ApiRegistry;
import org.wso2.carbon.graphql.api.devportal.impl.dao.LabelDAO;
import org.wso2.carbon.graphql.api.devportal.impl.dao.OperationDAO;
import org.wso2.carbon.graphql.api.devportal.impl.dao.ScopesDAO;
import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;
import org.wso2.carbon.graphql.api.devportal.modules.api.ApiDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.LabelNameDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.TierNameDTO;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
public class ApiService{

    ApiRegistry apiRegistry = new ApiRegistry();
    TierDAO tierDAO = new TierDAO();
    ScopesDAO scopesDAO = new ScopesDAO();
    OperationDAO operationDAO = new OperationDAO();
    LabelDAO labelDAO = new LabelDAO();
    ApiDAO apiDAO = new ApiDAO();
    ApiListing apiListing = new ApiListing();




    public DataFetcher getApiListing(){
        return env-> {
            int start = env.getArgument("start");
            int offset = env.getArgument("offset");
            return apiListing.getApiListing(start,offset);
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
            return operationDAO.getOperationDetailsFromDAO(api.getId());

        };
    }
    public DataFetcher getLabelsDetails(){
        return env->{
            LabelNameDTO labelNameDTO = env.getSource();
            return labelDAO.getLabelDetailsFromDAO(labelNameDTO.getName());
        };
    }
    public DataFetcher getScopeInformation(){
        return env->{
            ApiDTO api = env.getSource();
            return scopesDAO.getScopesDetailsFromDAO(api.getId());
        };
    }






}
