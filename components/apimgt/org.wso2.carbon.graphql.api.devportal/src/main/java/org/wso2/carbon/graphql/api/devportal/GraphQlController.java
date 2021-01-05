package org.wso2.carbon.graphql.api.devportal;


import graphql.ExecutionInput;
import graphql.GraphQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class GraphQlController {

        @Autowired
        private GraphQL graphql;

        @PostMapping(value="graphql", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public Map<String,Object> execute(@RequestBody GraphQlRequestBody body) {
                return graphql.execute(ExecutionInput.newExecutionInput().query(body.getQuery())
                        .operationName(body.getOperationName()).build()).toSpecification();

        }







}
