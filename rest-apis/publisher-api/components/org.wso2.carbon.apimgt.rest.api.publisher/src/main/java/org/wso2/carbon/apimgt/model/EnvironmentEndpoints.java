package org.wso2.carbon.apimgt.model;




@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class EnvironmentEndpoints  {
  
  private String http = null;
  private String https = null;

  /**
   * HTTP environment URL
   **/
  public String getHttp() {
    return http;
  }
  public void setHttp(String http) {
    this.http = http;
  }

  /**
   * HTTPS environment URL
   **/
  public String getHttps() {
    return https;
  }
  public void setHttps(String https) {
    this.https = https;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class EnvironmentEndpoints {\n");
    
    sb.append("  http: ").append(http).append("\n");
    sb.append("  https: ").append(https).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
