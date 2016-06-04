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

/**
 * A method to instantiate valid task models from
 * raw data.
 */
function TaskFactory() {

    /**
     * Build a new Task
     */
    this.build = function (id, name, code, level, start, duration, scopePercent, collapsed, floor, workCount, person) {
        // Set at beginning of day
        var adjusted_start = computeStart(start);
        var calculated_end = computeEndByDuration(adjusted_start, duration);

        return new Task(id, name, code, level, adjusted_start, calculated_end, duration, scopePercent, collapsed, floor, workCount, person);
    };

}

function Task(id, name, code, level, start, end, duration, scopePercent, collapsed, floor, workCount, person) {
    this.id = id;
    this.name = name;
    this.progress = 0;
    this.description = "";
    this.code = code;
    this.level = level;
    this.status = "STATUS_UNDEFINED";
    this.depends = "";
    this.canWrite = false; // by default all tasks are writeable
    this.start = start;
    this.duration = duration;
    this.scopePercent = 0;
    this.end = end;
    this.startIsMilestone = false;
    this.endIsMilestone = false;

    this.collapsed = collapsed;

    this.rowExpHide = false;
    this.rowElement; //row editor html element
    this.ganttElement; //gantt html element
    this.master;

    this.assign = "";
    this.assignIds = [];
    this.floor = "";
    this.workCount = "";
    this.person = 0;
    this.material = 0;
    this.technical = 0;
}

Task.prototype.clone = function () {
    var ret = {};
    for (var key in this) {
        if (typeof(this[key]) != "function") {
            ret[key] = this[key];
        }
    }
    return ret;
};


//<%---------- SET PERIOD ---------------------- --%>
Task.prototype.setPeriod = function (start, end, dur) {
    //console.debug(setPeriodTimeType + " = setPeriod ", this.name, new Date(start), new Date(end));
    //var profilerSetPer = new Profiler("gt_setPeriodJS");

    if (start instanceof Date) {
        start = start.getTime();
    }

    if (end instanceof Date) {
        end = end.getTime();
    }

    var originalPeriod = {
        start: this.start,
        end: this.end,
        duration: this.duration
    };

    //console.debug("setStart",date,date instanceof Date);
    var wantedStartMillis = start;

    //cannot start after end
    if (start > end) {
        start = end;
    }
    //set a legal start
    start = computeStart(start);
    //if depends -> start is set to max end + lag of superior
    var sups = this.getSuperiors();
    if (sups && sups.length > 0) {

        var supEnd = 0;
        for (var i = 0; i < sups.length; i++) {
            var link = sups[i];
            if (link.from.duration > 0)supEnd = Math.max(supEnd, incrementDateByWorkingDays(link.from.end, link.lag));
        }
        //if changed by depends move it
        if (supEnd > 0 && computeStart(supEnd) != start) {
            return this.moveTo(supEnd + 1, false);
        }
    }

    var somethingChanged = false;

    //move date to closest day
    var date = new Date(start);

    if (this.start != start || this.start != wantedStartMillis) {
        this.start = start;
        somethingChanged = true;
    }

    //set end
    var wantedEndMillis = end;
    end = computeEnd(end);

    if (this.end != end || this.end != wantedEndMillis) {
        this.end = end;
        somethingChanged = true;
    }

    if (parseInt(dur) == 0)this.duration = 0;
    else this.duration = recomputeDuration(this.start, this.end);
    //profilerSetPer.stop();

    //nothing changed exit
    if (!somethingChanged)
        return true;

    //cannot write exit
    if (!this.canWrite) {
        this.master.setErrorOnTransaction(GanttMaster.messages["CANNOT_WRITE"] + "\n" + this.name, this);
        return false;
    }

    //external dependencies: exit with error
    if (this.hasExternalDep) {
        this.master.setErrorOnTransaction(GanttMaster.messages["TASK_HAS_EXTERNAL_DEPS"] + "\n" + this.name, this);
        return false;
    }

    var todoOk = true;

    //I'm restricting
    var deltaPeriod = originalPeriod.duration - this.duration;
    var restricting = deltaPeriod > 0;
    var restrictingStart = restricting && (originalPeriod.start < this.start);
    var restrictingEnd = restricting && (originalPeriod.end > this.end);

    //console.debug( " originalPeriod.duration "+ originalPeriod.duration +" deltaPeriod "+deltaPeriod+" "+"restricting "+restricting);

    if (restricting) {
        //loops children to get boundaries
        var children = this.getChildren();
        var bs = Infinity;
        var be = 0;
        for (var i = 0; i < children.length; i++) {

            ch = children[i];
            //console.debug("restricting: test child "+ch.name+" "+ch.end)
            if (restrictingEnd) {
                be = Math.max(be, ch.end);
            } else {
                bs = Math.min(bs, ch.start);
            }
        }

        if (restrictingEnd) {
            //console.debug("restricting end ",be, this.end);
            this.end = Math.max(be, this.end);
        } else {
            //console.debug("restricting start");
            this.start = Math.min(bs, this.start);
        }

        if (parseInt(dur) == 0)this.duration = 0;
        else this.duration = recomputeDuration(this.start, this.end);
    } else {

        //check global boundaries
        if (this.start < this.master.minEditableDate || this.end > this.master.maxEditableDate) {
            this.master.setErrorOnTransaction(GanttMaster.messages["CHANGE_OUT_OF_SCOPE"], this);
            todoOk = false;
        }

        //console.debug("set period: somethingChanged",this);
        if (todoOk && !updateTree(this)) {
            todoOk = false;
        }
    }

    if (todoOk) {
        //and now propagate to inferiors
        var infs = this.getInferiors();
        if (infs && infs.length > 0) {
            for (var i = 0; i < infs.length; i++) {
                var link = infs[i];
                if (!link.to.canWrite) {
                    this.master.setErrorOnTransaction(GanttMaster.messages["CANNOT_WRITE"] + "\n" + link.to.name, link.to);
                    break;
                }
                todoOk = link.to.moveTo(end, false); //this is not the right date but moveTo checks start
                if (!todoOk)
                    break;
            }
        }
    }
    return todoOk;
};

