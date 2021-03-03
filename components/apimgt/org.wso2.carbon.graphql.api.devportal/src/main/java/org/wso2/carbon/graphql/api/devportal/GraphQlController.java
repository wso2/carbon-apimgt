package org.wso2.carbon.graphql.api.devportal;


import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.graphql.api.devportal.dataFetcher.ApiDataFetcherImpl;
import org.wso2.carbon.graphql.api.devportal.modules.api.ContextDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import reactor.core.publisher.Mono;


@RestController
public class GraphQlController {

        @Autowired
        private GraphQL graphql;


        private final ApiDataFetcherImpl apiDataFetcher;


        public GraphQlController(GraphQL graphql, ApiDataFetcherImpl apiDataFetcher) {
                this.graphql = graphql;
                this.apiDataFetcher = apiDataFetcher;
        }

        @PostMapping(value="graphql", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ExecutionResult execute(@RequestBody GraphQlRequestBody body) {
//                return graphql.execute(ExecutionInput.newExecutionInput().query(body.getQuery())
//                        .operationName(body.getOperationName()).build()).toSpecification();

                ExecutionInput.Builder executionInputBuilder = ExecutionInput.newExecutionInput()
                        .query(body.getQuery())
                        .operationName(body.getOperationName());
                Map<String, ContextDTO> contextDTOMap = new HashMap<>();
                executionInputBuilder.context(contextDTOMap);

                ExecutionInput executionInput = executionInputBuilder.build();

                return graphql.execute(executionInput);

        }







}
