	<cfscript>
// testing access type
public function accessTypePublic()
{
	return "public";
}

private function accessTypePrivate()
{
	return "private";
}

// testing return type
public string function returnTypeString( arg1 )
{
	return arg1;
}

public boolean function returnTypeBoolean( arg1 )
{
	return arg1;
}

public array function returnTypeArray( arg1 )
{
	return arg1;
}

// testing parameter types
public function paramString( string foo){
	return structkeyexists( arguments, 'foo' );
}

// testing required
public function paramRequired( required string foo){
	return structkeyexists( arguments, 'foo' );
}

// testing default value
public function paramDefault( string foo = "bar"){
	return arguments.foo;
}

// testing multiple args, including dynamically evaluated default
public function paramMulti( required string foo1, any foo2, foo3, foo4='foo4defaulted', foo5="test#str#" ){
	return "#arguments.foo1# - #arguments.foo2# - #arguments.foo3# - #arguments.foo4# - #arguments.foo5#";
}

// testing other attributes
public function functionAttributes( string foo)
	output=true foo="bar" description="my description" roles="my roles" hint="my hint" displayname="my displayname"
{
	return foo;
}

// testing output attribute
public function functionOutputAttributeTrue()
	output=true
{
	writeoutput( "some output" );
	return true;
}

public function functionOutputAttributeFalse()
	output=false
{
	writeoutput( "some output" );
	return true;
}

function functionBareMinimum(){
  return "passed";
}
</cfscript>

