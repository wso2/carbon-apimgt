Handlebars.registerHelper('html_sanitize', function(context, options) {
  context = html_sanitize(context);
  return context;
});

Handlebars.registerHelper('if_creator', function(creator, user, options) {
    if(creator != user) {
        return options.inverse(this);
    } else {
        return options.fn(this);
    }
});
// Load forum topics for the given page and the search term.
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

    $.getJSON(requestURL + 'forum/api/topic', params, function (result) {
        if (result.error == false) {

            var template = Handlebars.partials['topics_list']({
                'topics': result.data
            });
            $('#forum_topics_list').html(template);

            if(result.total_pages > 0){
                $('#forum_topics_list').show();
                $('.forum_topics_list').show();
                $('#forum_no_topics').hide();
            }else{
                $('#forum_topics_list').hide();
                $('.forum_topics_list').hide();
                $('#forum_no_topics').show();
            }

            // Show the paginator if the list has more than one page.
            if (result.total_pages > 1) {

                //set the pages
                var options = {
                    currentPage: result.page,
                    bootstrapMajorVersion:3,                    
                    totalPages: result.total_pages,
                    alignment: 'right',
                    onPageClicked: function (e, originalEvent, type, page) {
                        forum_load_topics(page, search);
                    }
                }

                $('#forum_topics_list_paginator').bootstrapPaginator(options);
                $('#forum_topics_list_paginator').show();
            } else {
                $('#forum_topics_list_paginator').hide();
            }

        } else {
            jagg.message({
                content: result.message,
                type: "error"
            });
        }
    });
}

// Loads replies for a topic in the give page.
function forum_load_replies(page) {
    var params = {};
    if (page == undefined) {
        page = 1
    }
    params.page = page;

    var currentLocation = window.location.pathname;
    var id = currentLocation.split('/').pop();


    $.getJSON(requestURL + 'forum/api/topic/' + id, params, function (result) {
        console.log(result);
        if (result.error == false) {
            
            var title = Handlebars.partials['topic_title']({
                'replies': result.data
            });

            $('#forum_topic_title_bar').html(title);                

            var template = Handlebars.partials['topic_details']({
                'replies': result.data
            });
            $('#forum_topic_content').html(template);

            $('.rating-tooltip-manual').rating({
              extendSymbol: function () {
                var title;
                $(this).tooltip({
                  container: 'body',
                  placement: 'bottom',
                  trigger: 'manual',
                  title: function () {
                    return title;
                  }
                });
                $(this).on('rating.rateenter', function (e, rate) {
                  title = rate;
                  $(this).tooltip('show');
                })
                .on('rating.rateleave', function () {
                  $(this).tooltip('hide');
                });
              }
            });

            var topicId=result.data.topic[0].topicId;
            $('input.rate_save').on('change',{ topicID:topicId },   function (event) {
                jagg.post("/site/blocks/forum/ajax/ratings.jag", {
                    action: "rateTopic",
                    topicId: event.data.topicID,
                    rating: $(this).val()
                }, function (result) {
                    if (result.error == false) {
                        if($('.average-rating').length > 0){
                            $('.average-rating').text(result.averageRating.toFixed(1));
                            $('.average-rating').show();
                            }else{
                                $('.user_rating').before("<div class='average-rating'>"+result.averageRating+"</div>");
                            }
                            $('.your_rating').text(parseInt(result.averageRating)+"/5");
                        } else {
                            jagg.message({content:result.message,type:"error"});
                        }
                }, "json");
            });

            $('.remove_rating').on("click",{ topicID:topicId },function(event){

                $('input.rate_save').val(0);
                $('input.rate_save').rating('rate', 0);
                //var api = jagg.api;
                jagg.post("/site/blocks/forum/ajax/ratings.jag", {
                        action: "removeRating",
                        topicId: event.data.topicID
                }, function (result) {
                    if (!result.error) {
                        $('.average-rating').hide();
                        $('.your_rating').text("N/A");
                    } else {
                        jagg.message({content:result.message,type:"error"});
                    }
                }, "json");
            });

            var template = Handlebars.partials['replies_list']({
                'replies': result.data
            });
            $('#forum_replies_block').html(template);
            if($(template).find("div").hasClass("comment-container-dyn")){
                $("#no_topic_msg").hide();
            }else{
                $("#no_topic_msg").show();
            }
            $('#forum_replies_list').show();

            // If there are more than one pages show the paginator.
            if (result.total_pages > 1) {
                var options = {
                    currentPage: result.page,
                    bootstrapMajorVersion:3,
                    totalPages: result.total_pages,
                    onPageClicked: function (e, originalEvent, type, page) {
                        forum_load_replies(page);
                    }
                }
                $('#forum_replies_paginator').bootstrapPaginator(options);
                /*$('#forum_replies_paginator').bootpag({
                   total: result.total_pages,
                   page: result.page,
                   maxVisible: 10
                }).on('page', function(event, page){
                    forum_load_replies(page) // or some ajax content loading...
                });  */              
            }

            $(forum_reply_editor).summernote({
                height: 300
            });


            $('#forum-rating').rating();

        } else {
            jagg.message({
                content: result.message,
                type: "error"
            });
        }
    });
}


