package org.wso2.carbon.graphql.api.devportal.security;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

@RestController
public class UserController {


    @PostMapping("/login")
    public String Login(@Valid @RequestBody JwtUser user){

       // User user1 = new User();


        RestTemplate restTemplate = new RestTemplate();

        List<JwtUser> users = Arrays.asList(
                new JwtUser("1","ruk","12","admin"),
                new JwtUser("2","tha","1998","admin")
        );
        JwtUser jwtAnonymousUser = new JwtUser("-1", "wso2.anonymous.user","-1","system/wso2.anonymous.role");
        JwtGenerator jwtAnonymousGenerator = new JwtGenerator();
        String valid =jwtAnonymousGenerator.generate(jwtAnonymousUser);
        for (JwtUser other : users ){
            if(other.getUserName().equals(user.getUserName()) & other.getPassword().equals(user.getPassword())){
               JwtGenerator jwtGenerator = new JwtGenerator();
                valid = jwtGenerator.generate(user);
            }

        }

        return valid;


    }



}
