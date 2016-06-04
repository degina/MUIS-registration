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
function Ganttalendar(zoom, startmillis, endMillis, master, minGanttSize) {
    this.master = master; // is the a GantEditor instance
    this.element; // is the jquery element containing gantt
    this.highlightBar;

    this.svg; // instance of svg object containing gantt
    this.tasksGroup; //instance of svg group containing tasks
    this.linksGroup; //instance of svg group containing links

    this.zoom = zoom;
    this.minGanttSize = minGanttSize;
    this.includeToday = true; //when true today is always visible. If false boundaries comes from tasks periods
    this.showCriticalPath = false; //when true critical path is highlighted

    this.zoomLevels = ["d", "w", "m", "q", "s", "y"];

    this.element = this.create(zoom, startmillis, endMillis);

    this.linkOnProgress = false; //set to true when creating a new link

    this.rowHeight = 30; // todo get it from css?
    this.taskHeight = 16;
    this.taskVertOffset = (this.rowHeight - this.taskHeight) / 2

}

Ganttalendar.prototype.zoomGantt = function (isPlus) {
    var curLevel = this.zoom;
    var pos = this.zoomLevels.indexOf(curLevel + "");

    var newPos = pos;
    if (isPlus) {
        newPos = pos <= 0 ? 0 : pos - 1;
    } else {
        newPos = pos >= this.zoomLevels.length - 1 ? this.zoomLevels.length - 1 : pos + 1;
    }
    if (newPos != pos) {
        curLevel = this.zoomLevels[newPos];
        this.zoom = curLevel;
        this.refreshGantt();
    }
};


