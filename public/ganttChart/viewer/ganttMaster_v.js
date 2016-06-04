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
function GanttMaster() {
    this.tasks = [];
    this.deletedTaskIds = [];
    this.links = [];

    this.editor; //element for editor
    this.gantt; //element for gantt
    this.splitter; //element for splitter

    this.element;

    this.canWriteOnParent = true;
    this.canWrite = true;

    this.firstDayOfWeek = Date.firstDayOfWeek;

    this.currentTask; // task currently selected;

    var self = this;
}


GanttMaster.prototype.init = function (place) {
    this.element = place;
    var self = this;
    //load templates
    $("#gantEditorTemplates").loadTemplates().remove();

    //create editor
    this.editor = new GridEditor(this);
    place.append(this.editor.gridified);

    //create gantt
    this.gantt = new Ganttalendar("m", new Date().getTime() - 3600000 * 24 * 2, new Date().getTime() + 3600000 * 24 * 15, this, place.width() * .6);

    //setup splitter
    self.splitter = $.splittify.init(place, this.editor.gridified, this.gantt.element, 60);
    self.splitter.firstBoxMinWidth = 30;

    //prepend buttons
    place.before($.JST.createFromTemplate({}, "GANTBUTTONS"));


    //bindings
    place.bind("refreshTasks.gantt", function () {
        self.redrawTasks();
    }).bind("refreshTask.gantt", function (e, task) {
        self.drawTask(task);

    }).bind("zoomPlus.gantt", function () {
        self.gantt.zoomGantt(true);
    }).bind("zoomMinus.gantt", function () {
        self.gantt.zoomGantt(false);

    }).bind("resize.gantt", function () {
        self.resize();
    });

    //keyboard management bindings
    $("body").bind("keydown.body", function (e) {
        //console.debug(e.keyCode+ " "+e.target.nodeName)

        //manage only events for body -> not from inputs
        if (e.target.nodeName.toLowerCase() == "body" || e.target.nodeName.toLowerCase() == "svg") { // chrome,ff receive "body" ie "svg"
            //something focused?
            //console.debug(e.keyCode, e.ctrlKey)
            var eventManaged = true;
            switch (e.keyCode) {
                case 46: //del
                case 8: //backspace
                    var focused = self.gantt.element.find(".focused.focused");// orrible hack for chrome that seems to keep in memory a cached object
                    if (focused.is(".taskBox")) { // remove task
                        self.deleteCurrentTask();
                    } else if (focused.is(".linkGroup")) {
                        self.removeLink(focused.data("from"), focused.data("to"));
                    }
                    break;

                case 38: //up
                    if (self.currentTask) {
                        if (self.currentTask.ganttElement.is(".focused")) {
                            self.moveUpCurrentTask();
                            self.gantt.element.oneTime(100, function () {
                                self.currentTask.ganttElement.addClass("focused");
                            });

                        } else {
                            self.currentTask.rowElement.prev().click();
                        }
                    }
                    break;

                case 40: //down
                    if (self.currentTask) {
                        if (self.currentTask.ganttElement.is(".focused")) {
                            self.moveDownCurrentTask();
                            self.gantt.element.oneTime(100, function () {
                                self.currentTask.ganttElement.addClass("focused");
                            });
                        } else {
                            self.currentTask.rowElement.next().click();
                        }
                    }
                    break;

                case 39: //right
                    if (self.currentTask) {
                        if (self.currentTask.ganttElement.is(".focused")) {
                            self.indentCurrentTask();
                            self.gantt.element.oneTime(100, function () {
                                self.currentTask.ganttElement.addClass("focused");
                            });
                        }
                    }
                    break;

                case 37: //left
                    if (self.currentTask) {
                        if (self.currentTask.ganttElement.is(".focused")) {
                            self.outdentCurrentTask();
                            self.gantt.element.oneTime(100, function () {
                                self.currentTask.ganttElement.addClass("focused");
                            });
                        }
                    }
                    break;


                case 89: //Y
                    if (e.ctrlKey) {
                        self.redo();
                    }
                    break;

                case 90: //Z
                    if (e.ctrlKey) {
                        self.undo();
                    }
                    break;

                default :
                {
                    eventManaged = false;
                }

            }
            if (eventManaged) {
                e.preventDefault();
                e.stopPropagation();
            }
        }
    });
};

