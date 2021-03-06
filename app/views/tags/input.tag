%{
	String[] pieces = _arg.split("\\.");
	Object obj = _caller.get(pieces[0]);
	this.setProperty(pieces[0], obj);

	if(_type == null) {
		_type = "text";
	}
}%

#{field _arg}
<div class="control-group#{ifError field.name} error#{/ifError}">
	<label class="control-label" for="${field.id}">&{field.name}</label>
	<div class="controls">
	#{if _type == 'select'}
		#{if _multiple}
			#{select items:_items, labelProperty:_labelProperty, name:field.name, id:field.name, value:field.value, multiple:'', class:_class}
				#{doBody /}
			#{/select}
		#{/if}
		#{else}
			#{select items:_items, labelProperty:_labelProperty, name:field.name, id:field.name, value:field.value, class:_class}
				#{doBody /}
			#{/select}
		#{/else}
	#{/if}
	#{else}
		#{html5.input for:field.name, id:field.id, class:_class, type:_type, accept:_accept, placeholder:_placeholder /}
	#{/else}
	<span class="help-inline">#{ifError field.name}${field.error.raw()}#{/ifError}#{else}${_hint}#{/else}</span>
	</div>
</div>
#{/field}