$(document).ready(function () {
    // START - Topic bindings
    // Add new forum topic.
    $(document).on("click", '#add-forum-topic', function () {
	jagg.sessionAwareJS({redirect:'/site/pages/index.jag'});
        var currentLocation = window.location.pathname;
        var id = currentLocation.split('/').pop();

        var queryString = window.location.search;

        if (queryString) {
            var queryParameters = queryString.split('&');
        }

        var tenantDomain = "";

        if (queryParameters) {
            for (var i = 0; i < queryParameters.length; i++) {
                if (queryParameters[i].indexOf("tenant") > -1) {
                    tenantDomain = "?" + queryParameters[i];
                }
            }
        }

        // Validate inputs.
        if ($('#subject').val().trim() == "") {
            jagg.message({
                content: i18n.t('The topic subject cannot be empty.'),
                type: "error"
            });
            return;
        }

        if ($('<div>').append($('#topicDescriptioEditor').code()).text().trim() == "") {
            jagg.message({
                content: i18n.t('The topic description cannot be empty.'),
                type: "error"
            });
            return;
        }

        var topic = {
            "parentId": $('#parentId').val(),
            "subject": $('#subject').val(),
            "description": $('#topicDescriptioEditor').code()
        };
        $.ajax({
            type: 'POST',
            url: requestURL + 'forum/api/topic/',
            data: JSON.stringify(topic),
            contentType: "application/json",
            dataType: 'json',
            success: function (result) {
                window.location = requestURL + 'forum/topic/' + result.id + tenantDomain;
            }
        });

    });

    // Delete a topic
    $(document).on("click", ".forum_delete_topic_icon", function (event) {
	jagg.sessionAwareJS({redirect:'/site/pages/index.jag'});
        var deleteButton = this;

        // Show confirmation dialog box.

        $('#messageModal').html($('#confirmation-data').html());
        $('#messageModal div.modal-body').text('\n\n' + i18n.t('Do you want to remove the topic ') + '"' + $(deleteButton).attr('data-subject') + '" ?');
        $('#messageModal h3.modal-title').html(i18n.t('Confirm Delete'));
        $('#messageModal a.btn-primary').html(i18n.t('Yes'));
        $('#messageModal a.btn-other').html(i18n.t('No'));
        $('#messageModal a.btn-primary').click(function () {
            $.ajax({
                type: 'DELETE',
                url: requestURL + 'forum/api/topic/' + $(deleteButton).attr('data-id'),
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
                        jagg.message({
                            content: errorMessage,
                            type: "error"
                        });
                    }
                }
            });
        });

        $('#messageModal').modal();

    });

    // Search topics when the user hits on the enter button.
    $(document).on("keypress", '#forum_topic_search_value', function (e) {
        if (e.which == 13) {
            forum_load_topics(1, $('#forum_topic_search_value').val());
        }
    });

    // Search topic when the user hits on the search button.
    $(document).on("click", '#forum_topic_search', function () {
        forum_load_topics(1, $('#forum_topic_search_value').val());
    })

    // Show topic edit page.
    $(document).on("click", '#forum_edit_topic_icon', function (event) {

        var currentLocation = window.location.pathname;
        var topicId = currentLocation.split('/').pop();

        // Add topic edit input controls.
        var subject = $('#forum_topic_subject_lable').text().trim();
        $('#forum_topic_subject_edit_input').val(subject);

        var description = $('#forum_topic_description').html().trim();
        var topicDescriptionEditor = $("#forum_topic_description_edit_editor");
        $(topicDescriptionEditor).summernote({
            height: 100,
            width:"99.9%"
        });

        $(topicDescriptionEditor).code(description);

        $('#forum_topic_description').hide();
        $('#forum_topic_edit_block').show();
        $('#forum_topic_subject_lable').parent().hide();
        $('#forum_topic_subject_edit_input').parent().show();
        $('#forum_edit_topic_icon').hide();
        $('#forum_topic_subject_edit_input').focus();
    });

    // Cancel topic editing.
    $(document).on("click", '#forum_cancel_topic_edit_button', function (event) {

        $('#forum_topic_edit_block').hide();
        $('#forum_topic_description').show();
        $('#forum_edit_topic_icon').show();
        $('#forum_topic_subject_lable').parent().show();
        $('#forum_topic_subject_edit_input').parent().hide();
    });

    // Saves updated topic.
    $(document).on("click", '#forum_save_updated_topic_button', function (event) {

        var currentLocation = window.location.pathname;
        var topicId = currentLocation.split('/').pop();

        // Validate inputs.
        var newSubject = $('#forum_topic_subject_edit_input').val().trim();
        if (newSubject == "") {
            jagg.message({
                content: i18n.t('The topic subject cannot be empty.'),
                type: "error"
            });
            return;
        }

        var newDescription = $('#forum_topic_description_edit_editor').code();
        if ($('<div>').append(newDescription).text().trim() == "") {
            jagg.message({
                content: i18n.t('The topic description cannot be empty.'),
                type: "error"
            });
            return;
        }

        var topic = {
            "subject": newSubject,
            "description": newDescription,
            "topicId": topicId
        };

        $.ajax({
            type: 'PUT',
            url: requestURL + 'forum/api/topic/',
            data: JSON.stringify(topic),
            contentType: "application/json",
            dataType: 'html',
            success: function (data) {
                var response = JSON.parse(data);
                if (response.error == false) {
                    forum_load_replies(1);
                } else {
                    var errorMessage = i18n.t('Cannot edit the topic. ');
                    errorMessage = errorMessage + response.message.split(':')[1];
                    jagg.message({
                        content: errorMessage,
                        type: "error"
                    });
                }
            }
        });

    });

    // Topic search bindings.
    function getStyleClassFuntion(shouldAddClass) {
        return shouldAddClass ? 'addClass' : 'removeClass';
    }

    $(document).on('input', '.clearable', function () {
        $(this)[getStyleClassFuntion(this.value)]('x');
    }).on('mousemove', '.x', function (e) {
        $(this)[getStyleClassFuntion(this.offsetWidth - 18 < e.clientX - this.getBoundingClientRect().left)]('onX');
    }).on('click', '.onX', function () {
        $(this).removeClass('x onX').val('');
        forum_load_topics(1); // Load all topis when the user clears the search text.
    });

    // END - Topic bindings

    // START - Reply binding

    //Add new reply.

    $(document).on("click", '#forum_add_reply_button', function () {
	jagg.sessionAwareJS({redirect:'/site/pages/index.jag'});
        var currentLocation = window.location.pathname;
        var id = currentLocation.split('/').pop();

        // Validate inputs.
        var replyContent = $('#forum_reply_editor').code();
        if ($('<div>').append(replyContent).text().trim() == "") {
            jagg.message({
                content: i18n.t('Reply cannot be empty.'),
                type: "error"
            });
            return;
        }

        var date = new Date();
        var time = date.getTime();

        var replyInfo = '<div class="comment-extra">Posted By <strong>You</strong> on <span class="dateFull">' +getDate(date)+ ', ' + getTime(time) + '</span></div>';
        $('#forum_replies_list').show();
        $('#forum_reply_content_temp').html(replyContent);
        $('#forum_reply_added_block').show();
        $("#no_topic_msg").hide();
        $('#forum_reply_info_temp').html(replyInfo);

        $('#forum_reply_editor').code("");
        $(".note-editable").attr('contenteditable','false');

        setTimeout(function () {
            $('#forum_reply_added_block').hide();
            forum_load_replies(1);
        }, 12000);


        $('#forum_reply_editor')

        var topic = {
            "reply": replyContent,
            "topicId": id
        };

        jagg.post("/forum/api/reply", {
            topic: JSON.stringify(topic)
        }, function (result) {
            if (result.error == false) {
                var sHTML = "";
                $("#summernote1").code(sHTML);
            } else {
                jagg.message({
                    content: result.message,
                    type: "error"
                });
            }
        $(".note-editable").attr('contenteditable','true');
        }, "json");


    });

    // Shows reply edit block
    $(document).on("click", ".forum_edit_reply_icon", function (event) {
	jagg.sessionAwareJS({redirect:'/site/pages/index.jag'});
        var currentLocation = window.location.pathname;
        var topicId = currentLocation.split('/').pop();
        var reply = $(this).parent().next().children().html();
        var id = $(this).data('id');

        // Hide reply content.
        var contentCell = $("#forum_reply_content_cell_" + id);
        contentCell.hide();

        // Show the editor.
        var editor = $("#forum_reply_edit_editor_" + id);
        $(editor).summernote({
            height: 300
        });
        $(editor).code(reply);

        var replyEditorCell = $("#forum_reply_edit_cell_" + id);
        replyEditorCell.show();

    });

    // Saves updated reply.
    $(document).on("click", '.forum_save_updated_reply_button', function (event) {
	jagg.sessionAwareJS({redirect:'/site/pages/index.jag'});
        var currentLocation = window.location.pathname;
        var topicId = currentLocation.split('/').pop();
        var replyId = $(this).data('id');

        var content = $("#forum_reply_edit_editor_" + replyId).code();

        // Validate inputs.
        if ($('<div>').append(content).text().trim() == "") {
            jagg.message({
                content: i18n.t('Reply cannot be empty.'),
                type: "error"
            });
            return;
        }

        var topic = {
            "replyId": replyId,
            "reply": content,
            "topicId": topicId
        };

        $.ajax({
            type: 'PUT',
            url: requestURL + 'forum/api/reply',
            data: JSON.stringify(topic),
            contentType: "application/json",
            dataType: 'html',
            success: function (data) {
                var response = JSON.parse(data);
                if (response.error == false) {
                    forum_load_replies(1);
                } else {
                    var errorMessage = i18n.t('Cannot edit the reply. ');
                    errorMessage = errorMessage + response.message.split(':')[1];
                    jagg.message({
                        content: errorMessage,
                        type: "error"
                    });
                }
            }
        });

    });

    // Hides reply edit block
    $(document).on("click", '.forum_cancel_reply_edit_button', function (event) {

        var replyId = $(this).data('id');

        var replyEditorCell = $("#forum_reply_edit_cell_" + replyId);
        replyEditorCell.hide();

        var contentCell = $("#forum_reply_content_cell_" + replyId);
        contentCell.show();

    });

    //Deletes a reply.
    $(document).on("click", '.forum_delete_reply_icon', function (event) {
	jagg.sessionAwareJS({redirect:'/site/pages/index.jag'});
        var deleteButton = this;

        $('#messageModal').html($('#confirmation-data').html());
        $('#messageModal div.modal-body').html(i18n.t('Do you want to remove this reply?'));
        $('#messageModal h3.modal-title').html(i18n.t('Confirm Delete'));
        $('#messageModal a.btn-primary').html(i18n.t('Yes'));
        $('#messageModal a.btn-other').html(i18n.t('Yes'));
        $('#messageModal a.btn-primary').click(function () {
            $.ajax({
                type: 'DELETE',
                url: requestURL + 'forum/api/reply/' + $(deleteButton).attr('data-id'),
                data: "",
                dataType: 'html',
                success: function (data) {
                    var response = JSON.parse(data);
                    if (response.error == false) {
                        $('#messageModal').modal('hide');
                        forum_load_replies(1);
                    } else {
                        var errorMessage = i18n.t('Cannot delete the reply. ');
                        errorMessage = errorMessage + response.message.split(':')[1];
                        jagg.message({
                            content: errorMessage,
                            type: "error"
                        });
                    }
                }
            });

        });

        $('#messageModal').modal();

    });

    // END - Reply bindings


    // If we are in the topic list page.
    if ($('#forum_topics_list_page').length) {
        var source = $("#forum_template_topics_list").html();
        Handlebars.partials['topics_list'] = Handlebars.compile(source);

        forum_load_topics(1);
    }

    // If we are in the topic details page.
    if ($('#forum_topic_details_page').length) {
        
        var titleSource = $("#fourm_topic_title_template").html();
        Handlebars.partials['topic_title'] = Handlebars.compile(titleSource);    

        var source = $("#forum_topic_details_template").html();
        Handlebars.partials['topic_details'] = Handlebars.compile(source);

        var source = $("#forum_replies_list_template").html();
        Handlebars.partials['replies_list'] = Handlebars.compile(source);

        forum_load_replies(1);
        
    }

    // If we are in the add new topic page.
    if ($('#forum_add_new_topic_page').length) {
        $('#topicDescriptioEditor').summernote({
            height: 350
        });
    }


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

