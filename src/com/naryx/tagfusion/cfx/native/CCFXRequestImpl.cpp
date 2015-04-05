/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
 *
 *  This file is part of Open BlueDragon (OpenBD) CFML Server Engine.
 *  
 *  OpenBD is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  Free Software Foundation,version 3.
 *  
 *  OpenBD is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with OpenBD.  If not, see http://www.gnu.org/licenses/
 *  
 *  Additional permission under GNU GPL version 3 section 7
 *  
 *  If you modify this Program, or any covered work, by linking or combining 
 *  it with any of the JARS listed in the README.txt (or a modified version of 
 *  (that library), containing parts covered by the terms of that JAR, the 
 *  licensors of this Program grant you additional permission to convey the 
 *  resulting work. 
 *  README.txt @ http://www.openbluedragon.org/license/README.txt
 *  
 *  http://www.openbluedragon.org/
 */
#include "CCFXRequestImpl.h"
#include "CCFXExceptionImpl.h"
#include "CCFXStringSetImpl.h"
#include "CCFXQueryImpl.h"

	
CCFXRequestImpl::CCFXRequestImpl( JNIEnv *env, jobject request, jobject response )
{
	_env        = env;
	_request    = request;
	_response   = response;
	_customData = NULL;
}


CCFXRequestImpl::~CCFXRequestImpl( void )
{
	while ( _stringSets.size() > 0 )
	{
		CCFXStringSet* set = _stringSets.back();
		delete set;
		_stringSets.pop_back();
	}
	
	while ( _queries.size() > 0 )
	{
		CCFXQuery* query = _queries.back();
		delete query;
		_queries.pop_back();
	}
}

	
// @cmember Checks to see whether the attribute was passed
//          to the tag.
BOOL
CCFXRequestImpl::AttributeExists( LPCSTR lpszName )
{
	if ( lpszName == NULL )
		return false;
	
	// boolean _request.attributeExists( String name )
	jmethodID attributeExists = _env->GetMethodID( _env->GetObjectClass( _request ), "attributeExists",
													"(Ljava/lang/String;)Z" );
	if ( attributeExists == NULL )
		throw new CCFXExceptionImpl( "Failed to get _request.attributeExists() method", "CCFXRequestImpl::AttributeExists" );
	
	return _env->CallBooleanMethod( _request, attributeExists, _env->NewStringUTF( lpszName ) );
}


// @cmember Retrieves the value of the passed attribute.
LPCSTR
CCFXRequestImpl::GetAttribute( LPCSTR lpszName )
{
	if ( ( lpszName == NULL ) || !AttributeExists( lpszName ) )
		return NULL;
	
	// String _request.getAttribute( String name )
	jmethodID getAttribute = _env->GetMethodID( _env->GetObjectClass( _request ), "getAttribute",
													"(Ljava/lang/String;)Ljava/lang/String;" );
	if ( getAttribute == NULL )
		throw new CCFXExceptionImpl( "Failed to get _request.getAttribute() method", "CCFXRequestImpl::GetAttribute" );
	
	jstring attrValue = (jstring)_env->CallObjectMethod( _request, getAttribute, _env->NewStringUTF( lpszName ) );
	
	const char *attrChars = _env->GetStringUTFChars( attrValue, NULL );
		
	return attrChars;
}

	
// @cmember Retrieves a list of all attribute names passed to the tag.
CCFXStringSet*
CCFXRequestImpl::GetAttributeList()
{
	CCFXStringSet* stringSet = CreateStringSet();
	
	// String[] _request.getAttributeList()
	jmethodID getAttributeList = _env->GetMethodID( _env->GetObjectClass( _request ), "getAttributeList",
													"()[Ljava/lang/String;" );
	if ( getAttributeList == NULL )
		throw new CCFXExceptionImpl( "Failed to get _request.getAttributeList() method", "CCFXRequestImpl::GetAttributeList" );
	
	jobjectArray attributes = (jobjectArray)_env->CallObjectMethod( _request, getAttributeList );
	if ( attributes == NULL )
		return stringSet;
		
	return ((CCFXStringSetImpl*)stringSet)->InitFromJavaObjectArray( _env, attributes );
}


// @cmember Retrieves the query which was passed to the tag.
CCFXQuery*
CCFXRequestImpl::GetQuery()
{
	// Query _request.getQuery()
	jmethodID getQuery = _env->GetMethodID( _env->GetObjectClass( _request ), "getQuery",
												"()Lcom/allaire/cfx/Query;" );
	if ( getQuery == NULL )
		throw new CCFXExceptionImpl( "Failed to get _request.getQuery() method", "CCFXRequestImpl::GetQuery" );
	
	jobject query = _env->CallObjectMethod( _request, getQuery );
	if ( query == NULL )
		return NULL;
		
	return CreateQuery( query );
}	


// @cmember Retrieves the value of a custom tag setting.
LPCSTR
CCFXRequestImpl::GetSetting( LPCSTR lpszSettingName )
{
	return GetAttribute( lpszSettingName );
}


// @cmember Writes text output back to the user.
void
CCFXRequestImpl::Write( LPCSTR lpszOutput )
{
	if ( lpszOutput == NULL )
		return;
    
	// void _response.write( String output )
	jmethodID write = _env->GetMethodID( _env->GetObjectClass( _response ), "write",
											"(Ljava/lang/String;)V" );
	if ( write == NULL )
		throw new CCFXExceptionImpl( "Failed to get _response.write() method", "CCFXRequestImpl::Write" );
	
	jstring output = _env->NewStringUTF( lpszOutput );
	
	_env->CallVoidMethod( _response, write, output );
	_env->DeleteLocalRef( output );
}


// @cmember Sets a variable in the template which contains this tag.
void
CCFXRequestImpl::SetVariable(  LPCSTR lpszName, LPCSTR lpszValue )
{
	if ( lpszName == NULL )
		return;
	
	// void _response.setVariable( String name, String value )
	jmethodID setVariable = _env->GetMethodID( _env->GetObjectClass( _response ), "setVariable",
												"(Ljava/lang/String;Ljava/lang/String;)V" );
	if ( setVariable == NULL )
		throw new CCFXExceptionImpl( "Failed to get _response.setVariable() method", "CCFXRequestImpl::SetVariable" );
	
	jstring name  = _env->NewStringUTF( lpszName );
	jstring value = _env->NewStringUTF( lpszValue );
	
	_env->CallVoidMethod( _response, setVariable, name, value );
	_env->DeleteLocalRef( name );
	_env->DeleteLocalRef( value );
}


// @cmember Adds a query to the template which contains this tag.
CCFXQuery*
CCFXRequestImpl::AddQuery( LPCSTR lpszName, CCFXStringSet* pColumns )
{
	if ( ( lpszName == NULL ) || ( pColumns == NULL ) ) 
		return NULL;
	
	// Query _response.addQuery( String name, String[] columns )
	jmethodID addQuery = _env->GetMethodID( _env->GetObjectClass( _response ), "addQuery",
									"(Ljava/lang/String;[Ljava/lang/String;)Lcom/allaire/cfx/Query;" );
	if ( addQuery == NULL )
		throw new CCFXExceptionImpl( "Failed to get _response.addQuery() method", "CCFXRequestImpl::AddQuery" );
	
	jobjectArray columns = ((CCFXStringSetImpl*)pColumns)->ToJavaObjectArray( _env );
	jstring name = _env->NewStringUTF( lpszName );
	
	return CreateQuery( _env->CallObjectMethod( _response, addQuery, name, columns ) );
}


// @cmember Checks whether the tag contains the DEBUG attribute.
BOOL
CCFXRequestImpl::Debug()
{
	// boolean _request.debug()
	jmethodID debug = _env->GetMethodID( _env->GetObjectClass( _request ), "debug", "()Z" );
	if ( debug == NULL )
		throw new CCFXExceptionImpl( "Failed to get _request.debug() method", "CCFXRequestImpl::Debug" );
	
	return _env->CallBooleanMethod( _request, debug );
}

// @cmember Writes text output into the debug stream.
void
CCFXRequestImpl::WriteDebug( LPCSTR lpszOutput )
{
	if ( lpszOutput == NULL )
		return;
	
	// void _response.writeDebug( String output )
	jmethodID writeDebug = _env->GetMethodID( _env->GetObjectClass( _response ), "writeDebug",
													"(Ljava/lang/String;)V" );
	if ( writeDebug == NULL )
		throw new CCFXExceptionImpl( "Failed to get _response.writeDebug() method", "CCFXRequestImpl::WriteDebug" );

	jstring output = _env->NewStringUTF( lpszOutput );
	_env->CallVoidMethod( _response, writeDebug, output );
	_env->DeleteLocalRef( output );
}


// @cmember Allocates and returns a new CCFXStringSet instance.
CCFXStringSet*
CCFXRequestImpl::CreateStringSet()
{
	CCFXStringSet* stringSet = new CCFXStringSetImpl();
	_stringSets.push_back( stringSet );
	
	return stringSet;
}


// @cmember Allocates and returns a new CCFXQuery instance
CCFXQuery*
CCFXRequestImpl::CreateQuery( jobject query )
{
	CCFXQuery* ccfxQuery = new CCFXQueryImpl( _env, this, query );
	_queries.push_back( ccfxQuery );
	
	return ccfxQuery;
}


// @cmember Throws an exception and ends processing of this request.
void
CCFXRequestImpl::ThrowException( LPCSTR lpszError, LPCSTR lpszDiagnostics )
{	
	throw new CCFXExceptionImpl( lpszError, lpszDiagnostics );
}


// @cmember Re-throws an exception which has been caught.
void
CCFXRequestImpl::ReThrowException( CCFXException* e )
{
	throw e;
}


// @cmember Sets custom (tag specific) data to carry along with the request.
void
CCFXRequestImpl::SetCustomData( LPVOID lpvData )
{
	_customData = lpvData;
}


// @cmember Gets the custom (tag specific) data for the request
LPVOID
CCFXRequestImpl::GetCustomData()
{
	return _customData;
}
