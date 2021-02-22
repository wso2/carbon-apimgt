package org.wso2.carbon.graphql.api.devportal.service;


import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;
import org.wso2.carbon.graphql.api.devportal.impl.tag.TagDataImpl;

@Component
public class TagSevice {

    TagDataImpl tagData = new TagDataImpl();

    public DataFetcher getTagsData(){
        return env->tagData.getAllTags();
    }

}
