function APISamples (defaultTier) {
    this.sample1_swagger = "{\"paths\":{\"/add\":{\"get\":{\"summary\":\"add x and y\", " +
	"\"x-auth-type\":\"Application & Application User\",\"x-throttling-tier\":\"Unlimited\", " +
	"\"produces\":\"application/json\",\"parameters\":[{\"name\":\"x\",\"required\":true, " +
	"\"type\":\"string\",\"in\":\"query\"},{\"name\":\"y\",\"required\":true,\"type\": " +
	"\"string\",\"in\":\"query\"}],\"responses\":{\"200\":{}}}},\"/subtract\":{\"get\":{ " +
	"\"summary\":\"subtract y from x\",\"x-auth-type\":\"Application & Application User\", " +
	"\"x-throttling-tier\":\"Unlimited\",\"produces\":\"application/json\",\"parameters\":[{ " +
	"\"name\":\"x\",\"required\":true,\"type\":\"string\",\"in\":\"query\"},{\"name\":\"y\", " +
	"\"required\":true,\"type\":\"string\",\"in\":\"query\"}],\"responses\":{\"200\":{}}}}," +
	"\"/multiply\":{\"get\":{\"summary\": \"multiply x by y\",\"x-auth-type\":" +
	"\"Application & Application User\", \"x-throttling-tier\":\"Unlimited\",\"produces\":" +
	"\"application/json\",\"parameters\":[{ \"name\":\"x\",\"required\":true,\"type\":" +
	"\"string\",\"in\":\"query\"},{\"name\":\"y\", \"required\":true,\"type\":\"string\"," +
	"\"in\":\"query\"}],\"responses\":{\"200\":{}}}}, \"/divide\":{\"get\":{\"summary\":" +
	"\"divide x by y\",\"x-auth-type\": \"Application & Application User\"," +
	"\"x-throttling-tier\":\"Unlimited\",\"produces\": \"application/json\",\"parameters\":[{ " +
	"\"name\":\"x\",\"required\":true,\"type\": \"string\",\"in\":\"query\"},{\"name\":\"y\", " +
	"\"required\":true,\"type\":\"string\",\"in\": \"query\"}],\"responses\":{\"200\":{}}}}}, " +
	"\"swagger\":\"2.0\",\"info\":{\"title\": \"Calculator\",\"description\": " +
	"\"Simple calculator API to perform addition, subtraction, multiplication and division.\", " +
	"\"version\":\"1.0\"},\"basePath\":\"/calc/1.0\",\"host\":\"localhost:8243\"}";
}


APISamples.prototype.deploySample1 = function(defaultTier){
	var formData = new FormData();
	formData.append("action", "sampleDesign");
	formData.append("name", "CalculatorAPI");
	formData.append("provider", username);
	formData.append("version", "1.0");
	formData.append("description", "Simple calculator API to perform addition, subtraction, multiplication and division.");
	formData.append("tags", "calculator");
	formData.append("visibility", "public");
	formData.append("context", "calc");
	formData.append("swagger", this.sample1_swagger);
	formData.append("apiThumb","/site/themes/default/images/calculatorAPI.png");
	
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
			var resultImplement = jagg.post(urlDesign,{action: "implement", name: "CalculatorAPI", version: "1.0",
			provider: username,  implementation_methods: "endpoint", endpoint_type: "http",
			endpoint_config: '{"production_endpoints":{"url": ' +
			'"http://localhost:9763/apimgt-calculator-api/api","config":null}, ' +
			'"sandbox_endpoints":{"url":"http://localhost:9763/apimgt-calculator-api/api", ' +
			'"config":null}, "endpoint_type":"http"}', production_endpoints:
			"http://localhost:9763/apimgt-calculator-api/api", sandbox_endpoints:
			"http://localhost:9763/apimgt-calculator-api/api", endpointType: "nonsecured",
			swagger: this.sample1_swagger},
			function(result){
				var resultManage = jagg.post(urlDesign,{action: "manage", name: "CalculatorAPI",
				provider: username, version: "1.0",
				default_version_checked: " ", tier: defaultTier,
				tiersCollection: defaultTier, transport_http: "http",
				transport_https: "https", swagger: this.sample1_swagger},

				function(result){
					if (isPublishPermitted) {
						var urlPublished="/site/blocks/life-cycles/ajax/life-cycles.jag";

						var result2 = jagg.post(urlPublished,{action: "updateStatus",
							name: "CalculatorAPI", version: "1.0", provider: username,
							status: "Publish", publishToGateway: true, requireResubscription: true},
						function(result){
							if(!result.error){
								window.location.assign(siteContext + "/site/pages/index.jag");
								$(".modal-body").removeClass("loadingButton");
								jagg.message({
									content:"Sample CalculatorAPI is Deployed Successfully" ,
									type:"info",
									title:"Success",
								});
							}

						},'json');
					} else {
						window.location.assign(siteContext + "/site/pages/index.jag");
						$(".modal-body").removeClass("loadingButton");
						jagg.message({
							content:"Sample CalculatorAPI is Deployed Successfully" ,
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
