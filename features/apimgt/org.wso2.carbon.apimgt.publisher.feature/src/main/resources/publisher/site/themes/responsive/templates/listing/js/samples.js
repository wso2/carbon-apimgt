function APISamples (defaultTier) {
    this.sample1_swagger = "{\"paths\":{ \"/*\":{\"get\":{\"parameters\":[{\"description\":\"Name of the City\",\"name\":\"q\",\"type\":\"string\",\"required\":false,\"in\":\"query\"}],\"responses\":{\"200\":{ } },\"x-auth-type\":\"Application & Application User\", \"x-throttling-tier\":\"" + defaultTier + "\" }}},\"swagger\":\"2.0\",\"info\":{ \"title\":\"WeatherAPI\",\"version\":\"1.0.0\"}}";
}


APISamples.prototype.deploySample1 = function(defaultTier){
	var formData = new FormData();
	formData.append("action", "sampleDesign");
	formData.append("name", "WeatherAPI");
	formData.append("provider", username);
	formData.append("version", "1.0.0");
	formData.append("description", "The WeatherAPI gives the weather details according to the city name");
	formData.append("tags", "weather");
	formData.append("visibility", "public");
	formData.append("context", "weatherapi");
	formData.append("swagger", this.sample1_swagger);
	formData.append("apiThumb","/site/themes/default/images/weatherAPI.png");
	
	var request = new XMLHttpRequest();
	request.open("POST", siteContext + "/site/blocks/item-design/ajax/add.jag");
	request.send(formData);
         
		jagg.message({
			content:"" ,
		type:"info",
	 	title:"",
	});
        $('#messageModal').modal({backdrop: 'static', keyboard: false });
        $(".modal-header .close").hide();
        $(".modal-footer").html("");
        $(".modal-title").html("Please wait");
        $(".modal-body").addClass("loadingButton");
        $(".modal-body").css({"margin-left":25});
        $(".modal-body").html("The Sample API is being Deployed" );
        $(".modal").css({width:550});

	request.onreadystatechange=function()
		{
			if (request.readyState==4 && request.status==200)
		{
			var urlDesign = '/site/blocks/item-design/ajax/add.jag';
			var dataJSON = '{action:"implement",name:"WeatherAPI",provider:username,version:"1.0.0",provider:"'+username+'",implementation_methods:"endpoint",endpoint_type:"http",endpoint_config:\'{"production_endpoints":{"url":"http://api.openweathermap.org/data/2.5/weather","config":null},"endpoint_type":"http"}\',production_endpoints:"http://api.openweathermap.org/data/2.5/weather",sandbox_endpoints:" ",endpointType:"nonsecured",swagger:this.sample1_swagger}';
			var resultImplement = jagg.post(urlDesign,{action:"implement",name:"WeatherAPI",version:"1.0.0",provider:username,implementation_methods:"endpoint",endpoint_type:"http",endpoint_config:'{"production_endpoints":{"url":"http://api.openweathermap.org/data/2.5/weather","config":null}, "sandbox_endpoints":{"url":"http://api.openweathermap.org/data/2.5/weather","config":null}, "endpoint_type":"http"}',production_endpoints:"http://api.openweathermap.org/data/2.5/weather",sandbox_endpoints:"http://api.openweathermap.org/data/2.5/weather",endpointType:"nonsecured",swagger:this.sample1_swagger},
		
			function(result){
			
			var resultManage = jagg.post(urlDesign,{action:"manage",name:"WeatherAPI",provider:username,version:"1.0.0",default_version_checked:" ",tier:defaultTier,tiersCollection:defaultTier,transport_http:"http",transport_https:"https",swagger:this.sample1_swagger},
			
			function(result){
				if (isPublishPermitted) {
					var urlPublished="/site/blocks/life-cycles/ajax/life-cycles.jag";

					var result2 = jagg.post(urlPublished,{action:"updateStatus",name:"WeatherAPI",version:"1.0.0",provider:username,status:"PUBLISHED",publishToGateway:true,requireResubscription:true},
					function(result){
						if(!result.error){
							window.location.assign(siteContext + "/site/pages/index.jag");
							$(".modal-body").removeClass("loadingButton");
							jagg.message({
 								content:"Sample WeatherAPI is Deployed Successfully" ,
    							type:"info",
     							title:"Success",
							});
						}
				
					},'json');
				} else {
					window.location.assign(siteContext + "/site/pages/index.jag");
					$(".modal-body").removeClass("loadingButton");
					jagg.message({
 						content:"Sample WeatherAPI is Deployed Successfully" ,
    						type:"info",
     						title:"Success",
					});
				}	
			
			},'json');
		
		
		},'json');	
	}
} 
} 

var deploySampleApi = function(defaultTier){
    var deployer = new APISamples(defaultTier);
    deployer.deploySample1(defaultTier);
};
