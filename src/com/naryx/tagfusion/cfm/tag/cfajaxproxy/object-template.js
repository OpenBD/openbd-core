this.formId = null;
this.callbackHandler = null;
this.errorHandler = null;
this.asyncMode = true;
this.returnFormat = "json"; 
this.queryFormat = "row";
this.httpMethod = "POST";

this.remoteCall = function(params){
	params["__BDQUERYFORMAT"] = this.queryFormat;
	params["__BDRETURNFORMAT"] = this.returnFormat;
	params["__BDJSONENCODED"] = 1;
	params["__BDNODEBUG"] = 1;
	
	var v = [];
	if (params != null ){
		for ( var k in params ){v[v.length] = k;}	
	}
	params["__BDPARAMS"]	= v.join(",");

	return blueDragonAjaxLibrary.load(this.remoteCFC, params, this.formId, this.callbackHandler, this.errorHandler, this.returnFormat, this.httpMethod, this.asyncMode);
};

this.setCallbackHandler = function(callbackF){this.asyncMode=true;this.callbackHandler=callbackF;};
this.setErrorHandler = function(callbackF){this.errorHandler=callbackF;};
this.setAsyncMode = function(){this.asyncMode=true;};
this.setSyncMode = function(){this.asyncMode=false;};
this.setForm = function(formId){this.formId=formId;};
this.setHTTPMethod = function(method){this.httpMethod=method.toUpperCase();};

this.setQueryFormat = function(format){
	format = format.toLowerCase();
	if (format != "row" && format != "column"){
		format = "row";
	}
	this.queryFormat = format;
};

this.setReturnFormat = function(format){
	format = format.toLowerCase();
	if (format != "json" && format != "plain" && format != "wddx"){
		format = "json";
	}
	this.returnFormat = format;
};