GanttMaster.messages = {
    "CANNOT_WRITE": "CANNOT_WRITE",
    "CHANGE_OUT_OF_SCOPE": "NO_RIGHTS_FOR_UPDATE_PARENTS_OUT_OF_EDITOR_SCOPE",
    "START_IS_MILESTONE": "START_IS_MILESTONE",
    "END_IS_MILESTONE": "END_IS_MILESTONE",
    "TASK_HAS_CONSTRAINTS": "TASK_HAS_CONSTRAINTS",
    "GANTT_ERROR_DEPENDS_ON_OPEN_TASK": "GANTT_ERROR_DEPENDS_ON_OPEN_TASK",
    "GANTT_ERROR_DESCENDANT_OF_CLOSED_TASK": "GANTT_ERROR_DESCENDANT_OF_CLOSED_TASK",
    "TASK_HAS_EXTERNAL_DEPS": "TASK_HAS_EXTERNAL_DEPS",
    "GANTT_ERROR_LOADING_DATA_TASK_REMOVED": "GANTT_ERROR_LOADING_DATA_TASK_REMOVED",
    "CIRCULAR_REFERENCE": "CIRCULAR_REFERENCE",
    "ERROR_SETTING_DATES": "ERROR_SETTING_DATES",
    "CANNOT_DEPENDS_ON_ANCESTORS": "CANNOT_DEPENDS_ON_ANCESTORS",
    "CANNOT_DEPENDS_ON_DESCENDANTS": "CANNOT_DEPENDS_ON_DESCENDANTS",
    "INVALID_DATE_FORMAT": "INVALID_DATE_FORMAT",
    "GANTT_QUARTER_SHORT": "GANTT_QUARTER_SHORT",
    "GANTT_SEMESTER_SHORT": "GANTT_SEMESTER_SHORT",
    "CANNOT_CLOSE_TASK_IF_OPEN_ISSUE": "CANNOT_CLOSE_TASK_IF_OPEN_ISSUE"
};


GanttMaster.prototype.createTask = function (id, name, code, level, start, duration) {
    var factory = new TaskFactory();
    return factory.build(id, name, code, level, start, duration);
};


//update depends strings
GanttMaster.prototype.updateDependsStrings = function () {
    //remove all deps
    for (var i = 0; i < this.tasks.length; i++) {
        this.tasks[i].depends = "";
    }

    for (var i = 0; i < this.links.length; i++) {
        var link = this.links[i];
        var dep = link.to.depends;
        link.to.depends = link.to.depends + (link.to.depends == "" ? "" : ",") + (link.from.getRow() + 1) + (link.lag ? ":" + link.lag : "");
    }

};

GanttMaster.prototype.removeLink = function (fromTask, toTask) {
    //console.debug("removeLink");
    if (!this.canWrite || (!fromTask.canWrite && !toTask.canWrite))
        return;

    this.beginTransaction();
    var found = false;
    for (var i = 0; i < this.links.length; i++) {
        if (this.links[i].from == fromTask && this.links[i].to == toTask) {
            this.links.splice(i, 1);
            found = true;
            break;
        }
    }

    if (found) {
        this.updateDependsStrings();
        if (this.updateLinks(toTask))
            this.changeTaskDates(toTask, toTask.start, toTask.end, toTask.duration); // fake change to force date recomputation from dependencies
    }
    this.endTransaction();
};

GanttMaster.prototype.removeAllLinks = function (task, openTrans) {
    //console.debug("removeLink");
    if (!this.canWrite || (!task.canWrite && !task.canWrite))
        return;

    if (openTrans)
        this.beginTransaction();
    var found = false;
    for (var i = 0; i < this.links.length; i++) {
        if (this.links[i].from == task || this.links[i].to == task) {
            this.links.splice(i, 1);
            found = true;
        }
    }

    if (found) {
        this.updateDependsStrings();
    }
    if (openTrans)
        this.endTransaction();
};


