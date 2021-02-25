package org.wso2.carbon.graphql.api.devportal.mapping;

import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.graphql.api.devportal.modules.tag.TagDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TagMapping {

    public List<TagDTO> fromTagToTagDTO(Set<Tag> tagSet){
        List<TagDTO> tagDTOList = new ArrayList<>();
        for(Tag tag: tagSet){
            String value = tag.getName();
            int count  = tag.getNoOfOccurrences();
            tagDTOList.add(new TagDTO(value,count));

        }
        return tagDTOList;
    }
}
