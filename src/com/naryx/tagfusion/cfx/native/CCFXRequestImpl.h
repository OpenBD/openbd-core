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
#ifndef __CCFXREQUESTIMPL_H__
#define __CCFXREQUESTIMPL_H__

#ifdef __unix__
#define OutputDebugString(A)	fprintf(stderr,A)
#else
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#endif
#include <jni.h>
#include "cfx.h"

#include <vector>
using std::vector;

			
class CCFXRequestImpl : public CCFXRequest
{
// Construction/Destruction
public:
			CCFXRequestImpl( JNIEnv *env, jobject request, jobject response );
			~CCFXRequestImpl( void );

// Operations
public:
	// Retreiving data from the template
	
	// @cmember Checks to see whether the attribute was passed
	//          to the tag.
	BOOL AttributeExists( LPCSTR lpszName );
	
	// @cmember Retrieves the value of the passed attribute.
	LPCSTR GetAttribute( LPCSTR lpszName );
		
	// @cmember Retrieves a list of all attribute names passed to the tag.
	CCFXStringSet* GetAttributeList();
	
	// @cmember Retrieves the query which was passed to the tag.
	CCFXQuery* GetQuery();	

	// @cmember Retrieves the value of a custom tag setting.
	LPCSTR GetSetting( LPCSTR lpszSettingName );

	// Returning data to the template
	
	// @cmember Writes text output back to the user.
	void Write( LPCSTR lpszOutput );
	
	// @cmember Sets a variable in the template which contains this tag.
	void SetVariable(  LPCSTR lpszName, LPCSTR lpszValue );
	
	// @cmember Adds a query to the template which contains this tag.
	CCFXQuery* AddQuery( LPCSTR lpszName, CCFXStringSet* pColumns );

	// Debugging support
	
	// @cmember Checks whether the tag contains the DEBUG attribute.
	BOOL Debug();

	// @cmember Writes text output into the debug stream.
	void WriteDebug( LPCSTR lpszOutput );

	// Allocating a string set instance (should NEVER be done
	// directly (e.g. w/ 'new') by extensions
	
	// @cmember Allocates and returns a new CCFXStringSet instance.
	CCFXStringSet* CreateStringSet();

	// Throwing exceptions in the case of errors (should NEVER
	// throw an exception directly from within a custom tag
	
	// @cmember Throws an exception and ends processing of this request.
	void ThrowException( LPCSTR lpszError, LPCSTR lpszDiagnostics );
	
	// @cmember Re-throws an exception which has been caught.
	void ReThrowException( CCFXException* e );

	
	// Custom data

	// @cmember Sets custom (tag specific) data to carry along with the request.
	void SetCustomData( LPVOID lpvData );

	// @cmember Gets the custom (tag specific) data for the request
	LPVOID GetCustomData();

private:

	CCFXQuery* CreateQuery( jobject query );

	JNIEnv* _env;
	jobject _request;
	jobject _response;
	
	vector< CCFXStringSet* > _stringSets;
	vector< CCFXQuery* >	 _queries;
	
	LPVOID _customData;
};

#endif // __CCFXREQUESTIMPL_H__



	



