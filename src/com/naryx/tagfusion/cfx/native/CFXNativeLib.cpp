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
#ifdef __unix__
#include <dlfcn.h>
#include <link.h>
#else
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#endif

#ifdef __MWERKS__
#include <ansi_prefix.Win32.h>
#endif

#include "CFXNativeLib.h"
#include "CCFXRequestImpl.h"
#include "CCFXExceptionImpl.h"

typedef void (*ProcessTagRequestProc) (CCFXRequest*); 

void ThrowJavaException( JNIEnv *env, LPCSTR lpszError, LPCSTR lpszDiagnostics );

void
ThrowJavaException( JNIEnv *env, LPCSTR lpszError, LPCSTR lpszDiagnostics )
{
	char* str = new char[ strlen( "Error Message = %s, Diagnostic Message = %s" ) +
	                      strlen( lpszError ) +
	                      strlen( lpszDiagnostics ) ];
	                      
	sprintf( str, "Error = %s, Diagnostics = %s", lpszError, lpszDiagnostics );

	jclass exceptionClass = env->FindClass( "java/lang/Exception" );
	if ( exceptionClass != NULL )
		env->ThrowNew( exceptionClass, str );
		
	delete [] str;
}

/*
 * Class:     com_naryx_tagfusion_cfx_CFXNativeLib
 * Method:    processRequest
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/naryx/tagfusion/cfx/sessionRequest;Lcom/naryx/tagfusion/cfx/sessionResponse;Z)V
 */
JNIEXPORT void JNICALL Java_com_naryx_tagfusion_cfx_CFXNativeLib_processRequest
	( JNIEnv *env, jclass obj, jstring moduleName, jstring functionName, jobject request, jobject response, jboolean keepLibraryLoaded )
{	
	// This declaration is necessary to prevent g++ compiler error regarding 
	// "jump to label" crossing initialization.
	CCFXRequestImpl *cCFXRequest = NULL;
	ProcessTagRequestProc processTagRequest = NULL;
	const char *funcChars = NULL;

	// load the user's native tag module
	const char *modChars = env->GetStringUTFChars( moduleName, NULL );
	if ( modChars == NULL )
	{
		ThrowJavaException( env, "CFXNativeLib could not retrieve module name", "" );
		return;
	}
#ifdef __unix__
	void* hModuleDLL = dlopen (modChars, RTLD_LAZY);
#else
	HINSTANCE hModuleDLL = LoadLibrary( modChars );
#endif
	if ( hModuleDLL == NULL )
	{
		char *errorStr = NULL;
#ifdef __unix__
		errorStr = dlerror();
#endif
		char* exceptionStr = new char[ strlen( "CFXNativeLib could not load native tag module: %s." ) +
					  strlen( modChars ) ];
					  
		sprintf( exceptionStr, "CFXNativeLib could not load native tag module: %s.", modChars );
		ThrowJavaException( env, exceptionStr, errorStr );
		delete [] exceptionStr;
		goto cleanup;
	}

	funcChars = env->GetStringUTFChars( functionName, NULL );
	if ( funcChars == NULL )
	{
		ThrowJavaException( env, "CFXNativeLib could not retrieve function name", funcChars );
		goto cleanup;
	}
	
	// look up the user's ProcessTagRequest method
#ifdef __unix__
	processTagRequest = (ProcessTagRequestProc) dlsym (hModuleDLL, funcChars );
#else
	processTagRequest = (ProcessTagRequestProc) GetProcAddress( hModuleDLL, funcChars );
#endif
	if ( processTagRequest == NULL )
	{
		ThrowJavaException( env, "CFXNativeLib could not get tag function", funcChars );
		goto cleanup;
	}
		
	// create native CCFXRequest object to wrap the Java request/response objects
#ifdef __unix__
	cCFXRequest = new CCFXRequestImpl( env, request, response );
#else
	cCFXRequest = new CCFXRequestImpl( env, request, response );
#endif
	try
	{
		// invoke the user's ProcessTagRequest method
		(*processTagRequest) (cCFXRequest);
	}
	catch( CCFXException* e )	// processTagRequest should really only throw CCFXException
	{
		ThrowJavaException( env, e->GetError(), e->GetDiagnostics() );
	}
	catch ( ... )				// should catch any exception not caught above
	{
		ThrowJavaException( env, "CFXNativeLib caught an unrecognized exception", "" );
	}

cleanup:

	if ( modChars != NULL )
		env->ReleaseStringUTFChars( moduleName, modChars );

	if ( funcChars != NULL )
		env->ReleaseStringUTFChars( functionName, funcChars );
	
	if ( cCFXRequest != NULL )
		delete cCFXRequest;
	
	if ( !keepLibraryLoaded && ( hModuleDLL != NULL ) )
#ifdef __unix__
		dlclose (hModuleDLL);
#else
		FreeLibrary( hModuleDLL );
#endif
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
