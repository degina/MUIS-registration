$(function () {

    //$.ajax({
    //    url:'http://news.gogo.mn/weather/',
    //    type:'GET',
    //    success: function(data){
    //       // $('#content').html($(data).find('#content').html());
    //        $(".weather-city").html($(data).find('div#wsambar0 div.wtoday').html());
    //    }
    //});
    //
    //$.ajaxPrefilter( function (options) {
    //    if (options.crossDomain && jQuery.support.cors) {
    //        var http = (window.location.protocol === 'http:' ? 'http:' : 'https:');
    //        options.url = http + '//cors-anywhere.herokuapp.com/' + options.url;
    //        //options.url = "http://cors.corsproxy.io/url=" + options.url;
    //    }
    //});
    //
    //$.get(
    //    'http://news.gogo.mn/weather/',
    //    function (response) {
    //        console.log("> ", response);
    //        $(".weather-bg").html($(response).find('.wtoday').html());
    //    });
    $.simpleWeather({
        location: 'Ulaanbaatar',
        woeid: '',
        unit: 'c',
        success: function (weather) {
            region = weather.country;
            tomorrow_date = weather.forecast[1].date;
            weather_icon = '<i class="icon-' + weather.code + '"></i>';
            $(".weather-city").html(weather.city);
            $(".weather-currently").html(weather.currently);
            $(".today-img").html('<i class="big-img-weather icon-' + weather.code + '"></i>');
            $(".today-temp").html(weather.low + '° / ' + weather.high + '°');
            $(".weather-region").html(region);
            $(".weather-day").html(tomorrow_date);
            $(".weather-icon").html(weather_icon);
            $(".weather-speed").html(weather.wind.direction + ' ' + weather.wind.speed + ' ' + weather.units.speed);
            for (var c = 1; c <= 4; c++) {
                $("." + c + "-days-day").html(weather.forecast[c].day);
                $("." + c + "-days-image").html('<i class="icon-' + weather.forecast[c].code + '"></i>');
                $("." + c + "-days-temp").html(weather.forecast[c].low + '° / ' + weather.forecast[c].high + '°');
            }
        }
    });
    $('div#postArea div.post-new div.textPostSmall').click(function () {
        $(this).hide();
        $('div#postArea div.post-new div.post-new-footer').show( "slow" );
        $('div#postArea div.post-new textarea#textPostFull').show().focus();
        $('div#postArea div.post-new div#postHidden').show( "slow" );
        $('div#postArea div.post-new div#postHidden').find('div#attachPost').hide();
    });
    $.ajax({
        type: "POST",
        data: {

        }, url: "/event/listDataThisWeek",
        beforeSend: function(){
            $("div#eventList").html("Loading...");
        }
    }).success(
        function (data) {
            $("div#eventList").html(data);
        });
});
var calendar;
function runCalendar() {
    calendar = $('#calendar').fullCalendar({
        slotDuration: '00:15:00', /* If we want to split day time each 15minutes */
        scrollTime: '07:00:00',
        allDayText: 'Өдрийн\nтурш',
        minTime: '00:00:00',
        height: "auto",
        maxTime: '24:00:00',
        displayEventEnd: true,
        header: {
            left: 'prev,next today',
            center: 'title',
            right: 'month,agendaWeek,agendaDay'
        },
        monthNames: ["Нэгдүгээр сар", "Хоёрдугаар cар", "Гуравдугаар сар", "Дөрөвдүгээр сар", "Тавдугаар сар", "Зургаадугаар сар", "Долоодугаар сар", "Наймдугаар сар", "Есдүгээр сар", "Аравдугаар сар", "Арваннэгдүгээр сар", "Арванхоёрдугаар сар"],
        monthNamesShort: ["1 сар", "2 cар", "3 сар", "4 сар", "5 сар", "6 сар", "7 сар", "8 сар", "9 сар", "10 сар", "11 сар", "12 сар"],
        dayNames: ["Даваа", "Мягмар", "Лхагва", "Пүрэв", "Баасан", "Бямба", "Ням"],
        dayNamesShort: ["Ня", "Да", "Мя", "Лх", "Пү", "Ба", "Бя"],
        titleFormat: {
            month: 'YYYY MMMM',
            week: 'YYYY MMMM D',
            day: 'YYYY MMMM D'
        },
        axisFormat: 'H(:mm) цаг',
        timeFormat: { // for event elements
            month: 'H:mm',
            'default': 'H:mm'

        },
        buttonText: {

            prevYear: "өмнөх жил",
            nextYear: "дараагын жил",
            today: 'өнөөдөр',
            month: 'сар',
            week: '7 хоног',
            day: 'өдөр'
        },
        forceEventDuration: true,
        editable: true,
        eventResize: function (event, delta, revertFunc) {
        },
        eventRender: function (event, element) {
            $(element).find('span.fc-title').after(" <i class='fa fa-picture-o f-9'></i> ");
        },
        eventDrop: function (event, delta, revertFunc) {

        },
        selectable: true,
        eventClick: function (calEvent, jsEvent, view) {
            $("form#formEvent").attr("action", "/events/" + calEvent.eventId).submit();
        },
        select: function (start, end, allDay) {
        },
        eventSources: [
            // your event source
            {
                url: "/event/eventSources",
                type: 'POST',
                data: {
                    custom_param1: 'something',
                    custom_param2: 'somethingelse'
                },
                error: function () {
                },
                success: function (data) {
                    // console.log(" data " + JSON.stringify(data));
                }
                //  color: 'yellow',   // a non-ajax option
                // textColor: 'black' // a non-ajax option
            }
            // any other sources...
        ]
    });
}
//$(document).ready(function () {
//    runCalendar();
//    $('div[contentEditable=true]').each(function () {
//        $(this).on('paste', function (e) {
//            e.preventDefault();
//            var text = (e.originalEvent || e).clipboardData.getData('text/html') || prompt('Paste something..');
//            var $result = $('<div></div>').append($(text));
//            $(this).html($result.html());
//            // remove unnecesary tags (if paste from word)
//            $(this).children('style').remove();
//            $(this).children('meta').remove()
//            $(this).children('link').remove();
//        });
//        $(this).keydown(function (e) {
//            // trap the return key being pressed
//            if (e.keyCode == 13) {
//                // insert 2 br tags (if only one br tag is inserted the cursor won't go to the second line)
//                document.execCommand('insertHTML', false, '<br><br>');
//                // prevent the default behaviour of return key pressed
//                return false;
//            }
//        });
//    });
//});

