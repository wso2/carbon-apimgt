import org.wso2.carbon.apimgt.ballerina.publisher;

function publishData () {

    json event = {};
    event.streamName= "org.wso2.apimgt.statistics.fault";
    event.streamVersion= "1.0.0";

    json metaData = ["clientType"];
    json correlationData = [];
    json payloadData = ["testconsumerid", "/test", "1.0.0", "Test API",
                        "customer/{id}", "GET", "1.0.0",
                        "2323", "Backend Error", 9007199254740992,
                        "testuser", "carbon.super", "localhost",
                        "apiPublisher" , "TestApp" , "1",
                        "http"];

    event.metaData = metaData;
    event.correlationData = correlationData;
    event.payloadData = payloadData;

    map propertiesMap = {
                        "type":"thrift",
                        "receiverURLSet":"tcp://localhost:7614",
                        "authURLSet":"ssl://localhost:7714",
                        "username":"admin",
                        "password":"admin",
                        "configPath":"bre/conf/data.agent.config.yaml"
                        };

    publisher:EventConnector das = create publisher:EventConnector(propertiesMap);
    publisher:EventConnector.publish(das, event);
}
