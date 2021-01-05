'use strict';

var utils = require('../utils/writer.js');
var Default = require('../service/DefaultService');

module.exports.apiAmPublisherV1SettingsGET = function apiAmPublisherV1SettingsGET (req, res, next) {
  Default.apiAmPublisherV1SettingsGET()
    .then(function (response) {
      utils.writeJson(res, response);
    })
    .catch(function (response) {
      utils.writeJson(res, response);
    });
};

module.exports.apiAmPublisherV1Swagger_yamlGET = function apiAmPublisherV1Swagger_yamlGET (req, res, next) {
  Default.apiAmPublisherV1Swagger_yamlGET()
    .then(function (response) {
      utils.writeJson(res, response);
    })
    .catch(function (response) {
      utils.writeJson(res, response);
    });
};

module.exports.testGET = function testGET (req, res, next) {
  Default.testGET()
    .then(function (response) {
      utils.writeJson(res, response);
    })
    .catch(function (response) {
      utils.writeJson(res, response);
    });
};
