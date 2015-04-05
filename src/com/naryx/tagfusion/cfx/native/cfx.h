/////////////////////////////////////////////////////////////////////
// Standard headers for ColdFusion extensions (CFX)
//
// Copyright 1996 Allaire Corp. 
// All Rights Reserved.
//
// @doc 
// (use AutoDuck to generate documentation)
//
// @contents1 ColdFusion Extension API | 

// @index class |

//  BlueDragon note:
//		This file is now a combined version for both Windows and Unix systems.  The
//		only change necessary was the addition of the "#ifdef __unix__" below.


#ifndef __CFX_H__
#define __CFX_H__

#include <sys/types.h>

#ifdef __unix__
// Porting help
typedef unsigned short BOOL;
typedef unsigned char BYTE;
typedef unsigned short WORD;
typedef unsigned int DWORD;
typedef char *LPSTR;
typedef const char *LPCSTR;
typedef unsigned int UINT;
typedef BYTE  BOOLEAN;
typedef BYTE *LPBYTE;
typedef DWORD *LPDWORD;
typedef void *LPVOID;

#else
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#endif

// Forward declarations
class CCFXException ;
class CCFXStringSet ;
class CCFXQuery ;


/////////////////////////////////////////////////////////////////////
// CCFXRequest 

// @class Abstract class which represents a request made
//        to a ColdFusion Extension (CFX). An instance of
//        this class is passed to the main function of your
//        extension DLL. The class provides several interfaces which may
//        be used by the custom extension including functions
//        for reading and writing variables, returning output,
//        creating and using queries, and throwing exceptions.
//
class CCFXRequest
{
// Construction/Destruction
public:
	virtual ~CCFXRequest() {} ;

// Operations
public:
	// Retreiving data from the template
	
	// @cmember Checks to see whether the attribute was passed
	//          to the tag.
	virtual BOOL AttributeExists( LPCSTR lpszName ) = 0 ;
	
	// @cmember Retrieves the value of the passed attribute.
	virtual LPCSTR GetAttribute( LPCSTR lpszName ) = 0 ;
		
	// @cmember Retrieves a list of all attribute names passed to the tag.
	virtual CCFXStringSet* GetAttributeList() = 0 ;
	
	// @cmember Retrieves the query which was passed to the tag.
	virtual CCFXQuery* GetQuery() = 0 ;	

	// @cmember Retrieves the value of a custom tag setting.
	virtual LPCSTR GetSetting( LPCSTR lpszSettingName ) = 0 ;

	// Returning data to the template
	
	// @cmember Writes text output back to the user.
	virtual void Write( LPCSTR lpszOutput ) = 0 ;
	
	// @cmember Sets a variable in the template which contains this tag.
	virtual void SetVariable(  LPCSTR lpszName, LPCSTR lpszValue ) = 0 ;
	
	// @cmember Adds a query to the template which contains this tag.
	virtual CCFXQuery* AddQuery( LPCSTR lpszName, CCFXStringSet* pColumns ) = 0 ;

	// Debugging support
	
	// @cmember Checks whether the tag contains the DEBUG attribute.
	virtual BOOL Debug() = 0 ;

	// @cmember Writes text output into the debug stream.
	virtual void WriteDebug( LPCSTR lpszOutput ) = 0 ;

	// Allocating a string set instance (should NEVER be done
	// directly (e.g. w/ 'new') by extensions
	
	// @cmember Allocates and returns a new CCFXStringSet instance.
	virtual CCFXStringSet* CreateStringSet() = 0 ;

	// Throwing exceptions in the case of errors (should NEVER
	// throw an exception directly from within a custom tag
	
	// @cmember Throws an exception and ends processing of this request.
	virtual void ThrowException( 
		LPCSTR lpszError, LPCSTR lpszDiagnostics ) = 0 ;
	
	// @cmember Re-throws an exception which has been caught.
	virtual void ReThrowException( CCFXException* e ) = 0 ;

	
	// Custom data

	// @cmember Sets custom (tag specific) data to carry along with the request.
	virtual void SetCustomData( LPVOID lpvData ) = 0 ;

	// @cmember Gets the custom (tag specific) data for the request
	virtual LPVOID GetCustomData() = 0 ;
} ;



/////////////////////////////////////////////////////////////////////
// CCFXQuery -  Abstract class which represents a query used
//              or created by a ColdFusion extension (CFX)

