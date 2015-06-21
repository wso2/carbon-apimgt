/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
var name;
var hps = require('/themes/default/helpers/list-assets.js');
var that = this;
/*
 In order to inherit all variables in the default helper
 */
for (name in hps) {
	if (hps.hasOwnProperty(name)) {
		that[name] = hps[name];
	}
}
var fn = that.resources;
var resources = function(page, meta) {
	var o = fn(page, meta);
	if (!o.css) {
		o.css = [];
	}
	o.css.push('styles.css');
	o.css.push('assets.css');
	o.js.push('jquery/jquery.event.mousestop.js');
	o.js.push('jquery/jquery.history.js');
	o.js.push('assets.js');
	o.js.push('asset-helpers.js');
	return o;
};

var format = function(data){
	var unixtime, newDate,
		newDate = new Date(),
		artifacts = data.artifacts;
	for(var i in artifacts){
		unixtime = artifacts[i].attributes['overview_createdtime'];
		newDate.setTime(unixtime);
		data.artifacts[i].attributes['overview_createdtime'] = newDate.toUTCString();
	}
	return data;
}