//<%---------- MOVE TO ---------------------- --%>
Task.prototype.moveTo = function (start, ignoreMilestones) {
    //console.debug("moveTo ",this,start,ignoreMilestones);
    //var profiler = new Profiler("gt_task_moveTo");

    if (start instanceof Date) {
        start = start.getTime();
    }
    var originalPeriod = {
        start: this.start,
        end: this.end
    };

    var wantedStartMillis = start;
    //set a legal start
    start = computeStart(start);
    //if start is milestone cannot be move
    if (!ignoreMilestones && this.startIsMilestone && start != this.start) {
        //notify error
        this.master.setErrorOnTransaction(GanttMaster.messages["START_IS_MILESTONE"], this);
        return false;
    } else if (this.hasExternalDep) {
        //notify error
        this.master.setErrorOnTransaction(GanttMaster.messages["TASK_HAS_EXTERNAL_DEPS"], this);
        return false;
    }
    //if depends start is set to max end + lag of superior
    var sups = this.getSuperiors();
    if (sups && sups.length > 0) {
        var supEnd = 0;
        for (var i = 0; i < sups.length; i++) {
            var link = sups[i];
            if (link.from.duration > 0)supEnd = Math.max(supEnd, incrementDateByWorkingDays(link.from.end, link.lag));
        }
        if (supEnd > 0) {
            start = supEnd + 1;
            //set a legal start
            start = computeStart(start);
        }
    }

    var end = computeEndByDuration(start, this.duration);

    if (this.start != start || this.start != wantedStartMillis) {
        //in case of end is milestone it never changes, but recompute duration
        if (!ignoreMilestones && this.endIsMilestone) {
            end = this.end;
            this.duration = recomputeDuration(start, end);
        }
        this.start = start;
        this.end = end;
        //profiler.stop();
        //check global boundaries
        if (this.start < this.master.minEditableDate || this.end > this.master.maxEditableDate) {
            this.master.setErrorOnTransaction(GanttMaster.messages["CHANGE_OUT_OF_SCOPE"], this);
            return false;
        }


        var panDelta = originalPeriod.start - this.start;
        //console.debug("panDelta",panDelta);
        //loops children to shift them
        var children = this.getChildren();
        for (var i = 0; i < children.length; i++) {
            ch = children[i];
            if (!ch.moveTo(ch.start - panDelta, false)) {
                return false;
            }
        }


        //console.debug("set period: somethingChanged",this);
        if (!updateTree(this)) {
            return false;
        }


        //and now propagate to inferiors
        var infs = this.getInferiors();
        if (infs && infs.length > 0) {
            for (var i = 0; i < infs.length; i++) {
                var link = infs[i];
                //this is not the right date but moveTo checks start
                if (!link.to.canWrite) {
                    this.master.setErrorOnTransaction(GanttMaster.messages["CANNOT_WRITE"] + "\n" + link.to.name, link.to);
                } else if (!link.to.moveTo(this.duration > 0 ? end : start, false)) {
                    return false;
                }
            }
        }

    }

    return true;
};