// @class   Abstract class which represents a query used
//          or created by a ColdFusion Extension (CFX). Queries
//          contain 1 or more columns of data which extend over
//          a varying number of rows.
class CCFXQuery
{
// Construction/Destruction
public:
	virtual ~CCFXQuery() {} ;

// Operations
public:  
	// Retreiving data from the query
	
	// @cmember Retrieves the name of the query.
	virtual LPCSTR GetName() = 0 ;

	// @cmember Retrieves the number of rows in the query.
	virtual int GetRowCount() = 0 ;

	// @cmember Retrieves a list of the query's column names.
	virtual CCFXStringSet* GetColumns() = 0 ;

	// @cmember Retrieve a data element from a row and column of the query.
	virtual LPCSTR GetData( int iRow, int iColumn ) = 0 ;

	// Adding data to the query

	// @cmember Add a new row to the query.
	virtual int AddRow() = 0 ;	
	
	// @cmember Set a data element within a row and column of the query.
	virtual void SetData( int iRow, int iColumn, LPCSTR lpszData ) = 0 ;
	
	// Setting query debug attributes

	// @cmember Set the query string which will displayed along with
	//          query debug output.
	// This call is  deprecated. No-op.
	virtual void SetQueryString( LPCSTR lpszQuery ) = 0 ;
	
	// @cmember Set the total time which was required to process the
	//          query (used for debug outupt). 
	// This call is  deprecated. No-op.
	virtual void SetTotalTime( DWORD dwMilliseconds ) = 0 ;
} ;


/////////////////////////////////////////////////////////////////////
// CCFXStringSet - Class which represents a set of ordered
//                 strings (used extensively in the ColdFusion 
//                 extension interface)

#define CFX_STRING_NOT_FOUND	-1

// @class Abstract class which represents a set of 
//        ordered strings. Strings can be added to
//        set and can be retrieved by numeric index (the 
//        index values for strings are 1-based).
//	      To create a string set you should use the 
//		  CCFXRequest member function <mf CCFXRequest::CreateStringSet>.
//
class CCFXStringSet
{
// Construction/Destruction
public:
	virtual ~CCFXStringSet() {} ;

// Operations
public:
	// Adding strings to the collection
	
	// @cmember Add a string to the end of the list.
	virtual int AddString( LPCSTR lpszString ) = 0 ;

	// Retreiving strings from the collection

	// @cmember Get the number of strings contained in the list.
	virtual int GetCount() = 0 ;

	// @cmember Get the string located at the passed index.
	virtual LPCSTR GetString( int iIndex ) = 0 ;

	// @cmember Get the index for the passed string.
	virtual int GetIndexForString( LPCSTR lpszString ) = 0 ;
} ;


/////////////////////////////////////////////////////////////////////
// CCFXException - Class which represents an exception thrown
//                 by the ColdFusion Extension (CFX) library 

// @class Abstract class which represents an exception thrown
//        during the processing of a ColdFusion Extension (CFX)
//		  procedure.<nl>
//        <nl>Exceptions of this type can be thrown by
//		  the classes <c CCFXRequest>, <c CCFXQuery>, and 
//		  <c CCFXStringSet>. Your ColdFusion Extension code must
//        therefore be written to handle exceptions of this type
//		  (see CCFXRequest::ReThrowException for more details on
//        doing this correctly).
class CCFXException
{
// Construction/Destruction
public:
	virtual ~CCFXException() {} ;
	virtual LPCSTR GetError() = 0;
	virtual LPCSTR GetDiagnostics() = 0;
} ;

#endif // __CFX_H__



// CCFXRequest::AttributeExists //////////////////////////////////////////////////
// 
// @mfunc BOOL | CCFXRequest | AttributeExists | 
//   Checks to see whether the attribute was passed to the tag.
//
// @parm LPCSTR | lpszName | Name of the attribute to check (case insensitive).
//
// @rdesc Returns TRUE if the attribute is available otherwise returns FALSE.
//
// @ex The following example checks to see if the user passed an
//     attribute named DESTINATION to the tag and throws an exception
//     if the attribute was not passed: |
//
//	if ( pRequest->AttributeExists("DESTINATION")==FALSE )
//	{
//		pRequest->ThrowException( 
//			"Missing DESTINATION parameter",
//			"You must pass a DESTINATION parameter in "
//			"order for this tag to work correctly." ) ;   
//	}
//
// @xref <c CCFXRequest> <mf CCFXRequest::GetAttribute> 
//       <mf CCFXRequest::GetAttributeList>


