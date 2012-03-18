function changeTaskStatus(twId, taskStatus) {
    var urlstr = "/changetaskstatus/" + twId + "/" + taskStatus
    $.post(
        urlstr,
        {},
        function(json) {
            if(json.result == false) {
                alert(json.errmsg);
            }
            return;
        },
        "json"
    );
}