/**
 * a project contais tasks and info about permisions
 * @param project
 */
GanttMaster.prototype.loadProject = function (project) {
    //console.debug("loadProject",project)
    this.beginTransaction();
    this.canWrite = project.canWrite;
    this.canWriteOnParent = project.canWriteOnParent;
    this.cannotCloseTaskIfIssueOpen = project.cannotCloseTaskIfIssueOpen;

    if (project.minEditableDate)
        this.minEditableDate = computeStart(project.minEditableDate);
    else
        this.minEditableDate = -Infinity;

    if (project.maxEditableDate)
        this.maxEditableDate = computeEnd(project.maxEditableDate);
    else
        this.maxEditableDate = Infinity;

    this.loadTasks(project.tasks, project.selectedRow);
    this.deletedTaskIds = [];

    //recover saved splitter position
    if (project.splitterPosition)
        this.splitter.resize(project.splitterPosition);

    //recover saved zoom level
    if (project.zoom)
        this.gantt.zoom = project.zoom;

    this.endTransaction();
    var self = this;
    this.gantt.element.oneTime(800, function () {
        self.gantt.centerOnToday()
    });
};


GanttMaster.prototype.loadTasks = function (tasks, selectedRow) {
    var factory = new TaskFactory();
    //reset
    this.reset();

    for (var i = 0; i < tasks.length; i++) {
        var task = tasks[i];
        var tstamp = task.startDate.split(/[- :]/);
        task.start = new Date(tstamp[0], tstamp[1] - 1, tstamp[2], tstamp[3], tstamp[4], tstamp[5]).getTime();
        tstamp = task.endDate.split(/[- :]/);
        task.end = new Date(tstamp[0], tstamp[1] - 1, tstamp[2], tstamp[3], tstamp[4], tstamp[5]).getTime();
        if (!(task instanceof Task)) {
            var t = factory.build(task.id, task.name, task.code, task.level, task.start, task.duration, task.scopePercent, task.collapsed,task.floor,task.workCount,task.person);
            for (var key in task) {
                if (key != "end" && key != "start")
                    t[key] = task[key]; //copy all properties
            }
            task = t;
        }
        task.master = this; // in order to access controller from task
        this.tasks.push(task);  //append task at the end
    }

    //var prof=new Profiler("gm_loadTasks_addTaskLoop");
    for (var i = 0; i < this.tasks.length; i++) {
        var task = this.tasks[i];


        var numOfError = this.__currentTransaction && this.__currentTransaction.errors ? this.__currentTransaction.errors.length : 0;
        //add Link collection in memory
        while (!this.updateLinks(task)) {  // error on update links while loading can be considered as "warning". Can be displayed and removed in order to let transaction commits.
            if (this.__currentTransaction && numOfError != this.__currentTransaction.errors.length) {
                var msg = "";
                while (numOfError < this.__currentTransaction.errors.length) {
                    var err = this.__currentTransaction.errors.pop();
                    msg = msg + err.msg + "\n\n";
                }
                alert(msg);
            }
            this.removeAllLinks(task, false);
        }

        if (!task.setPeriod(task.start, task.end, task.duration)) {
            alert(GanttMaster.messages.GANNT_ERROR_LOADING_DATA_TASK_REMOVED + "\n" + task.name + "\n" + GanttMaster.messages.ERROR_SETTING_DATES);
            //remove task from in-memory collection
            this.tasks.splice(task.getRow(), 1);
        } else {
            //append task to editor
            this.editor.addTask(task, null, true);
            //append task to gantt
            this.gantt.addTask(task);
        }
    }

    this.editor.fillEmptyLines();
    //prof.stop();

    // re-select old row if tasks is not empty
    if (this.tasks && this.tasks.length > 0) {
        selectedRow = selectedRow ? selectedRow : 0;
        this.tasks[selectedRow].rowElement.click();
    }
};