// CCFXRequest::GetAttribute //////////////////////////////////////////////////
//
// @mfunc LPCSTR | CCFXRequest | GetAttribute | 
//   Retrieves the value of the passed attribute. Returns
//   an empty string if the attribute does not exist (use
//   <mf CCFXRequest::AttributeExists> to test whether an 
//   attribute was passed to the tag).
//
// @parm LPCSTR | lpszName | Name of the attribute to retrieve (case insensitive).
//
// @rdesc The value of the attribute passed to the tag. If no attribute
//        of that name was passed to the tag then an empty string is returned.
//
// @ex The following example retrieves an attribute named DESTINATION 
//     and writes its value back to the user: |
//
//	LPCSTR lpszDestination = pRequest->GetAttribute("DESTINATION") ;
//	pRequest->Write( "The destination is: " ) ;
//	pRequest->Write( lpszDestination ) ;
//
// @xref <c CCFXRequest> <mf CCFXRequest::AttributeExists> 
//       <mf CCFXRequest::GetAttributeList>


// CCFXRequest::GetAttributeList //////////////////////////////////////////////
//
// @mfunc <c CCFXStringSet>* | CCFXRequest | GetAttributeList | 
//   Retrieves a list of all attribute names passed to the tag. To retrieve
//   the value of an individual attribute you should use the 
//	 <mf CCFXRequest::GetAttribute> member function.
//
// @rdesc An object of class <c CCFXStringSet> which contains a
//        list of all attributes passed to the tag.<nl>You are not 
//        responsible for freeing the memory allocated for the
//        returned string set (it will be automatically freed by
//        ColdFusion after the request is completed). 
//
// @ex The following example retrieves the list of attributes and then
//     iterates over the list, writing each attribute and its value
//     back to the user. |
//  
//	LPCSTR lpszName, lpszValue ;    	
//	CCFXStringSet* pAttribs = pRequest->GetAttributeList() ;
//	int nNumAttribs = pAttribs->GetCount() ;	
//	
//	for( int i=1; i<=nNumAttribs; i++ )
//	{
//		lpszName = pAttribs->GetString( i ) ;
//		lpszValue = pRequest->GetAttribute( lpszName ) ;
//		pRequest->Write( lpszName ) ;
//		pRequest->Write( " = " ) ;
//		pRequest->Write( lpszValue ) ;
//		pRequest->Write( "<BR>" ) ;
//	}
//
// @xref <c CCFXRequest> <c CCFXStringSet> <mf CCFXRequest::AttributeExists> 
//       <mf CCFXRequest::GetAttribute>


// CCFXRequest::GetQuery //////////////////////////////////////////////
//
// @mfunc <c CCFXQuery>* | CCFXRequest | GetQuery | 
//   Retrieves the query which was passed to the tag. To pass a query
//   to a custom tag you use the QUERY attribute. This attribute should
//   be set to the name of an existing query (created using the DBQUERY
//   tag or another custom tag). The QUERY attribute is optional and 
//   should only be used by tags which need to process an existing dataset.
//
// @rdesc An object of class <c CCFXQuery> which represents the
//        query which was passed to the tag. If no query was passed
//        to the tag then NULL is returned. <nl>You are not 
//        responsible for freeing the memory allocated for the
//        returned query (it will be automatically freed by
//        ColdFusion after the request is completed). 
//
// @ex The following example retrieves the query which was passed
//	   to the tag. If no query was passed then an exception is thrown: |
//  
//	CCFXQuery* pQuery = pRequest->GetQuery() ;
//	if ( pQuery == NULL )
//	{
//		pRequest->ThrowException( 
//			"Missing QUERY parameter",
//			"You must pass a QUERY parameter in "
//			"order for this tag to work correctly." ) ;   
//	}
//
// @xref <c CCFXRequest> <c CCFXQuery> <mf CCFXRequest::AddQuery>


// CCFXRequest::GetSetting //////////////////////////////////////////////////
//
// @mfunc LPCSTR | CCFXRequest | GetSetting | 
//   Retrieves the value of a global custom tag setting.
//   Custom tag settings are stored within the CustomTags
//   section of the ColdFusion Registry key.
//
// @parm LPCSTR | lpszSettingName | Name of the setting to retrieve (case insensitive).
//
// @rdesc The value of the custom tag setting. If no setting
//        of that name exists then an empty string is returned.
//
// @ex The following example retrieves the value of a setting named 
//     'VerifyAddress' and uses the returned value to determine
//     what actions to take next: |
//
//	LPCSTR lpszVerify = pRequest->GetSetting("VerifyAddress") ;
//	BOOL bVerify = atoi(lpszVerify) ;
//	if ( bVerify == TRUE )
//	{
//		// Do address verification...
//	}
//
// @xref <c CCFXRequest> 


