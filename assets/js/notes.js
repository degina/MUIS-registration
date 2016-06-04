var noteContainer = $('div#noteContents');
var noteAddingNow = false, noteDeleting = false, noteShowContentOpened = false;
$(function () {
    noteContainer.fadeOut();
    var notes = notes || {};

    /*  Search Notes Function  */
    if ($('input#notes-finder').length) {
        $('input#notes-finder').val('').quicksearch('.note-item');
    }

    notes.$container = $(".page-notes");
    $.extend(notes, {
        $currentNote: $(null),
        $currentNoteTitle: $(null),
        $currentNoteDescription: $(null),
        $currentNoteId: 0,
        addNote: function () {
            if (!noteAddingNow) {
                noteAddingNow = true;
                $.ajax({
                    type: "POST",
                    data: {id: 0, title: "", notes: ''},
                    url: "/dashboardNoteUpdate"
                }).success(function (data) {
                    var $note = $(data);
                    notes.$notesList.prepend($note);
                    notes.$notesList.find('.note-item').removeClass('current');
                    $note.addClass('current');
                    notes.$writeNote.focus();
                    notes.checkCurrentNote();
                    customScroll();
                    noteShowContent();
                    noteAddingNow = false;
                });
            }
        },
        checkCurrentNote: function () {
            var $current_note = notes.$notesList.find('div.current').first();

            if ($current_note.length) {
                notes.$currentNote = $current_note;
                notes.$currentNoteTitle = $current_note.find('.note-name');
                notes.$currentNoteDescription = $current_note.find('.note-desc');
                notes.$currentNoteId = $current_note.find('.note-name').attr('nid');
                var $space = notes.$currentNoteTitle.text().indexOf("\r");
                $note_title = notes.$currentNoteTitle.html();
                /* If there are no breaklines, we add one */
                if ($space == -1) {
                    $note_title = notes.$currentNoteTitle.append('&#13;').html();
                }
                var completeNote = $note_title + $.trim(notes.$currentNoteDescription.html());
                $space = $note_title.indexOf("\r");
                notes.$writeNote.val(completeNote).trigger('autosize.resize');

            } else {
                var first = notes.$notesList.find('div:first:not(.no-notes)');
                if (first.length) {
                    first.addClass('current');
                    notes.checkCurrentNote();
                } else {
                    notes.$writeNote.val('');
                    notes.$currentNote = $(null);
                    notes.$currentNoteTitle = $(null);
                    notes.$currentNoteDescription = $(null);
                    notes.$currentNoteId = 0;
                }
            }
        },
        updateCurrentNoteText: function () {
            var text = $.trim(notes.$writeNote.val());
            if (notes.$currentNote.length) {
                var title = '',
                    description = '';
                if (text.length) {
                    var _text = text.split("\n"),
                        currline = 1;
                    for (var i = 0; i < _text.length; i++) {
                        if (_text[i]) {
                            if (currline == 1) {
                                title = _text[i];
                            } else {
                                description += _text[i] + "\n";
                            }
                            currline++;
                        }
                        //if (currline > 2)break;
                    }
                }
                var send_title = title.length ? title : '';
                var send_description = description.length ? description : '';
                $.ajax({
                    type: "POST",
                    data: {
                        id: notes.$currentNoteId,
                        title: send_title,
                        notes: send_description
                    },
                    url: "/dashboardNoteUpdate"
                }).complete(function () {
                    notes.$currentNoteTitle.text(send_title);
                    notes.$currentNoteDescription.text(send_description);
                });
            } else if (text.length) {
                notes.addNote();
            }
        }
    });
    if (notes.$container.length > 0) {
        notes.$notesList = notes.$container.find('#notes-list');
        notes.$txtContainer = notes.$container.find('.note-write');
        notes.$writeNote = notes.$txtContainer.find('textarea');
        notes.$addNote = notes.$container.find('#add-note');
        notes.$addNote.on('click', function (ev) {
            notes.addNote();
            notes.$writeNote.val('');
        });
        notes.$writeNote.on('keyup', function (ev) {
            notes.updateCurrentNoteText();
        });
        notes.checkCurrentNote();
        notes.$notesList.on('click', '.note-item', function (ev) {
            ev.preventDefault();
            notes.$notesList.find('.note-item').removeClass('current');
            $(this).addClass('current');
            notes.checkCurrentNote();
        });
    }

    var messages_list = $('.list-notes');
    var message_detail = $('.detail-note');

    noteTextarea();

    $(window).bind('enterBreakpoint768', function () {
        $('.page-notes .withScroll').each(function () {
            $(this).mCustomScrollbar("destroy");
            $(this).removeClass('withScroll');
            $(this).height('');
        });

        if (messages_list.height() > message_detail.height()) $('.detail-note  .panel-body').height(messages_list.height() - 119);
        if (messages_list.height() < message_detail.height()) $('.list-notes .panel-body').height(message_detail.height() - 2);

    });

    $(window).bind('enterBreakpoint1200', function () {
        messages_list.addClass('withScroll');
        message_detail.addClass('withScroll');
        customScroll();
    });

    /* Show / hide note if screen size < 991 px */
    $('#notes-list').on('click', '.note-item', function () {
        //if ($(window).width() < 991) {
        //    $('.list-notes').fadeOut();
        //    $('.detail-note').css('padding-left', '15px');
        if (!noteDeleting)noteShowContent();
        //}
    });

    $('#hideContent').on('click', function () {
        //notes.$currentNoteId;
        //notes.$currentNoteTitle.text();
        //notes.$currentNoteDescription.text();
        noteShowContentOpened = false;
        noteContainer.fadeOut();
    });
    $("button.noteDelete").on('click', function (e) {
        //e.stopPropagation();
        noteDeleting = true;
        if (!noteAddingNow) {
            noteAddingNow = true;
            $.ajax({
                type: "POST",
                data: {id: $(this).attr('nid')},
                url: "/dashboardNoteDelete"
            }).complete(function () {
                noteAddingNow = false;
                noteDeleting = false;
            });
        }
    });
});
var listNotesFirst;
function noteShowContent() {
    noteShowContentOpened = true;
    listNotesFirst = $('div.list-notes:first').offset();
    noteContainer.fadeIn().offset({
        top: listNotesFirst.top + 47,
        left: listNotesFirst.left - noteContainer.width()
    });
}

function noteTextarea() {
    $('.note-write textarea').height($(window).height() - 144);
}
$(window).scroll(function () {
    if (noteShowContentOpened) {
        noteContainer.fadeIn().offset({
            top: listNotesFirst.top + 47,
            left: listNotesFirst.left - noteContainer.width()
        });
    }
});
///****  On Resize Functions  ****/
//$(window).resize(function () {
//
//    if ($(window).width() > 991) {
//        noteTextarea();
//        $('.list-notes').show();
//        $('.detail-note').css('padding-left', '0');
//        $('.detail-note').show();
//    }
//
//});