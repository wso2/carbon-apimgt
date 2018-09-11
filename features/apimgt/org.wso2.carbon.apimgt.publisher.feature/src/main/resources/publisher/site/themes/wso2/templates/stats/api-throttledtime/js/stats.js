var t_on = {
    'apiChart': 1,
    'subsChart': 1,
    'serviceTimeChart': 1,
    'tempLoadingSpace': 1
};
var currentLocation;

var dataTable;

var chartColorScheme1 = ["#3da0ea", "#bacf0b", "#e7912a", "#4ec9ce", "#f377ab", "#ec7337", "#bacf0b", "#f377ab", "#3da0ea", "#e7912a", "#bacf0b"];
//fault colors || shades of red
var chartColorScheme2 = ["#ED2939", "#E0115F", "#E62020", "#F2003C", "#ED1C24", "#CE2029", "#B31B1B", "#990000", "#800000", "#B22222", "#DA2C43"];
//fault colors || shades of blue
var chartColorScheme3 = ["#0099CC", "#436EEE", "#82CFFD", "#33A1C9", "#8DB6CD", "#60AFFE", "#7AA9DD", "#104E8B", "#7EB6FF", "#4981CE", "#2E37FE"];
currentLocation = window.location.pathname;
var statsEnabled = isDataPublishingEnabled();

require(["dojo/dom", "dojo/domReady!"], function (dom) {
    currentLocation = window.location.pathname;
    //Initiating the fake progress bar
    jagg.fillProgress('apiChart');
    jagg.fillProgress('subsChart');
    jagg.fillProgress('serviceTimeChart');
    jagg.fillProgress('tempLoadingSpace');

    jagg.post("/site/blocks/stats/api-throttledtime/ajax/stats.jag", { action: "getFirstAccessTime", currentLocation: currentLocation  },
        function (json) {

            if (!json.error) {

                if (json.usage && json.usage.length > 0) {
                    var d = new Date();
                    var firstAccessDay = new Date(json.usage[0].year, json.usage[0].month - 1, json.usage[0].day);
                    var currentDay = new Date(d.getFullYear(), d.getMonth(), d.getDate(),d.getHours(),d.getMinutes());


                        //day picker
                        $('#today-btn').on('click',function(){
                            var to = convertTimeString(currentDay);
                            var from = convertTimeString(currentDay-86400000);
                            var dateStr= from+" to "+to;
                            $("#date-range").html(dateStr);
                            $('#date-range').data('dateRangePicker').setDateRange(from,to);
                            drawAPIsTable(from,to);

                        });

                        //hour picker
                        $('#hour-btn').on('click',function(){
                            var to = convertTimeString(currentDay);
                            var from = convertTimeString(currentDay-3600000);
                            var dateStr= from+" to "+to;
                            $("#date-range").html(dateStr);
                            $('#date-range').data('dateRangePicker').setDateRange(from,to);
                            drawAPIsTable(from,to);
                        })

                        //week picker
                        $('#week-btn').on('click',function(){
                            var to = convertTimeString(currentDay);
                            var from = convertTimeString(currentDay-604800000);
                            var dateStr= from+" to "+to;
                            $("#date-range").html(dateStr);
                            $('#date-range').data('dateRangePicker').setDateRange(from,to);
                            drawAPIsTable(from,to);
                        })

                        //month picker
                        $('#month-btn').on('click',function(){

                            var to = convertTimeString(currentDay);
                            var from = convertTimeString(currentDay-(604800000*4));
                            var dateStr= from+" to "+to;
                            $("#date-range").html(dateStr);
                            $('#date-range').data('dateRangePicker').setDateRange(from,to);
                            drawAPIsTable(from,to);
                        });

                        //date picker
                        $('#date-range').dateRangePicker(
                            {
                                startOfWeek: 'monday',
                                separator : ' to ',
                                format: 'YYYY-MM-DD HH:mm',
                                autoClose: false,
                                time: {
                                    enabled: true
                                },
                                shortcuts:'hide',
                                endDate:currentDay
                            })
                            .bind('datepicker-apply',function(event,obj)
                            {
                                 btnActiveToggle(this);
                                 var from = convertDate(obj.date1);
                                 var to = convertDate(obj.date2);
                                 $('#date-range').html(from + " to "+ to);
                                 drawAPIsTable(from,to);
                            });

                        //setting default date
                        var to = new Date();
                        var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

                        $('#date-range').data('dateRangePicker').setDateRange(from,to);
                        $('#date-range').html($('#date-range').val());
                        var fromStr = convertDate(from);
                        var toStr = convertDate(to);
                        drawAPIsTable(fromStr,toStr);


                        $('#date-range').click(function (event) {
                        event.stopPropagation();
                        });

                        $('body').on('click', '.btn-group button', function (e) {
                            $(this).addClass('active');
                            $(this).siblings().removeClass('active');
                        });

                    var width = $("#rangeSliderWrapper").width();
                    //$("#rangeSliderWrapper").affix();
                    $("#rangeSliderWrapper").width(width);
                }

                else if (json.usage && json.usage.length == 0 && statsEnabled) {
                    $('#middle').html("");
                    $('#middle').append($('<div class="errorWrapper"><img src="../themes/wso2/images/statsEnabledThumb.png" alt="'+ i18n.t('Thumbnail image when stats are enabled') + '"></div>'));
                }

                else {
                    $('#middle').html("");
                    $('#middle').append($('<div class="errorWrapper"><span class="label top-level-warning"><i class="icon-warning-sign icon-white" title="Stats-not-configured"></i>' +
                        '<a href=\'https://docs.wso2.com/display/AM260/Configuring+APIM+Analytics\' target=\'_blank\'' +
		       	        'title=' + i18n.t('Refer our documentation to correctly configure API Manager Analytics') + ' class=\'warningLink\'>' +
	                    i18n.t('Refer our documentation to correctly configure API Manager Analytics') + '</a></span><br/>' +
			            '<img src="../themes/wso2/images/statsThumb.png" alt="'+ i18n.t('Thumbnail image when stats are not configured') + '"></div>'));
                }
            }
            else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
            t_on['apiChart'] = 0;
        }, "json");

});

