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

    TagService tagData = new TagService();

    public DataFetcher getTagsData(){
        return env->tagData.getAllTags();
    }


    public DataFetcher getApiListing(){
        return env-> {
            int start = env.getArgument("start");
            int offset = env.getArgument("offset");
            //String oauth = env.getArgument("oauth");
            return apiService.getAllApis(start,offset);
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
            //String oauth = env.getArgument("oauth");
            return apiService.getApi(uuid);
        };
    }

    public DataFetcher getCreatedTime(){
        return env->{
            ApiDTO api = env.getSource();
            Map<String, ContextDTO> contextDTOMap = env.getContext();
            String createdTime = null;
            if(contextDTOMap.get(api.getUuid())==null){
                contextDTOMap.put(api.getUuid(), apiService.getApiTimeDetails(api.getUuid()));
            }
            createdTime = contextDTOMap.get(api.getUuid()).getCreatedTime();
            return createdTime;
        };
    }

    public DataFetcher getLastUpdate(){
        return env->{
            ApiDTO api = env.getSource();
            Map<String, ContextDTO> contextDTOMap = env.getContext();
            String lastUpdate = null;
            if(contextDTOMap.get(api.getUuid())==null){
                contextDTOMap.put(api.getUuid(), apiService.getApiTimeDetails(api.getUuid()));
            }
            lastUpdate = contextDTOMap.get(api.getUuid()).getLastUpdate();
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
            String tiers = api.getThrottlingPolicies();
            return apiService.getMonetizationLabel(tiers);
        };
    }

    public DataFetcher getOperationInformation(){
        return env->{
            ApiDTO api = env.getSource();
            Map<String, ContextDTO> contextDTOMap = env.getContext();
            if(contextDTOMap.get(api.getUuid())==null){
                contextDTOMap.put(api.getUuid(), apiService.getApiTimeDetails(api.getUuid()));
            }
            return operationService.getOperationDetails(contextDTOMap, api.getUuid());

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
