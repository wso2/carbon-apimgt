function forum_load_topics(page, search) {
    var params = {};

	params.parentId = parentId;

    if (page == undefined) {
        page = 1
    }
    params.page = page;
    
	if (search != undefined) {
        params.search = search;
    }

    $.getJSON(requestURL + '/forum/api/topic', params, function (result) {
        console.log(result);
        if (result.error == false) {
			// Don't show paginator if topics list has only one page.
			var showPaginator = false;	
			if(result.total_pages  > 1){
				showPaginator = true;
			}

            var template = Handlebars.partials['topic_list']({ 'topics': result.data });
            $('#forum_topic_list').html(template);

			var paginatorTemplate = Handlebars.partials['paginator']({ 'showPaginator':showPaginator });
			$('#paginator_container').html(paginatorTemplate);
	            
			//set the pages
            $('#pages').html();
            var options = {
                currentPage: result.page,
                totalPages: result.total_pages,
                alignment: 'right',
                onPageClicked: function (e, originalEvent, type, page) {
                    forum_load_topics(page, search);
                }
            }
            $('#pages').bootstrapPaginator(options);
        }
        else {
            jagg.message({content: result.message, type: "error"});
        }
    });
}


function forum_load_replies(page) {
    var params = {};
    if (page == undefined) {
        page = 1
    }
    params.page = page;

    var currentLocation = window.location.pathname;
    var id = currentLocation.split('/').pop();


    $.getJSON(requestURL + '/forum/api/topic/' + id, params, function (result) {
        console.log(result);
        if (result.error == false) {
			
			var template = Handlebars.partials['topic_details']({ 'replies': result.data });
            $('#topic_details').html(template);
			
            var template = Handlebars.partials['reply_list']({ 'replies': result.data });
            $('#forum_reply_list').html(template);

			if(result.data.replies.length > 0){
				$('.replies-list').show();
			}            

			//set the pages
            $('#pages1').html();
            var options = {
                currentPage: result.page,
                totalPages: result.total_pages,
                alignment: 'right',
                onPageClicked: function (e, originalEvent, type, page) {
                    forum_load_replies(page);
                }
            }
            $('#pages1').bootstrapPaginator(options);
        }
        else {
            jagg.message({content: result.message, type: "error"});
        }
    });
}


