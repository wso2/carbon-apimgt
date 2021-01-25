package org.wso2.carbon.graphql.api.devportal;


import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;
import org.wso2.carbon.graphql.api.devportal.data.tagData.TagData;

@Component
public class TagSevice {

    TagData tagData = new TagData();

    public DataFetcher getTagsData(){
        return env->tagData.getAllTags();
    }

}
