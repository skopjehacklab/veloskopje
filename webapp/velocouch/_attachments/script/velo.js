jQuery(function($) {
    $Couch.view('reports', {}).done(
        function(data) {
            for (var i = 0; i < data.rows.length; i++) {
                var report = $("<div>").html(data.rows[i].value.timestamp + "<br />" +
                    data.rows[i].value.latitude + "-:-" + data.rows[i].value.longitude + "<br />" + "<img src='data:image/jpg;base64," + data.rows[i].value.image + "' /><br />" + data.rows[i].value.comment + "</div>");
                $('#container').append(report);
            }
        });
})
