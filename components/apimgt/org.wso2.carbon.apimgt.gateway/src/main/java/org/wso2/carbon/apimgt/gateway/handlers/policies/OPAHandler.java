package org.wso2.carbon.apimgt.gateway.handlers.policies;

import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.dto.OPADto;

public class OPAHandler extends AbstractHandler  {


    private String apiSecurity;
    OPADto opaDto;

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        String[] apiSecurityLevels = apiSecurity.split(",");

        opaDto = new OPADto(
                messageContext.getConfiguration().getLocalRegistry().toString(),
                apiSecurityLevels,
                messageContext.getProperty("SYNAPSE_REST_API").toString(),
                messageContext.getProperty("SYNAPSE_REST_API_VERSION").toString(),
                messageContext.getProperty("REST_API_CONTEXT").toString(),
                messageContext.getProperty("REST_FULL_REQUEST_PATH").toString(),
                messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD).toString(),
                messageContext.getProperty(APIMgtGatewayConstants.API_TYPE).toString(),
                messageContext.getProperty("ARTIFACT_NAME").toString()
        );
        return false;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return false;
    }

//    private final CloseableHttpClient httpClient = HttpClients.createDefault();
//
//    public void close() throws Exception{
//        httpClient.close();
//    }
//
//    private void sendGet() throws Exception{
//        HttpGet request = new HttpGet("http://0.0.0.0:8181/v1/policies");
//        try(CloseableHttpResponse response = httpClient.execute(request)){
//            String print_response = response.getStatusLine().toString();
//
//            HttpEntity entity = response.getEntity();
//            if(response.getEntity().getContentLength() != 0){
//                StringBuilder sb = new StringBuilder();
//                try{
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()),65728);
//                    String line = null;
//
//                    while ((line = reader.readLine()) != null) {
//                        sb.append(line);
//                    }
//                }
//                catch (IOException e){ e.printStackTrace(); }
//                catch (Exception e) { e.printStackTrace(); }
//                print_response = "final string" + sb.toString();
//            }
//
////            try{
////                jsonString = obj.writeValueAsString(opaDto);
////            }
////            catch (IOException e){
////                e.printStackTrace();
////            }
//
//            if(entity != null){
//                String result = EntityUtils.toString(entity);
//                System.out.println(print_response);
//            }
//        }
//    }
//
//    private  void sendPost(){
//        System.out.println("im post");
//
//    }
//
//
//    private OPADto opaDto;
//    private String jsonString;


}
