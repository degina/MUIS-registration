
function addVote(postId,choiceId, url, Uname){
    var flag = $('div#divChoice'+choiceId).find("input[type='checkbox']").is(":checked");
    $.ajax({
        type: "POST",
        data: {
            postId: postId,
            choiceId: choiceId,
            flag: flag
        }, url: "/dashboard/addVote",
        beforeSend: function(){
            $('div#choice'+choiceId).parent().find("input").each(function(){
                $(this).prop("disabled", true);
            });
        }
    }).success(
        function (data) {
            if(flag){
                if($('div#choice'+choiceId+' div.askImage').find('span').html() != undefined) {
                    var size = $('div#choice' + choiceId + ' div.askImage').find('span').html();
                    var temp = size.substr(1);
                    temp++;
                    $('div#choice' + choiceId + ' div.askImage').find('span').html('+'+ temp);
                }
                else{
                    var i = $('div#choice'+choiceId+' div.askImage').find("img").length;
                    if(i == 3)
                        $('div#choice'+choiceId+' div.askImage').append("<div class='pull-right' style='cursor: pointer'><span style='margin-top: 5px' class='badge badge-default m-l-10 f-15' onclick='showVoteSize("+ choiceId +")'>+1</span></div>")
                }
                $('div#divChoice'+choiceId).attr('class', 'progress-bar progress-bar-danger');
                //$('div#divChoice'+choiceId).css({"width":"30%"});
                var image = document.createElement('img');
                image.src = url;
                image.setAttribute('data-rel','tooltip');
                image.setAttribute('data-original-title',Uname);
                image.width = "30";
                $('div#choice' + choiceId + ' div.askImage').prepend(image);
                //$('div#choice' + choiceId + ' div.askImage').html("<img src='" + url + "' width='30'>" + $('div#choice' + choiceId + ' div.askImage').html());
                if ($('div#choice' + choiceId + ' div.askImage img').length > 3) {
//                            alert($('div#choice' + choiceId + ' div.askImage img').length);
                    $('div#choice' + choiceId + ' div.askImage img:nth-child(4)').addClass("dhidden");
                }

            }
            else{
                if($('div#choice'+choiceId+' div.askImage').find('span').html() != undefined) {
                    var size = $('div#choice' + choiceId + ' div.askImage').find('span').html();
                    var temp = size.substr(1);
                    if(temp != 1) {
                        temp--;
                        $('div#choice' + choiceId + ' div.askImage').find('span').html('+' + temp);
                    }
                    else
                        $('div#choice' + choiceId + ' div.askImage').find('div').remove();
                }
                $('div#divChoice'+choiceId).attr('class', 'progress-bar progress-bar-info');
                var isRemove = false;
                $('div#choice'+choiceId+' div.askImage').find("img").each(function(){
                    if($(this).attr("src") == url){
                        $(this).remove();
                        isRemove = true;
                    }
                });
                if(isRemove)
                    $('div#choice'+choiceId+' div.askImage img.dhidden').removeClass("dhidden");

            }
            $('div#choice'+choiceId).parent().find("div.progress").each(function(){
                var bar = $(this).find("div.progress-bar");
                for(i = 0; i < data.length; i++)
                    if(bar.attr("id") == "divChoice" + data[i].id){
                        bar.css("width", data[i].procent + "%");
                    }
            });
            $('div#choice'+choiceId).parent().find("input").each(function(){
                $(this).prop("disabled", false);
            });
        });
}

$('div.post div.post-body input.form-control').bind("enterKey",function(e){
    $.ajax({
        type: "POST",
        data: {
            choice:$(this).val(),
            postId:$(this).attr("name")
        },
        url: "/dashboardAddChoice"
    }).success(function (data) {
        location.reload();
    });
});

$('div.post div.post-body input.form-control').keyup(function(e){
    if(e.keyCode == 13) {
        $(this).trigger("enterKey");
    }
});


function pinPost(postId,flag){
    $.ajax({
        type: "POST",
        data: {
            flag:flag,
            postId: postId
        },
        url: "/dashboardPinPost"
    }).success(function (data) {
        //$('div#postArea div.post-new').parent('div').after($('div#postId' + postId).parent('div'));
        location.reload();
    });
}


