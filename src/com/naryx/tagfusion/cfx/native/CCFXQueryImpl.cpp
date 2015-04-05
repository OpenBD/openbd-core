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
#include "CCFXQueryImpl.h"
#include "CCFXStringSetImpl.h"
#include "CCFXExceptionImpl.h"

CCFXQueryImpl::CCFXQueryImpl( JNIEnv *env, CCFXRequest* request, jobject query )
{
	_env     = env;
	_request = request;
	_query   = query;
}

CCFXQueryImpl::~CCFXQueryImpl()
{
}
  
// Retreiving data from the query

// @cmember Retrieves the name of the query.
LPCSTR
CCFXQueryImpl::GetName()
{
	// String _query.getName()
	jmethodID getName = _env->GetMethodID( _env->GetObjectClass( _query ), "getName",
													"()Ljava/lang/String;" );
	if ( getName == NULL )
		throw new CCFXExceptionImpl( "Failed to get _query.getName() method", "CCFXQueryImpl::GetName" );
	
	jstring name = (jstring)_env->CallObjectMethod( _query, getName );
	if ( name == NULL )
		throw new CCFXExceptionImpl( "Failed to retrieve query name", "CCFXQueryImpl::GetName" );
	
	const char *nameChars = _env->GetStringUTFChars( name, NULL );
		
	return nameChars;
}

// @cmember Retrieves the number of rows in the query.
int
CCFXQueryImpl::GetRowCount()
{
	// int _query.getRowCount()
	jmethodID getRowCount = _env->GetMethodID( _env->GetObjectClass( _query ), "getRowCount", "()I" );
	if ( getRowCount == NULL )
		throw new CCFXExceptionImpl( "Failed to get _query.getRowCount() method", "CCFXQueryImpl::GetRowCount" );
	
	return _env->CallIntMethod( _query, getRowCount );
}

// @cmember Retrieves a list of the query's column names.
CCFXStringSet*
CCFXQueryImpl::GetColumns()
{
	CCFXStringSet* stringSet = _request->CreateStringSet();
	
	// String[] _query.getColumns()
	jmethodID getColumns = _env->GetMethodID( _env->GetObjectClass( _query ), "getColumns",
													"()[Ljava/lang/String;" );
	if ( getColumns == NULL )
		throw new CCFXExceptionImpl( "Failed to get _query.getColumns() method", "CCFXQueryImpl::GetColumns" );
	
	jobjectArray columns = (jobjectArray)_env->CallObjectMethod( _query, getColumns );
	if ( columns == NULL )
		return stringSet;
	
	jsize arrayLen = _env->GetArrayLength( columns );
	
	for ( int i = 0; i < arrayLen; i ++ )
	{
		jstring column = (jstring)_env->GetObjectArrayElement( columns, i );
		const char *columnChars = _env->GetStringUTFChars( column, NULL );
		
		stringSet->AddString( columnChars );
		
		_env->ReleaseStringUTFChars( column, columnChars );
	}
	
	return stringSet;
}

// @cmember Retrieve a data element from a row and column of the query.
LPCSTR
CCFXQueryImpl::GetData( int iRow, int iColumn )
{
	// String _query.getData( int iRow, int iCol )
	jmethodID getData = _env->GetMethodID( _env->GetObjectClass( _query ), "getData",
													"(II)Ljava/lang/String;" );
	if ( getData == NULL )
		throw new CCFXExceptionImpl( "Failed to get _query.getData() method", "CCFXQueryImpl::GetData" );
	
	jstring data = (jstring)_env->CallObjectMethod( _query, getData, iRow, iColumn );
	if ( data == NULL )
		throw new CCFXExceptionImpl( "Failed to retrieve query data", "CCFXQueryImpl::GetData" );
	
	const char *dataChars = _env->GetStringUTFChars( data, NULL );
		
	return dataChars;
}

// Adding data to the query

// @cmember Add a new row to the query.
int
CCFXQueryImpl::AddRow()
{
	// int _query.addRow()
	jmethodID addRow = _env->GetMethodID( _env->GetObjectClass( _query ), "addRow", "()I" );
	if ( addRow == NULL )
		throw new CCFXExceptionImpl( "Failed to get _query.addRow() method", "CCFXQueryImpl::AddRow" );
	
	return _env->CallIntMethod( _query, addRow );
}	

// @cmember Set a data element within a row and column of the query.
void
CCFXQueryImpl::SetData( int iRow, int iColumn, LPCSTR lpszData )
{
	// void _query.setData( int iRow, int iCol, String data )
	jmethodID setData = _env->GetMethodID( _env->GetObjectClass( _query ), "setData",
													"(IILjava/lang/String;)V" );
	if ( setData == NULL )
		throw new CCFXExceptionImpl( "Failed to get _query.setData() method", "CCFXQueryImpl::SetData" );
	
	jstring data = _env->NewStringUTF( lpszData );
	
	_env->CallVoidMethod( _query, setData, iRow, iColumn, data );
	_env->DeleteLocalRef( data );
}

// Setting query debug attributes

// @cmember Set the query string which will displayed along with
//          query debug output.
// This call is  deprecated. No-op.
void
CCFXQueryImpl::SetQueryString( LPCSTR lpszQuery )
{
}

// @cmember Set the total time which was required to process the
//          query (used for debug outupt). 
// This call is  deprecated. No-op.
void
CCFXQueryImpl::SetTotalTime( DWORD dwMilliseconds )
{
}
