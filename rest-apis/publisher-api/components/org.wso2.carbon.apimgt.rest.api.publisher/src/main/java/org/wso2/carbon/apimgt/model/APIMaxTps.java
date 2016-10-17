package org.wso2.carbon.apimgt.model;




@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class APIMaxTps  {
  
  private Long production = null;
  private Long sandbox = null;

  /**
   **/
  public Long getProduction() {
    return production;
  }
  public void setProduction(Long production) {
    this.production = production;
  }

  /**
   **/
  public Long getSandbox() {
    return sandbox;
  }
  public void setSandbox(Long sandbox) {
    this.sandbox = sandbox;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIMaxTps {\n");
    
    sb.append("  production: ").append(production).append("\n");
    sb.append("  sandbox: ").append(sandbox).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
