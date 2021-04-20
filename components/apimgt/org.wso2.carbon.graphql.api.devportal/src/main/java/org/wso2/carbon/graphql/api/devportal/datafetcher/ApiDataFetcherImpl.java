package org.wso2.carbon.graphql.api.devportal.datafetcher;

import org.wso2.carbon.graphql.api.devportal.modules.api.ContextDTO;
import org.wso2.carbon.graphql.api.devportal.service.ApiService;
import org.wso2.carbon.graphql.api.devportal.service.LabelService;
import org.wso2.carbon.graphql.api.devportal.service.OperationService;
import org.wso2.carbon.graphql.api.devportal.service.ScopesService;
import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;
import org.wso2.carbon.graphql.api.devportal.service.TagService;
import org.wso2.carbon.graphql.api.devportal.service.TierService;
import org.wso2.carbon.graphql.api.devportal.modules.api.ApiDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.LabelNameDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.TierNameDTO;

import java.util.*;

@Component
public class ApiDataFetcherImpl {

    ApiService apiService = new ApiService();
    TierService tierService = new TierService();
    ScopesService scopesService = new ScopesService();
    OperationService operationService = new OperationService();
    LabelService labelService = new LabelService();


    public DataFetcher getApiListing(){
        return env-> {
            int offset = env.getArgument("offset");
            int  limit= env.getArgument("limit");
            return apiService.getAllApis(offset,limit);
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
            String tiers = api.getThrottlingPolicies();
            return apiService.getMonetizationLabel(tiers);
        };
    }
    public DataFetcher getCreatedTime(){
        return env->{
            ApiDTO api = env.getSource();
            Map<String, ContextDTO> contextDTOMap = env.getContext();
            ContextDTO contextDTO = contextDTOMap.get(api.getUuid());
            if(contextDTO==null){
                contextDTO = apiService.getApiTimeDetails(api.getUuid());
                contextDTOMap.put(api.getUuid(), contextDTO);
            }
            return contextDTO.getCreatedTime();
        };
    }

    public DataFetcher getLastUpdate(){
        return env->{
            ApiDTO api = env.getSource();
            Map<String, ContextDTO> contextDTOMap = env.getContext();
            ContextDTO contextDTO = contextDTOMap.get(api.getUuid());
            if(contextDTO==null){
                contextDTO = apiService.getApiTimeDetails(api.getUuid());
                contextDTOMap.put(api.getUuid(), contextDTO);
            }
            return contextDTO.getLastUpdate();
        };
    }

    public DataFetcher getOperationInformation(){
        return env->{
            ApiDTO api = env.getSource();
            Map<String, ContextDTO> contextDTOMap = env.getContext();
            ContextDTO contextDTO = contextDTOMap.get(api.getUuid());
            if(contextDTO==null){
                contextDTO = apiService.getApiTimeDetails(api.getUuid());
                contextDTOMap.put(api.getUuid(), contextDTO);
            }
            return operationService.getOperationDetails(contextDTOMap, api.getUuid());

        };
    }
    public DataFetcher getScopeInformation(){
        return env->{
            ApiDTO api = env.getSource();
            return scopesService.getScopesDetails(api.getUuid());
        };
    }

    public DataFetcher getApiDefinition(){
        return env->{
            ApiDTO api = env.getSource();
            return apiService.getApiDefinition(api.getUuid());
        };
    }

    public DataFetcher getApiFromArtifact(){
        return env->{
            String uuid = env.getArgument("id");
            return apiService.getApi(uuid);
        };
    }




}
