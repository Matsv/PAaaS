// Copied from prototype, TODO MAKE THIS BETTER
var web = {
    isHidden: false,
    addVersion: function (oldVersion, newVersion) {
        var title = "<strong>INFORMATION</strong>";
        var template = "<strong>Protocol Version:</strong> ";
        this.addHtml(title, template + oldVersion, title, template + newVersion);
    },
    addPacket: function (state, id, direction, oldPacket, newPacket) {
        console.log(state + id + direction);
        var title = "<strong>" + state + ": </strong><ins>" + id + "</ins> - " + direction;
        this.addHtml(title,
            oldPacket,
            title,
            newPacket);
    },
    addHtml: function (title, footer, newTitle, newFooter) {
        $("#data")
            .append("<div class=\"row\">"
                + "   <div class=\"col-md-6\">"
                + "       <div class=\"panel panel-danger\">"
                + "           <div class=\"panel-heading\">" + title + "</div>"
                + "           <div class=\"panel-footer code\">" + footer + "</div>"
                + "       </div>"
                + "    </div>"
                + "   <div class=\"col-md-6\">"
                + "       <div class=\"panel panel-success\">"
                + "           <div class=\"panel-heading\">" + newTitle + "</div>"
                + "           <div class=\"panel-footer code\">" + newFooter + "</div>"
                + "       </div>"
                + "    </div>");
    },
    setStatus: function (msg) {
        console.log(msg);
        document.getElementById("status").innerHTML = msg;
    },
    showOverlayError: function (msg) {
        $("#overlayerror").find("#errortext").html(msg);
        $("#overlayerror").show();
    }
};

$(document).ready(function () {
    $("#compare").prop("disabled", true);
    // $('.selectpicker').prop('disabled', true);

    jQuery('#beginModal').modal('show').on('hide.bs.modal', function (e) {
        if (web.isHidden)
            return;
        e.preventDefault();
    });
});