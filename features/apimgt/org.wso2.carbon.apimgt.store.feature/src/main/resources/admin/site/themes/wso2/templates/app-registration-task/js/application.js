$(document).ready(function(){
    $('.js_startBtn').click(function(){
        var btn = $(this);
        var taskId=btn.attr("data");
        btn.attr("disabled","disabled");
        var iteration=btn.attr("iteration");
        jagg.post("/site/blocks/task-manager/ajax/task.jag", { action:"startTask",taskId:taskId,taskType:"appRegistration" },
            function (json) {
                if (!json.error) {
                	btn.parent().next().show();
                    $('#js_completeBtn'+iteration).show();
                    btn.parent().remove();
                    $('#status'+iteration).text("IN_PROGRESS");
                } else {
                    jagg.showLogin();
                }
            }, "json");

    }).removeAttr("disabled","disabled");

    $('.js_completeBtn').click(function(){
        var btn = $(this);
        var taskId=btn.attr("data");
        var iteration=btn.attr("iteration");
        var description=$('#desc'+iteration).text();
        var status=$('#js_stateDropDown'+iteration).val();
        btn.attr("disabled","disabled");
        jagg.post("/site/blocks/task-manager/ajax/task.jag", { action:"completeTask",status:status,taskId:taskId,taskType:"appRegistration",description:description },
            function (json) {
                if (!json.error) {
                    btn.next().show();
                    btn.next().next().html(json.msg);
                    btn.hide();
                    window.location.reload();
                } else {
                    jagg.showLogin();
                }
            }, "json");

    }).removeAttr("disabled","disabled");

    $('.js_assignBtn').click(function(){
        var btn = $(this);
        var taskId=btn.attr("data");
        var iteration=btn.attr("iteration");
        btn.attr("disabled","disabled");
        jagg.post("/site/blocks/task-manager/ajax/task.jag", { action:"assignTask",taskId:taskId,taskType:"appRegistration" },
            function (json) {
                if (!json.error) {
                    btn.next().show();
                    $('#js_startBtn'+iteration).show();
                    btn.remove();
                    $('#status'+iteration).text("RESERVED");
                } else {
                    jagg.showLogin();
                }
            }, "json");
    }).removeAttr("disabled","disabled");
    
    /***********************************************************
     *  data-tables config
     ***********************************************************/
	$('#appreg-tasks').datatables_extended({
	     "fnDrawCallback": function(){
	       if(this.fnSettings().fnRecordsDisplay()<=$("#appreg-tasks_length option:selected" ).val()
	     || $("#appreg-tasks option:selected" ).val()==-1)
	       $('#appreg-tasks_paginate').hide();
	       else $('#appreg-tasks_paginate').show();
	     } ,
         "aoColumns": [
         null,
         null,
         null,
         null,
         { "bSortable": false }
         ]
	});

});