GanttMaster.prototype.getTask = function (taskId) {
    var ret;
    for (var i = 0; i < this.tasks.length; i++) {
        var tsk = this.tasks[i];
        if (tsk.id == taskId) {
            ret = tsk;
            break;
        }
    }
    return ret;
};


GanttMaster.prototype.changeTaskDates = function (task, start, end, dur) {
    return task.setPeriod(start, end, dur);
};


GanttMaster.prototype.moveTask = function (task, newStart) {
    return task.moveTo(newStart, true);
};


GanttMaster.prototype.taskIsChanged = function () {
    //console.debug("taskIsChanged");
    var master = this;

    //refresh is executed only once every 50ms
    this.element.stopTime("gnnttaskIsChanged");
    //var profilerext = new Profiler("gm_taskIsChangedRequest");
    this.element.oneTime(50, "gnnttaskIsChanged", function () {
        //console.debug("task Is Changed real call to redraw");
        //var profiler = new Profiler("gm_taskIsChangedReal");
        master.editor.redraw();
        setTimeout(function () {
            master.gantt.refreshGantt();
        }, 200);
        //profiler.stop();
    });
    //profilerext.stop();
};


GanttMaster.prototype.redraw = function () {
    this.editor.redraw();
    this.gantt.refreshGantt();
};

GanttMaster.prototype.reset = function () {
    this.tasks = [];
    this.links = [];
    this.deletedTaskIds = [];
    if (!this.__inUndoRedo) {
        this.__undoStack = [];
        this.__redoStack = [];
    } else { // don't reset the stacks if we're in an Undo/Redo, but restart the inUndoRedo control
        this.__inUndoRedo = false;
    }
    delete this.currentTask;

    this.editor.reset();
    this.gantt.reset();
};
GanttMaster.prototype.updateLinks = function (task) {
    //console.debug("updateLinks",task);
    //var prof= new Profiler("gm_updateLinks");

    // defines isLoop function
    function isLoop(task, target, visited) {
        //var prof= new Profiler("gm_isLoop");
        //console.debug("isLoop :"+task.name+" - "+target.name);
        if (target == task) {
            return true;
        }

        var sups = task.getSuperiors();

        //my parent' superiors are my superiors too
        var p = task.getParent();
        while (p) {
            sups = sups.concat(p.getSuperiors());
            p = p.getParent();
        }

        //my children superiors are my superiors too
        var chs = task.getChildren();
        for (var i = 0; i < chs.length; i++) {
            sups = sups.concat(chs[i].getSuperiors());
        }


        var loop = false;
        //check superiors
        for (var i = 0; i < sups.length; i++) {
            var supLink = sups[i];
            if (supLink.from == target) {
                loop = true;
                break;
            } else {
                if (visited.indexOf(supLink.from.id + "x" + target.id) <= 0) {
                    visited.push(supLink.from.id + "x" + target.id);
                    if (isLoop(supLink.from, target, visited)) {
                        loop = true;
                        break;
                    }
                }
            }
        }

        //check target parent
        var tpar = target.getParent();
        if (tpar) {
            if (visited.indexOf(task.id + "x" + tpar.id) <= 0) {
                visited.push(task.id + "x" + tpar.id);
                if (isLoop(task, tpar, visited)) {
                    loop = true;
                }
            }
        }

        //prof.stop();
        return loop;
    }

    //remove my depends
    this.links = this.links.filter(function (link) {
        return link.to != task;
    });

    var todoOk = true;
    if (task.depends) {

        //cannot depend from an ancestor
        var parents = task.getParents();
        //cannot depend from descendants
        var descendants = task.getDescendant();

        var deps = task.depends.split(",");
        var newDepsString = "";

        var visited = [];
        for (var j = 0; j < deps.length; j++) {
            var dep = deps[j]; // in the form of row(lag) e.g. 2:3,3:4,5
            var par = dep.split(":");
            var lag = 0;

            if (par.length > 1) {
                lag = parseInt(par[1]);
            }

            var sup = this.tasks[parseInt(par[0] - 1)];

            if (sup) {
                if (parents && parents.indexOf(sup) >= 0) {
                    this.setErrorOnTransaction(task.name + "\n" + GanttMaster.messages.CANNOT_DEPENDS_ON_ANCESTORS + "\n" + sup.name);
                    todoOk = false;

                } else if (descendants && descendants.indexOf(sup) >= 0) {
                    this.setErrorOnTransaction(task.name + "\n" + GanttMaster.messages.CANNOT_DEPENDS_ON_DESCENDANTS + "\n" + sup.name);
                    todoOk = false;

                } else if (isLoop(sup, task, visited)) {
                    todoOk = false;
                    this.setErrorOnTransaction(GanttMaster.messages.CIRCULAR_REFERENCE + "\n" + task.name + " -> " + sup.name);
                } else {
                    this.links.push(new Link(sup, task, lag));
                    newDepsString = newDepsString + (newDepsString.length > 0 ? "," : "") + dep;
                }
            }
        }

        task.depends = newDepsString;

    }

    //prof.stop();

    return todoOk;
};

