$(function () {
    $('a').click(function () {
        if ($(this).attr('href').length > 0 && $(this).attr('href') != "#")
            return confirm("Өөр хуудасруу шилжих үү?");
//            var parentOffset = $(this).offset();
//            //or $(this).offset(); if you really just want the current element's offset
//            var relX = e.pageX - parentOffset.left;
//            var relY = e.pageY - parentOffset.top;
    });
});
