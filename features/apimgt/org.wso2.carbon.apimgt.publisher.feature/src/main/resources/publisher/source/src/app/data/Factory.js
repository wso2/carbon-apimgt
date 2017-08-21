/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
"use strict";
import ConfigManager from './ConfigManager'
import SingleClient from './SingleClient'

class Factory {

       constructor(environment){
           this.map = new Map();
           this.configmanager = new ConfigManager();
           this.currentenv = environment;
           // if (this.map.has(environment) == 'True'){
           //     return this.map.get(environment)
           // } else {
               this.configmanager.env_response.then((response) => {
                   let allenvs = response.data.environments;
                   if (this.currentenv == "default") {
                       this.host = window.location.host;
                       this.port = window.location.port;
                   } else {
                       let environmentValue;
                       for (let value of allenvs) {

                           if (this.currentenv == value.env) {

                               environmentValue = value;

                           }
                       }
                       this.host = environmentValue.envIsHost;
                       this.port = environmentValue.envIsPort;
                   }
                    this.instance = new SingleClient(this.host,this.port);
                   console.log(this.instance.client)
                  // this.map.set(environment, instance)
                   return this.instance.client;
               });

       }

    static factoryCheck (environment){
       let configurationManager = new ConfigManager();
     let value = configurationManager.env_response.then((response)=> {
           let allenvs = response.data.environments;
           let environmentValue;
           for (let value of allenvs) {

               if (environment == value.env) {

                   environmentValue = value;

               }
           }
           let host = environmentValue.envIsHost;
           let port = environmentValue.envIsPort;

           let instance = new SingleClient(host,port);
           return instance.client;
        });

     return value;
    }


}

export default Factory;
