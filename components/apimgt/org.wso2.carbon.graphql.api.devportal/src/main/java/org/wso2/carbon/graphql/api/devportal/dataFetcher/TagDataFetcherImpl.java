package org.wso2.carbon.graphql.api.devportal.dataFetcher;


import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;
import org.wso2.carbon.graphql.api.devportal.service.TagService;

@Component
public class TagDataFetcherImpl {

    TagService tagData = new TagService();

    public DataFetcher getTagsData(){
        return env->tagData.getAllTags();
    }

}