Ganttalendar.prototype.create = function (zoom, originalStartmillis, originalEndMillis) {
    //console.debug("Gantt.create " + new Date(originalStartmillis) + " - " + new Date(originalEndMillis));

    var self = this;

    function getPeriod(zoomLevel, stMil, endMillis) {
        var start = new Date(stMil);
        var end = new Date(endMillis);
        end.setMonth(end.getMonth() + 1);

        //reset hours
        if (zoomLevel == "d") {
            start.setHours(0, 0, 0, 0);
            end.setHours(23, 59, 59, 999);

            start.setFirstDayOfThisWeek();
            end.setFirstDayOfThisWeek();
            end.setDate(end.getDate() + 6);


            //reset day of week
        } else if (zoomLevel == "w") {
            start.setHours(0, 0, 0, 0);
            end.setHours(23, 59, 59, 999);

            start.setFirstDayOfThisWeek();
            end.setFirstDayOfThisWeek();
            end.setDate(end.getDate() + 6);

            //reset day of month
        } else if (zoomLevel == "m") {
            start.setHours(0, 0, 0, 0);
            end.setHours(23, 59, 59, 999);

            start.setDate(1);
            end.setDate(1);
            end.setMonth(end.getMonth() + 1);
            end.setDate(end.getDate() - 1);

            //reset to quarter
        } else if (zoomLevel == "q") {
            start.setHours(0, 0, 0, 0);
            end.setHours(23, 59, 59, 999);
            start.setDate(1);
            start.setMonth(Math.floor(start.getMonth() / 3) * 3);
            end.setDate(1);
            end.setMonth(Math.floor(end.getMonth() / 3) * 3 + 3);
            end.setDate(end.getDate() - 1);

            //reset to semester
        } else if (zoomLevel == "s") {
            start.setHours(0, 0, 0, 0);
            end.setHours(23, 59, 59, 999);
            start.setDate(1);

            start.setMonth(Math.floor(start.getMonth() / 6) * 6);
            end.setDate(1);
            end.setMonth(Math.floor(end.getMonth() / 6) * 6 + 6);
            end.setDate(end.getDate() - 1);

            //reset to year - > gen
        } else if (zoomLevel == "y") {
            start.setHours(0, 0, 0, 0);
            end.setHours(23, 59, 59, 999);

            start.setDate(1);
            start.setMonth(0);

            end.setDate(1);
            end.setMonth(12);
            end.setDate(end.getDate() - 1);
        }
        return {start: start.getTime(), end: end.getTime()};
    }

    function createHeadCell(lbl, span, additionalClass, width) {
        var th = $("<th>").html(lbl).attr("colSpan", span);
        if (width)
            th.width(width);
        if (additionalClass)
            th.addClass(additionalClass);
        return th;
    }

    function createBodyCell(span, isEnd, additionalClass) {
        var ret = $("<td>").html("").attr("colSpan", span).addClass("ganttBodyCell");
        if (isEnd)
            ret.addClass("end");
        if (additionalClass)
            ret.addClass(additionalClass);
        return ret;
    }

    function createGantt(zoom, startPeriod, endPeriod) {
        var tr1 = $("<tr>").addClass("ganttHead1");
        var tr2 = $("<tr>").addClass("ganttHead2");
        var trBody = $("<tr>").addClass("ganttBody");

        function iterate(renderFunction1, renderFunction2) {
            var start = new Date(startPeriod);
            //loop for header1
            while (start.getTime() <= endPeriod) {
                renderFunction1(start);
            }

            //loop for header2
            start = new Date(startPeriod);
            while (start.getTime() <= endPeriod) {
                renderFunction2(start);
            }
        }

        //this is computed by hand in order to optimize cell size
        var computedTableWidth;

        // year
        if (zoom == "y") {
            computedTableWidth = Math.floor(((endPeriod - startPeriod) / (3600000 * 24 * 180)) * 100); //180gg = 1 sem = 100px
            iterate(function (date) {
                tr1.append(createHeadCell(date.format("yyyy"), 2));
                date.setFullYear(date.getFullYear() + 1);
            }, function (date) {
                var sem = (Math.floor(date.getMonth() / 6) + 1);
                tr2.append(createHeadCell(GanttMaster.messages["GANTT_SEMESTER_SHORT"] + sem, 1, null, 100));
                trBody.append(createBodyCell(1, sem == 2));
                date.setMonth(date.getMonth() + 6);
            });

            //semester
        } else if (zoom == "s") {
            computedTableWidth = Math.floor(((endPeriod - startPeriod) / (3600000 * 24 * 90)) * 100); //90gg = 1 quarter = 100px
            iterate(function (date) {
                var end = new Date(date.getTime());
                end.setMonth(end.getMonth() + 6);
                end.setDate(end.getDate() - 1);
                tr1.append(createHeadCell(date.format("MMM") + " - " + end.format("MMM yyyy"), 2));
                date.setMonth(date.getMonth() + 6);
            }, function (date) {
                var quarter = ( Math.floor(date.getMonth() / 3) + 1);
                tr2.append(createHeadCell(GanttMaster.messages["GANTT_QUARTER_SHORT"] + quarter, 1, null, 100));
                trBody.append(createBodyCell(1, quarter % 2 == 0));
                date.setMonth(date.getMonth() + 3);
            });

            //quarter
        } else if (zoom == "q") {
            computedTableWidth = Math.floor(((endPeriod - startPeriod) / (3600000 * 24 * 30)) * 300); //1 month= 300px
            iterate(function (date) {
                var end = new Date(date.getTime());
                end.setMonth(end.getMonth() + 3);
                end.setDate(end.getDate() - 1);
                tr1.append(createHeadCell(date.format("MMM") + " - " + end.format("MMM yyyy"), 3));
                date.setMonth(date.getMonth() + 3);
            }, function (date) {
                var lbl = date.format("MMM");
                tr2.append(createHeadCell(lbl, 1, null, 300));
                trBody.append(createBodyCell(1, date.getMonth() % 3 == 2));
                date.setMonth(date.getMonth() + 1);
            });

            //month
        } else if (zoom == "m") {
            computedTableWidth = Math.floor(((endPeriod - startPeriod) / (3600000 * 24 * 1)) * 25); //1 day= 20px
            iterate(function (date) {
                var sm = date.getTime();
                date.setMonth(date.getMonth() + 1);
                var daysInMonth = Math.round((date.getTime() - sm) / (3600000 * 24));
                tr1.append(createHeadCell(new Date(sm).format("MMMM yyyy"), daysInMonth)); //spans mumber of dayn in the month
            }, function (date) {
                tr2.append(createHeadCell(date.format("d"), 1, isHolidayWeekend(date) ? "holyH" : null, 25));
                var nd = new Date(date.getTime());
                nd.setDate(date.getDate() + 1);
                trBody.append(createBodyCell(1, nd.getDate() == 1, isHolidayWeekend(date) ? "holy" : null));
                date.setDate(date.getDate() + 1);
            });

            //week
        } else if (zoom == "w") {
            computedTableWidth = Math.floor(((endPeriod - startPeriod) / (3600000 * 24)) * 40); //1 day= 40px
            iterate(function (date) {
                var end = new Date(date.getTime());
                end.setDate(end.getDate() + 6);
                tr1.append(createHeadCell(date.format("MMM d") + " - " + end.format("MMM d'yy"), 7));
                date.setDate(date.getDate() + 7);
            }, function (date) {
                tr2.append(createHeadCell(date.format("EEEE").substr(0, 1), 1, isHolidayWeekend(date) ? "holyH" : null, 40));
                trBody.append(createBodyCell(1, date.getDay() % 7 == (self.master.firstDayOfWeek + 6) % 7, isHolidayWeekend(date) ? "holy" : null));
                date.setDate(date.getDate() + 1);
            });

            //days
        } else if (zoom == "d") {
            computedTableWidth = Math.floor(((endPeriod - startPeriod) / (3600000 * 24)) * 100); //1 day= 100px
            iterate(function (date) {
                var end = new Date(date.getTime());
                end.setDate(end.getDate() + 6);
                tr1.append(createHeadCell(date.format("MMMM d") + " - " + end.format("MMMM d yyyy"), 7));
                date.setDate(date.getDate() + 7);
            }, function (date) {
                tr2.append(createHeadCell(date.format("EEE d"), 1, isHolidayWeekend(date) ? "holyH" : null, 100));
                trBody.append(createBodyCell(1, date.getDay() % 7 == (self.master.firstDayOfWeek + 6) % 7, isHolidayWeekend(date) ? "holy" : null));
                date.setDate(date.getDate() + 1);
            });

        } else {
            console.error("Wrong level " + zoom);
        }

        //set a minimal width
        computedTableWidth = Math.max(computedTableWidth, self.minGanttSize);

        var table = $("<table cellspacing=0 cellpadding=0>");
        table.append(tr1).append(tr2).css({width: computedTableWidth + 220});

        var head = table.clone().addClass("fixHead");

        table.append(trBody).addClass("ganttTable");


        var height = self.master.editor.element.height();
        table.height(height);

        var box = $("<div>");
        box.addClass("gantt unselectable").attr("unselectable", "true").css({
            position: "relative",
            width: computedTableWidth
        });
        box.append(table);

        box.append(head);

        //highlightBar
        var hlb = $("<div>").addClass("ganttHighLight");
        box.append(hlb);
        self.highlightBar = hlb;

        //create the svg
        box.svg({
            settings: {class: "ganttSVGBox"},
            onLoad: function (svg) {
                //console.debug("svg loaded", svg);

                //creates gradient and definitions
                var defs = svg.defs('myDefs');


                //create backgound
                var extDep = svg.pattern(defs, "extDep", 0, 0, 10, 10, 0, 0, 10, 10, {patternUnits: 'userSpaceOnUse'});
                var img = svg.image(extDep, 0, 0, 10, 10, "/public/ganttChart/res/hasExternalDeps.png", {opacity: .3});

                self.svg = svg;
                $(svg).addClass("ganttSVGBox");

                //creates grid group
                var gridGroup = svg.group("gridGroup");

                //creates rows grid
                for (var i = 40; i <= height; i += self.rowHeight)
                    svg.line(gridGroup, 0, i, "100%", i, {class: "ganttLinesSVG"});

                //creates links group
                self.linksGroup = svg.group("linksGroup");

                //creates tasks group
                self.tasksGroup = svg.group("tasksGroup");

                //compute scalefactor fx
                self.fx = computedTableWidth / (endPeriod - startPeriod);

                // drawTodayLine
                if (new Date().getTime() > self.startMillis && new Date().getTime() < self.endMillis) {
                    var x = Math.round(((new Date().getTime()) - self.startMillis) * self.fx);
                    svg.line(gridGroup, x, 0, x, "100%", {class: "ganttTodaySVG"});
                }

            }
        });

        return box;
    }

    //if include today synch extremes
    if (this.includeToday) {
        var today = new Date().getTime();
        originalStartmillis = originalStartmillis > today ? today : originalStartmillis;
        originalEndMillis = originalEndMillis < today ? today : originalEndMillis;
    }

    //get best dimension fo gantt
    var period = getPeriod(zoom, originalStartmillis, originalEndMillis); //this is enlarged to match complete periods basing on zoom level

    //console.debug(new Date(period.start) + "   " + new Date(period.end));
    self.startMillis = period.start; //real dimension of gantt
    self.endMillis = period.end;
    self.originalStartMillis = originalStartmillis; //minimal dimension required by user or by task duration
    self.originalEndMillis = originalEndMillis;

    var table = createGantt(zoom, period.start, period.end);

    return table;
};


