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
jQuery.fn.forceNumericOnly =
    function () {
        $(this).keydown(function (e) {
            var key = e.charCode || e.keyCode || 0;
            // allow backspace, tab, delete, arrows, numbers and keypad numbers ONLY
            return (
            key == 8 ||
            key == 9 ||
            key == 46 ||
            (key >= 37 && key <= 40) ||
            (key >= 48 && key <= 57) ||
            (key >= 96 && key <= 105));
        });
    };
jQuery.fn.forceNumericCommaOnly =
    function () {
        $(this).keydown(function (e) {
            var key = e.charCode || e.keyCode || 0;
            console.log(key)
            // allow backspace, tab, delete, arrows, numbers and keypad numbers ONLY
            return (
            key == 188 ||
            key == 8 ||
            key == 9 ||
            key == 46 ||
            (key >= 37 && key <= 40) ||
            (key >= 48 && key <= 57) ||
            (key >= 96 && key <= 105));
        });
    };

jQuery.fn.forceNumericOnlyFloat =
    function () {
        $(this).keydown(function (e) {
            var key = e.charCode || e.keyCode || 0;
            // allow backspace, tab, delete, arrows, numbers and keypad numbers ONLY
            return (
            key == 8 ||
            key == 190 ||  // is point
            key == 110 ||  // is point
            key == 9 ||
            key == 46 ||
            (key >= 37 && key <= 40) ||
            (key >= 48 && key <= 57) ||
            (key >= 96 && key <= 105));
        });
    };
function GridEditor(master) {
    this.master = master; // is the a GantEditor instance
    this.gridified = $.gridify($.JST.createFromTemplate({}, "TASKSEDITHEAD"));
    this.element = this.gridified.find(".gdfTable").eq(1);
}


