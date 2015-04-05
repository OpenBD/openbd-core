#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <sql.h>
#include <sqlext.h>

#ifdef __MWERKS__
#include <ansi_prefix.Win32.h>
#endif

#include <string>
#include <vector>

using std::string;
using std::vector;

#include "ODBCNativeLib.h"

/***
void DisplaySQLDiagRec( SQLHENV henv );

void DisplaySQLDiagRec( SQLHENV henv )
{
	SQLSMALLINT recNumber = 1;
	SQLCHAR *sqlState = new SQLCHAR[ 32 ];
	SQLINTEGER nativeError;
	SQLCHAR *msgText = new SQLCHAR[ 255 ];
	SQLSMALLINT bufferLen = 255;
	SQLSMALLINT textLength;
	
	for ( SQLSMALLINT recNumber = 1; SQL_SUCCEEDED( SQLGetDiagRec( SQL_HANDLE_ENV, henv, recNumber, sqlState, &nativeError, msgText,
					bufferLen, &textLength ) ); recNumber++ ) 
	{
		OutputDebugString( "SqlState = " );
		OutputDebugString( (const char *)sqlState );
		OutputDebugString( "\n" );
		
		OutputDebugString( "MessageText = " );
		OutputDebugString( (const char *)msgText );
		OutputDebugString( "\n" );
	}
	
	delete [] sqlState;
	delete [] msgText;
}
***/

/*
 * Class:     com_naryx_tagfusion_cfm_sql_ODBCNativeLib
 * Method:    getOdbcDataSources
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_naryx_tagfusion_cfm_sql_ODBCNativeLib_getOdbcDataSources
	( JNIEnv *env, jclass obj )
{
	jclass exceptionClass = env->FindClass( "java/lang/Exception" );
	if ( exceptionClass == NULL )
		OutputDebugString( "ODBCNativeLib failed to find java.lang.Exception class\n" );
	
	SQLHENV henv;
	
	if ( SQLAllocHandle( SQL_HANDLE_ENV, SQL_NULL_HANDLE, &henv ) != SQL_SUCCESS )
	{
		env->ThrowNew( exceptionClass, "ODBCNativeLib failed to allocate SQL environment handle." );
		return NULL;
	}
	
	if ( SQLSetEnvAttr( henv, SQL_ATTR_ODBC_VERSION, (SQLPOINTER)SQL_OV_ODBC3, 0 ) != SQL_SUCCESS )
	{
		SQLFreeHandle( SQL_HANDLE_ENV, henv );
		env->ThrowNew( exceptionClass, "ODBCNativeLib failed to set SQL_ATTR_ODBC_VERSION." );
		return NULL;
	}
	
	SQLUSMALLINT direction = SQL_FETCH_FIRST_SYSTEM;
	
	SQLCHAR *serverName = new SQLCHAR[ SQL_MAX_DSN_LENGTH + 1 ];
	SQLSMALLINT serverNameLen;
	
	SQLCHAR *description = new SQLCHAR[ SQL_MAX_OPTION_STRING_LENGTH ];
	SQLSMALLINT descLen;
	
	vector< string > dataSourceArray;
	
	while ( SQL_SUCCEEDED( SQLDataSources( henv, direction, serverName, SQL_MAX_DSN_LENGTH + 1, &serverNameLen,
								description, SQL_MAX_OPTION_STRING_LENGTH, &descLen ) ) )
	{
		string s( (const char *)serverName );
		string d( (const char *)description );
	
		dataSourceArray.push_back( s );
		dataSourceArray.push_back( d );
		
		direction = SQL_FETCH_NEXT;
	}
	
	delete [] description;
	delete [] serverName;
	
	SQLFreeHandle( SQL_HANDLE_ENV, henv );
	
	int num = dataSourceArray.size();
	
	jobjectArray stringArray = env->NewObjectArray( num, env->FindClass( "java/lang/String" ), NULL );
	
	for ( int i = 0; i < num; i++ )
	{
		jstring s = env->NewStringUTF( dataSourceArray[ i ].c_str() );
		
		env->SetObjectArrayElement( stringArray, i, s );
		env->DeleteLocalRef( s );
	}
	
	return stringArray;
}

/*
 * The VM calls JNI_OnLoad when the native library is loaded (for example, through
 * System.loadLibrary). JNI_OnLoad must return the JNI version needed by the native library.
 * 
 * In order to use any of the new JNI functions, a native library must export a JNI_OnLoad
 * function that returns JNI_VERSION_1_2. If the native library does not export a JNI_OnLoad
 * function, the VM assumes that the library only requires JNI version JNI_VERSION_1_1. If
 * the VM does not recognize the version number returned by JNI_OnLoad, the native library
 * cannot be loaded. 
 */
JNIEXPORT jint JNICALL JNI_OnLoad( JavaVM *vm, void *reserved )
{
	return JNI_VERSION_1_2;
}


/*
 * The VM calls JNI_OnUnload when the class loader containing the native library is
 * garbage collected. This function can be used to perform cleanup operations. Because
 * this function is called in an unknown context (such as from a finalizer), the
 * programmer should be conservative on using Java VM services, and refrain from
 * arbitrary Java call-backs.
 */ 
JNIEXPORT void JNICALL JNI_OnUnload( JavaVM *vm, void *reserved )
{
}