$(document).ready(function () {

    //load the first page
    if ($('#forum_topic_list').length) {
        var source = $("#template_topic_list").html();
        Handlebars.partials['topic_list'] = Handlebars.compile(source);

		var paginatorSource = $("#template_paginator").html();
		Handlebars.partials['paginator'] = Handlebars.compile(paginatorSource);

        forum_load_topics(1);
    }
  	//load reply list first page

	if ($('#topic_details').length) {
        var source = $("#template_topic_details").html();
        Handlebars.partials['topic_details'] = Handlebars.compile(source);
    }

    if ($('#forum_reply_list').length) {
        var source = $("#template_reply_list").html();
        Handlebars.partials['reply_list'] = Handlebars.compile(source);
        forum_load_replies(1);
    }

    //delete topic
    $('.delete_topic').live("click", function (event) {

		var deleteButton = this;
		
		// Show confirmation dialog box.

		$('#messageModal').html($('#confirmation-data').html());
    	$('#messageModal div.modal-body').html('\n\n'+i18n.t('confirm.deleteMsgForForumTopic')+'"' + $(deleteButton).attr('data-subject') + '" ?');
   	 	$('#messageModal h3.modal-title').html(i18n.t('confirm.delete'));
    	$('#messageModal a.btn-primary').html(i18n.t('info.yes'));
    	$('#messageModal a.btn-other').html(i18n.t('info.no'));
    	$('#messageModal a.btn-primary').click(function() {
         	$.ajax({
            	type: 'DELETE',
            	url: requestURL + '/forum/api/topic/' + $(deleteButton).attr('data-id'),
            	data: "",
            	dataType: 'html',
            	success: function (data) {
					var response = JSON.parse(data);
					if (response.error == false) {
                		$('#messageModal').modal('hide');
						forum_load_topics(1);
            		} else {
						var errorMessage = "Cannot delete the topic. "
						errorMessage = errorMessage + response.message.split(':')[1];
		        		jagg.message({content:errorMessage,type:"error"});
            		}
            	}
        	});
    	});
		
    	$('#messageModal').modal();

    });


    //bind to search input enter key
    $('#forum_topic_search_value').keypress(function (e) {
        if (e.which == 13) {
            forum_load_topics(1, $('#forum_topic_search_value').val());
        }
    });
    //bind to search button
    $('#forum_topic_search').click(function () {
        forum_load_topics(1, $('#forum_topic_search_value').val());
    })

    $('#summernote').summernote({
        height: 300
    });
    $('#summernote1').summernote({
        height: 100
    });

     //add new forum topic
    $('#add-forum-topic').click(function () {
        var currentLocation = window.location.pathname;
        var id = currentLocation.split('/').pop();

		var queryString = window.location.search; 

		if(queryString){
			var queryParameters = queryString.split('&');
		}

		var tenantDomain = "";
		
		if(queryParameters){
			for(var i = 0; i < queryParameters.length; i++){
				if(queryParameters[i].indexOf("tenant") > -1){
					tenantDomain = "?" + queryParameters[i];
				}
			}
		}

        // alert( $('#subject').val());
        var topic = {
			"parentId" : $('#parentId').val(),
            "subject": $('#subject').val(),
            "description": $('#summernote').code()
        };
        $.ajax({
            type: 'POST',
            url: requestURL + '/forum/api/topic/',
            data: JSON.stringify(topic),
            contentType: "application/json",
            dataType: 'json',
            success: function (result) {
                window.location = requestURL + '/forum/topic/' + result.id + tenantDomain;
            }
        });

    });

    //add new forum reply
    $('#add-forum-reply').click(function () {
        var currentLocation = window.location.pathname;
        var id = currentLocation.split('/').pop();

        var date = new Date();
        var time = date.getTime();

        var htmlContent = getDate(date) + " <br/>" + getTime(time)	 + " <br/> " + i18n.t('info.replyAdded');
        var summernoteContent = $('#summernote1').code();
		$('.replies-list').show();        
		$('#reply_list_tr').show();
        $('#rely_list_td1').html(htmlContent);
        $('#rely_list_td2').html(summernoteContent);

        setTimeout(function () {
            $('#reply_list_tr').hide();
            forum_load_replies(1);

        }, 12000);


        var topic = {
            "reply": $('#summernote1').code(),
            "topicId": id
        };

        jagg.post("/forum/api/reply", {
            topic: JSON.stringify(topic)

        }, function (result) {
            if (result.error == false) {
                var sHTML = "";
                $("#summernote1").code(sHTML);
            } else {
                jagg.message({content: result.message, type: "error"});
            }
        }, "json");


    });


    //delete reply
    $('.delete_reply').live("click", function (event) {

        $.ajax({
            type: 'DELETE',
            url: requestURL + '/forum/api/reply/' + $(this).attr('data-id'),
            data: "",
            dataType: 'html',
            success: function (data) {
				var response = JSON.parse(data);
				if (response.error == false) {
                	forum_load_replies(1);
            	} else {
					var errorMessage = "Cannot delete the reply. "
					errorMessage = errorMessage + response.message.split(':')[1];
		        	jagg.message({content:errorMessage,type:"error"});
            	}
            }
        });
    });


    $('.edit_reply').live("click", function (event) {

        var currentLocation = window.location.pathname;
        var topicId = currentLocation.split('/').pop();
        var reply = $(this).parent().next().html();
        var id = $(this).data('id');

        $td = $("td[data-id *= " + id + "]");
        $($td[0]).hide();
        $($td[1]).show();
        $summernote = $("div[data-id *= " + id + "]");
        $($summernote).summernote({
            height: 100
        });
        var sHTML = reply;
        $($summernote).code(sHTML);


    });

    // add modified reply
    $(document).on("click", '.edit_forum_reply', function (event) {

        var currentLocation = window.location.pathname;
        var topicId = currentLocation.split('/').pop();
        var replyId = $(this).data('id');
        $summernote = $("div[data-id *= " + replyId + "]");
        $($summernote).code();

        var topic = {
            "replyId": replyId,
            "reply": $($summernote).code(),
            "topicId": topicId
        };
        $.ajax({
            type: 'PUT',
            url: requestURL + '/forum/api/reply',
            data: JSON.stringify(topic),
            contentType: "application/json",
            dataType: 'html',
            success: function (data) {
				var response = JSON.parse(data);
				if (response.error == false) {
                	forum_load_replies(1);
            	} else {
					var errorMessage = "Cannot edit the reply. "
					errorMessage = errorMessage + response.message.split(':')[1];
		        	jagg.message({content:errorMessage,type:"error"});
            	}
            }
        });

    });


    $(document).on("click", '.edit_cancel', function (event) {

        forum_load_replies(1);

    });

    $('.edit_topic_icon').live("click", function (event) {

        var currentLocation = window.location.pathname;
        var topicId = currentLocation.split('/').pop();

        var subject = $('#topic').text().trim();
        $('#topic').hide();
        $('#topic_edit').show();
		$('#edit-mode-title').show();
        var input = document.createElement("input");
        input.setAttribute('type', 'text');
        input.setAttribute('id', 'subject');
        input.setAttribute('placeholder', subject);
        input.setAttribute('class', 'input-block-level')
        input.setAttribute('value', subject)
        $('#input_inside').append(input);

        var description = $('#forum_description').html().trim();
        $('#forum_description').hide();
        $('#descritpion_edit').show();
        $summernote = $("div[id *='summernote3']");
        $($summernote).summernote({
            height: 100
        });
        var sHTML = description;
        $($summernote).code(sHTML);


    });

    //add modified topic
    $(document).on("click", '.edit_forum_topic', function (event) {

        var currentLocation = window.location.pathname;
        var topicId = currentLocation.split('/').pop();
        
        var topic = {
            "subject": $('#subject').val(),
            "description": $('#summernote3').code(),
            "topicId": topicId
        };
        $.ajax({
            type: 'PUT',
            url: requestURL + '/forum/api/topic/',
            data: JSON.stringify(topic),
            contentType: "application/json",
            dataType: 'html',
			success: function (data) {
				var response = JSON.parse(data);
				if (response.error == false) {
                	$('#messageModal').modal('hide');
					forum_load_replies(1);
            	} else {
					var errorMessage = "Cannot edit the topic. "
					errorMessage = errorMessage + response.message.split(':')[1];
		        	jagg.message({content:errorMessage,type:"error"});
            	}
            }
        });


    });

	// Add 'clear' button ('x') to search bar.
	
	function getStyleClassFuntion(shouldAddClass){return shouldAddClass?'addClass':'removeClass';} 
  
  	$(document).on('input', '.clearable', function () {
    	$(this)[getStyleClassFuntion(this.value)]('x');
	}).on('mousemove', '.x', function (e) {
    	$(this)[getStyleClassFuntion(this.offsetWidth - 18 < e.clientX - this.getBoundingClientRect().left)]('onX');
	}).on('click', '.onX', function () {
    	$(this).removeClass('x onX').val('');
		forum_load_topics(1); // Load all topis when the user clears the search text.
	});

	jagg.initStars($(".topic-rating"), function (rating, data) {
        jagg.post("/site/blocks/forum/ajax/ratings.jag", {
			action:"rateTopic",
            topicId:data.topicId,
            rating:rating
        }, function (result) {
            if (result.error == false) {
                addTopicRating(result.averageRating,rating);
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");
    }, function (data) {
		removeTopicRating(data);
    }, {topicId:topicId});


});

function getDate(date) {
    var dateStr = date.toString();
    var splitArray = dateStr.split(" ");
    var createdDate = splitArray[0] + " " + splitArray[1] + " " + splitArray[2] + " " + splitArray[3];
    return createdDate;
}

function getTime(time) {
    var date = new Date(time);
    var strArray = date.toString().split(" ");
    return strArray[4];
}

var removeTopicRating = function(topic) {
    jagg.post("/site/blocks/forum/ajax/ratings.jag", {
			action:"removeRating",
        	topicId:topic.topicId
    	}, function (result) {
         		if (result.error == false) {
                	removeTopicStars(result.averageRating);
            	} else {
                	jagg.message({content:result.message,type:"error"});
            	}
        }, "json");
};

var addTopicRating = function (newRating, userRating) {
    var tableRow = $('div.topic-rating').find('table.table > tbody > tr:nth-child(1)');
    var firstHeader = tableRow.find('th');
    var lastCell;
    
    var averageRating = tableRow.find('div.average-rating');
    if (averageRating.length > 0) {
    	averageRating.html(newRating.toFixed(1));
    } else {
    	$("<td></td>").append('<div class="average-rating">' + newRating + '</div>').insertAfter(firstHeader);
    }
    
	lastCell = tableRow.find('td:last')
    lastCell.attr('colspan', 1);

    $.getScript(context + '/site/themes/' + theme + '/utils/ratings/star-generator.js', function () {
    	lastCell.find('div.star-ratings').html(getDynamicStars(userRating));
        
		jagg.initStars($(".topic-rating"), function (rating, data) {
        	jagg.post("/site/blocks/forum/ajax/ratings.jag", {
					action:"rateTopic",
            		topicId:data.topicId,
            		rating:rating
        		}, function (result) {
           	 			if (result.error == false) {
                			addTopicRating(result.averageRating,rating);
            			} else {
                			jagg.message({content:result.message,type:"error"});
            			}
        			}, "json");
    			}, function (data) {
					removeTopicRating(data);
    			}, {topicId:topicId});

       });
        
};


var removeTopicStars = function (newRating) {
    var tableRow = $('div.topic-rating').find('table.table > tbody > tr:nth-child(1)');
    var firstHeader = tableRow.find('th');
    var lastCell = tableRow.find('td:last');
    
    var averageRating = tableRow.find('div.average-rating');
    if (averageRating.length > 0) {
    	averageRating.html(newRating.toFixed(1));
    }
		
	$.getScript(context + '/site/themes/' + theme + '/utils/ratings/star-generator.js', function () {
    	lastCell.find('div.star-ratings').html(getDynamicStars(0));

        jagg.initStars($(".topic-rating"), function (rating, data) {
        	jagg.post("/site/blocks/forum/ajax/ratings.jag", {
					action:"rateTopic",
            		topicId:data.topicId,
            		rating:rating
        		}, function (result) {
           	 			if (result.error == false) {
                			addTopicRating(result.averageRating,rating);
            			} else {
                			jagg.message({content:result.message,type:"error"});
            			}
        		}, "json");
    			}, function (data) {
					removeTopicRating(data);
    			}, {topicId:topicId});

                });
     
};