//<%-------------------------------------- GANT TASK GRAPHIC ELEMENT --------------------------------------%>
Ganttalendar.prototype.drawTask = function (task) {
    //console.debug("drawTask", task.name,new Date(task.start));
    var self = this;
    //var prof = new Profiler("ganttDrawTask");
    editorRow = task.rowElement;
    //var top = editorRow.position().top + self.master.editor.element.parent().scrollTop();
    var top = editorRow.position().top + editorRow.offsetParent().scrollTop();
    var x = Math.round((task.start - self.startMillis) * self.fx);
    task.hasChild = task.isParent();

    var taskBox = $(_createTaskSVG(task, {
        x: x,
        y: top + self.taskVertOffset,
        width: task.duration > 0 ? (Math.round((task.end - task.start) * self.fx)) : 0,
        height: self.taskHeight
    }));
    task.ganttElement = taskBox;

    taskBox.mouseenter(function () {
    }).click(function () {
        showInfoFn(task.id);
    });
    //ask for redraw link
    self.redrawLinks();

    //prof.stop();

    function _createTaskSVG(task, dimensions) {
        var svg = self.svg;
        var colorStatusClass = " taskStatusSVG", nowRowHeight = self.rowHeight;
        if (task.hasChild) {
            colorStatusClass = " taskStatusGroup";
            dimensions.height = 12;
            nowRowHeight = 25;
        }
        var taskSvg = svg.svg(self.tasksGroup, dimensions.x, dimensions.y, dimensions.width, dimensions.height, {
            class: "taskBox taskBoxSVG" + colorStatusClass,
            status: task.status,
            taskid: task.id
        });

        //svg.title(taskSvg, task.name);
        var groupPolyW = 10;
        if (self.zoom == 'm')groupPolyW = 10;
        else if (self.zoom == 'q')groupPolyW = 8;
        else if (self.zoom == 's')groupPolyW = 6;
        else if (self.zoom == 'y')groupPolyW = 6;
        if (task.hasChild) {
            svg.polygon(taskSvg, [[0, dimensions.height - 1], [groupPolyW, dimensions.height - 1], [groupPolyW / 2, dimensions.height + 7]]);
            svg.polygon(taskSvg, [[0, dimensions.height - 1], [groupPolyW, dimensions.height - 1], [groupPolyW / 2, dimensions.height + 7]], {fill: "rgba(255,255,255,.3)"});
            svg.polygon(taskSvg, [[0 + dimensions.width - groupPolyW, dimensions.height - 1], [dimensions.width, dimensions.height - 1], [(groupPolyW / 2) + dimensions.width - groupPolyW, dimensions.height + 7]]);
            svg.polygon(taskSvg, [[0 + dimensions.width - groupPolyW, dimensions.height - 1], [dimensions.width, dimensions.height - 1], [(groupPolyW / 2) + dimensions.width - groupPolyW, dimensions.height + 7]], {fill: "rgba(255,255,255,.3)"});
        }
        //external box
        svg.rect(taskSvg, 0, 0, "100%", "100%", {class: "taskLayout", rx: "2", ry: "2"});

        svg.rect(taskSvg, 0, 0, "100%", "100%", {fill: "rgba(255,255,255,.3)"});

        //external dep
        if (task.hasExternalDep)
            svg.rect(taskSvg, 0, 0, "100%", "100%", {fill: "url(#extDep)"});

        //progress
        if (task.progress > 0) {
            var progress = svg.rect(taskSvg, 0, (dimensions.height - (dimensions.height * 3 / 5)) / 2, (task.progress > 100 ? 100 : task.progress) + "%", "60%", {
                rx: "2",
                ry: "2"
            });
            if (dimensions.width > 50) {
                var textStyle = {
                    fill: "#888",
                    "font-size": "10px",
                    class: "textPerc teamworkIcons",
                    transform: "translate(5)"
                };
                if (task.progress > 100)
                    textStyle["font-weight"] = "bold";
                if (task.progress > 90)
                    textStyle.transform = "translate(-40)";
                svg.text(taskSvg, (task.progress > 90 ? 100 : task.progress) + "%", (nowRowHeight - 6) / 2, (task.progress > 100 ? "!!! " : "") + task.progress + "%", textStyle);
            }
        }

        //if (task.hasChild)
        //    svg.rect(taskSvg, 0, 0, "100%", 3, {fill: "#000"});

        if (task.duration == 0) {
            //svg.circle(taskSvg,  7, dimensions.height / 2, 9);
            svg.image(taskSvg, -9, dimensions.height / 2 - 9, 18, 18, "/public/ganttChart/res/day_" + task.status + ".png")
        }

        if (task.startIsMilestone) {
            svg.image(taskSvg, -9, dimensions.height / 2 - 9, 18, 18, "/public/ganttChart/res/milestone.png")
        }

        if (task.endIsMilestone) {
            svg.image(taskSvg, "100%", dimensions.height / 2 - 9, 18, 18, "/public/ganttChart/res/milestone.png", {transform: "translate(-9)"})
        }

        //task label
        svg.text(taskSvg, "100%", 18, task.name, {class: "taskLabelSVG", transform: "translate(20,-5)"});

        //link tool
        if (task.level > 0) {
            svg.circle(taskSvg, "0", dimensions.height / 2, dimensions.height / 3, {
                class: "taskLinkStartSVG linkHandleSVG",
                transform: "translate(" + (-dimensions.height / 3 - 1) + ")"
            });
            svg.circle(taskSvg, "100%", dimensions.height / 2, dimensions.height / 3, {
                class: "taskLinkEndSVG linkHandleSVG",
                transform: "translate(" + (dimensions.height / 3 + 1) + ")"
            });
        }
        return taskSvg
    }

};