function showVoteSize(choiceId){
    $.ajax({
        type: "POST",
        data: {
            choiceId: choiceId
        },
        url: "/dashboard/showVoteSize"
    }).success(function (data) {
        $('div#showVoteSize').modal("show");
        var contentdata;
        $('div#showVoteSize').find('div.modal-body').html("");
        for(var i = 0; i < data.length; i++) {
            contentdata="<div style='margin-top: 5px'>";
            contentdata += "<img style='margin-right:10px' class='modalVoteUsers' src='" + data[i].pic + "' width='30'/>";
            contentdata += data[i].user;
            contentdata += "</div>";
            $('div#showVoteSize').find('div.modal-body').append(contentdata);
        };
    });
}

function showMoreComment(postId) {
    var type = 'show';
    var spanI = $('div#postArea div#postId' + postId + ' span#spanChevron' + postId + ' i');
    if (spanI.hasClass('fa-chevron-down')) {
        spanI.removeClass('fa-chevron-down').addClass('fa-chevron-up');
        type = 'show';
    } else {
        type = 'hide';
        spanI.removeClass('fa-chevron-up').addClass('fa-chevron-down');
    }
    $.ajax({
        type: "POST",
        data: {
            type: type,
            postId: postId
        }, url: "/dashboard/showMoreComment"
    }).success(
        function (data) {
            $('div#postArea div#post-comments' + postId).html(data);
        });
}

function cancelComment(postId) {
    var post = $('div#postId' + postId);
    post.find('div.full-comment').hide();
    post.find('textarea#textComment').val('');
    post.find('div.small-comment').css('border', '1px solid #ddd').show();
}

function addPostShow(){
    $( "#triggerClickUploadEvent" ).trigger( "click" );
    $('div#postArea div.post-new div.textPostSmall').hide();
    $('div#postArea div.post-new div#postHidden div#attachPost').show("slow");
    $('div#postArea div.post-new div.post-new-footer').show("slow");
    $('div#postArea div.post-new textarea#textPostFull').show().focus();
    $('div#postArea div.post-new div#postHidden').show("slow");
}

var id = 1;

function addPost(type,event) {
    var eventId = "";
    var data;
    if(event != null)
        eventId = event;
    if(type == 'post') {
        var attaches = [];
        var users = [];
        var text = urlify($('textarea#textPostFull').val().replace(/\r\n|\r|\n/g,"<br />"));
        $('div.post-new').find("ul#uPost img.imgIcon").each(function () {
            attaches.push({
                "filename": $(this).attr('filename'),
                "filedir": $(this).attr('filedir'),
                "extension": $(this).attr('extension')
            });
        });
        $("div#sendUserTag").find("li").each(function () {
            users.push(this.id);
        });
        if(attaches.length == 0 && text == "") {
            showErrorMessageCustom("Мэдээллүүдийг гүйцэт оруулна уу!", "center", "top", 2000);
            return;
        }
        data = {
            "eventId": eventId,
            "attach": attaches,
            "users": users,
            "text": text,
            "type": type,
        };
    }
    else if(type == 'question'){
        var choices = [];
        $('div#addQuestionBox').modal('hide');
        var i = 0;
        $('div.choiceContainer').find('input.form-control').each(function () {
            if($(this).val() != "") {
                choices[i] = $(this).val();
                i++;
            }
        });
        data = {
            "eventId": eventId,
            "type": type,
            "question": urlify($('#Question').val().replace(/\r\n|\r|\n/g,"<br />")),
            "choices": choices
        };
    }
    $.ajax({
        data: {
            data: JSON.stringify(data)
        },
        url: "/dashboard/addPost"
    }).success(function (dataPost) {
        cancelPost();
        $('div#postArea div.post-new').parent('div').after(dataPost);
    });
}

function addComment(postId) {
    var divAddComment = $('div#postArea div#addComment' + postId);
    var comment = urlify(divAddComment.find('textarea#textComment').val().replace(/\r\n|\r|\n/g,"<br />"));
    $.ajax({
        type: "POST",
        data: {
            postId: postId,
            comment: comment
        }, url: "/dashboard/addComment"
    }).success(
        function (data) {
            $('div#postArea div#postId' + postId + ' div.post-comments').append(data);
            $('span#postSize'+postId).html(parseInt($('span#postSize'+postId).html(),10)+1+" сэтгэгдлүүд");
            $('div#postId'+postId).find('div.show-more-comment').show();
            cancelComment(postId);
        });
}

