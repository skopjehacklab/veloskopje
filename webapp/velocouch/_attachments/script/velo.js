function formatDate(dt) {
    date = dt.getDate() + "." + parseInt(dt.getMonth()+1) + "." + dt.getFullYear();
    time = dt.getHours() + ":" + dt.getMinutes() + ":" + dt.getSeconds();
    return date + " " + time;
}

jQuery(function($) {
    $Couch.view('reports', {}).done(
        function(data) {
            for (var i = 0; i < data.rows.length; i++) {
                var values = data.rows[i].value;
                var c = $("<div>");
                c.appendTo($("#container"));


                c.append($("<p>").text(formatDate((new Date(values.timestamp)))));
                c.append($("<p>").text(values.comment).appendTo(c));
                c.append($("<img>").attr("src", "data:image/jpg;base64," + values.image));
                c.append($("<a>").attr("href", "http://www.openstreetmap.org/?lat=" + values.latitude +
                        "&lon=" + values.longitude + "&zoom=17&layers=M").text("Види на мапа"));
            }
        });
})
