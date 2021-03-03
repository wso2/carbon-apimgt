package org.wso2.carbon.graphql.api.devportal;


import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

import org.springframework.core.io.Resource;
import org.wso2.carbon.graphql.api.devportal.dataFetcher.ApiDataFetcherImpl;
import org.wso2.carbon.graphql.api.devportal.dataFetcher.TagDataFetcherImpl;

@Component
public class GraphqlProvider {

    @Value("classpath:schema.graphql")
    Resource resource;


    @Autowired
    ApiDataFetcherImpl apiDataFetcher;

    @Autowired
    TagDataFetcherImpl tagDataFetcher;




    private GraphQL graphQL;



    @Bean
    public GraphQL graphQL()
    {
        return graphQL;
    }


    @PostConstruct
    public void init() throws IOException
    {
        File schemaFile = resource.getFile();
        GraphQLSchema graphQLSchema = buildSchema(schemaFile);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private GraphQLSchema buildSchema(File schema){
        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(schema);
        RuntimeWiring runtimeWiring = buildRuntime();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return  schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    }

    private RuntimeWiring buildRuntime(){
        return RuntimeWiring.newRuntimeWiring()
                .type(queryApi())
                .type(queryApiTags())
                .type(ApiDefinitionDataFetcher())
                .type(ApiRatingDataFetcher())
                .type(OperationInformationDataFetcher())
                .type(TierDetailsDataFetcher())
                .type(MonetizationLabelDataFetcher())
                .type(LabelDetailsDataFetcher())
                .type(ScopeInformationDataFetcher())
                .type(QueryApiListing())
                //.type(ApiTimeDetailsDataFetcher())
                .type(c())
                .type(u())
                .build();
    }

    private TypeRuntimeWiring.Builder QueryApiListing(){
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getApiListing", apiDataFetcher.getApiListing());
    }

    private TypeRuntimeWiring.Builder queryApi(){
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getApi", apiDataFetcher.getApiFromArtifact());
    }
    private TypeRuntimeWiring.Builder queryApiTags(){
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getTags", tagDataFetcher.getTagsData());
    }
//    private TypeRuntimeWiring.Builder ApiTimeDetailsDataFetcher(){
//        return TypeRuntimeWiring.newTypeWiring("Api")
//                .dataFetcher("timeDetails",apiDataFetcher.getApiTimeDetails());
//    }
    private TypeRuntimeWiring.Builder ApiDefinitionDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("apiDefinition",apiDataFetcher.getApiDefinition());
    }

    private TypeRuntimeWiring.Builder ApiRatingDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("rating",apiDataFetcher.getApiRating());
    }

    private TypeRuntimeWiring.Builder TierDetailsDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Tier")
                .dataFetcher("tierDetails",apiDataFetcher.getTierDetails());
    }
    private TypeRuntimeWiring.Builder MonetizationLabelDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("monetizationLabel",apiDataFetcher.getMonetizationLabel());
    }

    private TypeRuntimeWiring.Builder OperationInformationDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("operationInformation",apiDataFetcher.getOperationInformation());
    }
    private TypeRuntimeWiring.Builder LabelDetailsDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Label")
                .dataFetcher("labelDetails",apiDataFetcher.getLabelsDetails());
    }
    private TypeRuntimeWiring.Builder ScopeInformationDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("scope",apiDataFetcher.getScopeInformation());
    }

    private TypeRuntimeWiring.Builder c(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("createdTime",apiDataFetcher.getCreatedTime());
    }

    private TypeRuntimeWiring.Builder u(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("lastUpdate",apiDataFetcher.getLastUpdate());
    }




}
