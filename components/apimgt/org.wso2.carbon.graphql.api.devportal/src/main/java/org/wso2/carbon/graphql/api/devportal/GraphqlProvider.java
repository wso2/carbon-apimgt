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

    @Autowired
    TagSevice tagSevice;




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
                .type(queryAllApis())
                .type(queryApi())
                .type(queryApiTags())
                .type(ApiTypeDataFetcher())
                .type(ApiCreateTimeDataFetcher())
                .type(ApiUpdateTimeDataFetcher())
                .type(ApiDefinitionDataFetcher())
                .type(queryApiCount())
                .type(ApiRatingDataFetcher())
                .type(OperationInformationDataFetcher())
                .type(TierNamesDataFetcher())
                .type(TierDetailsDataFetcher())
                .type(MonetizationLabelDataFetcher())
                .type(IngressUrlDataFetcher())
                .type(ClusterInformationDataFetcher())
                .type(BusinessInformationDataFetcher())
                .type(LabelInformationDataFetcher())
                .type(LabelDetailsDataFetcher())
                .type(ScopeInformationDataFetcher())
                .type(AdvertiseInformationDataFetcher())
                .type(ApiEndPointUrlsInformationDataFetcher())
                .type(APIURLsDataFetcher())
                .type(DefaultAPIURLsDataFetcher())
                .type(PaginationDataFetcher())
                .build();
    }


    private TypeRuntimeWiring.Builder queryAllApis(){
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getApis",apiService.getApisFromArtifact());
    }
    private TypeRuntimeWiring.Builder queryApi(){
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getApi", apiService.getApiFromArtifact());
    }
    private TypeRuntimeWiring.Builder queryApiCount(){
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getApisCount",apiService.getApiCount());
    }
    private TypeRuntimeWiring.Builder queryApiTags(){
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getTags", tagSevice.getTagsData());
    }
    private TypeRuntimeWiring.Builder ApiTypeDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("type",apiService.getApiType());
    }
    private TypeRuntimeWiring.Builder ApiCreateTimeDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("createdTime",apiService.getApiCreatedTime());
    }
    private TypeRuntimeWiring.Builder ApiUpdateTimeDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("lastUpdate",apiService.getApiUpdatedTime());
    }
    private TypeRuntimeWiring.Builder ApiDefinitionDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("apiDefinition",apiService.getApiDefinition());
    }

    private TypeRuntimeWiring.Builder ApiRatingDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("rating",apiService.getApiRating());
    }

    private TypeRuntimeWiring.Builder TierNamesDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("tierInformation",apiService.getTierNames());
    }
    private TypeRuntimeWiring.Builder TierDetailsDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Tier")
                .dataFetcher("tierDetails",apiService.getTierDetails());
    }
    private TypeRuntimeWiring.Builder MonetizationLabelDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("monetizationLabel",apiService.getMonetizationLabel());
    }

    private TypeRuntimeWiring.Builder IngressUrlDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("ingressUrl",apiService.getDeploymentEnvironmentName());
    }
    private TypeRuntimeWiring.Builder ClusterInformationDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("IngressUrl")
                .dataFetcher("clusterDetails",apiService.getDeploymentClusterInformation());
    }
    private TypeRuntimeWiring.Builder OperationInformationDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("operationInformation",apiService.getOperationInformation());
    }
    private TypeRuntimeWiring.Builder BusinessInformationDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("businessInformation",apiService.getBusinessInformation());
    }
    private TypeRuntimeWiring.Builder LabelInformationDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("label",apiService.getLabelInformation());
    }
    private TypeRuntimeWiring.Builder LabelDetailsDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Label")
                .dataFetcher("labelDetails",apiService.getLabelsDetails());
    }
    private TypeRuntimeWiring.Builder ScopeInformationDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("scope",apiService.getScopeInformation());
    }
    private TypeRuntimeWiring.Builder AdvertiseInformationDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("advertiseInfo",apiService.getAdvertiseInformation());
    }

    private TypeRuntimeWiring.Builder ApiEndPointUrlsInformationDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Api")
                .dataFetcher("apiEndPointInformation",apiService.getApiUrlsEndPoint());
    }
    private TypeRuntimeWiring.Builder APIURLsDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("APIEndpointURLsDTO")
                .dataFetcher("urLs",apiService.getApiUrlsDTO());
    }
    private TypeRuntimeWiring.Builder DefaultAPIURLsDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("APIEndpointURLsDTO")
                .dataFetcher("defaultUrls",apiService.getDefaultApiUrlsDTO());
    }
    private TypeRuntimeWiring.Builder PaginationDataFetcher(){
        return TypeRuntimeWiring.newTypeWiring("Query")
                .dataFetcher("getPagination", apiService.getPagination());
    }
}