function liveTitlePurple() {
    //animationComplete
//get the first item from the list
    var content = ["<h4 class='m-0 w-300 bg-purple' style='height: 40px;' >Тэст</h4>",
        "<p>content2</p>",
        "<div  class='full accent green'>content3</div>",
        "content4",
        "<div class='full accent cobalt'>content5</div>",
        "<p>content6</p>"];
    $('.my-live-tile').each(function () {
        $(this).liveTile("destroy", true);
        $(this).find('div:last-child').html(content[0]);
        /* To get new size if resize event */
        var tile_height = $(this).data("height") ? $(this).data("height") : $(this).find('.panel-body').height() + 52;
        $(this).height(tile_height);
        $(this).liveTile({
            speed: $(this).data("speed") ? $(this).data("speed") : 500, // Start after load or not
            mode: $(this).data("animation-easing") ? $(this).data("animation-easing") : 'carousel', // Animation type: carousel, slide, fade, flip, none
            playOnHover: $(this).data("play-hover") ? $(this).data("play-hover") : false, // Play live tile on hover
            repeatCount: $(this).data("repeat-count") ? $(this).data("repeat-count") : -1, // Repeat or not (-1 is infinite
            delay: $(this).data("delay") ? $(this).data("delay") : 0, // Time between two animations
            startNow: $(this).data("start-now") ? $(this).data("start-now") : true, //Start after load or not
            backContent: content,
            swapBack: 'html',
            backIsRandom: false
        });
    });
}

$(document).ready(function () {
    var track_load = 1; //total loaded record group(s)
    var loading = false; //to prevents multipal ajax loads
    //total record group(s)
    $(window).scroll(function () { //detect page scroll
        if ($(window).scrollTop() + $(window).height() == $(document).height())  //user scrolled to bottom of the page?
        {
            if (track_load <= total_groups && loading == false) //there's more data to load
            {
                loading = true; //prevent further ajax loading
                $('.load-more-animation').show(); //show loading image
                $.ajax({
                    type: "POST",
                    data: {
                        groupNumber: track_load
                    }, url: "/dashboard/loadMorePost"
                }).success(
                    function (data) {
                        $('div#postArea div#postAreaPadding').append(data);
                        $('.load-more-animation').hide(); //hide loading image once data is received
                        track_load++; //loaded group increment
                        loading = false;

                    }).error(function (thrownError) {
                        alert(thrownError); //alert with HTTP error
                        $('.load-more-animation').hide(); //hide loading image
                        loading = false;
                    });
            }
        }
    });
});