var removeTopicRating = function (topic) {
    jagg.post("/site/blocks/forum/ajax/ratings.jag", {
        action: "removeRating",
        topicId: topic.topicId
    }, function (result) {
        if (result.error == false) {
            removeTopicStars(result.averageRating);
        } else {
            jagg.message({
                content: result.message,
                type: "error"
            });
        }
    }, "json");
};

var addTopicRating = function (newRating, userRating) {
    var tableRow = $("#forum_topic_rating_block").find('table.table > tbody > tr:nth-child(1)');
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

        jagg.initStars($("#forum_topic_rating_block"), function (rating, data) {
            jagg.post("/site/blocks/forum/ajax/ratings.jag", {
                action: "rateTopic",
                topicId: data.topicId,
                rating: rating
            }, function (result) {
                if (result.error == false) {
                    addTopicRating(result.averageRating, rating);
                } else {
                    jagg.message({
                        content: result.message,
                        type: "error"
                    });
                }
            }, "json");
        }, function (data) {
            removeTopicRating(data);
        }, {
            topicId: topicId
        });

    });

};

var removeTopicStars = function (newRating) {
    var tableRow = $("#forum_topic_rating_block").find('table.table > tbody > tr:nth-child(1)');
    var firstHeader = tableRow.find('th');
    var lastCell = tableRow.find('td:last');

    var averageRating = tableRow.find('div.average-rating');
    if (averageRating.length > 0) {
        averageRating.html(newRating.toFixed(1));
    }

    $.getScript(context + '/site/themes/' + theme + '/utils/ratings/star-generator.js', function () {
        lastCell.find('div.star-ratings').html(getDynamicStars(0));

        jagg.initStars($("#forum_topic_rating_block"), function (rating, data) {
            jagg.post("/site/blocks/forum/ajax/ratings.jag", {
                action: "rateTopic",
                topicId: data.topicId,
                rating: rating
            }, function (result) {
                if (result.error == false) {
                    addTopicRating(result.averageRating, rating);
                } else {
                    jagg.message({
                        content: result.message,
                        type: "error"
                    });
                }
            }, "json");
        }, function (data) {
            removeTopicRating(data);
        }, {
            topicId: topicId
        });

    });

};

var validateCancel = function (tenantSuffix) {
    jagg.message({
        content: i18n.t('Are you sure you want to cancel creating the Topic ?'),
        type: "confirm",
        title: "Cancel Creating Topic",
        okCallback: function() {
            document.location.href=jagg.site.context+"/forum?" + tenantSuffix;
        }
    });
};