Ganttalendar.prototype.addTask = function (task) {
    //set new boundaries for gantt
    this.originalEndMillis = this.originalEndMillis > task.end ? this.originalEndMillis : task.end;
    this.originalStartMillis = this.originalStartMillis < task.start ? this.originalStartMillis : task.start;
};


//<%-------------------------------------- GANT DRAW LINK SVG ELEMENT --------------------------------------%>
//'from' and 'to' are tasks already drawn
Ganttalendar.prototype.drawLink = function (from, to, type) {
    var self = this;
    //console.debug("drawLink")
    var peduncolusSize = 10;

    /**
     * Given an item, extract its rendered position
     * width and height into a structure.
     */
    function buildRect(item) {
        var p = item.ganttElement.position();
        var rect = {
            left: parseFloat(item.ganttElement.attr("x")),
            top: parseFloat(item.ganttElement.attr("y")),
            width: parseFloat(item.ganttElement.attr("width")),
            height: parseFloat(item.ganttElement.attr("height"))
        };
        return rect;
    }

    /**
     * The default rendering method, which paints a start to end dependency.
     */
    function drawStartToEnd(from, to, ps) {
        var svg = self.svg;

        //this function update an existing link
        function update() {
            var group = $(this);
            var from = group.data("from");
            var to = group.data("to");

            var rectFrom = buildRect(from);
            var rectTo = buildRect(to);

            var fx1 = rectFrom.left;
            var fx2 = rectFrom.left + rectFrom.width;
            var fy = rectFrom.height / 2 + rectFrom.top;

            var tx1 = rectTo.left;
            var tx2 = rectTo.left + rectTo.width;
            var ty = rectTo.height / 2 + rectTo.top;


            var tooClose = tx1 < fx2 + 2 * ps;
            var r = 5; //radius
            var arrowOffset = 5;
            var up = fy > ty;
            var fup = up ? -1 : 1;

            var prev = fx2 + 2 * ps > tx1;
            var fprev = prev ? -1 : 1;

            var image = group.find("image");
            var p = svg.createPath();

            if (tooClose) {
                var firstLine = fup * (rectFrom.height / 2 - 2 * r + 2);
                p.move(fx2, fy)
                    .line(ps, 0, true)
                    .arc(r, r, 90, false, !up, r, fup * r, true)
                    .line(0, firstLine, true)
                    .arc(r, r, 90, false, !up, -r, fup * r, true)
                    .line(fprev * 2 * ps + (tx1 - fx2), 0, true)
                    .arc(r, r, 90, false, up, -r, fup * r, true)
                    .line(0, (Math.abs(ty - fy) - 4 * r - Math.abs(firstLine)) * fup - arrowOffset, true)
                    .arc(r, r, 90, false, up, r, fup * r, true)
                    .line(ps, 0, true);
                image.attr({x: tx1 - 5, y: ty - 5 - arrowOffset});

            } else {
                p.move(fx2, fy)
                    .line((tx1 - fx2) / 2 - r, 0, true)
                    .arc(r, r, 90, false, !up, r, fup * r, true)
                    .line(0, ty - fy - fup * 2 * r + arrowOffset, true)
                    .arc(r, r, 90, false, up, r, fup * r, true)
                    .line((tx1 - fx2) / 2 - r, 0, true);
                image.attr({x: tx1 - 5, y: ty - 5 + arrowOffset});
            }

            group.find("path").attr({d: p.path()});
        }


        // create the group
        var group = svg.group(self.linksGroup, "" + from.id + "-" + to.id);
        svg.title(group, from.name + " -> " + to.name);

        var p = svg.createPath();

        //add the arrow
        svg.image(group, 0, 0, 5, 10, "/public/ganttChart/res/linkArrow.png");
        //create empty path
        svg.path(group, p, {class: "taskLinkPathSVG"});

        //set "from" and "to" to the group, bind "update" and trigger it
        var jqGroup = $(group).data({from: from, to: to}).attr({
            from: from.id,
            to: to.id
        }).on("update", update).trigger("update");

        if (self.showCriticalPath && from.isCritical && to.isCritical)
            jqGroup.addClass("critical");

        jqGroup.addClass("linkGroup");
        return jqGroup;
    }


    /**
     * A rendering method which paints a start to start dependency.
     */
    function drawStartToStart(from, to) {
        console.error("StartToStart not supported on SVG");
        var rectFrom = buildRect(from);
        var rectTo = buildRect(to);
    }

    var link;
    // Dispatch to the correct renderer
    if (type == 'start-to-start') {
        link = drawStartToStart(from, to, peduncolusSize);
    } else {
        link = drawStartToEnd(from, to, peduncolusSize);
    }
};