var drawAPIsTable = function(from,to){   
    var fromDate = from;
    var toDate = to;     
    jagg.post("/site/blocks/stats/api-throttledtime/ajax/stats.jag", { action:"getAPIsFromAPIRequestsPerHourTable",fromDate:fromDate,toDate:toDate},
    function(json){
    	 var $dataTables = $('<table class="display defaultTable"  style="background-color:transparent"	width="100%" cellspacing="0" id="apiSelectTable"></table>');
                        $dataTables.append($('<thead class="tableHead" style="background-color:#C1DAD7" ><tr>' +
                            '<th width="10%"></th>' +
                    	        '<th>' + i18n.t('API') + '</th>' +
                            '<th width="10%" style="text-transform:none;text-align:left">Selected</th>'+
                            '</tr></thead>'));


		var chartData=[];
		var state_array = [];
		var defaultFilterValues=[];
		var filterValues=[];

		for (var i = 0; i < json.usage.length; i++) {
			chartData.push({"label":json.usage[i].apiName})
		}
		chartData.sort(function(obj1, obj2) {
			return obj2.value - obj1.value;
		});

                        //default display of 15 checked entries on table
                        for (var i = 0; i < chartData.length; i++) {
                            if(i<15){
                                $dataTables.append($('<tr><td style="text-align:left;"><label for=' + i + '>' + chartData[i].label + '</label></td>'
                                    + '<td><input name="item_checkbox" style="float:right; margin-right:2px" checked   id=' + i + '  type="radio"  data-item=' + chartData[i].label
                                    + ' class="inputCheckbox" /></td></tr>'));
                        
                            } else {

                                $dataTables.append($('<tr><td >'
                                     + '<input name="item_checkbox" id=' + i + '  type="radio"  data-item=' + chartData[i].label
                                     + ' class="inputCheckbox" />'
                                     + '</td><td style="text-align:left;"><label for=' + i + '>' + chartData[i].label + '</label></td>'
                                     + '<td style="text-align:right;"><label for=' + i + '>' + chartData[i].value + '</label></td></tr>'));

                            }
                        }
                        $('#tableContainer').empty();
                        $('#tableContainer').append($dataTables);
    					$('#tableContainer').show();
    					for(var i = 0 ; i < chartData.length; i++ ){
 							if(document.getElementById(''+i).checked){
 								drawThrottledOutTime(from,to,chartData[i].label);
 							}
 						}
    					$('input:radio').on('change', function(){
 						for(var i = 0 ; i < chartData.length; i++ ){
 							if(document.getElementById(''+i).checked){
 								drawThrottledOutTime(from,to,chartData[i].label);
 							}
 						}
});
    						
    },'json');
    

}

