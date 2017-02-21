package org.wso2.carbon.apimgt.rest.api.core;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/*
* Response class for object list
*
* */
@XmlRootElement(name = "responseList")
public class ResponseList {

    private List<Object> list;

    public List<Object> getList() {
        return list;
    }

    public void setList(List<Object> list) {
        this.list = list;
    }
}