function updateTree(task) {
    //console.debug("updateTree ",task);
    var error;

    //try to enlarge parent
    var p = task.getParent();

    //no parent:exit
    if (!p)
        return true;

    var newStart = p.start;
    var newEnd = p.end;

    if (p.start > task.start) {
        if (p.startIsMilestone) {
            task.master.setErrorOnTransaction(GanttMaster.messages["START_IS_MILESTONE"] + "\n" + p.name, task);
            return false;
        } else if (p.depends) {
            task.master.setErrorOnTransaction(GanttMaster.messages["TASK_HAS_CONSTRAINTS"] + "\n" + p.name, task);
            return false;
        }

        newStart = task.start;
    }

    if (p.end < task.end) {
        if (p.endIsMilestone) {
            task.master.setErrorOnTransaction(GanttMaster.messages["END_IS_MILESTONE"] + "\n" + p.name, task);
            return false;
        }

        newEnd = task.end;
    }

    //propagate updates if needed
    if (newStart != p.start || newEnd != p.end) {

        //can write?
        if (!p.canWrite) {
            task.master.setErrorOnTransaction(GanttMaster.messages["CANNOT_WRITE"] + "\n" + p.name, task);
            return false;
        }

        //has external deps ?
        if (p.hasExternalDep) {
            task.master.setErrorOnTransaction(GanttMaster.messages["TASK_HAS_EXTERNAL_DEPS"] + "\n" + p.name, task);
            return false;
        }

        return p.setPeriod(newStart, newEnd);
    }


    return true;
}

Task.prototype.isLocallyBlockedByDependencies = function () {
    var sups = this.getSuperiors();
    var blocked = false;
    for (var i = 0; i < sups.length; i++) {
        if (sups[i].from.status != "STATUS_DONE") {
            blocked = true;
            break;
        }
    }
    return blocked;
};

//<%---------- TASK STRUCTURE ---------------------- --%>
Task.prototype.getRow = function () {
    ret = -1;
    if (this.master)
        ret = this.master.tasks.indexOf(this);
    return ret;
};


Task.prototype.getParents = function () {
    var ret;
    if (this.master) {
        var topLevel = this.level;
        var pos = this.getRow();
        ret = [];
        for (var i = pos; i >= 0; i--) {
            var par = this.master.tasks[i];
            if (topLevel > par.level) {
                topLevel = par.level;
                ret.push(par);
            }
        }
    }
    return ret;
};


Task.prototype.getParent = function () {
    var ret;
    if (this.master) {
        for (var i = this.getRow(); i >= 0; i--) {
            var par = this.master.tasks[i];
            if (this.level > par.level) {
                ret = par;
                break;
            }
        }
    }
    return ret;
};


Task.prototype.isParent = function () {
    var ret = false;
    if (this.master) {
        var pos = this.getRow();
        if (pos < this.master.tasks.length - 1)
            ret = this.master.tasks[pos + 1].level > this.level;
    }
    return ret;
};


Task.prototype.getChildren = function () {
    var ret = [];
    if (this.master) {
        var pos = this.getRow();
        for (var i = pos + 1; i < this.master.tasks.length; i++) {
            var ch = this.master.tasks[i];
            if (ch.level == this.level + 1)
                ret.push(ch);
            else if (ch.level <= this.level) // exit loop if parent or brother
                break;
        }
    }
    return ret;
};


Task.prototype.getDescendant = function () {
    var ret = [];
    if (this.master) {
        var pos = this.getRow();
        for (var i = pos + 1; i < this.master.tasks.length; i++) {
            var ch = this.master.tasks[i];
            if (ch.level > this.level)
                ret.push(ch);
            else
                break;
        }
    }
    return ret;
};


Task.prototype.getSuperiors = function () {
    var ret = [];
    var task = this;
    if (this.master) {
        ret = this.master.links.filter(function (link) {
            return link.to == task;
        });
    }
    return ret;
};

Task.prototype.getSuperiorTasks = function () {
    var ret = [];
    var sups = this.getSuperiors();
    for (var i = 0; i < sups.length; i++)
        ret.push(sups[i].from);
    return ret;
};


Task.prototype.getInferiors = function () {
    var ret = [];
    var task = this;
    if (this.master) {
        ret = this.master.links.filter(function (link) {
            return link.from == task;
        });
    }
    return ret;
};

Task.prototype.getInferiorTasks = function () {
    var ret = [];
    var infs = this.getInferiors();
    for (var i = 0; i < infs.length; i++)
        ret.push(infs[i].to);
    return ret;
};


Task.prototype.isDependent = function (t) {
    //console.debug("isDependent",this.name, t.name)
    var task = this;
    var dep = this.master.links.filter(function (link) {
        return link.from == task;
    });

    // is t a direct dependency?
    for (var i = 0; i < dep.length; i++) {
        if (dep[i].to == t)
            return true;
    }
    // is t an indirect dependency
    for (var i = 0; i < dep.length; i++) {
        if (dep[i].to.isDependent(t)) {
            return true;
        }
    }
    return false;
};

Task.prototype.setLatest = function (maxCost) {
    this.latestStart = maxCost - this.criticalCost;
    this.latestFinish = this.latestStart + this.duration;
};


//<%------------------------------------------------------------------------  LINKS OBJECT ---------------------------------------------------------------%>
function Link(taskFrom, taskTo, lagInWorkingDays) {
    this.from = taskFrom;
    this.to = taskTo;
    this.lag = lagInWorkingDays;
}



