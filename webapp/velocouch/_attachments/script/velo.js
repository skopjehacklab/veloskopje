function formatDate(dt) {
    date = dt.getDate() + "." + parseInt(dt.getMonth()+1) + "." + dt.getFullYear();
    time = dt.getHours() + ":" + dt.getMinutes() + ":" + dt.getSeconds();
    return date + " - " + time;
}

jQuery(function($) {
    $Couch.view('reports', {descending:true, include_docs:true}).done(
        function(data) {
            for (var i = 0; i < data.rows.length; i++) {
                var doc = data.rows[i].doc;
                var c = $("<div>");
                c.appendTo($("#container"));

                c.append($("<p>").addClass("date").text(formatDate((new Date(doc.timestamp)))));
                var image = '';
                if(doc._attachments && doc._attachments["slika.jpg"]) {
                    image = "/veloskopje/" + doc._id + "/slika.jpg";
                }
                else {
                    image = "data:image/jpg;base64," + doc.image;
                }
                c.append($("<img>").attr("src", image));
                c.append($("<p>").addClass("comment").text(doc.comment).appendTo(c));
                c.append($("<a>").addClass("map").attr("href", "http://www.openstreetmap.org/?lat=" +
                        doc.latitude + "&lon=" + doc.longitude +
                        "&zoom=17&layers=M").append($("<img>").attr("src", "images/osm.png").attr("alt", "види на мапа")));
            }
        });
})