// CCFXRequest::Write //////////////////////////////////////////////////
//
// @mfunc void | CCFXRequest | Write | 
//   Writes text output back to the user.
//
// @parm LPCSTR | lpszOutput | Text to output.
//
// @ex The following example creates a buffer to hold an output string,
//     fills the buffer with data, and then writes the output 
//     back to the user: |
//
//	CHAR buffOutput[1024] ;
//	wsprintf( buffOutput, "The destination is: %s", 
//			  pRequest->GetAttribute("DESTINATION") ) ;
//	pRequest->Write( buffOutput ) ;
//
//
// @xref <c CCFXRequest> 


// CCFXRequest::SetVariable///////////////////////////////////////////////
//
// @mfunc void | CCFXRequest | SetVariable | 
//   Sets a variable in the calling template. If the 
//   variable name specified already exists in the template then its
//   value is replaced. If it does not already exist then a new variable
//   is created. The values of variables created using SetVariable can
//   be accessed in the same manner as other template variables (e.g. #MessageSent#).
//
// @parm LPCSTR | lpszName | Name of variable.
// @parm LPCSTR | lpszValue | Value of variable.
//
// @ex The following example sets the value of a variable
//     named 'MessageSent' based on the success of an operation
//     performed by the custom tag: |
//	
//	BOOL bMessageSent ;
//
//	...attempt to send the message...
//
//	if ( bMessageSent == TRUE )
//	{
//		pRequest->SetVariable( "MessageSent", "Yes" ) ;
//	}
//	else
//	{
//		pRequest->SetVariable( "MessageSent", "No" ) ;
//	}
//
// @xref <c CCFXRequest>
//


// CCFXRequest::AddQuery ///////////////////////////////////////////////
//
// @mfunc <c CCFXQuery>* | CCFXRequest | AddQuery | 
//   
// Adds a query to the calling template. This query can
// then be accessed by DBML tags (e.g. DBOUTPUT or DBTABLE) within the 
// template. Note that after calling AddQuery
// the query exists but is empty (i.e. it has 0 rows). To populate the
// query with data you should call the CCFXQuery member functions
// <mf CCFXQuery::AddRow> and <mf CCFXQuery::SetData>.
//
// @parm LPCSTR | lpszName | Name of query to add to the template (must be unique).
// @parm <c CCFXStringSet>* | pColumns | List of columns names to be used in the query.
//
// @rdesc Returns a pointer to the query which was added to the template (an 
// object of class <c CCFXQuery>). You are not responsible for freeing the 
// memory allocated for the returned query (it will be automatically freed by
// ColdFusion after the request is completed).  
//
// @ex The following example adds a query named 'People' to the calling template.
//     The query has two columns ('FirstName' and 'LastName') and 2 rows: |
//	
//	// Create a string set and add the column names to it
//	CCFXStringSet* pColumns = pRequest->CreateStringSet() ;
//	int iFirstName = pColumns->AddString( "FirstName" ) ;
//	int iLastName = pColumns->AddString( "LastName" ) ;
//
//	// Create a query which contains these columns
//	CCFXQuery* pQuery = pRequest->AddQuery( "People", pColumns ) ;
//					
//	// Add data to the query
//	int iRow ;
//	iRow = pQuery->AddRow() ;
//	pQuery->SetData( iRow, iFirstName, "John" ) ;
//	pQuery->SetData( iRow, iLastName, "Smith" ) ;
//	iRow = pQuery->AddRow() ;
//	pQuery->SetData( iRow, iFirstName, "Jane" ) ;
//	pQuery->SetData( iRow, iLastName, "Doe" ) ;
//
// @xref <c CCFXRequest> <c CCFXQuery> <mf CCFXRequest::GetQuery>
// <mf CCFXQuery::AddRow> <mf CCFXQuery::SetData>
//


