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
#ifndef __CCFXSTRINGSETIMPL_H__
#define __CCFXSTRINGSETIMPL_H__

#ifdef __unix__
#define OutputDebugString(A)	fprintf(stderr,A)
#else
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#endif

#include <jni.h>
#include "cfx.h"

#include <string>
#include <vector>

using std::string;
using std::vector;

typedef vector< string > stringVector;

/////////////////////////////////////////////////////////////////////
// CCFXStringSet - Class which represents a set of ordered
//                 strings (used extensively in the ColdFusion 
//                 extension interface)

// @class Abstract class which represents a set of 
//        ordered strings. Strings can be added to
//        set and can be retrieved by numeric index (the 
//        index values for strings are 1-based).
//	      To create a string set you should use the 
//		  CCFXRequest member function <mf CCFXRequest::CreateStringSet>.
//
class CCFXStringSetImpl : public CCFXStringSet
{
// Construction/Destruction
public:
			CCFXStringSetImpl();
			~CCFXStringSetImpl();

// Operations
public:
	// Adding strings to the collection
	
	// @cmember Add a string to the end of the list.
	int AddString( LPCSTR lpszString );

	// Retreiving strings from the collection

	// @cmember Get the number of strings contained in the list.
	int GetCount();

	// @cmember Get the string located at the passed index.
	LPCSTR GetString( int iIndex );

	// @cmember Get the index for the passed string.
	int GetIndexForString( LPCSTR lpszString );
	
	// @cmember Initialize from Java object array, returns this pointer
	CCFXStringSetImpl* InitFromJavaObjectArray( JNIEnv* env, jobjectArray array );
	
	// @cmember Convert the string set to a Java object array
	jobjectArray ToJavaObjectArray( JNIEnv* env );

private:

	stringVector stringSet;
};

#endif // __CCFXSTRINGSETIMPL_H__
