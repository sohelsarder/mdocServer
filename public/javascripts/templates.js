
    //Global template variables

    var question;
    var commonProperties;
    var commonBranchingProperties;
    var nextQuestionOptions;
    var stringBranch;
    var stringBranchingProperties;
    var intBranch;
    var intBranchingProperties;
    var selectText;
    var selectProperties;
    var selectOption;
    var selectBranch;
    var selectBranchingProperties;
    var select1Option;
    var select1Branch;
    var select1BranchingProperties;
    var select1Properties;
    var dateBranch;
    var dateBranchingProperties;
    var dateProperties;
    var intValidation;
    var intProperties;
    var stringValidation;
    var stringProperties;
    var binaryProperties;
    var endProperties;
    var formatQuestionTitle;

$(function(){

    /* ********************************************************************** */
    // Templates
    /* ********************************************************************** */

    question = '<div id="question${id}" class="question well ${qtype}"><div class="wrap"><div class="label">${name}</div><div class="caption">${caption}</div><input type="hidden" id="qtype${id}" name="qtype${id}" value="${qtype}"><div class="icon"></div></div></div>';

    commonProperties = '<div class="properties">' +
                           '<span class="label label-important">Properties</span>' +
                           '<div class="commonProperties">' +
                                '<label for="txtName">Name</label> ' +
                                '<input type="text" class="propElement varType" id="txtName" name="txtName" value="${qname}">' +
                                '<span class="help-inline small">Required &amp; Alphanumeric only.</span>' +
                                '<label for="txtCaption">Caption</label> ' +
                                '<input type="text" class="propElement" id="txtCaption" name="txtCaption" value="${caption}">' +
                                '<label for="txtHint">Hint</label> ' +
                                '<input type="text" class="propElement" id="txtHint" name="txtHint" value="${hint}">' +
                                '<label for="txtDefaultVal">Default Value</label> ' +
                                '{{if qtype == "select1"}}' +
                                    '<select class="propElement" name="txtDefaultVal" id="txtDefaultVal">' +
                                        '<option {{if defaultval}}{{if defaultval == ""}}selected{{/if}}{{/if}} value="">None</option>' +
                                        '{{each(i, option) options}}' +
                                        '<option {{if defaultval}}{{if defaultval == "option" + option.id}}selected{{/if}}{{/if}} class="option${option.id}" value="option${i+1}">Option ${option.id}</option>' +
                                        '{{/each}}' +
                                    '</select>' +
                                '{{else qtype == "select"}}' +
                                    '<select multiple="multiple" class="propElement" name="txtDefaultVal" id="txtDefaultVal">' +
                                        '<option {{if defaultval}}{{if defaultval == ""}}selected{{/if}}{{/if}} value="">None</option>' +
                                        '{{each(i, option) options}}' +
                                        '<option {{if defaultval}}{{each(j, cval) defaultval}}{{if cval == "option" + option.id}}selected{{/if}}{{/each}}{{/if}} class="option${option.id}" value="option${option.id}">Option ${option.id}</option>' +
                                        '{{/each}}' +
                                    '</select>' +
                                '{{else qtype=="date"}}' +
                                    '<input type="text" class="propElement calendar" id="txtDefaultVal" name="txtDefaultVal" value="${defaultval}">' +
                                    '<span class="help-inline small">Leave blank for the current date</span>' +
                                '{{else}}' +
                                    '<input type="text" class="propElement {{if qtype=="int"}}numType{{/if}}" id="txtDefaultVal" name="txtDefaultVal" value="${defaultval}">' +
                                '{{/if}}' +
                                '<label for="chkRequired" class="checkbox inline"><input type="checkbox" {{if required}} checked {{/if}} class="propElement" id="chkRequired" name="chkRequired" >' +
                                '<span>Required</span></label>' +
                                '<label for="chkReadonly" class="checkbox inline"><input type="checkbox" {{if readonly}} checked {{/if}} class="propElement" id="chkReadonly" name="chkReadonly" >' +
                                '<span>Read only</span></label>' +
                                '<div class="clear"></div>' +
                            '</div>';

     nextQuestionOptions =  '{{each(j, next) nexts}}' +
                                    '<option {{if branch.nextq==next}} selected {{/if}} value="${next}">${formatQuestionTitle(next)}</option>' +
                            '{{/each}}';

    commonBranchingProperties =
                            '<div class="commonBranchingProperties separator">' +
                                '<span class="label label-success">Branching rules</span> ' +
                                '<div class="items">' +
                                    '<div class="item">' +
                                        '<label for="nextCondition1">If condition</label> ' +
                                        '<select class="nextc propElement" name="nextCondition1" id="nextCondition1">' +
                                            '<option value="any">Any</option>' +
                                        '</select>' +

                                        '{{each(i, branch) branches}}' +
                                        '<label for="nextQuestion1">Go to next question</label> ' +
                                        '<select class="nextq propElement" name="selectNext1" id="nextQuestion1">' +
                                            '<option {{if branch.nextq=="disconnect"}} selected {{/if}} value="disconnect">Stop here</option>' +
                                            nextQuestionOptions +
                                        '</select>' +
                                        '{{/each}}' +
                                    '</div>' +
                                '</div>' +
                            '</div>';


     stringBranch = '<div class="item" >' +
                            '<div class="rght"> {{if i !=0}}<a class="no-ajaxy propElement small danger removeBranch cross" href="javascript://">x</a>{{/if}}</div>' +
                            '<label for="nextCondition${i + 1}">If condition</label> ' +
                            '<select class="nextc propElement" name="nextCondition${i + 1}" id="nextCondition${i + 1}">' +
                                '{{if i==0}} <option {{if branch.rule=="any"}} selected {{/if}} value="any">Any value</option> {{/if}}' +
                                '{{if i!=0}} <option {{if branch.rule=="is"}} selected {{/if}} value="is">Value is</option>' +
                                '<option {{if branch.rule=="not"}} selected {{/if}} value="not">Value is not</option>' +
                                '<option {{if branch.rule=="calc"}} selected {{/if}} value="calc">Custom Calculation</option> {{/if}}' +
                            '</select>' +

                            '{{if i!=0}}<label for="branchVal${i + 1}">Value</label> ' +
                            '<input type="text" class="branchVal propElement" id="branchVal${i + 1}" name="branchVal${i + 1}" value="${branch.value}">{{/if}}' +

                            '<label for="nextQuestion${i + 1}">Go to next question</label> ' +
                            '<select class="nextq propElement" name="selectNext${i + 1}" id="nextQuestion${i + 1}">' +
                                '<option {{if branch.nextq=="disconnect"}} selected {{/if}} value="disconnect">Stop here</option>' +
                                nextQuestionOptions +
                            '</select>' +
                        '</div>';


    stringBranchingProperties = '<div class="stringBranchingProperties separator">' +
                                        '<span class="label label-success">Branching rules</span> ' +
                                        '<div class="items">' +
                                            '{{each(i, branch) branches}}' +
                                            stringBranch +
                                            '{{/each}}' +
                                        '</div>' +
                                        '<a class="no-ajaxy small success propElement addBranch" href="javascript://">Add Branch</a>' +
                                    '</div>';

    selectText =        '<div class="item">' +
                                '<div class="rght"><a data="${option.id}" class="no-ajaxy propElement small danger removeOption cross" href="javascript://">Delete Option ${option.id}</a></div>' +
                                '<label for="optCap${option.id}">Caption</label>' +
                                '<input name="optCap${option.id}" class="propElement notNil" id="optCap${option.id}" type="text" value="${option.caption}">' +

                                '<label for="optVal${option.id}">Value</label>' +
                                '<input name="optVal${option.id}" class="propElement notNil" id="optVal${option.id}" type="text" value="${option.value}">' +
                            '</div>';

    selectProperties =  '<div class="selectProperties separator">' +
                                '<span class="label label-warning">Options</span>' +
                                '<div class="items">' +
                                    '{{each(i, option) options}}' +
                                    selectText +
                                    '{{/each}}' +
                                '</div>' +
                                '<a class="no-ajaxy propElement small success addOption" href="javascript://">Add Option</a>' +
                            '</div>';

    selectOption = '<option {{each(j, rule) branch.rule}}{{if rule == "option" + (k)}}selected{{/if}}{{/each}} class="option${k}" value="option${k}">Option ${k}</option>';

    selectBranch = '<div class="item">' +
                            '<div class="rght">{{if i!=0}}<a class="no-ajaxy propElement small danger removeBranch cross" href="javascript://">x</a>{{/if}}</div>' +
                            '<label for="nextCondition">If condition</label> ' +
                            '<select multiple="multiple" class="nextc propElement notNil" name="nextCondition" id="nextCondition">' +
                                '{{if i==0}} <option selected value="any">Any</option> {{/if}}' +
                                '{{if i!=0}} {{each(key, k) branchItems}}' +
                                    selectOption +
                                '{{/each}} ' +
                                '<option {{each(j, rule) branch.rule}}{{if rule == "calc"}}selected{{/if}}{{/each}} class="calc" value="calc">Custom Calculation</option>' +
                                '{{/if}}' +
                            '</select>' +

                            '{{if i!=0}} <div class="hide"> <label for="calcValue${i + 1}">Custom Calculation</label> ' +
                            '<input type="text" class="propElement calcValue" id="calcValue${i + 1}" name="calcValue${i + 1}" value="${branch.calcValue}"> </div> {{/if}}' +                            

                            '<label for="nextQuestion">Go to next question</label> ' +
                            '<select class="nextq propElement" name="selectNext" id="nextQuestion">' +
                                '<option value="disconnect">Stop here</option>' +
                                nextQuestionOptions +
                            '</select>' +
                        '</div>';

    selectBranchingProperties = '<div class="selectBranchingProperties separator">' +
                                        '<span class="label label-success">Branching rules</span> ' +
                                        '<div class="items">' +
                                        '{{each(i, branch) branches}}' +
                                            selectBranch +
                                        '{{/each}}' +
                                        '</div>' +
                                        '<a class="no-ajaxy propElement small success addBranch" href="javascript://">Add Branch</a>' +
                                    '</div>';

    select1Option = '<option {{if branch.rule == "option" + (k)}}selected{{/if}} class="option${k}" value="option${k}">Option ${k}</option>';

    select1Branch = '<div class="item">' +
                            '<div class="rght">{{if i!=0}}<a class="no-ajaxy propElement small danger cross removeBranch" href="javascript://">x</a>{{/if}}</div>' +
                            '<label for="nextCondition">If condition</label> ' +
                            '<select class="nextc propElement" name="nextCondition" id="nextCondition">' +
                                '{{if i==0}}<option {{if branch.rule=="any"}}selected{{/if}} value="any">Any</option>{{/if}}' +
                                '{{if i!=0}} {{each(key, k) branchItems}}' +
                                    select1Option +
                                '{{/each}}' +
                                '<option {{if branch.rule == "calc"}}selected{{/if}} class="calc" value="calc">Custom Calculation</option>' +
                                '{{/if}}' +
                            '</select>' +

                            '{{if i!=0}}<div class="hide"> <label for="calcValue${i + 1}">Custom Calculation</label> ' +
                            '<input type="text" class="propElement calcValue" id="calcValue${i + 1}" name="calcValue${i + 1}" value="${branch.calcValue}"> </div>{{/if}}' +

                            '<label for="nextQuestion">Go to next question</label> ' +
                            '<select class="nextq propElement" name="selectNext" id="nextQuestion">' +
                                '<option value="disconnect">Stop here</option>' +
                                nextQuestionOptions +
                            '</select>' +
                        '</div>';

    select1BranchingProperties = '<div class="select1BranchingProperties separator">' +
                                        '<span class="label label-success">Branching rules</span> ' +
                                        '<div class="items">' +
                                        '{{each(i, branch) branches}}' +
                                            select1Branch +
                                        '{{/each}}' +
                                        '</div>' +
                                        '<a class="no-ajaxy propElement small success addBranch" href="javascript://">Add Branch</a>' +
                                    '</div>';

    select1Properties = '<div class="select1Properties separator">' +
                                '<span class="label label-warning">Options</span>' +
                                '<div class="items">' +
                                    '{{each(i, option) options}}' +
                                    selectText +
                                    '{{/each}}' +
                                '</div>' +
                                '<a class="no-ajaxy propElement small success addOption" href="javascript://">Add Option</a>' +
                            '</div>';

    intBranch = '<div class="item" >' +
                            '<div class="rght"> {{if i !=0}} <a class="no-ajaxy propElement small danger removeBranch cross" href="javascript://">x</a> {{/if}}</div>' +
                            '<label for="nextCondition${i + 1}">If condition</label> ' +
                            '<select class="nextc propElement" name="nextCondition${i + 1}" id="nextCondition${i + 1}">' +
                                '{{if i==0}} <option {{if branch.rule=="any"}} selected {{/if}} value="any">Any value</option> {{/if}}' +
                                '{{if i!=0}} <option {{if branch.rule=="is"}} selected {{/if}} value="is">Value is</option>' +
                                '<option {{if branch.rule=="not"}} selected {{/if}} value="not">Value is not</option>' +
                                '<option {{if branch.rule=="greater"}} selected {{/if}} value="greater">Value greater than</option>' +
                                '<option {{if branch.rule=="less"}} selected {{/if}} value="less">Value less than</option>' +
                                '<option {{if branch.rule=="calc"}} selected {{/if}} value="calc">Custom Calculation</option>  {{/if}}' +
                            '</select>' +

                            '{{if i!=0}} <label for="branchVal${i + 1}">Value</label> ' +
                            '<input type="text" class="branchVal propElement" id="branchVal${i + 1}" name="branchVal${i + 1}" value="${branch.value}">{{/if}}' +

                            '<label for="nextQuestion${i + 1}">Go to next question</label> ' +
                            '<select class="nextq propElement" name="selectNext${i + 1}" id="nextQuestion${i + 1}">' +
                                '<option {{if branch.nextq=="disconnect"}} selected {{/if}} value="disconnect">Stop here</option>' +
                                nextQuestionOptions +
                            '</select>' +
                    '</div>';


    intBranchingProperties = '<div class="intBranchingProperties separator">' +
                                        '<span class="label label-success">Branching rules</span> ' +
                                        '<div class="items">' +
                                            '{{each(i, branch) branches}}' +
                                            intBranch +
                                            '{{/each}}' +
                                        '</div>' +
                                        '<a class="no-ajaxy propElement small success addBranch" href="javascript://">Add Branch</a>' +
                                    '</div>';

    dateBranch = '<div class="item" >' +
                            '<div class="rght"> {{if i !=0}} <a class="no-ajaxy propElement small cross danger removeBranch" href="javascript://">x</a> {{/if}} </div>' +
                            '<label for="nextCondition${i + 1}">If condition</label> ' +
                            '<select class="nextc propElement" name="nextCondition${i + 1}" id="nextCondition${i + 1}">' +
                                '{{if i==0}} <option {{if branch.rule=="any"}} selected {{/if}} value="any">Any value</option> {{/if}}' +
                                '{{if i!=0}} <option {{if branch.rule=="least"}} selected {{/if}} value="least">At least</option>' +
                                '<option {{if branch.rule=="most"}} selected {{/if}} value="most">At most</option>' +
                                '<option {{if branch.rule=="exact"}} selected {{/if}} value="exact">Exact</option>' +
                                '<option {{if branch.rule=="calc"}} selected {{/if}} value="calc">Custom Calculation</option>  {{/if}}' +
                            '</select>' +

                            '{{if i!=0}} <label for="branchVal${i + 1}">Value</label> ' +
                            '<input type="text" class="branchVal propElement" id="branchVal${i + 1}" name="branchVal${i + 1}" value="${branch.value}">' +
                            '<span class="help-inline small">Value in number of Day(s)</span>' +

                            '<select class="nextRange propElement" name="nextRange${i + 1}" id="nextRange${i + 1}">' +
                                '<option {{if branch.nextRange=="after"}} selected {{/if}} value="after">After</option>' +
                                '<option {{if branch.nextRange=="before"}} selected {{/if}} value="before">Before</option>' +
                            '</select>' +
                            '{{/if}}' +
                            '<label for="nextQuestion${i + 1}">Go to next question</label> ' +
                            '<select class="nextq propElement" name="selectNext${i + 1}" id="nextQuestion${i + 1}">' +
                                '<option {{if branch.nextq=="disconnect"}} selected {{/if}} value="disconnect">Stop here</option>' +
                                nextQuestionOptions +
                            '</select>' +
                        '</div>';


    dateBranchingProperties = '<div class="dateBranchingProperties separator">' +
                                        '<span class="label label-success">Branching rules</span> ' +
                                        '<div class="items">' +
                                            '{{each(i, branch) branches}}' +
                                            dateBranch +
                                            '{{/each}}' +
                                        '</div>' +
                                        '<a class="no-ajaxy propElement small addBranch" href="javascript://">Add Branch</a>' +
                                    '</div>';

     dateProperties =   '<div class="dateProperties separator">' +
                                '<span class="label label-warning">Date Range</span> ' +
                                '{{each(i, range) validations}}' +
                                '<div>' +
                                '<label for="dDays">Days</label> ' +
                                '<input name="dDays" class="propElement" id="dDays" type="text" value="{{if range}}${range.value}{{/if}}">' +

                                '<label for="dFrom">From</label> ' +
                                '<input class="calendar propElement" name="dFrom" id="dFrom" type="text" value="{{if range}}${range.baseDate}{{/if}}">' +
                                '<span class="help-inline small">Leave blank for the current date</span>' +
                                '<label for="dMsg">Message</label> ' +
                                '<input class="propElement" name="dMsg" id="dMsg" type="text" value="${validationMsg}">' +
                                '<span class="help-inline small">Error message if validation fails.</span>' +
                                '{{/each}}' +
                                '</div>' +
                            '</div>';

    intValidation =     '<div class="item">' +
                                '<div class="rght"><a class="no-ajaxy propElement small danger cross removeValidation" href="javascript://">x</a></div>' +
                                '<label for="validationType${i + 1}">Validation type</label>' +
                                '<select class="validationType propElement" name="validationType${i + 1}" id="validationType${i + 1}">' +
                                /*  '<option {{if validation.validationType=="none"}} selected {{/if}} value="none">No validation</option>' + */
                                    '<option {{if validation.validationType=="min"}} selected {{/if}} value="min">Minimum value</option>' +
                                    '<option {{if validation.validationType=="max"}} selected {{/if}} value="max">Maximum value</option>' +
                                    '<option {{if validation.validationType=="customRegex"}} selected {{/if}} value="customRegex">Custom Regex</option>' +
                                '</select>' +

                                '<label for="validationVal${i + 1}">Value</label> ' +
                                '<input type="text" class="propElement validationVal" id="validationVal${i + 1}" name="validationVal${i + 1}" value="${validation.value}">' +
                                '{{if i==0}}<label for="validationMsg">Message</label> ' +
                                '<input type="text" class="propElement validationMsg" id="validationMsg" name="validationMsg" value="${validationMsg}">' +
                                '<span class="help-inline small">Error message if validation fails.</span>{{/if}}' +
                            '</div>';

    intProperties = '<div class="intValidationProperties separator">' +
                                '<label for="intType">Number type</label> ' +
                                '<select class="intType propElement" name="intType" id="intType">' +
                                    '<option {{if numType}} {{if numType=="int"}} selected {{/if}} {{/if}} value="int">Integer</option>' +
                                    '<option {{if numType}} {{if numType=="dec"}} selected {{/if}} {{/if}} value="dec">Decimal</option>' +
                                '</select>' +

                                '<div class="items">' +
                                    '<span class="label label-info">Validation rules</span> ' +
                                    '{{each(i, validation) validations}}' +
                                    intValidation +
                                    '{{/each}}' +
                                '</div>' +
                                '<a class="no-ajaxy propElement small success addValidation" href="javascript://">Add Validation</a>' +
                            '</div>';

    stringValidation =  '<div class="item">' +
                                '<div class="rght"><a class="no-ajaxy propElement small danger cross removeValidation" href="javascript://">x</a></div>' +
                                '<label for="validationType${i + 1}">Validation type</label>' +
                                '<select class="validationType propElement" name="validationType${i + 1}" id="validationType${i + 1}">' +
                                /*  '<option {{if validation.validationType=="none"}} selected {{/if}} value="none">No validation</option>' +  */
                                    '<option {{if validation.validationType=="minLen"}} selected {{/if}} value="minLen">Minimum length</option>' +
                                    '<option {{if validation.validationType=="maxLen"}} selected {{/if}} value="maxLen">Maximum length</option>' +
                                    '<option {{if validation.validationType=="customRegex"}} selected {{/if}} value="customRegex">Custom Regex</option>' +
                                '</select>' +

                                '<label for="validationVal${i + 1}">Value</label> ' +
                                '<input type="text" class="propElement validationVal" id="validationVal${i + 1}" name="validationVal${i + 1}" value="${validation.value}">' +
                                '{{if i==0}}<label for="validationMsg">Message</label> ' +
                                '<input type="text" class="propElement validationMsg" id="validationMsg" name="validationMsg" value="${validationMsg}">' +
                                '<span class="help-inline small">Error message if validation fails.</span>{{/if}}' +
                            '</div>';

    stringProperties = '<div class="stringValidationProperties separator">' +
                                '<div class="items">' +
                                    '<span class="label label-success">Validation rules</span> ' +
                                    '{{each(i, validation) validations}}' +
                                    stringValidation +
                                    '{{/each}}' +
                                '</div>' +
                                '<a class="no-ajaxy propElement small success addValidation" href="javascript://">Add Validation</a>' +
                            '</div>';

    binaryProperties = '<div class="validationProperties separator"' +
                                '<label for="mediaType">Media type</label> ' +
                                '<select class="mediaType propElement" name="mediaType" id="mediaType">' +
                                    '<option {{if mediaType}} {{if mediaType=="image"}} selected {{/if}} {{/if}} value="image">Image</option>' +
                                    '<option {{if mediaType}} {{if mediaType=="audio"}} selected {{/if}} {{/if}} value="audio">Audio</option>' +
                                    '<option {{if mediaType}} {{if mediaType=="video"}} selected {{/if}} {{/if}} value="video">Video</option>' +
                                '</select>' +
                            '</div>';

    endProperties = '<a href="#" class="btn btn-danger delete btnDelete" id="btnDelete">Delete</a><a href="#" class="btn btn-success btnApply" id="btnApply">Apply</a></div>';
    //endProperties = '<a href="#" class="no-ajaxy btn danger delete btnDelete" id="btnDelete">Delete</a></div>';

    $.template('stringTmpl', commonProperties + stringProperties +  stringBranchingProperties + endProperties);
    $.template('intTmpl', commonProperties + intProperties + intBranchingProperties + endProperties);
    $.template('dateTmpl', commonProperties + dateProperties + dateBranchingProperties + endProperties);
    $.template('geopointTmpl', commonProperties + commonBranchingProperties + endProperties);
    $.template('barcodeTmpl', commonProperties + commonBranchingProperties + endProperties);
    $.template('binaryTmpl', commonProperties + binaryProperties + commonBranchingProperties + endProperties);
    $.template('selectTmpl', commonProperties + selectProperties + selectBranchingProperties + endProperties);
    $.template('select1Tmpl', commonProperties + select1Properties + select1BranchingProperties + endProperties);

    $.template('qTmpl', question);

    formatQuestionTitle = function (str)
    {
        var resultS = str.charAt(0).toUpperCase() + str.substr(1).toLowerCase();
        return resultS.slice(0,8) + ' ' + resultS.slice(8);
    };

});