//<%----------------------------- TRANSACTION MANAGEMENT ---------------------------------%>
GanttMaster.prototype.beginTransaction = function () {
    if (!this.__currentTransaction) {
        this.__currentTransaction = {
            snapshot: '',
            errors: []
        };
    } else {
        console.error("Cannot open twice a transaction");
    }
    return this.__currentTransaction;
};


GanttMaster.prototype.endTransaction = function () {
    if (!this.__currentTransaction) {
        console.error("Transaction never started.");
        return true;
    }

    var ret = true;

    //no error -> commit
    if (this.__currentTransaction.errors.length <= 0) {
        //console.debug("committing transaction");

        //put snapshot in undo
        this.__undoStack.push(this.__currentTransaction.snapshot);
        //clear redo stack
        this.__redoStack = [];

        //shrink gantt bundaries
        this.gantt.originalStartMillis = Infinity;
        this.gantt.originalEndMillis = -Infinity;
        for (var i = 0; i < this.tasks.length; i++) {
            var task = this.tasks[i];
            if (this.gantt.originalStartMillis > task.start)
                this.gantt.originalStartMillis = task.start;
            if (this.gantt.originalEndMillis < task.end)
                this.gantt.originalEndMillis = task.end;

        }
        this.taskIsChanged(); //enqueue for gantt refresh


        //error -> rollback
    } else {
        ret = false;
        //console.debug("rolling-back transaction");
        //try to restore changed tasks
        var oldTasks = JSON.parse(this.__currentTransaction.snapshot);
        this.deletedTaskIds = oldTasks.deletedTaskIds;
        this.loadTasks(oldTasks.tasks, oldTasks.selectedRow);
        this.redraw();

        //compose error message
        var msg = "";
        for (var i = 0; i < this.__currentTransaction.errors.length; i++) {
            var err = this.__currentTransaction.errors[i];
            msg = msg + err.msg + "\n\n";
        }
        alert(msg);
    }
    //reset transaction
    this.__currentTransaction = undefined;

    //[expand]
    this.editor.refreshExpandStatus(this.currentTask);

    return ret;
};

//this function notify an error to a transaction -> transaction will rollback
GanttMaster.prototype.setErrorOnTransaction = function (errorMessage, task) {
    if (this.__currentTransaction) {
        this.__currentTransaction.errors.push({msg: errorMessage, task: task});
    } else {
        console.error(errorMessage);
    }
};

GanttMaster.prototype.resize = function () {
    //console.debug("GanttMaster.resize")
    this.splitter.resize();
};


GanttMaster.prototype.getCollapsedDescendant = function () {
    var allTasks = this.tasks;
    var collapsedDescendant = [];
    for (var i = 0; i < allTasks.length; i++) {
        var task = allTasks[i];
        if (collapsedDescendant.indexOf(task) >= 0) continue;
        if (task.collapsed) collapsedDescendant = collapsedDescendant.concat(task.getDescendant());
    }
    return collapsedDescendant;
};
