/*
 Copyright (c) 2012-2014 Open Lab
 Written by Roberto Bicchierai and Silvia Chelazzi http://roberto.open-lab.com
 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
var funcSelectionCurrentTaskId = 0;
function GridEditor(master) {
    this.master = master; // is the a GantEditor instance
    this.gridified = $.gridify($.JST.createFromTemplate({}, "TASKSEDITHEAD"));
    this.element = this.gridified.find(".gdfTable").eq(1);
}


GridEditor.prototype.fillEmptyLines = function () {
    var factory = new TaskFactory();

    //console.debug("GridEditor.fillEmptyLines");
    var rowsToAdd = 30 - this.element.find(".taskEditRow").size();

    //fill with empty lines
    for (var i = 0; i < rowsToAdd; i++) {
        var emptyRow = $.JST.createFromTemplate({}, "TASKEMPTYROW");
        //click on empty row create a task and fill above
        var master = this.master;
        emptyRow.click(function (ev) {
            var emptyRow = $(this);
            //add on the first empty row only
            if (!master.canWrite || emptyRow.prevAll(".emptyRow").size() > 0)
                return

            master.beginTransaction();
            var lastTask;
            var start = new Date().getTime();
            var level = 0;
            if (master.tasks[0]) {
                start = master.tasks[0].start;
                level = master.tasks[0].level + 1;
            }

            //fill all empty previouses
            emptyRow.prevAll(".emptyRow").andSelf().each(function () {
                var ch = factory.build("tmp_fk" + new Date().getTime(), "", "", level, start, 1);
                var task = master.addTask(ch);
                lastTask = ch;
            });
            master.endTransaction();
            lastTask.rowElement.click();
            lastTask.rowElement.find("[name=name]").focus()//focus to "name" input
                .blur(function () { //if name not inserted -> undo -> remove just added lines
                    var imp = $(this);
                    if (imp.val() == "") {
                        lastTask.name = "Task " + (lastTask.getRow() + 1);
                        imp.val(lastTask.name);
                    }
                });
        });
        this.element.append(emptyRow);
    }
};


GridEditor.prototype.addTask = function (task, row, hideIfParentCollapsed) {
    //console.debug("GridEditor.addTask",task,row);
    //var prof = new Profiler("editorAddTaskHtml");

    //remove extisting row
    this.element.find("[taskId=" + task.id + "]").remove();

    var taskRow = $.JST.createFromTemplate(task, "TASKROW");
    //save row element on task
    task.rowElement = taskRow;

    this.bindRowEvents(task, taskRow);

    if (typeof(row) != "number") {
        var emptyRow = this.element.find(".emptyRow:first"); //tries to fill an empty row
        if (emptyRow.size() > 0)
            emptyRow.replaceWith(taskRow);
        else
            this.element.append(taskRow);
    } else {
        var tr = this.element.find("tr.taskEditRow").eq(row);
        if (tr.size() > 0) {
            tr.before(taskRow);
        } else {
            this.element.append(taskRow);
        }

    }
    // this.element.find(".taskRowIndex").each(function (i, el) {
    //   $(el).html(i + 1);
    // });
    //prof.stop();


    //[expand]
    if (hideIfParentCollapsed) {
        if (task.collapsed) taskRow.find(".exp-controller").removeClass('exp');
        var collapsedDescendant = this.master.getCollapsedDescendant();
        if (collapsedDescendant.indexOf(task) >= 0) {
            taskRow.rowExpHide = true;
            taskRow.hide();
        }
    }


    return taskRow;
};

GridEditor.prototype.refreshExpandStatus = function (task) {
    if (!task) return;
    //[expand]
    var child = task.getChildren();
    if (child.length > 0 && task.rowElement.has(".expcoll").length == 0) {
        task.rowElement.find(".exp-controller").addClass('expcoll exp');
    }
    else if (child.length == 0 && task.rowElement.has(".expcoll").length > 0) {
        task.rowElement.find(".exp-controller").removeClass('expcoll exp');
    }

    var par = task.getParent();
    if (par && par.rowElement.has(".expcoll").length == 0) {
        par.rowElement.find(".exp-controller").addClass('expcoll exp');
    }

};

GridEditor.prototype.refreshTaskRow = function (task) {
    //console.debug("refreshTaskRow")
    //var profiler = new Profiler("editorRefreshTaskRow");
    var row = task.rowElement;

    row.find(".taskRowIndex").html(task.getRow() + 1);
    row.find(".indentCell").css("padding-left", task.level * 10 + 18);
    row.find("[name=name]").val(task.name);
    row.find("[name=duration]").val(task.duration);
    row.find("[name=start]").val(new Date(task.start).format('yy-MM-dd')).updateOldValue(); // called on dates only because for other field is called on focus event
    row.find("[name=end]").val(new Date(task.end).format('yy-MM-dd')).updateOldValue();
    row.find(".taskAssigs").html(task.assign);
};

GridEditor.prototype.redraw = function () {
    for (var i = 0; i < this.master.tasks.length; i++) {
        this.refreshTaskRow(this.master.tasks[i]);
    }
};

GridEditor.prototype.reset = function () {
    this.element.find("[taskid]").remove();
};


GridEditor.prototype.bindRowEvents = function (task, taskRow) {
    var self = this;
    //console.debug("bindRowEvents",this,this.master,this.master.canWrite, task.canWrite);
    if (this.master.canWrite && task.canWrite) {
        self.bindRowInputEvents(task, taskRow);

    } else { //cannot write: disable input
        taskRow.find("input").attr("readonly", true);
    }

    self.bindRowExpandEvents(task, taskRow);
};


GridEditor.prototype.bindRowExpandEvents = function (task, taskRow) {
    var self = this;
    //expand collapse
    taskRow.find(".exp-controller").click(function () {
        //expand?
        var el = $(this);
        var taskId = el.closest("[taskid]").attr("taskid");
        var task = self.master.getTask(taskId);
        var descs = task.getDescendant();
        el.toggleClass('exp');
        task.collapsed = !el.is(".exp");
        var collapsedDescendant = self.master.getCollapsedDescendant();

        if (el.is(".exp")) {
            for (var i = 0; i < descs.length; i++) {
                var childTask = descs[i];
                if (collapsedDescendant.indexOf(childTask) >= 0) continue;
                childTask.rowElement.show();
                descs[i].rowExpHide = false;
            }
        } else {
            for (var i = 0; i < descs.length; i++) {
                descs[i].rowExpHide = true;
                descs[i].rowElement.hide();
            }
        }
        self.master.gantt.refreshGantt();
    });
};

GridEditor.prototype.bindRowInputEvents = function (task, taskRow) {
    var self = this;

    //bind dateField on dates
    taskRow.find("input").each(function () {
        $(this).attr("readonly", "true");
    });

    //bind row selection
    taskRow.click(function () {
        var row = $(this);
        //var isSel = row.hasClass("rowSelected");
        row.closest("table").find(".rowSelected").removeClass("rowSelected");
        row.addClass("rowSelected");
        funcSelectionCurrentTaskId = row.attr("taskId");
        //set current task
        self.master.currentTask = self.master.getTask(row.attr("taskId"));

        //move highlighter
        self.master.gantt.synchHighlight();

        //if offscreen scroll to element
        var top = row.position().top;
        if (top > self.element.parent().height()) {
            row.offsetParent().scrollTop(top - self.element.parent().height() + 100);
        } else if (top < 40) {
            row.offsetParent().scrollTop(row.offsetParent().scrollTop() - 40 + top);
        }
    });

};