function  drawThrottledOutTime(from,to,api){
	dataTable = null ;
    var fromDate = from;
    var toDate = to;
    var api = api;
    jagg.post("/site/blocks/stats/api-throttledtime/ajax/stats.jag", { action:"getAPIRequestsPerHour",fromDate:fromDate,toDate:toDate,api:api},
    function(json){
    	var width = document.getElementById("bar").offsetWidth; //canvas width
    	var height = 270;
    	
       	var config = {
        	"title": "",
        	"yAxis": [1,2,3,4,5,6,7,8],
        	"xAxis": 0,
        	"pointLabel": 0,
        	"width": 700,
        	"height": height,
        	"padding": 60,
        	"interpolationMode":"linear",
        	"chartType": "area"
   		}

	var nameDataTableJSONText = '{"metadata":{"names":["Time",';
	var graphsDataTableJSONTEXT = "";
	var typesDataTableJSONText = '"types":["T",';
	
	var tier1 = (res.tiers[0].tierDescription).split(" ")[1];
	var tier2 = (res.tiers[1].tierDescription).split(" ")[1];
	var tier3 = (res.tiers[2].tierDescription).split(" ")[1];

	
	for(var i = 0 ; i < (res.tiers.length) ; i++){
		nameDataTableJSONText = nameDataTableJSONText + "\""+res.tiers[i].tierName+"\",";
		if(i!=(res.tiers.length-1)){
			graphsDataTableJSONTEXT = graphsDataTableJSONTEXT + "\"Requests/min - "+res.tiers[i].tierName+" Tier\",";
			typesDataTableJSONText = typesDataTableJSONText + "\"N\",\"N\",";
		}
		else{
			graphsDataTableJSONTEXT = graphsDataTableJSONTEXT + "\"Requests/min - "+res.tiers[i].tierName+" Tier\"],";
			typesDataTableJSONText = typesDataTableJSONText + "\"N\",\"N\"]},\"data\":";
		}
		
	}
		dataTableJSONText = nameDataTableJSONText+""+graphsDataTableJSONTEXT+""+typesDataTableJSONText
		console.log(dataTableJSONText);
  	var data = "[[";
  	var array=[] ;
  	var isMultipleTiers = false ; 	
  	for(var i = 0 ; i <  json.usage.length-1; i++){
  		var j = 0 ;
  		for(var k = i ; k < json.usage.length-1 ; k++){
  			if((json.usage[k].Date).search(json.usage[k+1].Date)==-1){
  				break;
  			}
  			isMultipleTiers = true ; 
  			array[j] = json.usage[k];
  			array[j+1] = json.usage[k+1];
  			i++;
  			j++;
  		}
  		if(isMultipleTiers){
  			j=0;
  			isMultipleTiers = false;
  			data = data+"\""+json.usage[i].Date+"\""+","+tier1+","+tier2+","+tier3+",0,"+decodeData(array,res)+"]";
  			data=data+",[";
  		}else{
  			array[0]= json.usage[i];
  			data = data+"\""+json.usage[i].Date+"\""+","+tier1+","+tier2+","+tier3+",0,"+decodeData(array,res)+"]";
  			data=data+",[";
  		}
  		
  	 }
  	 console.log(data.substring(0,data.lastIndexOf(",")));
  	 data = data.substring(0,data.lastIndexOf(","));
  	if(json.usage.length==0){
  		data = data+"\"\",\"\"]";
  	}
  	data=data+"]}";
  	dataTableJSONText=dataTableJSONText+""+data;
  	dataTable = JSON.parse(dataTableJSONText);
  	chart=igviz.setUp("#bar",config,dataTable);
  	chart.setXAxis({"zero":false})
	chart.plot(dataTable.data);             
    
    }
    , "json"); 
    
}

function decodeData(array,res){
	var data = "";
	var isMatched = false;
	for(var i = 0 ; i < res.tiers.length ; i++) {
		for(var j = 0 ; j < array.length ; j++){
			if((array[j].tier).search(res.tiers[i].tierName)!=-1){
				data = data + "" + array[j].request_count+",";
				isMatched = true ;
			}
		}
		if(!isMatched){
			data = data + "0,";
		}
		isMatched = false;
	}
	return data.substring(0,data.lastIndexOf(","));
}

