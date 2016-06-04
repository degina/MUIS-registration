function dateToRelative(localTime) {
    var diff = new Date().getTime() - localTime;
    var ret = "";

    var min = 60000;
    var hour = 3600000;
    var day = 86400000;
    var wee = 604800000;
    var mon = 2629800000;
    var yea = 31557600000;

    if (diff < -yea * 2)
        ret = "in ## years".replace("##", (-diff / yea).toFixed(0));

    else if (diff < -mon * 9)
        ret = "in ## months".replace("##", (-diff / mon).toFixed(0));

    else if (diff < -wee * 5)
        ret = "in ## weeks".replace("##", (-diff / wee).toFixed(0));

    else if (diff < -day * 2)
        ret = "in ## days".replace("##", (-diff / day).toFixed(0));

    else if (diff < -hour)
        ret = "in ## hours".replace("##", (-diff / hour).toFixed(0));

    else if (diff < -min * 35)
        ret = "in about one hour";

    else if (diff < -min * 25)
        ret = "in about half hour";

    else if (diff < -min * 10)
        ret = "in some minutes";

    else if (diff < -min * 2)
        ret = "in few minutes";

    else if (diff <= min)
        ret = "just now";

    else if (diff <= min * 5)
        ret = "few minutes ago";

    else if (diff <= min * 15)
        ret = "some minutes ago";

    else if (diff <= min * 35)
        ret = "about half hour ago";

    else if (diff <= min * 75)
        ret = "about an hour ago";

    else if (diff <= hour * 5)
        ret = "few hours ago";

    else if (diff <= hour * 24)
        ret = "## hours ago".replace("##", (diff / hour).toFixed(0));

    else if (diff <= day * 7)
        ret = "## days ago".replace("##", (diff / day).toFixed(0));

    else if (diff <= wee * 5)
        ret = "## weeks ago".replace("##", (diff / wee).toFixed(0));

    else if (diff <= mon * 12)
        ret = "## months ago".replace("##", (diff / mon).toFixed(0));

    else
        ret = "## years ago".replace("##", (diff / yea).toFixed(0));

    return ret;
}

//override date format i18n

Date.monthNames = new Array('Нэгдүгээр сар', "Хоёрдугаар cар", "Гуравдугаар сар", "Дөрөвдүгээр сар", "Тавдугаар сар", "Зургаадугаар сар", "Долоодугаар сар", "Наймдугаар сар", "Есдүгээр сар", "Аравдугаар сар", "Арваннэгдүгээр сар", "Арванхоёрдугаар сар");
// Month abbreviations. Change this for local month names
Date.monthAbbreviations = new Array("1 сар", "2 cар", "3 сар", "4 сар", "5 сар", "6 сар", "7 сар", "8 сар", "9 сар", "10 сар", "11 сар", "12 сар");
// Full day names. Change this for local month names
Date.dayNames = new Array("Ням", "Даваа", "Мягмар", "Лхагва", "Пүрэв", "Баасан", "Бямба");
// Day abbreviations. Change this for local month names
Date.dayAbbreviations = new Array("Ням", "Дав", "Мяг", "Лха", "Пүр", "Баа", "Бям");
Date.dayAbbreviationMini = new Array("Ня", "Да", "Мя", "Лх", "Пү", "Ба", "Бя");
// Used for parsing ambiguous dates like 1/2/2000 - default to preferring 'American' format meaning Jan 2.
// Set to false to prefer 'European' format meaning Feb 1
Date.preferAmericanFormat = false;

Date.firstDayOfWeek = 1;
Date.defaultFormat = "yy-MM-dd";

Number.decimalSeparator = ".";
Number.groupingSeparator = ",";
Number.minusSign = "-";
Number.currencyFormat = "##0.00";

var millisInWorkingDay = 36000000;
var workingDaysPerWeek = 5;

var project_weekends = project_weekend.split('#');
function isHoliday(date) {
    pad = function (val) {
        val = "0" + val;
        return val.substr(val.length - 2);
    };

    var ymd = "#" + date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate()) + "#";
    var md = "#" + pad(date.getMonth() + 1) + "-" + pad(date.getDate()) + "#";
    var day = date.getDay();

    return (checkIsDayHoy(day) || project_holidays.indexOf(ymd) > -1 || project_holidays.indexOf(md) > -1);
}
function isHolidayWeekend(date) {
    var day = date.getDay();
    if (day == 0 || day == 6)return true;
    else return isHoliday(date);
}
function checkIsDayHoy(day) {
    if (project_weekend.length > 0) {
        for (var i = 0; i < project_weekends.length - 1; i++) {
            if (parseInt(project_weekends[i]) == day)return true;
        }
    }
    return false;
}

var i18n = {
    FORM_IS_CHANGED: "You have some unsaved data on the page!",
    YES: "yes",
    NO: "no",
    FLD_CONFIRM_DELETE: "confirm the deletion?",
    INVALID_DATA: "The data inserted are invalid for the field format.",
    ERROR_ON_FIELD: "Error on field",
    CLOSE_ALL_CONTAINERS: "close all?",


    DO_YOU_CONFIRM: "Do you confirm?"
};

  