function cancelPost() {
    $('div#postArea div.post-new div.post-new-footer').hide();
    $('div#postArea div.post-new textarea#textPostFull').hide();
    $('div#postArea div.post-new textarea#textPostFull').val("");
    $('div#postArea div.post-new div#postHidden').hide();
    $('div#postArea div.post-new div.textPostSmall').show();
    $('div#postArea div.post-new div#sendUserTag ul').html("<li id=a-0 seltype=a uid=0 ><span" +
        " onclick=sendToUserRemoveSelection('a','0') > <i class='fa fa-minus-circle asterisk' ></i> Бүгд</span></li>");
}
function likeThisPost(postId) {
    $.ajax({
        type: "POST",
        data: {
            postId: postId
        }, url: "/dashboard/likePost"
    }).success(
        function (data) {
            $('div#postArea span#spanPostLike' + postId).html("<i class='"+data.like+"'></i> " + data.likes).attr("data-original-title",data.likeUsers);
        });
}

function deletePost(postId) {
    var x = confirm("Энэ нийтлэлийг устгах уу?");
    if (x) {
        $.ajax({
            type: "POST",
            data: {
                id: postId
            }, url: "/dashboard/deletePost"
        }).success(
            function (data) {
                if(data == "true")
                    $('div#pin').remove();
                $('div#postArea div#postId' + postId).remove();
                if($('button.modal-fullscreen-close').html() != undefined)
                    $('button.modal-fullscreen-close').trigger('click');
            });
    }
}

function readMorePost(postId) {
    $.ajax({
        type: "POST",
        data: {
            id: postId,
            type: true
        }, url: "/dashboard/readMorePost"
    }).success(
        function (data) {
            $('div#postArea div#postId' + postId + ' div.post-body div.post-content').html(data + "</br><span class='post-read-more' onclick='showLessPost(" + postId + ")' >Хураах</span>");
        });
}

