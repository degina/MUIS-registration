var ganttCopyCurrentTask;
var copytasks = [];
function copyGanttItem() {
    $('ul#menuPanel').hide();
    if (ganttCopyCurrentTask.level > 0) {
        copytasks = [];
        var masterTasks = ganttCopyCurrentTask.master.tasks;
        copytasks[0] = masterTasks[ganttCopyCurrentTask.getRow()];
        var selectedLevel = copytasks[0].level;
        var indexCounter = 1;
        for (var i = ganttCopyCurrentTask.getRow() + 1; i < masterTasks.length; i++) {
            if (masterTasks[i].level > selectedLevel) {
                copytasks[indexCounter] = masterTasks[i];
                indexCounter++;
            } else i = masterTasks.length;
        }
    }
}
function pasteGanttItem() {
    $('ul#menuPanel').hide();
    if (copytasks.length > 0 && ganttCopyCurrentTask.level > 0) {
        var self = ganttCopyCurrentTask.master;
        var pasteRowIndex, startPasteRowIndex = 0;
        var factory = new TaskFactory();
        pasteRowIndex = ganttCopyCurrentTask.getRow() + 1;
        self.beginTransaction();

        if (ganttCopyCurrentTask.name.length == 0) {
            startPasteRowIndex = 1;
            ganttCopyCurrentTask.name = copytasks[0].name;
            ganttCopyCurrentTask.level = copytasks[0].level;
            ganttCopyCurrentTask.rowElement.attr('level', ganttCopyCurrentTask.level);
            ganttCopyCurrentTask.start = copytasks[0].start;
            ganttCopyCurrentTask.duration = copytasks[0].duration;
            ganttCopyCurrentTask.scopePercent = copytasks[0].scopePercent;
            if (copytasks[0].end == undefined)ganttCopyCurrentTask.end = computeEndByDuration(ganttCopyCurrentTask.start, ganttCopyCurrentTask.duration);
            else ganttCopyCurrentTask.end = copytasks[0].end;
        }
        for (var i = startPasteRowIndex; i < copytasks.length; i++) {
            var ch = factory.build("tmp_" + new Date().getTime(), copytasks[i].name, "", copytasks[i].level, copytasks[i].start, copytasks[i].duration, copytasks[i].scopePercent);
            self.addTask(ch, pasteRowIndex);
            ch.master.editor.refreshExpandStatus(ch);
            pasteRowIndex++;
        }
        self.endTransaction();
    }
}
function saveGanttOnTemplate() {
    var temp_name = window.prompt("Загварын нэр?");
    if (temp_name && temp_name.length > 0) {
        copyGanttItem();
        var names = [];
        var levels = [];
        var durations = [];
        var scopePercents = [];
        for (var i = 0; i < copytasks.length; i++) {
            names[i] = copytasks[i].name;
            levels[i] = copytasks[i].level;
            durations[i] = copytasks[i].duration;
            scopePercents[i] = copytasks[i].scopePercent;
        }
        $.ajax({
            dataType: "json",
            data: {
                name: temp_name,
                names: names,
                levels: levels,
                durations: durations,
                scopePercents: scopePercents
            },
            type: "POST",
            url: "/ganttSaveTemplate"
        }).complete(function () {
            showSuccessMessage("'" + temp_name + "' Загвар хадгалагдсан");
        });
    }
}
function loadTemplate(project) {
    var templateLoadDiv = $('div#templateLoad');
    templateLoadDiv.modal('show');
    loading2('div#templateLoad .modal-body');
    $.ajax({
        type: "POST",
        data: {project: project},
        url: "/ganttLoadTemplates"
    }).success(function (data) {
        templateLoadDiv.find("div.modal-body").html(data);
    });
}
function templateDelete(id) {
    $.ajax({
        type: "POST",
        data: "id=" + id,
        url: "/ganttDeleteTemplate"
    });
}
function templateSelect(id, project) {
    var startDate;
    if (project) {
        var date = new Date(ganttCopyCurrentTask.start);
        startDate = date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate() + " 00:00:00";
    }
    $.ajax({
        type: "POST",
        data: {id: id, projStart: startDate},
        url: project ? "/ganttLoadProjectTemp" : "/ganttGetTemplate"
    }).success(function (data) {
        $('div#templateLoad').modal('hide');
        if (project) {
            ge.loadProject(data);
        } else {
            copytasks = data;
            pasteGanttItem();
        }
    });
}
var assignFilterSelectedUser = 0;
function assignFilterChange(uid) {
    assignFilterSelectedUser = uid;
    ge.redraw();
}
function saveProjectTemplate() {
    var temp_name = window.prompt("Төслийн загварын нэр?");
    if (temp_name && temp_name.length > 0) {
        var prj = ge.saveProject();
        $.ajax({
            dataType: "json",
            data: {name: temp_name, prj: JSON.stringify(prj)},
            type: "POST",
            url: "/ganttSaveProjectTemp"
        }).complete(function () {
            showSuccessMessage("Төслийн загвар хадгалагдсан");
        });
    }
}