package org.wso2.carbon.apimgt.model;

import org.wso2.carbon.apimgt.model.Environment;
import java.util.*;



@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class EnvironmentList  {
  
  private Integer count = null;
  private List<Environment> list = new ArrayList<Environment>();

  /**
   * Number of Environments returned.

   **/
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   **/
  public List<Environment> getList() {
    return list;
  }
  public void setList(List<Environment> list) {
    this.list = list;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class EnvironmentList {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  list: ").append(list).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
