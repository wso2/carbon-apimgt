$(document).ready(function(){

        function developerFilter(){
        jagg.post("/site/blocks/stats/developers-list/ajax/stats.jag",
            {
              //  "fromDate": $('#date-range').data('daterangepicker').startDate.format('YYYY-MM-DD'),
              //  "toDate": $('#date-range').data('daterangepicker').endDate.format('YYYY-MM-DD')
            },
            function (json) {
            if (!json.error) {
            var developerName = '';
                for (var i = 0; i < json.data.length; i++) {
                    developerName += '<option>'+ json.data[i].userId+'</option>'
                }
                $('#developerSelect')
                   .append(developerName)
                   .selectpicker('refresh');

                $('#developerSelect').on('change', function() {
                    console.log(this.value);//selected value
                });
            }
            else {
                    if (json.message == "AuthenticateError") {
                        jagg.showLogin();
                    } else {
                        jagg.message({content: json.message, type: "error"});
                    }
                 }
        }, "json");

        }

        function apiFilter(){

        jagg.post("/site/blocks/stats/apis-list/ajax/stats.jag",
            {
                //"fromDate": $('#date-range').data('daterangepicker').startDate.format('YYYY-MM-DD'),
                //"toDate": $('#date-range').data('daterangepicker').endDate.format('YYYY-MM-DD')
            },
            function (json) {
            if (!json.error) {
            var  apiName = '';

                for ( var i=0; i< json.data.length ; i++){
                    apiName += '<option>'+ json.data[i].apiName+'</option>'
                }

                $('#apiSelect')
                   .append(apiName)
                   .selectpicker('refresh');

                $('#apiSelect').on('change', function() {
                    console.log(this.value);//selected value
                });

            }
            else {
                    if (json.message == "AuthenticateError") {
                        jagg.showLogin();
                    } else {
                        jagg.message({content: json.message, type: "error"});
                    }
                 }
        }, "json");
        }

        function appFilter(){
        jagg.post("/site/blocks/stats/applications-list/ajax/stats.jag",
            {
              //  "fromDate": $('#date-range').data('daterangepicker').startDate.format('YYYY-MM-DD'),
              //  "toDate": $('#date-range').data('daterangepicker').endDate.format('YYYY-MM-DD')
            },
            function (json) {
            if (!json.error) {
            var  appName = '';
                console.log("apps",json.data);
                for ( var i=0; i< json.data.length ; i++){
                    appName += '<option>'+ json.data[i].name+'</option>'
                }

                $('#appSelect')
                   .append(appName)
                   .selectpicker('refresh');

                $('#appSelect').on('change', function() {
                    console.log(this.value);//selected value
                });

            }
            else {
                    if (json.message == "AuthenticateError") {
                        jagg.showLogin();
                    } else {
                        jagg.message({content: json.message, type: "error"});
                    }
                 }
        }, "json");
        }

        if($('#appSelect').length != 0){
            appFilter();
        }if($('#apiSelect').length != 0){
            apiFilter();
        }if($('#developerSelect').length != 0){
            developerFilter();
        }

});