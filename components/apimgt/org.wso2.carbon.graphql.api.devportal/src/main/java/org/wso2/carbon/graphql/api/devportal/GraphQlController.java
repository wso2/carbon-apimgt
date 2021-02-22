package org.wso2.carbon.graphql.api.devportal;


import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.wso2.carbon.graphql.api.devportal.service.ApiService;
//import reactor.core.publisher.Mono;


@RestController
public class GraphQlController {

        @Autowired
        private GraphQL graphql;


        private final ApiService apiService;

        public GraphQlController(GraphQL graphql, ApiService apiService) {
                this.graphql = graphql;
                this.apiService = apiService;
        }

        @PostMapping(value="graphql", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ExecutionResult execute(@RequestBody GraphQlRequestBody body) {
//                return graphql.execute(ExecutionInput.newExecutionInput().query(body.getQuery())
//                        .operationName(body.getOperationName()).build()).toSpecification();

                ExecutionInput.Builder executionInputBuilder = ExecutionInput.newExecutionInput()
                        .query(body.getQuery())
                        .operationName(body.getOperationName());

                DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();
                DataLoader<String, Object> timeDataLoader = DataLoader.newDataLoader(apiService.timeBatchLoader);
                //DataLoader<String, Object> tierDataLoader = DataLoader.newDataLoader(apiService.timeBatchLoader);
                dataLoaderRegistry.register("times", timeDataLoader);
                //dataLoaderRegistry.register("tierdata", tierDataLoader);

                executionInputBuilder.dataLoaderRegistry(dataLoaderRegistry);
                executionInputBuilder.context(dataLoaderRegistry);
                ExecutionInput executionInput = executionInputBuilder.build();

                return graphql.execute(executionInput);

        }







}