// CCFXRequest::SetCustomData //////////////////////////////////////////////
//
// @mfunc void | CCFXRequest | SetCustomData | 
//   Sets custom (tag specific) data to carry along with the request. You
//   should use this function to store request specific data which you wish
//   to pass along to procedures within your custom tag implementation.
//
// @parm LPVOID | lpvData | Pointer to custom data
//
// @ex The following example creates a request-specific 
//     data structure of hypothetical type MYTAGDATA and 
//	   stores a pointer to the structure in the request for future use: |
//
//	void ProcessTagRequest( CCFXRequest* pRequest ) 
//	{
//		try
//		{
//			MYTAGDATA tagData ;
//			pRequest->SetCustomData( (LPVOID)&tagData ) ;
//			
//		... remainder of procedure ...
//	}
//
// @xref <c CCFXRequest> <mf CCFXRequest::GetCustomData>



// CCFXRequest::GetCustomData //////////////////////////////////////////////////
// 
// @mfunc LPVOID | CCFXRequest | GetCustomData | 
//   Gets the custom (tag specific) data for the request. This member is 
//   typically used from within subroutines of your tag implementation to
//   extract tag specific data from within the request.
//
// @rdesc Returns a pointer to the custom data or returns NULL if no
//        custom data has been set during this request using 
//        <mf CCFXRequest::SetCustomData>.
//
// @ex The following example retrieves a pointer to a request specific 
//     data structure of hypothetical type MYTAGDATA: |
//
//	void DoSomeGruntWork( CCFXRequest* pRequest ) 
//	{
//		MYTAGDATA* pTagData = 
//			(MYTAGDATA*)pRequest->GetCustomData() ;
//			
//		... remainder of procedure ...
//	}
//
// @xref <c CCFXRequest> <mf CCFXRequest::SetCustomData> 



// CCFXRequest::Debug //////////////////////////////////////////////////
// 
// @mfunc BOOL | CCFXRequest | Debug | 
//   Checks whether the tag contains the DEBUG attribute. You should use
//   this function to determine whether or not you need to write debug
//   information for this request (see <mf CCFXRequest::WriteDebug> for
//   details on writing debug information).
//
// @rdesc Returns TRUE if the tag contains the DEBUG attribute otherwise
//        returns FALSE.
//
// @ex The following example checks to see whether the DEBUG attribute
//     is present, and if it is then it writes a brief debug message: |
//
//	if ( pRequest->Debug() )
//	{
//		pRequest->WriteDebug( "Top secret debug info" ) ;
//	}
//
// @xref <c CCFXRequest> <mf CCFXRequest::WriteDebug> 


// CCFXRequest::WriteDebug //////////////////////////////////////////////
//
// @mfunc void | CCFXRequest | WriteDebug | 
//   Writes text output into the debug stream. This text is only displayed
//   to the end-user if the tag contains the DEBUG attribute (see the
//   <mf CCFXRequest::Debug> member function).
//
// @parm LPCSTR | lpszOutput | Text to output.
//
// @ex The following example checks to see whether the DEBUG attribute
//     is present, and if it is then it writes a brief debug message: |
//
//	if ( pRequest->Debug() )
//	{
//		pRequest->WriteDebug( "Top secret debug info" ) ;
//	}
//
// @xref <c CCFXRequest> <mf CFXRequest::Debug>


// CCFXRequest::CreateStringSet //////////////////////////////////////////
//
// @mfunc <c CCFXStringSet>* | CCFXRequest | CreateStringSet | 
//   Allocates and returns a new CCFXStringSet instance. Note that
//   string sets should always be created using this function as 
//   opposed to directly using the 'new' operator.
//
// @rdesc An object of class <c CCFXStringSet>. You are not 
//        responsible for freeing the memory allocated for the
//        returned string set (it will be automatically freed by
//        ColdFusion after the request is completed). 
//
// @ex The following example creates a string set and adds 3 strings
//     to it: |
//  
//	CCFXStringSet* pColors = pRequest->CreateStringSet() ;
//	pColors->AddString( "Red" ) ;
//	pColors->AddString( "Green" ) ;
//	pColors->AddString( "Blue" ) ;
//
// @xref <c CCFXRequest> <c CCFXStringSet>


