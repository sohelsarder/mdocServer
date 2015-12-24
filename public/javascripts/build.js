var store;
var propertyHolder;
var logger;
var updateAllPositions;
var checkNamesDontExist;
var selectQuestion;
var windowCloseWarning;

//Temporary placement for debuging
// Select items
var selectItems = [];

var buildInit = function(){

    /* global on unload notification */
    windowCloseWarning = true;

    /* ********************************************************************** */
    // JS Plumb codes
    /* ********************************************************************** */

    Document.onselectstart = function () { return false; };

    // JSPlumb options
    jsPlumb.Defaults.DragOptions = { cursor: "crosshair" };
    jsPlumb.Defaults.Connector = "Bezier";

    /* ********************************************************************** */
    // Container scroll with mouse drag
    /* ********************************************************************** */

    /* ********************************************************************** */
    // Initializers
    /* ********************************************************************** */

    // Total questions
    var qCounter = 0;

    var currentQuestion= '',
        currentQuestionType = '';

    // All Questions
    var allQuestions = [];

    // Init dropdown
    $('.topbar').dropdown();

    // Counters
    var optIndex = 0;
    var strValidationIndex = 0;
    var strBranchIndex = 0;
    var intValidationIndex = 0;
    var intBranchIndex = 0;
    var dateBranchIndex = 0;
    var selectBranchIndex = 0;

    //For pasting
    var pasteTop = 0;
    var pasteLeft = 0;

    /* ********************************************************************** */
    //Edit mode
    /* ********************************************************************** */

    if (typeof propertyHolder === "undefined") {

        //New form
        propertyHolder={};
        resetCounters();

    } else {

        //Edit mode
        resetCounters();
        compatibilityFix();
        plotQuestions();
        connectQuestions();
        setCounter();
        jsPlumb.repaintEverything();
    }

    /* ********************************************************************** */
    //Utility functions
    /* ********************************************************************** */

    function setCounter(){
        for (var prop in propertyHolder){
            if (propertyHolder.hasOwnProperty(prop)){
                if (parseInt(prop.slice(8),10) > qCounter){
                    qCounter = parseInt(prop.slice(8),10);
                }
            }
        }
    }

    /* ********************************************************************** */

    function decideAnyElse(){
        if (currentQuestionType=='string'){
            if (strBranchIndex > 0) {
                $('#nextCondition1 option:first').text('Else');
            } else {
                $('#nextCondition1 option:first').text('Any value');
            }
        } else if (currentQuestionType=='int'){
            if (intBranchIndex > 0) {
                $('#nextCondition1 option:first').text('Else');
            } else {
                $('#nextCondition1 option:first').text('Any value');
            }
        } else if (currentQuestionType=='date'){
            if (dateBranchIndex > 0) {
                $('#nextCondition1 option:first').text('Else');
            } else {
                $('#nextCondition1 option:first').text('Any value');
            }
        } else if (currentQuestionType=='select' || currentQuestionType=='select1'){
            if (selectBranchIndex > 0) {
                $('#nextCondition:first option:first').text('Else');
            } else {
                $('#nextCondition:first option:first').text('Any value');
            }
        }
    }

    /* ********************************************************************** */

    function showHideCustomCalc(){
        if (currentQuestionType=='select1'){
            $('.nextc').each(function(){
                if ($(this).val()=='calc') {
                    $(this).next('div').removeClass('hide');
                } else {
                    $(this).next('div').addClass('hide');
                }
            });
        }
        if (currentQuestionType=='select'){
            $('.nextc').each(function(){
                if ($.inArray('calc', $(this).val())>=0) {
                    $(this).next('div').removeClass('hide');
                } else {
                    $(this).next('div').addClass('hide');
                }
            });
        }
    }

    /* ********************************************************************** */

    function excludeQues(excludeItem){
        var newqArr = [];
        for (var i = 0; i<allQuestions.length;i++) {
            if (allQuestions[i]!==excludeItem){
                newqArr.push(allQuestions[i]);
            }
        }
        newqArr.sort();
        return newqArr;
    }

    /* ********************************************************************** */

    $('#modal-error').modal({
            keyboard: true,
            show: false,
            backdrop: false
        });

    var showModalError = function (errmsg, callback){

        $('#modal-error div.modal-body p').html(errmsg);

        $('#modal-error').modal('hide');
        $('#modal-error').modal('show');

        // Chorami buddhi
        if($.isFunction(callback)) {
            window.myModalCallback = callback;
        } else {
            $('#modal-error h4').text(callback);
        }
    };
    //Mu ha ha
    alert = showModalError;

    /* ********************************************************************** */

    $('.btnModalClose').live('mouseup', function(){
        $(this).parent().parent().modal('hide');
    });

    /* ********************************************************************** */

    $('#modal-error').bind('hide', function () {
        // Chorami buddhi
        if (typeof window.myModalCallback === 'function') {
            window.myModalCallback();
            delete window.myModalCallback;
        }
    });

    /* ********************************************************************** */

    function compatibilityFix(){
        //'offset' property has been replaced by 'pos'
        for (var prop in propertyHolder){
            if (propertyHolder.hasOwnProperty(prop)){
                if (typeof propertyHolder[prop].offset !== "undefined") {
                    var position = {};
                    position.top = propertyHolder[prop].offset.top + 'px';
                    position.left = propertyHolder[prop].offset.left + 'px';
                    propertyHolder[prop].pos = position;
                    delete propertyHolder[prop].offset;
                }
            }
        }
    }

    /* ********************************************************************** */

    function plotQuestions(){

        for (var prop in propertyHolder){
            if (propertyHolder.hasOwnProperty(prop)){

                var qtype = '';
                var position = {};

                qtype = propertyHolder[prop].qtype;
                addQues(qtype, parseInt(prop.slice(8),10), propertyHolder[prop].caption);

                position = propertyHolder[prop].pos;
                placeQuestion(prop, position);
            }
        }
    }

    /* ********************************************************************** */

    function placeQuestion(qid, position){
        setPosition($('#' + qid), position);
    }

    /* ********************************************************************** */

    function connectQuestions(){

        var nextq = '';

        for (var prop in propertyHolder){
            if (propertyHolder.hasOwnProperty(prop)){
                for(var i = 0; i < propertyHolder[prop].branches.length; i++ ){
                    nextq = propertyHolder[prop].branches[i].nextq;
                    if (nextq !== 'disconnect') {
                        jsPlumb.connect({
                            source: prop,
                            target: nextq,
                            anchors:["BottomCenter","TopCenter"],
                            paintStyle:{lineWidth:1,strokeStyle:'#5BB75B'},
                            connector:[ "Bezier", { curviness: 80 }],
                            endpointStyle:{ radius:3 },
                            overlays:[
                                [ "Arrow", { width:10, length:10, location: 0.5} ]
                            ]
                        });
                    }
                }
            }
        }
    }

    /* ********************************************************************** */

    function applySettings(){

        if (validateProperty()){
            return;
        }

        var prevCon = [],
            nextCon = [],
            i;

        if (typeof propertyHolder[currentQuestion] !== "undefined") {
            if (typeof propertyHolder[currentQuestion].branches !== "undefined") {
                for (i = 0; i < propertyHolder[currentQuestion].branches.length; i++){
                    if ((typeof propertyHolder[currentQuestion].branches[i].nextq !== "undefined") &&
                        (propertyHolder[currentQuestion].branches[i].nextq !== "disconnect")){
                        //logger(propertyHolder[currentQuestion].branches[i].nextq);
                        prevCon.push(propertyHolder[currentQuestion].branches[i].nextq);
                    }
                }
            }

            //Delete currently saved properties
            delete propertyHolder[currentQuestion];
        }

        propertyHolder[currentQuestion] = storePropertyValues(currentQuestionType);

        if (typeof propertyHolder[currentQuestion].branches !== "undefined") {
            for (i = 0; i < propertyHolder[currentQuestion].branches.length; i++){
                if ((typeof propertyHolder[currentQuestion].branches[i].nextq !== "undefined") &&
                (propertyHolder[currentQuestion].branches[i].nextq !== "disconnect")) {
                    nextCon.push(propertyHolder[currentQuestion].branches[i].nextq);
                }
            }
        }

        //Update question caption
        $('#' + currentQuestion + ' div.caption').text(propertyHolder[currentQuestion].caption);

        //Disconnect all previous connections
        jsPlumb.select({source:currentQuestion}).detach();

        //Remove activechild class from previous questions
        if (prevCon.length>0){
            for(i = 0; i < prevCon.length; i++ ){
                $('#' + prevCon[i]).removeClass('activechild');
            }
        }

        //Add new connections
        if (nextCon.length > 0){
            for(i = 0; i < nextCon.length; i++ ){
                if (nextCon[i] !== 'disconnect') {
                    jsPlumb.connect({
                            source: currentQuestion,
                            target: nextCon[i],
                            anchors:["BottomCenter","TopCenter"],
                            paintStyle:{lineWidth:1,strokeStyle:'#DA4F49'},
                            connector:[ "Bezier", { curviness: 80 }],
                            endpointStyle:{ radius:3 },
                            overlays:[
                                [ "Arrow", { width:10, length:10, location: 0.5} ]
                            ]
                    });
                    //Add activechild class
                    $('#' + nextCon[i]).addClass('activechild');
                }
            }
        }

        $("div.propertyContainer").animate({
            backgroundColor: "#fcfcc7"
        }, 500, function() {
            $("div.propertyContainer").animate({
                backgroundColor: "#fff"
            }, 1000 );
        });

        // logger(propertyHolder);
    }

    /* ********************************************************************** */

    updateAllPositions = function (){
        for (var prop in propertyHolder){
            if (propertyHolder.hasOwnProperty(prop)){
                propertyHolder[prop].pos = getPosition($('#' + prop));
            }
        }
    };

    /* ********************************************************************** */

    /* ********************************************************************** */

    function getPosition(qObject){
        var myPosition = {};

        myPosition.top = qObject[0].style.top;
        myPosition.left = qObject[0].style.left;

        return myPosition;
    }

    /* ********************************************************************** */

    function setPosition(qObject, myPosition){
        qObject[0].style.top = myPosition.top;
        qObject[0].style.left = myPosition.left;
    }

    /* ********************************************************************** */

    function bringToTop(qID){
        var zmax = 0;

        $('#' + qID).siblings('.question').each(function() {
            var cur = parseInt($(this).css( 'zIndex'), 10);
            zmax = cur > zmax ? cur : zmax;
        });

        $('#' + qID).css( 'zIndex', zmax+1 );
    }

    /* ********************************************************************** */

    selectQuestion = function (qObject){

        var i;
        var qID =  qObject.attr('id');
        var qtype = $('#qtype' + (qID).slice(8)).attr('value');

        if (currentQuestion !== qID){
            var pVals = null;

            resetCounters();
            //pVals = propertyHolder[qID];

            /*
             * Cloning Javascript object:
             * http://stackoverflow.com/questions/122102/what-is-the-most-efficient-way-to-clone-a-javascript-object
             *
             */
            pVals = $.extend(true, {}, propertyHolder[qID]);

            qObject.removeClass('activechild');
            qObject.addClass('activeq');
            $('#' + currentQuestion).removeClass('activeq');

            //Update child styles
            //Remove active child style from previous questions
            if(typeof propertyHolder[currentQuestion] !== 'undefined') {
                if(typeof propertyHolder[currentQuestion].branches !== 'undefined') {
                    for (i = 0; i < propertyHolder[currentQuestion].branches.length; i++) {
                        if(typeof propertyHolder[currentQuestion].branches[i].nextq !== 'undefined') {
                            $('#' + propertyHolder[currentQuestion].branches[i].nextq).removeClass('activechild');
                        }
                    }
                }
            }

            //Add active child style to current children
            if(typeof propertyHolder[qID] !== 'undefined') {
                if(typeof propertyHolder[qID].branches !== 'undefined') {
                    for (i = 0; i < propertyHolder[qID].branches.length; i++) {
                        if(typeof propertyHolder[qID].branches[i].nextq !== 'undefined') {
                            $('#' + propertyHolder[qID].branches[i].nextq).addClass('activechild');
                            //Bring child to top
                            bringToTop(propertyHolder[qID].branches[i].nextq);
                        }
                    }
                }
            }

            //Highlight activechilds
            $('.activechild .label').effect("highlight", {}, 1000);
            $('.activeq .label').effect("highlight", {}, 1000);

            //Remove highlight from previous connectors
            jsPlumb.select({source: currentQuestion}).setPaintStyle({ strokeStyle:'#5BB75B', lineWidth:1 });
            //Highlight connector style
            jsPlumb.select({source: qID}).setPaintStyle({ strokeStyle:'#DA4F49', lineWidth:1 });

            //Bring active question to top
            bringToTop(qID);

            $('.properties').remove();

            // Populate property pan

            //Fill with defaults
            if (typeof pVals === "undefined") {
                pVals = {
                    qname: '',
                    qtype: '',
                    caption: '',
                    hint: '',
                    defaultval: '',
                    required: '',
                    readonly: ''
                };
            }

            if (typeof pVals.branches === "undefined") {
                pVals.branches = [{}];
            }

            if (!pVals.qtype) {
                pVals.qtype = qtype;
            }

            pVals.nexts = excludeQues(qID);

            if(qtype === 'date') {
                if(typeof pVals.validations === 'undefined') {
                    pVals.validations = [{}];
                }
            }

            if (qtype==='select'){
                if(is('String', pVals.defaultval)) {
                    pVals.defaultval = pVals.defaultval.split(' ');
                }

                for (i = 0; i<pVals.branches.length; i++){
                    if (typeof pVals.branches[i].rule !== 'undefined'){
                        pVals.branches[i].rule = pVals.branches[i].rule.split(' ');
                    }
                }
            }

            if (qtype==='select' || qtype==='select1'){
                if (typeof pVals.options === "undefined" || typeof pVals.branchItems === "undefined") {
                    pVals.options = [{'id':'1'},{'id':'2'}];
                    pVals.branchItems = [1,2];
                } else {
                    if (pVals.branchItems.length < 2){
                        pVals.options = [{'id':'1'},{'id':'2'}];
                        pVals.branchItems = [1,2];
                    }
                }

                selectItems = pVals.branchItems.slice(0);
                logger(pVals.branchItems);
                logger('line 519');
                logger(selectItems);
                if (selectItems.length > 0){
                    optIndex = parseInt(selectItems[selectItems.length - 1], 10);
                } else {
                    optIndex = selectItems.length;
                }
            }

            $.tmpl(qtype + 'Tmpl', pVals).appendTo('.propertyContainer');

            currentQuestion = qID;
            currentQuestionType = qtype;

            // Enable Calendar
            $('.calendar').datepicker();

            //Show/hide custom calculation
            showHideCustomCalc();

            //Adjust Any/Else
            if (typeof pVals !== "undefined") {
                if (typeof pVals.branches !== "undefined") {
                    if (currentQuestionType=='string') {
                        strBranchIndex = pVals.branches.length - 1;
                    } else if (currentQuestionType=='int') {
                        intBranchIndex = pVals.branches.length - 1;
                    } else if (currentQuestionType=='date') {
                        dateBranchIndex = pVals.branches.length - 1;
                    } else if (currentQuestionType=='select' || currentQuestionType=='select1' ) {
                        selectBranchIndex = pVals.branches.length - 1;
                    }
                }
            }
            decideAnyElse();

        }
    };

    /* ********************************************************************** */

    function storePropertyValues(qtype){

        var pVals = {
            qname: '',
            qtype: '',
            caption: '',
            hint: '',
            defaultval: '',
            required: '',
            readonly: ''
        };

        //Common items
        pVals.qname = $.trim($('#txtName').attr('value'));
        pVals.qtype = qtype;
        pVals.caption = $('#txtCaption').attr('value');
        pVals.hint = $('#txtHint').attr('value');
        pVals.defaultval = $('#txtDefaultVal').val();
        pVals.required = $('#chkRequired').prop('checked');
        pVals.readonly = $('#chkReadonly').prop('checked');

        pVals.pos = getPosition($('#' + currentQuestion));

        var options, tClass, key, branches, validations;

        // Multiple Options
        if (qtype === 'select'){
            options = [];
            tClass = '.' + qtype + 'Properties .items .item';
            key = 0;

            if(is('Array', pVals.defaultval)) {
                pVals.defaultval = pVals.defaultval.join(' ');
            }

            //selectItems = [];
            //optIndex = 0;
            $(tClass).each(function(){
                key = $(this).find('input:first').attr('id').substring(6);
                options.push({
                    'id'        : key, //OptCap1
                    'caption'   : $(this).find('input:first').val(),
                    'value'     : $.trim($(this).find('input:last').val())
                });
                //selectItems.push(key);
                //optIndex = Math.max(optIndex, parseInt(key));
            });

            pVals.options = options.slice(0);
            branches = [];

            $('.selectBranchingProperties div.items div.item').each(function(){
                var tmpValue = [];

                //if (tmpCounter< $(this).find('.nextc').val()){
                    for (var tmpCounter = 0; tmpCounter< $(this).find('.nextc').val().length; tmpCounter++){
                        tmpValue.push($('#optVal' + $(this).find('.nextc').val()[tmpCounter].slice(6)).val());
                    }
                //}

                // logger(tmpValue);

                if ($(this).find('.nextc').val()){
                    branches.push({
                        'rule'  : $(this).find('.nextc').val()[0]==='any'? 'any' : $(this).find('.nextc').val().join(' ') ,
                        //'value' : $(this).find('.branchVal').val(),
                        'value' : tmpValue.join(' '),
                        'nextq' : $(this).find('.nextq').val(),
                        'calcValue': $.inArray('calc', $(this).find('.nextc').val()) >= 0 ? $(this).find('.calcValue').val() : ''
                    });
                }
            });

            logger('line 633');
            logger(selectItems);
            pVals.branchItems = selectItems.slice(0);
            pVals.branches = branches.slice(0);
            logger(selectItems);
        }
        // Radio Options
        else if (qtype === 'select1'){
            options = [];
            tClass = '.' + qtype + 'Properties .items .item';
            key = 0;

            //selectItems = [];
            //optIndex = 0;
            $(tClass).each(function(){
                key = $(this).find('input:first').attr('id').substring(6);
                options.push({
                    'id'        : key, //OptCap1
                    'caption'   : $(this).find('input:first').val(),
                    'value'     : $.trim($(this).find('input:last').val())
                });
                //selectItems.push(key);
                //optIndex = Math.max(optIndex, parseInt(key));
            });

            pVals.options = options.slice(0);
            branches = [];

            $('.select1BranchingProperties div.items div.item').each(function(){
                branches.push({
                    'rule'  : $(this).find('.nextc').val(),
                    //'value' : $(this).find('.branchVal').val(),
                    'value' : $('#optVal' + $(this).find('.nextc').val().slice(6)).val(),
                    'nextq' : $(this).find('.nextq').val(),
                    'calcValue': $(this).find('.nextc').val() === 'calc' ? $(this).find('.calcValue').val() : ''
                });
            });

            logger('line 633');
            logger(selectItems);
            pVals.branchItems = selectItems.slice(0);
            pVals.branches = branches.slice(0);
            logger(selectItems);

        } else if (qtype === 'date') {
            //Calender
            pVals.validations = [{'validationType' : 'range', 'value': $.trim($('#dDays').val()), 'baseDate': $.trim($('#dFrom').val())}];
            pVals.validationMsg = $('#dMsg').val();

            branches = [];

            $('.dateBranchingProperties div.items div.item').each(function(){
                branches.push({
                    'rule'      : $(this).find('.nextc').val(),
                    'value'     : $.trim($(this).find('.branchVal').val()),
                    'nextq'     : $(this).find('.nextq').val(),
                    'nextRange' : $(this).find('.nextRange').val()
                });
            });

            pVals.branches = branches.slice(0);

        } else if (qtype === "binary"){
            //Media
            pVals.mediaType = $('#mediaType').val();

            branches = [];

            branches.push({
                'rule'  : $('.nextc').val(),
                'nextq' : $('.nextq').val()
            });

            pVals.branches = branches.slice(0);

        } else if (qtype === "string"){
            //Text

            validations = [];

            $('.stringValidationProperties div.items div.item').each(function(){
                validations.push({
                    'validationType'    : $(this).find('select:first').val(),
                    'value'             : $(this).find('input:first').val()
                });
            });

            pVals.validations = validations.slice(0);
            pVals.validationMsg = $('#validationMsg').val();

            branches = [];

            $('.stringBranchingProperties div.items div.item').each(function(){
                branches.push({
                    'rule'  : $(this).find('.nextc').val(),
                    'value' : $.trim($(this).find('.branchVal').val()),
                    'nextq' : $(this).find('.nextq').val()
                });
            });

            pVals.branches = branches.slice(0);

        } else if (qtype==="int") {
            //Number

            pVals.numType = $('#intType').val();

            validations = [];

            $('.intValidationProperties div.items div.item').each(function(){
                validations.push({
                    'validationType'    : $(this).find('select:first').val(),
                    'value'             : $.trim($(this).find('input:first').val())
                });
            });

            pVals.validations = validations.slice(0);
            pVals.validationMsg = $('#validationMsg').val();

            branches = [];

            $('.intBranchingProperties div.items div.item').each(function(){
                branches.push({
                    'rule'  : $(this).find('.nextc').val(),
                    'value' : $.trim($(this).find('.branchVal').val()),
                    'nextq' : $(this).find('.nextq').val()
                });
            });

            pVals.branches = branches.slice(0);

        } else if ((qtype==="barcode") || (qtype==="geopoint")){
            //barcode or location

            branches = [];

            branches.push({
                'rule'  : $('.nextc').val(),
                'nextq' : $('.nextq').val()
            });

            pVals.branches = branches.slice(0);
        }

        return pVals;
    }

    /* ********************************************************************** */

    function resetCounters(){
        optIndex = 0;
        strValidationIndex = 0;
        strBranchIndex = 0;
        intValidationIndex = 0;
        intBranchIndex = 0;
        selectBranchIndex = 0;

        selectItems = [];
        console.log('counter reset');
    }

    /* ********************************************************************** */

    function showAlert(msg){
        $('.errorMsg').html(msg);
        $('.alert-message').fadeIn('slow');

        setTimeout( function() {
            hideAlert();
        }, 5000 );
    }

    /* ********************************************************************** */

    function hideAlert(){
        $('.alert-message').fadeOut('slow');
    }

    /* ********************************************************************** */

    logger = function (obj, msg){
        console.log ((msg || 'Log') + ': ' + JSON.stringify(obj, null, '\t'));
    };

    /* ********************************************************************** */

    function addQues(qtype, qindex, qcaption){

        if (typeof qindex !== "undefined"){
            qCounter = qindex;
        } else {
            ++qCounter;
        }

        if (typeof qcaption === "undefined"){
            qcaption = '';
        }

        var qItem = {};

        qItem.id = qCounter;
        qItem.name = 'Question ' + qCounter;
        qItem.qtype = qtype;
        qItem.caption = qcaption;

        //Add DOM element
        $.tmpl('qTmpl', qItem).appendTo('.questions');

        //Add item
        allQuestions.push('question' + qCounter);

        //Add copy listener
        addCopyListener($("#" +  'question' + qCounter));

        jsPlumb.draggable('question' + qCounter);
        $("#" +  'question' + qCounter).draggable( "option", "containment", [$('.questions').offset().left, $('.questions').offset().top, 10000, 10000] );
        $("#" +  'question' + qCounter).draggable( "option", "opacity", 0.35 );
        $("#" +  'question' + qCounter).draggable( "option", "distance", 20 );
        $("#" +  'question' + qCounter).draggable( "option", "scroll", true );
        $("#" +  'question' + qCounter).draggable({collide: 'block'});

        //Bring new question to Top
        bringToTop('question' + qCounter);
    }

    /* ********************************************************************** */

    function removeQues(qItem){

        var itemIndex = $.inArray(qItem, allQuestions);

        //Remove item
        if (itemIndex>=0){
            allQuestions.splice(itemIndex,1);
        }

        //Remove properties
        if (propertyHolder.hasOwnProperty(qItem)){
            delete propertyHolder[qItem];
        }

        //Remove connection
        jsPlumb.detachAllConnections(currentQuestion);
        jsPlumb.removeAllEndpoints(currentQuestion);
        jsPlumb.repaint(currentQuestion);

        //Remove this question from any other branch
        //Replace with "Disconnect"
        removeBranch(qItem);

        //Remove DOM elements
        $('#' + qItem).remove();
        $('.properties').remove();
    }

    /* ********************************************************************** */

    function removeBranch(qItem){
        for (var prop in propertyHolder){
            if (propertyHolder.hasOwnProperty(prop)){
                if(typeof prop.branches !== 'undefined') {
                    for (var i = 0; i < prop.branches.length; i++) {
                        if(typeof prop.branches[i].nextq !== 'undefined') {
                            if (prop.branches[i].nextq === qItem) {
                                prop.branches[i].nextq = 'disconnect';
                            }
                        }
                    }
                }
            }
        }
    }

    /* ********************************************************************** */
    // Event handlers
    /* ********************************************************************** */

    $('.addItem').live('click', function(){

        addQues($(this).attr('type'));

        return false;
    });

    /* ********************************************************************** */

    $('.question').live('click',function(e){
        selectQuestion($(this));
        selectRange($(this)[0]);
    });

    /* ********************************************************************** */

    $('.close').live('click', function(){
        hideAlert();
        return false;
    });

    /* ********************************************************************** */

    $('#modal-confirmation').modal({
            keyboard: true,
            show: false,
            backdrop: true

        });

    $('#btnDelete').live('click',function(){
        $('#modal-confirmation').modal('show');
        return false;
    });

    /* ********************************************************************** */

    $('.btnContinueDelete').live('click', function(){
        $(this).parent().parent().modal('hide');
        removeQues(currentQuestion);
    });

    /* ********************************************************************** */

    $('#btnApply').live('click', function(){

        applySettings();

        return false;
    });

    /* ********************************************************************** */

    // Date
    $('.dateBranchingProperties .addBranch').live('click',function(){
        $.tmpl(dateBranch, {'i': ++dateBranchIndex, 'branch': {}, 'nexts': excludeQues(currentQuestion)}).appendTo('.dateBranchingProperties .items');
        decideAnyElse();
    });

    $('.dateBranchingProperties .removeBranch').live('click',function(){
        $(this).parent().parent().remove();
        --dateBranchIndex;
        decideAnyElse();
    });

    /* ********************************************************************** */

    // Multiple Options
    $('.selectProperties .addOption').live('click',function(){
        $.tmpl(selectText, {'option': {'id': ++optIndex, 'caption': '', 'value': ''}}).appendTo('.selectProperties .items');
        var newOption = $.tmpl(selectOption, {'k': optIndex, 'branch': {'rule': 'any'}});
        newOption.clone().insertBefore('.nextc .calc');
        newOption.appendTo('#txtDefaultVal');
        selectItems.push(optIndex);
        logger('line 999');
        logger(selectItems);
    });

    $('.selectProperties .removeOption').live('click',function(){
        if (selectItems.length <= 2){
            showModalError("Options cannot be less than 2!");
        } else {
            var id = parseInt($(this).attr('data'),10);
            $('.selectBranchingProperties .option' + id).remove();
            $('#txtDefaultVal .option' + id).remove();
            selectItems.splice($.inArray(id, selectItems), 1);
            $(this).parent().parent().remove();
            logger('line 1012');
            logger(selectItems);
        }
    });

    $('.selectBranchingProperties .addBranch').live('click',function(){
        $.tmpl(selectBranch, {'i':++selectBranchIndex,'branch': {'rule': 'any'}, 'branchItems': selectItems, 'nexts': excludeQues(currentQuestion)}).appendTo('.selectBranchingProperties .items');
        decideAnyElse();
    });

    $('.selectBranchingProperties .removeBranch').live('click',function(){
        $(this).parent().parent().remove();
        --selectBranchIndex;
        decideAnyElse();
    });

    /* ********************************************************************** */

    // Radio Options

    $('.select1Properties .addOption').live('click',function(){
        $.tmpl(selectText, {'option': {'id': ++optIndex, 'caption': '', 'value': ''}}).appendTo('.select1Properties .items');
        var newOption = $.tmpl(select1Option, {'k': optIndex, 'branch': {'rule': 'any'}});
        newOption.clone().insertBefore('.nextc .calc');
        newOption.appendTo('#txtDefaultVal');
        selectItems.push(optIndex);
        logger('line 1038');
        logger(selectItems);
    });

    /* ********************************************************************** */

    $('.select1Properties .removeOption').live('click',function(){
        if (selectItems.length <= 2){
            showModalError("Options cannot be less than 2!");
        } else {
            var id = parseInt($(this).attr('data'),10);
            $('.select1BranchingProperties .option' + id).remove();
            $('#txtDefaultVal .option' + id).remove();
            selectItems.splice($.inArray(id, selectItems), 1);
            $(this).parent().parent().remove();
            logger('line 1053');
            logger(selectItems);
        }
    });

    /* ********************************************************************** */

    $('.select1BranchingProperties .addBranch').live('click',function(){
        $.tmpl(select1Branch, {'i':++selectBranchIndex,'branch': {'rule': 'any'}, 'branchItems': selectItems, 'nexts': excludeQues(currentQuestion)}).appendTo('.select1BranchingProperties .items');
        decideAnyElse();
    });

    $('.select1BranchingProperties .removeBranch').live('click',function(){
        $(this).parent().parent().remove();
        --selectBranchIndex;
        decideAnyElse();
    });

    /* ********************************************************************** */

    $('.intBranchingProperties .removeBranch').live('click',function(){
        $(this).parent().parent().remove();
        --intBranchIndex;
        decideAnyElse();
    });

    /* ********************************************************************** */

    $('.intBranchingProperties .addBranch').live('click',function(){
        $.tmpl(intBranch, {'i': ++intBranchIndex, 'branch': {}, 'nexts': excludeQues(currentQuestion)}).appendTo('.intBranchingProperties .items');
        decideAnyElse();
    });

    /* ********************************************************************** */

    $('.stringBranchingProperties .removeBranch').live('click',function(){
        $(this).parent().parent().remove();
        --strBranchIndex;
        decideAnyElse();
    });

    /* ********************************************************************** */

    $('.stringBranchingProperties .addBranch').live('click',function(){
        $.tmpl(stringBranch, {'i': ++strBranchIndex, 'branch': {}, 'nexts': excludeQues(currentQuestion)}).appendTo('.stringBranchingProperties .items');
        decideAnyElse();
    });

    /* ********************************************************************** */

    $('.stringValidationProperties .addValidation').live('click',function(){
        $.tmpl(stringValidation, {'i': strValidationIndex++, 'validation': {}}).appendTo('.stringValidationProperties .items');
    });

    /* ********************************************************************** */

    $('.intValidationProperties .addValidation').live('click',function(){
        $.tmpl(intValidation, {'i': intValidationIndex++, 'validation': {}}).appendTo('.intValidationProperties .items');
    }); 

    /* ********************************************************************** */

    $('.stringValidationProperties .removeValidation').live('click', function(){
        $(this).parent().parent().remove();
        --strValidationIndex;
    });

    /* ********************************************************************** */

    $('.intValidationProperties .removeValidation').live('click', function(){
        $(this).parent().parent().remove();
        --intValidationIndex;
    });

    /* ********************************************************************** */

//  $('#chkRequired').live('click', function(){
//      applySettings();
//  });

    /* ********************************************************************** */

//  $('#chkReadonly').live('click', function(){
//      applySettings();
//  });

    /* ********************************************************************** */

//  $('.propElement').live('blur', function(){
//      applySettings();
//  });

    /* ********************************************************************** */

//  $('.nextq').live('change', function(){
//      applySettings();
//  });

    /* ********************************************************************** */

    $('.selectBranchingProperties div.items div.item select.nextc').live('change', function(){
        showHideCustomCalc();
    });

    $('.select1BranchingProperties div.items div.item select.nextc').live('change', function(){
        showHideCustomCalc();
    });

    /* ********************************************************************** */

    $(".propertyContainer").bind("DOMSubtreeModified", function() {
        if ($(this).height() >= 700) {
            $('.questions').height($(this).height());
        } else {
            $('.questions').height(700);
        }
    });

    /* ********************************************************************** */

    /* global on unload notification */
    $(window).bind('beforeunload', function(event) {
        if(windowCloseWarning) {
            //Check if we are in builder
            //Quick fix for ajax problem
            if ($('.questions').length) {
                event.stopPropagation();
                event.returnValue = "Discard changes? \nYou are navigating away from build application.\nAny unsaved change will be lost.";
                return event.returnValue;
            }
        }
    });

    /* ********************************************************************** */
    /* Copy/paste feature */

    function addCopyListener(jObj){
        jObj[0].addEventListener('copy', function (e) {

            var jsn = getSelectedQuestionJson();

            if (jsn){
                if (e.clipboardData) { // Safari, Chrome
                    e.preventDefault();
                    e.clipboardData.setData('text/json', jsn);
                }
                if (window.clipboardData) { // IE
                    e.returnValue = false;
                    window.clipboardData.setData('text/json', jsn);
                }
            }
        }, false);
    }

    var selectRange = function (obj) {
        var range;
        if (!!window.getSelection) { // FF, Safari, Chrome, Opera
            var sel = window.getSelection();
            range = document.createRange();
            range.selectNodeContents(obj);
            range.collapse();
            sel.removeAllRanges();
            sel.addRange(range);
        } else if (!!document.selection) { // IE
            document.selection.empty();
            range = document.body.createTextRange();
            range.moveToElementText(obj);
            range.collapse();
            range.select();
        }
    };

    $('.questions')[0].onpaste = function (e) {
        var jsn = e.clipboardData.getData('text/json');
        if (jsn) {
            createQuestionfromJson(JSON.parse(jsn));
        }
    };

    function getSelectedQuestionJson(){
        if (typeof propertyHolder[currentQuestion] !== "undefined") {
            var jsn = $.extend(true, {}, propertyHolder[currentQuestion]);

            delete jsn.branches;

            return JSON.stringify(jsn);
        }
    }

    function createQuestionfromJson(jsn){
        var qtype = '';
        var position = {};

        jsn.qname = createNameCopy(jsn.qname);

        var top = parseInt(jsn.pos.top, 10) + 20;
        var left = parseInt(jsn.pos.left, 10) + 20;

        if ((top===pasteTop) || (left===pasteLeft)){
            top += 20;
            left += 20;
        }

        pasteTop = top;
        pasteLeft = left;

        jsn.pos.top = top + 'px';
        jsn.pos.left = left + 'px';

        qtype = jsn.qtype;
        addQues(qtype, ++qCounter, jsn.caption);

        position = jsn.pos;
        placeQuestion('question' + qCounter, position);

        propertyHolder['question' + qCounter] = jsn;
    }

    function createNameCopy(origName){
        var newName = origName + '_copy';

        if (nameExists(newName)){
            for (var i = 1; ; i++){
                var tempName = newName + i;

                if (!nameExists(tempName)){
                    newName = tempName;
                    break;
                }
            }
        }

        return newName;
    }

    function nameExists(qname){
        var itemFound = false;

        for (var prop in propertyHolder){
            if (propertyHolder.hasOwnProperty(prop)){
                if (typeof propertyHolder[prop].qname!=="undefined"){
                    if (propertyHolder[prop].qname===qname){
                        itemFound = true;
                        break;
                    }
                }
            }
        }

        return itemFound;
    }

    /* ********************************************************************** */

    //Disable all ajax links on build
    $('a').each(function(){
        var hasNoAjax = $(this).hasClass('no-ajaxy');
        if (!hasNoAjax){
            $(this).addClass('no-ajaxy');
        }
    });

    /* ********************************************************************** */
    // Validation checks
    /* ********************************************************************** */

    checkNamesDontExist = function(){
        var qID;
        var retVal = '';

        $('.question').each(function (index, domEle){
            qID = $(this).attr('id');

            if (typeof propertyHolder[qID]!=="undefined"){
                if (typeof propertyHolder[qID].qname!=="undefined"){
                    if (propertyHolder[qID].qname===""){
                        retVal = qID;
                        return retVal;
                    }
                } else {
                    retVal = qID;
                    return retVal;
                }
            } else {
                retVal = qID;
                return retVal;
            }
        });

        return retVal;
    };

    function validateProperty(){
        var retVal;

        //Check number of options
        if (currentQuestionType=='select' || currentQuestionType=='select1'){
            logger('line 1350');
            logger(selectItems);
            if (selectItems.length < 2) {
                showModalError("Options cannot be less than 2!");
                return true;
            }
        }

        //Variable name validation
        $('.varType').each(function (index, domEle){

            var that = this;

            if($(this).val()!=='') {
                if (!(/^[a-zA-Z_][0-9a-zA-Z_]*$/.test($(this).val()))) {
                    showModalError("Invalid name for variable!", function(){
                        $(that).focus();
                        $(that).select();
                    });
                    retVal = true;
                    return retVal;
                }
            }
        });

        if (retVal) return retVal;

        //Null item validation
        $('.notNil').each(function (index, domEle){

            var that = this;

            if(!$(this).val()) {
                showModalError("This property cannot be nil!", function(){
                    $(that).focus();
                    $(that).select();
                });
                retVal = true;
                return retVal;
            }
        });

        if (retVal) return retVal;

        //Number validation
        $('.numType').each(function (index, domEle){

            var that = this;

            if(!!$(this).val()) {
                if (!isNumber($(this).val())) {
                    showModalError("Invalid number!", function(){
                        $(that).focus();
                        $(that).select();
                    });
                    retVal = true;
                    return retVal;
                }
            }
        });

        if (retVal) return retVal;

        //Check for double quote
        $('.propElement').each(function (index, domEle){

            var that = this;

            if(!!$(this).val()) {
                if ($(this).val().indexOf('"') >= 0) {
                    showModalError('Double quote (") is an invalid character here!', function(){
                        $(that).focus();
                        $(that).select();
                    });
                    retVal = true;
                    return retVal;
                }
            }
        });

        if (retVal) return retVal;

        //Check for % sign
        $('.propElement').each(function (index, domEle){

            var that = this;

            if(!!$(this).val()) {
                if ($(this).val().indexOf('%')>=0) {
                    showModalError('Percent sign (%) is an invalid character here!', function(){
                        $(that).focus();
                        $(that).select();
                    });
                    retVal = true;
                    return retVal;
                }
            }
        });

        if (retVal) return retVal;

        // Check for duplicate question name
        $('div.commonProperties .varType').each(function (index, domEle){
            var itemVal = $(this).val();
            var itemFound = false;
            var that = this;

            if(itemVal!=='') {

                for (var prop in propertyHolder){
                    if (propertyHolder.hasOwnProperty(prop)){
                        if (typeof propertyHolder[prop].qname!=="undefined"){
                            if ((propertyHolder[prop].qname===itemVal) && (prop !=currentQuestion)){
                                itemFound = true;
                                break;
                            }
                        }
                    }
                }

                if (itemFound){
                    showModalError("Question name '" + itemVal + "' already exists!", function(){
                        $(that).focus();
                        $(that).select();
                    });
                    retVal = true;
                    return retVal;
                }
            } else {
                //Question name cannot be nil
                showModalError("Question name cannot be nil!", function(){
                    $(that).focus();
                });
                retVal = true;
                return retVal;
            }
        });

        if (retVal) return retVal;
    }

    //Add Number type validation to specific fields
    $('.validationVal').live('focusin',function(){

        var valType = $(this).prevAll('select').val();

        if (valType=='min' || valType=='max' || valType=='maxLen' || valType=='minLen') {
            $(this).addClass('numType');
        } else {
            $(this).removeClass('numType');
        }
    });

    /* http://stackoverflow.com/questions/18082/validate-numbers-in-javascript-isnumeric */
    function isNumber(n) {
        return !isNaN(parseFloat(n)) && isFinite(n);
    }

    function addNumAttr(f){
        $(f).addClass('numType');
    }

    function remNumAttr(f){
        $(f).removeClass('numType');
    }

    //Util
    function is(type, obj) {
        var clas = Object.prototype.toString.call(obj).slice(8, -1);
        return obj !== undefined && obj !== null && clas === type;
    }
};

//Document ready
$(function(){
    // console.log('invoked');
    buildInit();
    // Scroll shadow control
    var $shadow = $('.shadow');
    $('.questions').scroll(function(e){
        if(!!$(this).scrollTop()) {
            $shadow.fadeIn('slow');
        } else {
            $shadow.fadeOut('slow');
        }
    });
});