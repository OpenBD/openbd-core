/////////////////////////////////////////////////////////////////////
//
// CFX_TestTag - CFML Custom Tag to test the API
//
// Copyright 2002. All Rights Reserved.
//

#ifdef __unix__
#include <stddef.h>
using namespace std;	// needed for sprintf
#endif

#include "cfx.h"		// CFX Custom Tag API

extern "C"
#ifdef _WINDOWS
__declspec(dllexport)
#endif
void ProcessTagRequest( CCFXRequest* pRequest ) 
{
	try
	{
		// Write output back to the user here...
		pRequest->Write( "<h3>Hello again from CFX_TestTag!</h3>" );
		
		CCFXStringSet* attributes = pRequest->GetAttributeList();
		
		attributes->AddString( "DoesNotExist" );
		
		pRequest->Write( "<p><table border=1 cellspacing=4 cellpadding=6><th>Attribute</th><th>Exists</th><th>Value</th>" );
		
		for ( int i = 1; i <= attributes->GetCount(); i++ )
		{
			LPCSTR attrName = attributes->GetString( i );
			
			pRequest->Write( "<tr><td>" );
			pRequest->Write( attrName );
			pRequest->Write( "</td><td>" );
			pRequest->Write( pRequest->AttributeExists( attrName ) ? "Yes" : "No" );
			pRequest->Write( "</td><td>" );
			pRequest->Write( pRequest->GetAttribute( attrName ) );
			pRequest->Write( "</td></tr>" );
		}
		
		pRequest->Write( "</table>" );
		
		pRequest->SetVariable( "address", "140 Hollymount Road" );
		pRequest->SetVariable( "city", "Alpharetta" );
		
		CCFXQuery* pQuery = pRequest->GetQuery();
		
		if ( pQuery == NULL )
		{
			pRequest->Write( "<p>No QUERY parameter found for tag." );
		}
		else
		{
			pRequest->Write( "<p>QUERY name: " );
			pRequest->Write( pQuery->GetName() );
			pRequest->Write( "<p><table border=1 cellspacing=4 cellpadding=6>" );
			
			CCFXStringSet* columnNames = pQuery->GetColumns();
			
			for ( int i = 1; i <= columnNames->GetCount(); i++ )
			{
				pRequest->Write( "<th>" );
				pRequest->Write( columnNames->GetString( i ) );
				pRequest->Write( "</th>" );
			}
			
			for ( int j = 1; j <= pQuery->GetRowCount(); j++ )
			{
				pRequest->Write( "<tr>" );
				
				for ( int k = 1; k <= columnNames->GetCount(); k++ )
				{
					pRequest->Write( "<td>" );
					pRequest->Write( pQuery->GetData( j, k ) );
					pRequest->Write( "</td>" );
				}
				
				pRequest->Write( "</tr>" );
			}
				
			pRequest->Write( "</table>" );
			
			int x = pQuery->AddRow();
			pQuery->SetData( x, 6, "Atlanta" );
			pQuery->SetData( x, 9, "Spain" );
		}

		// Output optional debug info
		if ( pRequest->Debug() )
		{
			pRequest->WriteDebug( "<p>Debug info..." ) ;
		}
		
		//pRequest->ThrowException( "Test Error Message", "Test Diagnostic Message" ) ;
	}

	// Catch Cold Fusion exceptions & re-raise them
	catch( CCFXException* e )
	{
		pRequest->ReThrowException( e ) ;
	}
	
	// Catch ALL other exceptions and throw them as 
	// Cold Fusion exceptions (DO NOT REMOVE! -- 
	// this prevents the server from crashing in 
	// case of an unexpected exception)
	catch( ... )
	{
		pRequest->ThrowException( 
			"Error occurred in tag CFX_TestTag",
			"Unexpected error occurred while processing tag." ) ;
	}
}