GridEditor.prototype.fillEmptyLines = function () {
    var factory = new TaskFactory();

    //console.debug("GridEditor.fillEmptyLines");
    var rowsToAdd = 30 - this.element.find(".taskEditRow").size();
    if (rowsToAdd < 12)rowsToAdd = 12;
    //fill with empty lines
    for (var i = 0; i < rowsToAdd; i++) {
        var emptyRow = $.JST.createFromTemplate({}, "TASKEMPTYROW");
        //click on empty row create a task and fill above
        var master = this.master;
        emptyRow.mousedown(function (e) {
            var emptyRow = $(this);
            //add on the first empty row only
            if (!master.canWrite || emptyRow.prevAll(".emptyRow").size() > 0)
                return;
            /*
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
             //lastTask.name = "Task " + (lastTask.getRow() + 1);
             lastTask.name = "";
             imp.val(lastTask.name);
             }
             });
             */
            if (e.button == 2) {
                ganttCopyCurrentTask = master.tasks[master.tasks.length - 1];
                if (ganttCopyCurrentTask.level > 0) {
                    $('ul#menuPanel li').each(function () {
                        if ($(this).hasClass('nonProject') && $(this).hasClass('paste'))$(this).show();
                        else $(this).hide();
                    });
                    $('ul#menuPanel').css('top', e.pageY - 2 + "px").css('left', e.pageX - 2 + "px").show();
                }
            }
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
        if (collapsedDescendant.indexOf(task) >= 0) taskRow.hide();
    }


    return taskRow;
};

GridEditor.prototype.refreshExpandStatus = function (task) {
    if (!task) return;
    task.rowElement.attr('level', task.level);
    //[expand]
    var child = task.getChildren();
    if (child.length > 0 && task.rowElement.has(".expcoll").length == 0) {
        task.rowElement.find(".exp-controller").addClass('expcoll exp');
        task.rowElement.find("input[name=name]").addClass('groupTask');
    }
    else if (child.length == 0 && task.rowElement.has(".expcoll").length > 0) {
        task.rowElement.find(".exp-controller").removeClass('expcoll exp');
        task.rowElement.find("input[name=name]").removeClass('groupTask');
    }

    var par = task.getParent();
    if (par && par.rowElement.has(".expcoll").length == 0) {
        par.rowElement.find(".exp-controller").addClass('expcoll exp');
        par.rowElement.find("input[name=name]").addClass('groupTask');
    }

};

GridEditor.prototype.refreshTaskRow = function (task) {
    //console.debug("refreshTaskRow")
    //var profiler = new Profiler("editorRefreshTaskRow");
    var row = task.rowElement;

    row.find(".taskRowIndex").html(task.getRow() + 1);
    row.find(".indentCell").css("padding-left", task.level * 10 + 18);
    row.find("[name=name]").val(task.name);
    row.find("[name=code]").val(task.code);
    row.find("[status]").attr("status", task.status);
    row.find("[name=duration]").val(task.duration);
    row.find("[name=scopePercent]").val(task.scopePercent).scopePercentVal(task.level);
    row.find("[name=start]").val(new Date(task.start).format('yy-MM-dd')).updateOldValue(); // called on dates only because for other field is called on focus event
    row.find("[name=end]").val(new Date(task.end).format('yy-MM-dd')).updateOldValue();
    row.find("[name=depends]").val(task.depends).forceNumericCommaOnly();
    row.find(".taskAssigs").html(task.assign);
    row.find(".subGroup").html(task.floor);
    row.find("[name=workCount]").val(task.workCount);
    row.find("[name=person]").val(task.person).forceNumericOnly();
    //profiler.stop();
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

    taskRow.find(".taskAssigs").click(function () {//amgaa
        if (!task.isNew()) {
            if (task.level > 0)setAssignFn(task.id, task);
            else showErrorMessage("Зөвхөн ажил, болон бүлэг дээр");
        } else showErrorMessage("Эхлээд заавал хадгалах товч дарах ёстой");
    });
    taskRow.find(".mcenter").click(function () {
        if (!task.isNew()) {
            if (task.level > 1)setResourcesFn($(this).attr('rtype'), task.id, task);
            else showErrorMessage("Зөвхөн ажил дээр");
        } else showErrorMessage("Эхлээд заавал хадгалах товч дарах ёстой");
    });

    taskRow.find(".subGroup").click(function () {//amgaa
        if (!task.isNew()) {
            if (task.level == 1) {
                setFloorsFn(task.id, task);
            } else if (task.level > 1) {
                setFloorFn(task.id, task);
            } else showErrorMessage("Зөвхөн ажил дээр");
        } else showErrorMessage("Эхлээд заавал хадгалах товч дарах ёстой");
    });
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
}

GridEditor.prototype.bindRowInputEvents = function (task, taskRow) {
    var self = this;

    //bind dateField on dates
    taskRow.find(".date").each(function () {
        var el = $(this);

        //start is readonly in case of deps
        if (task.depends && el.attr("name") == "start") {
            el.attr("readonly", "true");
        } else {
            el.removeAttr("readonly");
        }
        el.click(function () {
            if (el.attr("name") == "start") {
                if (setPeriodTimeType == 0) el.attr("readonly", "true");
                else el.removeAttr("readonly");
            } else if (el.attr("name") == "end") {
                if (setPeriodTimeType == 1)el.attr("readonly", "true");
                else el.removeAttr("readonly");
            }
            var inp = $(this);
            inp.dateField({
                inputField: el
            });
        });

        el.blur(function (date) {
            var inp = $(this);
            if (inp.isValueChanged()) {
                if (!Date.isValid(inp.val())) {
                    alert(GanttMaster.messages["INVALID_DATE_FORMAT"]);
                    inp.val(inp.getOldValue());

                } else {
                    var date = Date.parseString(inp.val());
                    var row = inp.closest("tr");
                    var taskId = row.attr("taskId");
                    var task = self.master.getTask(taskId);
                    var lstart = task.start;
                    var lend = task.end;

                    if (inp.attr("name") == "start") {
                        lstart = date.getTime();
                        if (setPeriodTimeType == 2) { //duration freeze
                            if (lstart >= lend) {
                                var end_as_date = new Date(lstart);
                                lend = end_as_date.add('d', task.duration).getTime();
                            }
                            //update task from editor
                            self.master.beginTransaction();
                            self.master.moveTask(task, lstart);
                            self.master.endTransaction();
                        } else if (setPeriodTimeType == 1) { //end freeze
                            //update task from editor
                            self.master.beginTransaction();
                            self.master.changeTaskDates(task, lstart, lend);
                            self.master.endTransaction();
                        }
                    } else {
                        lend = date.getTime();
                        if (setPeriodTimeType == 0) { //start freeze
                            if (lstart >= lend) {
                                lend = lstart;
                            }
                            lend = lend + 3600000 * 20; // this 20 hours are mandatory to reach the correct day end (snap to grid)
                            self.master.beginTransaction();
                            self.master.changeTaskDates(task, lstart, lend);
                            self.master.endTransaction();
                        } else if (setPeriodTimeType == 2) { //duration freeze
                            lend = computeEndPrevDate(lend);
                            var stepDay;
                            var d = new Date(lstart);
                            d.setHours(0, 0, 0, 0);
                            if (task.end <= lend) {
                                stepDay = recomputeDuration(task.end, lend);
                                for (var i = 1; i < stepDay; i++) {
                                    if (isHoliday(d))stepDay++;
                                    d.setDate(d.getDate() + 1);
                                }
                            } else {
                                stepDay = recomputeDuration(lend, task.end);
                                for (var i = 1; i < stepDay; i++) {
                                    if (isHoliday(d))stepDay++;
                                    d.setDate(d.getDate() - 1);
                                }
                            }
                            lstart = d.getTime();
                            self.master.beginTransaction();
                            self.master.moveTask(task, lstart);
                            self.master.endTransaction();
                        }
                    }
                    inp.updateOldValue(); //in order to avoid multiple call if nothing changed
                }
            }
        });
    });
    taskRow.find("input[name='duration']").click(function () {
        if (setPeriodTimeType == 2) {
            $(this).attr("readonly", "true");
        } else $(this).removeAttr("readonly");
    });

    //binding on blur for task update (date exluded as click on calendar blur and then focus, so will always return false, its called refreshing the task row)
    taskRow.find("input:not(.date)").focus(function () {
        $(this).updateOldValue();
    }).blur(function () {
        var el = $(this);
        if (el.isValueChanged()) {
            var row = el.closest("tr");
            var taskId = row.attr("taskId");

            var task = self.master.getTask(taskId);

            //update task from editor
            var field = el.attr("name");

            self.master.beginTransaction();

            if (field == "depends") {
                var oldDeps = task.depends;
                task.depends = el.val();

                //start is readonly in case of deps
                if (task.depends) {
                    row.find("[name=start]").attr("readonly", "true");
                } else {
                    row.find("[name=start]").removeAttr("readonly");
                }


                // update links
                var linkOK = self.master.updateLinks(task);
                if (linkOK && task.getParent().duration > 0) {
                    //synchronize status from superiors states
                    var sups = task.getSuperiors();
                    for (var i = 0; i < sups.length; i++) {
                        if (!sups[i].from.synchronizeStatus())
                            break;
                    }
                    self.master.changeTaskDates(task, task.start, task.end, task.duration); // fake change to force date recomputation from dependencies
                }

            } else if (field == "duration") {
                var dur;
                if (el.val() == 0)dur = 0;
                else dur = parseInt(el.val()) || 1;
                el.val(dur);
                if (setPeriodTimeType == 0) {
                    var newEnd = computeEndByDuration(task.start, dur);
                    self.master.changeTaskDates(task, task.start, newEnd, dur);
                } else if (setPeriodTimeType == 1) {
                    var newStart = computeStartByDuration(task.end, dur);
                    self.master.changeTaskDates(task, newStart, task.end, dur);
                }
            } else if (field == "name" && el.val() == "") { // remove unfilled task
                var par = task.getParent();
                task.deleteTask();
                //[expand]
                if (par) self.master.editor.refreshExpandStatus(par);
            } else {
                task[field] = el.val();
            }
            self.master.endTransaction();
        }
    });

    //cursor key movement
    taskRow.find("input").keydown(function (event) {
        var theCell = $(this);
        var theTd = theCell.parent();
        var theRow = theTd.parent();
        var col = theTd.prevAll("td").size();

        var ret = true;
        switch (event.keyCode) {

            case 37: //left arrow
                if (this.selectionEnd == 0)
                    theTd.prev().find("input").focus();
                break;
            case 39: //right arrow
                if (this.selectionEnd == this.value.length)
                    theTd.next().find("input").focus();
                break;

            case 38: //up arrow
                var prevRow = theRow.prev();
                var td = prevRow.find("td").eq(col);
                var inp = td.find("input");

                if (inp.size() > 0)
                    inp.focus();
                break;
            case 40: //down arrow
                var nextRow = theRow.next();
                var td = nextRow.find("td").eq(col);
                var inp = td.find("input");
                if (inp.size() > 0)
                    inp.focus();
                //else nextRow.click(); //create a new row
                break;
            case 36: //home
                break;
            case 35: //end
                break;
            case 8: //backspace
                if ($(this).val().length == 0)return false;
                break;
            case 9: //tab
                break;
            case 13: //enter
                //theRow.next().click();
                //$('#workSpace').trigger('addNewCurrentTask.gantt');
                if (theCell.attr('name') == "name") {
                    var factory = new TaskFactory();
                    self.master.beginTransaction();
                    var ch;
                    var row = 0;
                    if (self.master.currentTask) {
                        if (self.master.currentTask.level <= 0)
                            return;
                        ch = factory.build("tmp_" + new Date().getTime(), "", "", self.master.currentTask.level, self.master.currentTask.start, 1);
                        row = self.master.currentTask.getRow() + 1;
                    } else {
                        ch = factory.build("tmp_" + new Date().getTime(), "", "", 0, new Date().getTime(), 1);
                    }
                    var task = self.master.addTask(ch, row);
                    if (task) {
                        task.rowElement.click();
                        task.rowElement.find("[name=name]").focus();
                    }
                    self.master.endTransaction();
                } else if (theCell.attr('name') == "scopePercent"
                    || theCell.attr('name') == "duration"
                    || theCell.attr('name') == "depends"
                    || theCell.attr('name') == "workCount") {
                    var nextRow = theRow.next();
                    var td = nextRow.find("td").eq(col);
                    var inp = td.find("input");
                    if (inp.size() > 0)inp.focus();
                    break;
                }
                break;
        }
        return ret;

    }).focus(function () {
        $(this).closest("tr").click();
    });


    //change status
    taskRow.find(".taskStatus").click(function () {
        var el = $(this);
        var tr = el.closest("[taskid]");
        var taskId = tr.attr("taskid");
        var task = self.master.getTask(taskId);

        var changer = $.JST.createFromTemplate({}, "CHANGE_STATUS");
        changer.find("[status=" + task.status + "]").addClass("selected");
        changer.find(".taskStatus").click(function (e) {
            e.stopPropagation();
            var newStatus = $(this).attr("status");
            changer.remove();
            self.master.beginTransaction();
            task.changeStatus(newStatus);
            self.master.endTransaction();
            el.attr("status", task.status);
        });
        el.oneTime(3000, "hideChanger", function () {
            changer.remove();
        });
        el.after(changer);
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
    taskRow.find("input[name=name]").mousedown(function (e) {
        if (e.button == 2) {
            ganttCopyCurrentTask = self.master.getTask($(this).parent().parent().attr("taskId"));
            if (ganttCopyCurrentTask.level > 0) {
                $('ul#menuPanel li').each(function () {
                    if ($(this).hasClass('nonProject'))$(this).show();
                    else $(this).hide();
                });
            } else {
                var hasChildren = (ganttCopyCurrentTask.getChildren().length > 0);
                $('ul#menuPanel li').each(function () {
                    if ($(this).hasClass('project')) {
                        if (hasChildren) {
                            if ($(this).hasClass('hasChild'))$(this).show();
                            else $(this).hide();
                        } else {
                            if ($(this).hasClass('noChild'))$(this).show();
                            else $(this).hide();
                        }
                    } else $(this).hide();
                });
            }
            var offsetThis = $(this).offset();
            $('ul#menuPanel').css('top', (offsetThis.top + 20) + "px").css('left', offsetThis.left + "px").show();
        }
    });
};
var scopePercentCheck = 0;
jQuery.fn.scopePercentVal =
    function (level) {
        if (level != 0) {
            $(this).keydown(function (e) {
                var key = e.charCode || e.keyCode || 0;
                // allow backspace, tab, delete, arrows, numbers and keypad numbers ONLY
                return (
                key == 8 ||
                key == 190 ||  // is point
                key == 110 ||  // is point
                key == 9 ||  // tab
                key == 46 ||
                (key >= 37 && key <= 40) ||
                (key >= 48 && key <= 57) ||
                (key >= 96 && key <= 105));
            });
            $(this).keyup(function (e) {
                var key = e.charCode || e.keyCode || 0;
                if (
                    key == 8 ||
                    key == 190 ||  // is point
                    key == 110 ||  // is point
                    key == 9 ||  // tab
                    key == 46 ||
                    (key >= 37 && key <= 40) ||
                    (key >= 48 && key <= 57) ||
                    (key >= 96 && key <= 105)) {
                    scopePercentCheck = 0;
                    $('table#mainGanttTable input.scope').each(function () {
                        scopePercentCheck += parseFloat($(this).val());
                        $(this).removeClass('scopeError');
                    });
                    if (!isNaN(scopePercentCheck)) {
                        $('span#scopeSumma').text(scopePercentCheck);
                        if (parseInt(scopePercentCheck) != 100) {
                            $(this).addClass('scopeError');
                            $('span#scopeSumma').addClass('scopeError');
                        } else {
                            $(this).removeClass('scopeError');
                            $('span#scopeSumma').removeClass('scopeError');
                        }
                    }
                }
                return true;
            });
            $(this).focus(function () {
                scopePercentCheck = 0;
                var tableSelected = $('table#mainGanttTable tbody');
                tableSelected.find('tr').each(function () {
                    if ($(this).attr('level') != undefined)$(this).find("input[name=scopePercent]").removeClass('scope').parent().removeClass('scope');
                });
                $(this).addClass('scope').parent().addClass('scope');
                var scopeTr = $(this).parent().parent();
                var selectedLevel = scopeTr.attr('level');
                var rowIndex = parseInt(tableSelected.find('tr').index(scopeTr));
                if (!isNaN(parseFloat($(this).val()))) {
                    scopePercentCheck += parseFloat($(this).val());
                } else scopePercentCheck -= 100;
                var i = rowIndex - 1;
                var indexedRow = tableSelected.find('tr').eq(i);
                scopeTr = parseInt(tableSelected.find('tr').length);
                var selectPercentInput;
                while (i > 0) {
                    if (selectedLevel <= indexedRow.attr('level')) {
                        selectPercentInput = indexedRow.find("input[name=scopePercent]");
                        if (selectedLevel == indexedRow.attr('level')) {
                            if (!isNaN(parseFloat(selectPercentInput.val()))) {
                                scopePercentCheck += parseFloat(selectPercentInput.val());
                            } else scopePercentCheck -= 100;
                            selectPercentInput.addClass('scope').parent().addClass('scope');
                        }
                        i--;
                        indexedRow = tableSelected.find('tr').eq(i);
                    } else i = 0;
                }
                i = rowIndex + 1;
                indexedRow = tableSelected.find('tr').eq(i);
                while (i < scopeTr) {
                    if (indexedRow.attr('level') != undefined && selectedLevel <= indexedRow.attr('level')) {
                        selectPercentInput = indexedRow.find("input[name=scopePercent]");
                        if (selectedLevel == indexedRow.attr('level')) {
                            if (!isNaN(parseFloat(selectPercentInput.val()))) {
                                scopePercentCheck += parseFloat(selectPercentInput.val());
                            } else scopePercentCheck -= 100;
                            selectPercentInput.addClass('scope').parent().addClass('scope');
                        }
                        i++;
                        indexedRow = tableSelected.find('tr').eq(i);
                    } else i = scopeTr;
                }
                if (!isNaN(scopePercentCheck)) {
                    $('span#scopeSumma').text(scopePercentCheck);
                    if (parseInt(scopePercentCheck) != 100) {
                        $(this).addClass('scopeError');
                        $('span#scopeSumma').addClass('scopeError');
                    } else {
                        $(this).removeClass('scopeError');
                        $('span#scopeSumma').removeClass('scopeError');
                    }
                }
            });
        } else {
            $(this).attr('readonly', 'readonly');
        }
    };
function checkScope() {
    var invalid = true;
    var tableSelected = $('table#mainGanttTable tbody');
    tableSelected.find('tr').each(function () {
        if ($(this).attr('level') != undefined)$(this).find("input[name=scopePercent]").removeClass('scope').attr('saveChecked', false).parent().removeClass('scope');
    });
    tableSelected.find('tr input[name=scopePercent]').each(function () {// check scopes;
        var scopeTr = $(this).parent().parent();
        scopePercentCheck = 0;
        if ($(this).attr('saveChecked') == "false" && scopeTr != undefined && parseInt(scopeTr.attr('level')) > 0) {
            $(this).addClass('scope').parent().addClass('scope');
            var selectedLevel = scopeTr.attr('level');
            var rowIndex = parseInt(tableSelected.find('tr').index(scopeTr));
            if (!isNaN(parseFloat($(this).val()))) {
                scopePercentCheck += parseFloat($(this).val());
            } else scopePercentCheck -= 100;
            var i = rowIndex - 1;
            var indexedRow = tableSelected.find('tr').eq(i);
            scopeTr = parseInt(tableSelected.find('tr').length);
            var selectPercentInput;
            while (i > 0) {
                if (selectedLevel <= indexedRow.attr('level')) {
                    selectPercentInput = indexedRow.find("input[name=scopePercent]");
                    if (selectedLevel == indexedRow.attr('level')) {
                        if (!isNaN(parseFloat(selectPercentInput.val()))) {
                            scopePercentCheck += parseFloat(selectPercentInput.val());
                        } else scopePercentCheck -= 100;
                        selectPercentInput.addClass('scope').parent().addClass('scope');
                        selectPercentInput.attr('saveChecked', true);
                    }
                    i--;
                    indexedRow = tableSelected.find('tr').eq(i);
                } else i = 0;
            }
            i = rowIndex + 1;
            indexedRow = tableSelected.find('tr').eq(i);
            while (i < scopeTr) {
                if (indexedRow.attr('level') != undefined && selectedLevel <= indexedRow.attr('level')) {
                    selectPercentInput = indexedRow.find("input[name=scopePercent]");
                    if (selectedLevel == indexedRow.attr('level')) {
                        if (!isNaN(parseFloat(selectPercentInput.val()))) {
                            scopePercentCheck += parseFloat(selectPercentInput.val());
                        } else scopePercentCheck -= 100;
                        selectPercentInput.addClass('scope').parent().addClass('scope');
                        selectPercentInput.attr('saveChecked', true);
                    }
                    i++;
                    indexedRow = tableSelected.find('tr').eq(i);
                } else i = scopeTr;
            }
            if (!isNaN(scopePercentCheck)) {
                $('span#scopeSumma').text(scopePercentCheck);
                if (parseInt(scopePercentCheck) != 100) {
                    $('span#scopeSumma').addClass('scopeError');
                    invalid = false;
                    return false;
                } else {
                    $('span#scopeSumma').removeClass('scopeError');
                    tableSelected.find('tr').each(function () {
                        if ($(this).attr('level') != undefined)$(this).find("input[name=scopePercent]").removeClass('scope').parent().removeClass('scope');
                    });
                }
            }
        }
    });
    return invalid;
}