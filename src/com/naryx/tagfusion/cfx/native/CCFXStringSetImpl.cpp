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
#include "CCFXStringSetImpl.h"

CCFXStringSetImpl::CCFXStringSetImpl()
{	
}

CCFXStringSetImpl::~CCFXStringSetImpl()
{
}

	
// @cmember Add a string to the end of the list.
int
CCFXStringSetImpl::AddString( LPCSTR lpszString )
{
	if ( lpszString != NULL )
	{
		string s( lpszString );
		stringSet.push_back( s );
		return (int)stringSet.size();
	}
	
	return CFX_STRING_NOT_FOUND;
}


// @cmember Get the number of strings contained in the list.
int
CCFXStringSetImpl::GetCount()
{
	return (int)stringSet.size();
}


// @cmember Get the string located at the passed index.
//	index is 1-based
LPCSTR
CCFXStringSetImpl::GetString( int index )
{
	if ( ( index > 0 ) && ( index <= (int)stringSet.size() ) )
		return stringSet[ index - 1 ].c_str();
	
	return NULL;
}


// @cmember Get the index for the passed string.
//	returned index is 1-based
int
CCFXStringSetImpl::GetIndexForString( LPCSTR lpszString )
{
	if ( lpszString != NULL )
	{
		for ( long i = 0; i < (int)stringSet.size(); i++ )
		{
			if ( strcmp( stringSet[ i ].c_str(), lpszString ) == 0 )
				return i + 1;
		}
	}
	
	return CFX_STRING_NOT_FOUND;
}


// @cmember Initialize from Java object array, returns this pointer
CCFXStringSetImpl*
CCFXStringSetImpl::InitFromJavaObjectArray( JNIEnv* env, jobjectArray array )
{
	jsize arrayLen = env->GetArrayLength( array );
	
	for ( int i = 0; i < arrayLen; i ++ )
	{
		jstring js = (jstring)env->GetObjectArrayElement( array, i );
		const char *chars = env->GetStringUTFChars( js, NULL );
		string s( chars );
	
		stringSet.push_back( s );
		env->ReleaseStringUTFChars( js, chars );
	}
	
	return this;
}


// @cmember Convert the string set to a Java object array
jobjectArray
CCFXStringSetImpl::ToJavaObjectArray( JNIEnv* env )
{
	int num = (int)stringSet.size();
	
	jobjectArray array = env->NewObjectArray( num, env->FindClass( "java/lang/String" ), NULL );
	
	for ( int i = 0; i < num; i++ )
	{
		jstring s = env->NewStringUTF( stringSet[ i ].c_str() );
		
		env->SetObjectArrayElement( array, i, s );
		env->DeleteLocalRef( s );
	}
	
	return array;
}