// CCFXRequest::ThrowException ///////////////////////////////////////////
//
// @mfunc void | CCFXRequest | ThrowException | 
//
//  Throws an exception and ends processing of this request. You should
//  call this function when you encounter an error which does not allow
//  you to continue processing the request. Note that this function
//  is almost always combined with the <mf CCFXRequest::ReThrowException>
//  member function to provide protection against resource leaks 
//  in extension code.
//
// @parm LPCSTR | lpszError | Short identifier for error.
// @parm LPCSTR | lpszDiagnostics | Error diagnostic information.
//
// @ex The following example throws an exception indicating that
//     an unexpected error occurred while processing the request: |
//	
//	char buffError[512] ;
//	wsprintf( buffError, 
//		"Unexpected Windows NT error number %ld "
//		"occurred while processing request.", GetLastError() ) ;
//	
//	pRequest->ThrowException( "Error occurred", buffError ) ;
//
// @xref <c CCFXRequest> <c CCFXException> <mf CCFXRequest::ReThrowException>
//


// CCFXRequest::ReThrowException ///////////////////////////////////////////
//
// @mfunc void | CCFXRequest | ReThrowException | 
//
//  Re-throws an exception which has been caught within an extension
//  procedure. This function is used to avoid having C++ exceptions
//  thrown by DLL extension code propagate back into ColdFusion. You
//  should catch ALL C++ exceptions which occur in your extension code
//  and then either re-throw them (if they are of the CCFXException class)
//  or create and throw a new exception using <mf CCFXRequest::ThrowException>.
//
// @parm <c CCFXException>* | e | An existing CCFXException which has been caught.
//
// @ex The following code demonstrates the correct way to handle 
//     exceptions in ColdFusion Extension DLL procedures: |
//	
//	try
//	{
//
//		...Code which could throw an exception...
//
//	}
//	catch( CCFXException* e )
//	{
//		...Do appropriate resource cleanup here...
//		
//		// Re-throw the exception
//		pRequest->ReThrowException( e ) ;
//	}
//	catch( ... )
//	{
//		// Something nasty happened, don't even try
//		// to do resource cleanup
//		
//		pRequest->ThrowException( 
//			"Unexpected error occurred in CFX tag", "" ) ;
//	}
//
// @xref <c CCFXRequest> <c CCFXException> <mf CCFXRequest::ThrowException>



// CCFXQuery::GetName //////////////////////////////////////////////////
//
// @mfunc LPCSTR | CCFXQuery | GetName | 
//   Retrieves the name of the query.
//
// @rdesc The name of the query.
//
// @ex The following example retrieves the name of the query 
//     and writes it back to the user: |
//
//	CCFXQuery* pQuery = pRequest->GetQuery() ;
//	pRequest->Write( "The query name is: " ) ;
//	pRequest->Write( pQuery->GetName() ) ;
//
// @xref <c CCFXQuery> 


// CCFXQuery::GetRowCount /////////////////////////////////////////////
//
// @mfunc LPCSTR | CCFXQuery | GetRowCount | 
//   Retrieves the number of rows in the query.
//
// @rdesc The number of rows contained in the query.
//
// @ex The following example retrieves the number of rows
//     in a query and writes it back to the user: | 
//
//	CCFXQuery* pQuery = pRequest->GetQuery() ;
//	char buffOutput[256] ;
//	wsprintf( buffOutput, 
//		"The number of rows in the query is %ld.",
//		pQuery->GetRowCount() ) ;
//	pRequest->Write( buffOutput ) ;
//
// @xref <c CCFXQuery> 


// CCFXQuery::GetColumns //////////////////////////////////////////////
//
// @mfunc <c CCFXStringSet>* | CCFXQuery | GetColumns | 
//   Retrieves a list of the column names contained in the query. 
//
// @rdesc An object of class <c CCFXStringSet> which contains a
//        list of the columns contained in the query. You are not 
//        responsible for freeing the memory allocated for the
//        returned string set (it will be automatically freed by
//        ColdFusion after the request is completed). 
//
// @ex The following example retrieves the list of columns and then
//     iterates over the list, writing each column name back to the user. |
//  
//	// Get the list of columns from the query
//	CCFXStringSet* pColumns = pQuery->GetColumns() ;
//	int nNumColumns = pColumns->GetCount() ;	
//	
//	// Print the list of columns to the user
//	pRequest->Write( "Columns in query: " ) ;
//	for( int i=1; i<=nNumColumns; i++ )
//	{
//		pRequest->Write( pColumns->GetString( i ) ) ;
//		pRequest->Write( " " ) ;
//	}
//
// @xref <c CCFXQuery> <c CCFXStringSet> 
//


