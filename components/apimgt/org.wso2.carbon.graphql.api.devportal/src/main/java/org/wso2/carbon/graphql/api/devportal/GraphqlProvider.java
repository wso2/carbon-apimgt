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

@Component
public class GraphqlProvider {

    @Value("classpath:schema.graphql")
    Resource resource;


    @Autowired
    ApiService apiService;




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
                .type(queryBuilder())
                .type(queryApi())
                //.type(queryApiDetails())
                .type(queryApiName())
                .type(queryApiContext())
                .type(queryApiVersion())
                .type(queryApiProvider())
                .type(queryApiType())
                .type(queryApiCreateTime())
                .type(queryApiUpdateTime())
                .type(queryApiAdditionalProperties())
                .type(queryApiCount())
                .type(queryApiRating())
                .type(queryOperationInformation())
                .type(queryTier())
                .type(queryTierDetails())
                .type(queryIngressUrl())
                .type(queryClusterInformation())
                .type(queryBusinessInformation())
                .type(queryLabelInformation())
                .type(queryScopeInformation())
                .type(queryAdvertiseInformation())
                .type(queryApiEndPointUrlsInformation())
                .type(queryAPIURLsDTO())
                .type(queryDefaultAPIURLsDTO())
                .type(queryPagination())
                .build();
    }


    private TypeRuntimeWiring.Builder queryBuilder(){
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getApis",apiService.getApis());
    }
    private TypeRuntimeWiring.Builder queryApi(){
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getApi", apiService.getApi());
    }

//    private TypeRuntimeWiring.Builder queryApiDetails(){
//        return TypeRuntimeWiring.newTypeWiring("Api")
//                .dataFetcher("apiDetails",apiService.getApiDetails());
//    }
    private TypeRuntimeWiring.Builder queryApiName(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("name",apiService.getApiName());
    }
    private TypeRuntimeWiring.Builder queryApiContext(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("context",apiService.getApiContext());
    }
    private TypeRuntimeWiring.Builder queryApiVersion(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("version",apiService.getApiVersion());
    }
    private TypeRuntimeWiring.Builder queryApiProvider(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("provider",apiService.getApiProvider());
    }
    private TypeRuntimeWiring.Builder queryApiType(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("type",apiService.getApiType());
    }
    private TypeRuntimeWiring.Builder queryApiCreateTime(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("createdTime",apiService.getApiCreatedTime());
    }
    private TypeRuntimeWiring.Builder queryApiUpdateTime(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("lastUpdate",apiService.getApiUpdatedTime());
    }
    private TypeRuntimeWiring.Builder queryApiAdditionalProperties(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("additionalProperties",apiService.getAdditionalProperties());
    }



    private TypeRuntimeWiring.Builder queryApiCount(){
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getApisCount",apiService.getApiCount());
    }

    private TypeRuntimeWiring.Builder queryApiRating(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("rating",apiService.getApiRating());
    }

    private TypeRuntimeWiring.Builder queryTier(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("tierInformation",apiService.getTierInformation());
    }
    private TypeRuntimeWiring.Builder queryTierDetails(){
        return TypeRuntimeWiring.newTypeWiring("Tier")
                .dataFetcher("tierDetails",apiService.getTiers());
    }

    private TypeRuntimeWiring.Builder queryIngressUrl(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("ingressUrl",apiService.getDeploymentEnvironmentName());
    }
    private TypeRuntimeWiring.Builder queryClusterInformation(){
        return TypeRuntimeWiring.newTypeWiring("IngressUrl")
                .dataFetcher("clusterDetails",apiService.getDeploymentClusterInformation());
    }
//    private TypeRuntimeWiring.Builder queryTags(){
//        return TypeRuntimeWiring.newTypeWiring("Api")
//                .dataFetcher("tagList",apiService.getTagsInformatio());
//    }
    private TypeRuntimeWiring.Builder queryOperationInformation(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("operationInformation",apiService.getOperationInformation());
    }
//    private TypeRuntimeWiring.Builder queryUrl(){
//        return TypeRuntimeWiring.newTypeWiring("Api")
//                .dataFetcher("url",apiService.getEndPoint());
//    }
    private TypeRuntimeWiring.Builder queryBusinessInformation(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("businessInformation",apiService.getBusinessInformation());
    }

    private TypeRuntimeWiring.Builder queryLabelInformation(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("label",apiService.getLabelInformation());
    }

    private TypeRuntimeWiring.Builder queryScopeInformation(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("scope",apiService.getScopeInformation());
    }

    private TypeRuntimeWiring.Builder queryAdvertiseInformation(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("advertiseInfo",apiService.getAdvertiseInformation());
    }

    private TypeRuntimeWiring.Builder queryApiEndPointUrlsInformation(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("apiEndPointInformation",apiService.getApiUrlsEndPoint());
    }
    private TypeRuntimeWiring.Builder queryAPIURLsDTO(){
        return TypeRuntimeWiring.newTypeWiring("APIEndpointURLsDTO")
                .dataFetcher("urLs",apiService.getApiUrlsDTO());
    }
    private TypeRuntimeWiring.Builder queryDefaultAPIURLsDTO(){
        return TypeRuntimeWiring.newTypeWiring("APIEndpointURLsDTO")
                .dataFetcher("defaultUrls",apiService.getDefaultApiUrlsDTO());
    }
    private TypeRuntimeWiring.Builder queryPagination(){
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getPagination", apiService.getPagination());
    }
}
