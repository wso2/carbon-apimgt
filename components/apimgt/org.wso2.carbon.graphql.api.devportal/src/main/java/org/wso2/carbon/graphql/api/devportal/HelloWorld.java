package org.wso2.carbon.graphql.api.devportal;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//import org.wso2.carbon.apimgt.impl.utils.APIUtil;


@RestController
public class HelloWorld {


    @RequestMapping("/")
    public String Hello(){



        return "Hello World";
    }
}
