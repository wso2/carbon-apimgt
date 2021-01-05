package org.wso2.carbon.graphql.api.devportal.modules;

public class OperationDTO {

    private String target ;
    private String verb ;

    public OperationDTO(String target,String verb){
        this.target = target;
        this.verb = verb;
    }
}