// CCFXQuery::GetData ////////////////////////////////////////////////
//
// @mfunc LPCSTR | CCFXQuery | GetData | 
//   Retrieves a data element from a row and column of the query.
//   Row and column indexes begin with 1. You can determine the 
//   number of rows in the query by calling <mf CCFXQuery::GetRowCount>.
//	 You can determine the number of columns in the query by 
//	 retrieving the list of columns using <mf CCFXQuery::GetColumns> and
//   then calling <mf CCFXStringSet::GetCount> on the returned 
//   string set.
//
// @parm int | iRow | Row to retrieve data from (1-based).
// @parm int | lColumn | Column to retrieve data from (1-based).
//
// @rdesc The value of the requested data element.
//
// @ex The following example iterates over the elements of a
//     query and writes the data in the query back to
//     the user in a simple, space-delimited format: |
//
//	int iRow, iCol ;
//	int nNumCols = pQuery->GetColumns()->GetCount() ;
//	int nNumRows = pQuery->GetRowCount() ;
//	for ( iRow=1; iRow<=nNumRows; iRow++ )
//	{
//		for ( iCol=1; iCol<=nNumCols; iCol++ )
//		{
//			pRequest->Write( pQuery->GetData( iRow, iCol ) ) ;
//			pRequest->Write( " " ) ;
//		}
//		pRequest->Write( "<BR>" ) ;
//	}
//
// @xref <c CCFXQuery> <c CFXStringSet> <mf CCFXQuery::SetData> 
//


// CCFXQuery::AddRow /////////////////////////////////////////////////
//
// @mfunc int | CCFXQuery | AddRow |
//   Add a new row to the query. You should call this function each
//   time you want to append a row to the query.
//
// @rdesc The index of the row which was appended to the query.
//
// @ex The following example demonstrates the addition of 2 rows
//     to a query which has 3 columns ('City', 'State', and 'Zip'): |
//
//	// First row
//	int iRow ;
//	iRow = pQuery->AddRow() ;
//	pQuery->SetData( iCity, iRow, "Minneapolis" ) ;
//	pQuery->SetData( iState, iRow, "MN" ) ;
//	pQuery->SetData( iZip, iRow, "55345" ) ;
//
//	// Second row
//	iRow = pQuery->AddRow() ;
//	pQuery->SetData( iCity, iRow, "St. Paul" ) ;
//	pQuery->SetData( iState, iRow, "MN" ) ;
//	pQuery->SetData( iZip, iRow, "55105" ) ;
//	
// @xref <c CCFXQuery> <mf CCFXQuery::SetData>
//


// CCFXQuery::SetData ////////////////////////////////////////////////
//
// @mfunc void | CCFXQuery | SetData | 
//   Sets a data element within a row and column of the query.
//   Row and column indexes begin with 1. Before calling SetData
//   for a given row you should be sure to call <mf CCFXQuery::AddRow>
//   and use the return value as the row index for your call
//   to SetData.
//
// @parm int | iRow | Row of data element to set (1-based).
// @parm int | lColumn | Column of data element to set (1-based).
// @parm LPCSTR | lpszData | New value for data element.
//
// @ex The following example demonstrates the addition of 2 rows
//     to a query which has 3 columns ('City', 'State', and 'Zip'): |
//
//	// First row
//	int iRow ;
//	iRow = pQuery->AddRow() ;
//	pQuery->SetData( iCity, iRow, "Minneapolis" ) ;
//	pQuery->SetData( iState, iRow, "MN" ) ;
//	pQuery->SetData( iZip, iRow, "55345" ) ;
//
//	// Second row
//	iRow = pQuery->AddRow() ;
//	pQuery->SetData( iCity, iRow, "St. Paul" ) ;
//	pQuery->SetData( iState, iRow, "MN" ) ;
//	pQuery->SetData( iZip, iRow, "55105" ) ;
//	
// @xref <c CCFXQuery> <mf CCFXQuery::AddRow>
//

// CCFXQuery::SetQueryString ////////////////////////////////////////////
//
// This call is deprecated.
//
// @mfunc void | CCFXQuery | SetQueryString | 
//   Set the query string which will displayed along with
//   the query debug output. For queries generated by the
//   DBQUERY tag this is the SQL statement. For your
//   custom tag it may be something different, or you may not
//   wish to display a query string at all.
//
// @parm LPCSTR | lpszQuery | Text of query string.
//
// @ex The following example is from a hypothetical custom
//     tag which does directory browsing based on a command
//     string passed to the tag: |
//
//	LPCSTR lpszDirListCommand = 
//		pRequest->GetAttribute("COMMAND") ;
//	
//	...Create a query (pQuery) and populate it with the
//	   contents of the directory listing...
//
//	pQuery->SetQueryString( lpszDirListCommand ) ;
//
// @xref <c CCFXQuery> <mf CCFXQuery::SetTotalTime>
//