function urlify(text) {
    var urlRegex = /^(https?|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i;
    return text.replace(urlRegex, function(url) {
        return '<a href="' + url + '">' + url + '</a>';
    })
}

function showLessPost(postId) {
    var content = $('div#postArea div#postId' + postId + ' div.post-body div.post-content');
    content.html(content.text().substring(0, 300) + "... </br> <span class='post-read-more' onclick=readMorePost(" + postId + ") >Цааш нь унших</span>");
}
function showfullPost(postId, number) {
    $('div#imageShowModal').modal('show');
    loading2('div#imageShowModal div.modal-body');
    $.ajax({
        type: "POST",
        data: {
            id: postId,
            number: number,
            type: false
        }, url: "/dashboard/readMorePost"
    }).success(
        function (data) {
            $('div#imageShowModal div.modal-body').html(data);
            $('div#imageShowModal #addComment' + postId).find('div.small-comment').click(function () {
                $(this).css('border', '2px solid #0090d9');
                var currentDiv = $(this);
                setTimeout(function () {
                    currentDiv.hide();
                    currentDiv.next('.full-comment').show().find('#textComment').focus();
                }, 100);
            });
            $(function () {
                $('span[data-rel="tooltip"]').tooltip();
            });
            $('img.img-responsive-full').each(function () {
                $(this).css({'max-height': (($(window).height()) - 60) + 'px'});
            });
        });
}

function deleteComment(postId, commentId) {
    var x = confirm("Энэ сэтгэгдлийг устгах уу?");
    if (x) {
        $.ajax({
            type: "POST",
            data: {
                commentId: commentId,
            }, url: "/dashboard/deleteComment"
        }).success(
            function (data) {
                $("div#postArea div#postId" + postId + " div#post-comment" + commentId).remove();
                var postSize = $('div#postId'+postId).find('span#postSize'+postId).html();
                var ret = postSize.split(" ");
                var str1 = ret[0];
                str1--;
                if(str1 <= 0) {
                    $('div#postId' + postId).find('div.show-more-comment').hide();
                }
                var str2 = ret[1];
                $('div#postId'+postId).find('span#postSize'+postId).html(str1 + " " + str2);
            });
    }
}



function editPost(postId) {
    var $div = $('div#postArea div#postId' + postId + ' div.post-body div.post-content');
    var text = $div.html();
    if($div.next("textarea").html() == undefined) {
        $div.after("<textarea class='form-control' id='textPostFull'>" + $div.text() + "</textarea>" +
            "<span class=RR style='margin-bottom: 7px'> <button type='button' class='btn btn-post' onclick='saveEditPost(" + postId + ",false)'>Хадгалах</button>" +
            "<button type=button class='btn btn-default btn-white m-l-5'>Цуцлах</button> </span>");
        $div.html("");
        $div.next('textarea').focus().val('').val(text.replace(/<br\s?\/?>/g,"\n"));
        $div.next("textarea").next("span").find('button.btn-default').click(function () {
            $div.next("textarea").remove();
            $div.next("span").remove();
            $div.html(text);
        });
    }
}

function editPostImage(postId) {
    var $div = $('div.post-image div#postId' + postId + ' div.post-body div.post-content');
    var text = $div.html();
    if($div.next("textarea").html() == undefined) {
        $div.after("<textarea class='form-control' id='textPostFull'>" + $div.text() + "</textarea>" +
            "<span class=RR style='margin-bottom: 7px'> <button type='button' class='btn btn-post' onclick='saveEditPost(" + postId + ",true)'>Хадгалах</button>" +
            "<button type=button class='btn btn-default btn-white m-l-5'>Цуцлах</button> </span>");
        $div.html("");
        $div.next('textarea').focus().val('').val(text.replace(/<br\s?\/?>/g,"\n"));
        $div.next("textarea").next("span").find('button.btn-default').click(function () {
            $div.next("textarea").remove();
            $div.next("span").remove();
            $div.html(text);
        });
    }
}

function saveEditPost(postId, flag) {
    var $div = $('div#postArea div#postId' + postId + ' div.post-body div.post-content');
    var text = urlify($div.next('#textPostFull').val().replace(/\r\n|\r|\n/g,"<br />"));
    if(flag) {
        var $div1 = $('div.post-image div#postId' + postId + ' div.post-body div.post-content');
        text = urlify($div1.next('#textPostFull').val().replace(/\r\n|\r|\n/g,"<br />"));
    }
    $.ajax({
        type: "POST",
        data: {
            postId: postId,
            text: text
        }, url: "/dashboard/saveEditPost"
    }).success(
        function (dataPost) {
            if(flag){
                $div1.next("textarea").remove();
                $div1.next("span").remove();
                $div1.html(text);
            }
            $div.next("textarea").remove();
            $div.next("span").remove();
            $div.html(text);
        });
}

function editComment(postId,commentId){
    var $div = $('div#post-comments'+postId+' div#post-comment' + commentId).find('div#commentText');
    if($div.next("textarea").html() == undefined) {
        var text = $div.html();
        $div.after('<textarea class="form-control" id="textPostFull" style="">' + $div.text() + '</textarea>' +
            '<span class=RR style="margin-bottom: 7px"> <button type="button" class="btn btn-post" onclick="saveEditComment(' + commentId + ',' + postId + ')">Хадгалах</button>' +
            '<button type=button class="btn btn-default btn-white m-l-5">Цуцлах</button> </span>');
        $div.html("");
        $div.next('textarea').focus().val('').val(text.replace(/<br\s?\/?>/g,"\n"));
        $div.next("textarea").next("span").find('button.btn-default').click(function () {
            $div.next("textarea").remove();
            $div.next("span").remove();
            $div.html(text);
        });
    }
}

function saveEditComment(commentId, postId) {
    var $div = $('div#post-comments'+postId+' div#post-comment' + commentId).find('div#commentText');
    var text = urlify($div.next('textarea').val().replace(/\r\n|\r|\n/g,"<br />"));
    $.ajax({
        type: "POST",
        data: {
            commentId: commentId,
            text: text
        }, url: "/dashboard/saveEditComment"
    }).success(
        function (dataPost) {
            $div.next("textarea").remove();
            $div.next("span").remove();
            $div.html(text);
        });
}
