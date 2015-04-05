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
#ifndef __CCFXQUERYIMPL_H__
#define __CCFXQUERYIMPL_H__

#ifdef __unix__
#define OutputDebugString(A)	fprintf(stderr,A)
#else
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#endif
#include <jni.h>
#include "cfx.h"

/////////////////////////////////////////////////////////////////////
// CCFXQuery -  Abstract class which represents a query used
//              or created by a ColdFusion extension (CFX)

// @class   Abstract class which represents a query used
//          or created by a ColdFusion Extension (CFX). Queries
//          contain 1 or more columns of data which extend over
//          a varying number of rows.
class CCFXQueryImpl: public CCFXQuery
{
// Construction/Destruction
public:
			CCFXQueryImpl( JNIEnv *env, CCFXRequest* request, jobject query );
			~CCFXQueryImpl();

// Operations
public:  
	// Retreiving data from the query
	
	// @cmember Retrieves the name of the query.
	LPCSTR GetName();

	// @cmember Retrieves the number of rows in the query.
	int GetRowCount();

	// @cmember Retrieves a list of the query's column names.
	CCFXStringSet* GetColumns();

	// @cmember Retrieve a data element from a row and column of the query.
	LPCSTR GetData( int iRow, int iColumn );

	// Adding data to the query

	// @cmember Add a new row to the query.
	int AddRow();	
	
	// @cmember Set a data element within a row and column of the query.
	void SetData( int iRow, int iColumn, LPCSTR lpszData );
	
	// Setting query debug attributes

	// @cmember Set the query string which will displayed along with
	//          query debug output.
	// This call is  deprecated. No-op.
	void SetQueryString( LPCSTR lpszQuery );
	
	// @cmember Set the total time which was required to process the
	//          query (used for debug outupt). 
	// This call is  deprecated. No-op.
	void SetTotalTime( DWORD dwMilliseconds );

private:

	JNIEnv *     _env;
	CCFXRequest* _request;
	jobject 	 _query;
} ;

#endif // __CCFXQUERYIMPL_H__
