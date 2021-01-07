'use strict';
var fs = require('fs');

/**
 * Returns settings
 * Returns settings
 *
 * no response value expected for this operation
 **/
exports.apiAmPublisherV1SettingsGET = function() {
  return new Promise(function(resolve, reject) {
    try {
      let fileContents = fs.readFileSync('service/settings', 'utf8');
      resolve(fileContents);
    } catch (e) {
      console.log(e);
    } 
  });
}


/**
 * Returns swagger.yaml
 * Returns swagger.yaml
 *
 * no response value expected for this operation
 **/
exports.apiAmPublisherV1Swagger_yamlGET = function() {
  return new Promise(function(resolve, reject) {
    try {
      let fileContents = fs.readFileSync('service/swagger.yaml', 'utf8');
      let data = yaml.safeLoad(fileContents);
      let data1 = yaml.safeDump(data);
      //console.log(data1);
      resolve(data1);
    } catch (e) {
      console.log(e);
    }
 
  });
}


/**
 * Returns swagger.yaml
 * Returns swagger.yaml
 *
 * no response value expected for this operation
 **/
exports.testGET = function() {
  return new Promise(function(resolve, reject) {
    resolve();
  });
}

