/**
 * Created by enkhamgalan on 4/9/15.
 */
function setFloorsFn(id, task) {
    floorSelectTask = task;
    id = id.substring(1, id.length);
    var diaHtml = "<div style='margin: 10px' class=noselect>" +
        "Дэд бүлгүүд: <span class='teamworkIcon' style='cursor: pointer' onclick=addFloor('')>+</span>" +
        "<button onclick=saveFloor('" + id + "') class='button' style='padding: 1px 4px;margin-left: 70px'>Хадгалах</button>" +
        "<div id=floorValues><img src='/public/images/loading.gif'/></div></div>";
    floorInputIndex = 0;
    createBlackPage(320, 500).html(diaHtml);
    $.ajax({
        type: "POST",
        data: {oid: id},
        url: "/ganttGetFloors"
    }).success(function (data) {
        $('div#floorValues').html(data);
    });
}

var floorSelectTask, floorInputIndex;
function setFloorFn(id, task) {
    floorSelectTask = task;
    id = id.substring(1, id.length);
    var diaHtml = "<div id=floorSelectionDiv style='margin: 10px;text-align: center' class=noselect><img src='/public/images/loading.gif'/></div>";
    createBlackPage(380, 120).html(diaHtml);
    $.ajax({
        type: "POST",
        data: {tid: id},
        url: "/ganttGetFloor"
    }).success(function (data) {
        $('div#floorSelectionDiv').html(data);
    });
}
function addFloor(val) {
    $("div#floorValues").append(addFloorTag(val));
    floorInputIndex++;
}
function addFloorTag(val) {
    return ("<div id=fi-" + floorInputIndex + "><input id=0 class=floorName type='text' style='width: 250px' value='" + val + "'>" +
    "<span class='teamworkIcon delAssig' style='cursor: pointer' onclick='removeFloor(" + floorInputIndex + ",0)'>d</span></div>");
}

function removeFloor(findex, type) {
    if (type == 1) {
        $("div#floorValues div#fi-s" + findex).remove();
        $.ajax({
            type: "POST", data: {floorId: findex},
            url: "/ganttDelFloors"
        }).complete(function () {
            floorSelectTask.floor--;
            $('div#main-content table.gdfTable td#floor' + floorSelectTask.id).html(floorSelectTask.floor);
        });
    } else $("div#floorValues div#fi-" + findex).remove();
}
function saveFloor(oid) {
    $("#__blackpopup__").trigger("close");
    var ids = [], names = [], i = 0;
    $("div#floorValues input").each(function () {
        ids[i] = $(this).attr('id');
        names[i] = $(this).val();
        i++;
    });
    $.ajax({
        type: "POST",
        data: {oid: oid, ids: ids, names: names},
        url: "/ganttUptFloors"
    }).success(function (data) {
        floorSelectTask.floor = data;
        $('div#main-content table.gdfTable td#floor' + floorSelectTask.id).html(data);
    });
}
function setFloor(tid) {
    $("#__blackpopup__").trigger("close");
    var selectedFloor = $('div#floorSelectionDiv option:selected');
    $.ajax({
        type: "POST", data: {tid: tid, fid: selectedFloor.val()},
        url: "/ganttSetFloor"
    });
    $('div#main-content table.gdfTable td#floor' + floorSelectTask.id).html(selectedFloor.text());
    floorSelectTask.floor = selectedFloor.text();
}
/*-----------------------------------------------Assign-----------------------------------------*/
function setAssignFn(id, task) {
    floorSelectTask = task;
    id = id.substring(1, id.length);
    var diaHtml = "<div style='margin: 10px' class=noselect>" +
        "<button type='button' class='' data-toggle='modal' data-target='#modal-select' id='selectSendUserButton' onclick='showAssignSelectDia()'>Хариуцагчид</button>" +
        "<button onclick=saveUsers('" + id + "') class='button' style='padding: 1px 4px;margin-left: 70px'>Хадгалах</button>" +
        "<div id=assignValues></div></div>";
    floorInputIndex = 0;
    createBlackPage(assignDialogWidth, 500).html(diaHtml);

    if (task.level > 1 && task.getChildren().length == 0) {
        $.ajax({
            type: "POST",
            data: {tid: id},
            url: "/ganttGetUsers"
        }).success(function (data) {
            $('div#assignValues').html(data);
        });
    }
}
function showAssignSelectDia() {
    closeButtonClick = false;
    sendToUserRemoveAll();
    $("div#assignValues div.assinedUsers").each(function () {
        $('div#divSelUser td#' + $(this).attr('uid')).attr('bsan', 1).parent().click();
    });
}

