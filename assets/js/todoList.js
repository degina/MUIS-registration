/**
 * Created by enkhamgalan on 5/3/15.
 */
$(function () {
    $('div#todoListDiv a.deleteTodo').on('click', function (e) {
        if (confirm("Устгах уу?")) {
            $.ajax({
                type: "POST",
                data: {tid: $(this).attr('tid')},
                url: "/dashboardTodoDelete"
            });
        } else e.stopPropagation();
    });
    jQuery('#todoTextPickerReminder').datetimepicker({lang: 'mn', step: 15});
    $('ul#sortable-todo input.task-item').on('change', function () {
        $.ajax({
            type: "POST",
            data: {
                tid: $(this).attr('tid'),
                done: $(this).prop('checked')
            },
            url: "/dashboardTodoUpdate"
        });
    });
});
function todoListAdd() {
    var todoDiv = $('div#todoListAdd');
    todoDiv.modal('show');
    todoDiv.find('input#new-todo').val('');
    todoDiv.find('input#todoTextPickerReminder').val('');
}

function todoListAddAction() {
    var todoDiv = $('div#todoListAdd');
    todoDiv.modal('hide');
    $.ajax({
        type: "POST",
        data: {
            text: todoDiv.find('input#new-todo').val(),
            reminder: todoDiv.find('input#todoTextPickerReminder').val()
        },
        url: "/dashboardTodoNew"
    }).success(function (data) {
        $('ul#sortable-todo').before(data);
    });
}
function todoListPickerReminderClear() {
    $('input#todoTextPickerReminder').val('');
}