Ganttalendar.prototype.redrawLinks = function () {
    //console.debug("redrawLinks ");
    var self = this;
    this.element.stopTime("ganttlnksredr");
    this.element.oneTime(60, "ganttlnksredr", function () {

        //var prof=new Profiler("gd_drawLink_real");

        //remove all links
        $("#linksSVG").empty();

        var collapsedDescendant = [];

        //[expand]
        var collapsedDescendant = self.master.getCollapsedDescendant();
        for (var i = 0; i < self.master.links.length; i++) {
            var link = self.master.links[i];

            if (collapsedDescendant.indexOf(link.from) >= 0 || collapsedDescendant.indexOf(link.to) >= 0) continue;

            self.drawLink(link.from, link.to);
        }
        //prof.stop();
    });
};


Ganttalendar.prototype.reset = function () {
    this.element.find(".linkGroup").remove();
    this.element.find("[taskid]").remove();
};

Ganttalendar.prototype.redrawTasks = function () {
    //[expand]
    var collapsedDescendant = this.master.getCollapsedDescendant();
    for (var i = 0; i < this.master.tasks.length; i++) {
        var task = this.master.tasks[i];
        var inValid = true;
        if (assignFilterSelectedUser > 0) {
            inValid = false;
            if (task.assignIds.length > 0) {
                for (var a = 0; a < task.assignIds.length; a++) {
                    if (assignFilterSelectedUser == task.assignIds[a])inValid = true;
                }
            }
        }
        if (inValid) {
            if (!task.rowExpHide)task.rowElement.show();
            if (collapsedDescendant.indexOf(task) >= 0) continue;
            this.drawTask(task);
        } else {
            task.rowElement.hide();
        }
    }
};