function removeUser(uindex, type) {
    if (type == 1) {
        $("div#assignValues div#fi-s" + uindex).remove();
        $.ajax({
            type: "POST", data: {aid: uindex},
            url: "/ganttDelUsers"
        }).success(function (data) {
            floorSelectTask.assign = data.names;
            floorSelectTask.assignIds = data.ids;
            $('div#main-content table.gdfTable td#assign' + floorSelectTask.id).html(data.names);
            updateAssignFilter();
        });
    } else $("div#assignValues div#fi-" + uindex).remove();
}
function saveUsers(tid) {
    $("#__blackpopup__").trigger("close");
    var ids = [], uids = [], vals = [];
    var counter = 0;
    $("div#assignValues div.assinedUsers").each(function () {
        ids[counter] = $(this).find('div').attr('id');
        uids[counter] = $(this).find('div').attr('uid');
        vals[counter] = $(this).find('input').val();
        counter++;
    });
    $.ajax({
        type: "POST",
        data: {tid: tid, level: floorSelectTask.level, ids: ids, uids: uids, vals: vals},
        url: "/ganttUptUsers"
    }).success(function (data) {
        if (floorSelectTask.level > 1 && floorSelectTask.getChildren().length == 0) {
            floorSelectTask.assign = data.names;
            floorSelectTask.assignIds = data.ids;
            $('div#main-content table.gdfTable td#assign' + floorSelectTask.id).html(data.names);
        } else {
            var children = floorSelectTask.getChildren();
            var ch;
            var tagCh = $('div#main-content table.gdfTable');
            for (var i = 0; i < children.length; i++) {
                ch = children[i];
                if (ch.getChildren().length == 0) {
                    ch.assign = data.names;
                    ch.assignIds = data.ids;
                    tagCh.find('td#assign' + ch.id).html(data.names);
                }
            }
        }
        floorSelectTask.master.tasks[0].assignIds = data.projectIds;
        setTaskParentAssigns(floorSelectTask, 1, data.objectIds);
        updateAssignFilter();
    });
}
function setTaskParentAssigns(task, level, array) {
    while (task.level != level) {
        task = task.getParent();
        setTaskParentAssigns(task, level, array);
    }
    task.assignIds = array;
}
function updateAssignFilter() {
    $.ajax({
        type: "POST",
        url: "/ganttGetAssignFilter"
    }).success(function (data) {
        $('select#selectUserId').html(data);
    });
}
function setResourcesFn(rtype, id, task) {
    var textTitle;
    if (rtype == 1)textTitle = "Материал";
    else if (rtype == 2)textTitle = "Техник";
    else if (rtype == 3)textTitle = "Хүн хүч";

    floorSelectTask = task;
    id = id.substring(1, id.length);
    var diaHtml = "<div style='margin: 10px' class=noselect>" +
        textTitle + ": <span class='teamworkIcon' style='cursor: pointer;' onclick=addResource(" + rtype + ")>+</span>" +
        "<button onclick=saveResource(" + rtype + ",'" + id + "') class='button' style='padding: 1px 4px;margin-left: 70px'>Хадгалах</button>" +
        "<div id=resourceValues><img src='/public/images/loading.gif'/></div></div>";
    floorInputIndex = 0;
    createBlackPage(360, 500).html(diaHtml);
    $.ajax({
        type: "POST",
        data: {rtype: rtype, tid: id},
        url: "/ganttGetResources"
    }).success(function (data) {
        $('div#resourceValues').html(data);
    });
}
function addResource(rtype) {
    $.ajax({
        type: "POST",
        data: {rtype: rtype},
        url: "/ganttAddResource"
    }).success(function (data) {
        $("div#resourceValues").append("<div id=fi-" + floorInputIndex + " class='resVals'>" + data +
            "<span class='teamworkIcon delAssig' style='cursor: pointer' onclick='removeResource(" + rtype + "," + floorInputIndex + ",0)'>d</span></div>");
        floorInputIndex++;
    });
}
function removeResource(rtype, id, remtype) {
    if (remtype == 1) {
        $("div#resourceValues div#fi-s" + id).remove();
        $.ajax({
            type: "POST", data: {rid: id, rtype: rtype},
            url: "/ganttDelResource"
        }).complete(function () {
            if (rtype == 1) {
                floorSelectTask.material--;
                $('div#main-content table.gdfTable td#material' + floorSelectTask.id).html(floorSelectTask.material);
            } else if (rtype == 2) {
                floorSelectTask.technical--;
                $('div#main-content table.gdfTable td#technical' + floorSelectTask.id).html(floorSelectTask.technical);
            } else if (rtype == 3) {
                floorSelectTask.person--;
                $('div#main-content table.gdfTable td#person' + floorSelectTask.id).html(floorSelectTask.person);
            }
        });
    } else $("div#resourceValues div#fi-" + id).remove();
}
function saveResource(rtype, tid) {
    $("#__blackpopup__").trigger("close");
    var ids = "", rids = "", vals = "";
    $("div#resourceValues div.resVals").each(function () {
        ids += "&ids=" + $(this).find('select').val();
        ids += "&rids=" + $(this).find('input').attr('id');
        vals += "&vals=" + $(this).find('input').val();
    });
    if (ids.length > 0) {
        $.ajax({
            type: "POST", data: "rtype=" + rtype + "&tid=" + tid + rids + ids + vals,
            url: "/ganttUptResource"
        }).success(function (data) {
            if (rtype == 1) {
                floorSelectTask.material = data;
                $('div#main-content table.gdfTable td#material' + floorSelectTask.id).html(data);
            } else if (rtype == 2) {
                floorSelectTask.technical = data;
                $('div#main-content table.gdfTable td#technical' + floorSelectTask.id).html(data);
            } else if (rtype == 3) {
                floorSelectTask.person = data;
                $('div#main-content table.gdfTable td#person' + floorSelectTask.id).html(data);
            }
        });
    }
}
