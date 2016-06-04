if ($('.summernote').length && $.fn.summernote) {
    $('.summernote').each(function () {
        $(this).summernote({
            height: 400,
            toolbar: [
                ["style", ["style"]],
                ["style", ["bold", "italic", "underline", "clear"]],
                ["fontsize", ["fontsize"]],
                ["color", ["color"]],
                ["para", ["ul", "ol", "paragraph"]],
                ["height", ["height"]],
                ["table", ["table"]]
            ]
        });
    });
    $(".note-codable").hide();
    $("div.note-editable").css("border", "1px solid darkgrey").css("padding", "4px").css("overflow-y", "auto");
}