Ganttalendar.prototype.refreshGantt = function () {
    //console.debug("refreshGantt")

    if (this.showCriticalPath) {
        this.master.computeCriticalPath();
    }


    var par = this.element.parent();

    //try to maintain last scroll
    var scrollY = par.scrollTop();
    var scrollX = par.scrollLeft();

    this.element.remove();
    //guess the zoom level in base of period
    if (!this.zoom) {
        var days = Math.round((this.originalEndMillis - this.originalStartMillis) / (3600000 * 24));
        this.zoom = this.zoomLevels[days < 2 ? 0 : (days < 15 ? 1 : (days < 60 ? 2 : (days < 150 ? 3 : 4  ) ) )];
    }
    var domEl = this.create(this.zoom, this.originalStartMillis, this.originalEndMillis);
    this.element = domEl;
    par.append(domEl);
    this.redrawTasks();

    //set old scroll
    //console.debug("old scroll:",scrollX,scrollY)
    par.scrollTop(scrollY);
    par.scrollLeft(scrollX);

    //set current task
    this.synchHighlight();

};


Ganttalendar.prototype.fitGantt = function () {
    delete this.zoom;
    this.refreshGantt();
};

Ganttalendar.prototype.synchHighlight = function () {
    if (this.master.currentTask && this.master.currentTask.ganttElement) {
        this.highlightBar.css("top", (parseInt(this.master.currentTask.ganttElement.attr("y")) - this.taskVertOffset) + "px");
    }
};


Ganttalendar.prototype.centerOnToday = function () {
    var x = Math.round(((new Date().getTime()) - this.startMillis) * this.fx) - 30;
    //console.debug("centerOnToday "+x);
    this.element.parent().scrollLeft(x);
};


/**
 * Allows drag and drop and extesion of task boxes. Only works on x axis
 * @param opt
 * @return {*}
 */
$.fn.dragExtedSVG = function (svg, opt) {

    //doing this can work with one svg at once only
    var target;
    var svgX;
    var rectMouseDx;

    var options = {
        canDrag: false,
        canResize: true,
        resizeZoneWidth: 15,
        minSize: 10,
        stopResize: function (e) {
        }
    };

    $.extend(options, opt);

    return this;


    function stopResize(e) {
        $(svg).unbind("mouseenter.deSVG").unbind("mouseup.deSVG").unbind("mouseleave.deSVG");
        //if (target && target.attr("oldw")!=target.attr("width"))
        if (target)
            options.stopResize.call(target.get(0), e); //callback
        target = undefined;
        $("body").clearUnselectable();
    }
};