// CCFXQuery::SetTotalTime //////////////////////////////////////////
//
// This call is deprecated.
//
// @mfunc void | CCFXQuery | SetTotalTime | 
//   Set the number of milliseconds which were required to process
//   this query. This number will be displayed along with
//   the query debug output.
//
// @parm DWORD | dwMilliseconds | Execution time in milliseconds.
//
// @ex The following example demonstrates the methodology used
//     to set the total time for a query: |
//
//	DWORD dwStartTime = GetCurrentTime() ;
//
//	...execute the query and populate it with data...
//
//	pQuery->SetTotalTime( GetCurrentTime() - dwStartTime ) ;
//
// @xref <c CCFXQuery> <mf CCFXQuery::SetQueryString>
//


// CCFXStringSet::AddString ////////////////////////////////////////
//
// @mfunc int | CCFXStringSet | AddString |
//	Add a string to the end of the list. 
//
// @parm LPCSTR | lpszString | String to add to the list.
//
// @rdesc The index of the string which was added.
//
// @ex The following example demonstrates adding three strings
//     to a string set and saving the indexes of the items 
//     which are added: |
//
//	CCFXStringSet* pSet = pRequest->CreateStringSet() ;
//	int iRed = pSet->AddString( "Red" ) ;
//	int iGreen = pSet->AddString( "Green" ) ;
//	int iBlue = pSet->AddString( "Blue" ) ;
//
// @xref <c CCFXStringSet>
//


// CCFXStringSet::GetCount ////////////////////////////////////
//
// @mfunc int | CCFXStringSet | GetCount |
//	Get the number of strings contained in the string set. This
//  value can be used along with the <mf CCFXStringSet::GetString>
//  function to iterate over the strings in the set (when iterating,
//  remember that the index values for strings in the list begin at 1).
//
// @rdesc The number of strings contained in the string set.
//
// @ex The following example demonstrates using GetCount along 
//     with GetString to iterate over a string set and write
//     the contents of the list back to the user: |
//
//	int nNumItems = pStringSet->GetCount() ;
//	for ( int i=1; i<=nNumItems; i++ )
//	{
//		pRequest->Write( pStringSet->GetString( i ) ) ;
//		pRequest->Write( "<BR>" ) ;
//	}
//
// @xref <c CCFXStringSet>
//


// CCFXStringSet::GetIndexForString ////////////////////////////////////
//
// @mfunc int | CCFXStringSet | GetIndexForString |
//	Do a case insensitive search for the passed string. 
//
// @parm LPCSTR | lpszString | String to search for.
//
// @rdesc If the string is found then its index within the
//        string set is returned. If it is not found then
//        the constant CFX_STRING_NOT_FOUND is returned.
//
// @ex The following example illustrates searching for a
//     a string and throwing an exception if it is not 
//     found: |
//
//	CCFXStringSet* pAttribs = pRequest->GetAttributeList() ;
//
//	int iDestination = 
//		pAttribs->GetIndexForString("DESTINATION") ;
//	if ( iDestination == CFX_STRING_NOT_FOUND )
//	{
//		pRequest->ThrowException( 
//			"DESTINATION attribute not found."
//			"The DESTINATION attribute is required "
//			"by this tag." ) ;
//	}
//
// @xref <c CCFXStringSet>
//

// CCFXStringSet::GetString ///////////////////////////////////////////
//
// @mfunc LPCSTR | CCFXStringSet | GetString |
//	Retrieve the string located at the passed index (note that
//  index values are 1-based).
//
// @parm int | iIndex | Index of string to retrieve.
//
// @rdesc The string located at the passed index.
//
// @ex The following example demonstrates using GetString along 
//     with GetCount to iterate over a string set and write
//     the contents of the list back to the user: |
//
//	int nNumItems = pStringSet->GetCount() ;
//	for ( int i=1; i<=nNumItems; i++ )
//	{
//		pRequest->Write( pStringSet->GetString( i ) ) ;
//		pRequest->Write( "<BR>" ) ;
//	}
//
// @xref <c CCFXStringSet>
//



	



