// global delete button behavior
$('a.delete').click(function(e){
    var that = this;
    e.preventDefault();
    bootbox.dialog("Are you sure you want to delete this?",
        [{
            "label": "Cancel"
        }, {
            "label": "Delete",
            "class": "btn-danger",
            "callback": function() {
                $.post($(that).data('url'), function() {
                    $(that).parent().parent().slideUp();
                });
            }
        }],
        {
            "header": "Confirmation"
        }
    );
});

// $("select, input:checkbox, input:radio, input:file").uniform();
$("input:checkbox, input:radio").uniform();

$('a.image').fancybox({
    'transitionIn'  : 'elastic',
    'transitionOut' : 'elastic',
    'margin'        : '60'
});

$('a.imageGroup').fancybox({
    'transitionIn'  : 'elastic',
    'transitionOut' : 'elastic',
    'opacity'       : true,
    'cyclic'        : true
});

$('a.gmap').fancybox({
    'autoScale'     : false,
    'width'         : 640,
    'height'        : 640,
    'transitionIn'  : 'elastic',
    'transitionOut' : 'elastic',
    'margin'        : '60'
});

$('#modal-alert .btnModalClose').live('mouseup', function(){
    $(this).parent().parent().modal('hide');
});

var bootstrapAlert = function (errmsg, callback){
    $('#modal-alert div.modal-body p').html(errmsg);
    $('#modal-alert').modal('hide');
    $('#modal-alert').modal('show');
    // Chorami buddhi
    window.myModalCallback = callback;
}

// Mu ha ha
alert = bootstrapAlert;

// Search
$('.search-query').on('keypress', function(e) {
    var keycode = (e.keyCode ? e.keyCode : e.which),
        term = $('.search-query').val();
    if(keycode === 13 && term.length) {
        $('.searchable tr').each(function(id, row) {
            var $row = $(row),
                visible = false,
                $tds = $row.find('td');
                
            if($tds.length) {
                $tds.each(function(cId, cell) {
                    if($(cell).text().indexOf(term) !== -1) {
                        visible = true;
                        return;
                    }
                });
                if(visible) {
                    $row.fadeIn();
                } else {
                    $row.fadeOut();
                }
            }
        });
        e.preventDefault();
    }
});
