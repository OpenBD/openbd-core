/* 
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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

package com.naryx.tagfusion.servlet.jsp;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfCatchData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmAbortException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
//import javax.servlet.jsp.*;

/**
 * This class is modelled to be a subclass of javax.servlet.jsp.PageContext,
 * but it's not because we don't really want to support all of the abstract
 * methods defined by PageContext. Also, we need to change the signatures of
 * the forward() and include() methods to throw cfmRunTimeException, which we
 * can't do if this class extends PageContext.
 * 
 * The PageContext abstract methods are still here (commented-out), in case we
 * ever decide that we need or want to implement any of them.
 */

public class CfmlPageContext //extends PageContext
{
	private cfSession _session;
	private CfmlJspWriter writer;
	
	public CfmlPageContext( cfSession session ) {
		_session = session;
		writer = new CfmlJspWriter(_session);
	}
	
	/**
     * <p>
     * The initialize method is called to initialize an uninitialized PageContext
     * so that it may be used by a JSP Implementation class to service an
     * incoming request and response within it's _jspService() method.
     *
     * <p>
     * This method is typically called from JspFactory.getPageContext() in
     * order to initialize state.
     *
     * <p>
     * This method is required to create an initial JspWriter, and associate
     * the "out" name in page scope with this newly created object.
     *
     * <p>
     * This method should not be used by page  or tag library authors.
     *
     * @param servlet The Servlet that is associated with this PageContext
     * @param request The currently pending request for this Servlet
     * @param response The currently pending response for this Servlet
     * @param errorPageURL The value of the errorpage attribute from the page directive or null
     * @param needsSession The value of the session attribute from the page directive
     * @param bufferSize The value of the buffer attribute from the page directive
     * @param autoFlush The value of the autoflush attribute from the page directive
     *
     * @throws IOException during creation of JspWriter
     * @throws IllegalStateException if out not correctly initialized
     */
//    public void initialize( Servlet servlet, ServletRequest request, ServletResponse response,
//    						String errorPageURL, boolean needsSession, int bufferSize,
//    						boolean autoFlush) throws IllegalStateException
//    {
//    }

    /**
     * <p>
     * This method shall "reset" the internal state of a PageContext, releasing
     * all internal references, and preparing the PageContext for potential
     * reuse by a later invocation of initialize(). This method is typically
     * called from JspFactory.releasePageContext().
     *
     * <p>
     * Subclasses shall envelope this method.
     *
     * <p>
     * This method should not be used by page  or tag library authors.
     *
     */
//    public void release()
//    {
//    	_session = null;
//    }


    /**
     * Register the name and object specified with page scope semantics.
     *
     * @param name the name of the attribute to set
     * @param attribute  the object to associate with the name
     * 
     * @throws NullPointerException if the name or object is null
     */
//    public void setAttribute( String name, Object attribute )
//    {
//    }

    /**
     * register the name and object specified with appropriate scope semantics
     * 
     * @param name the name of the attribute to set
     * @param o    the object to associate with the name
     * @param scope the scope with which to associate the name/object
     * 
     * @throws NullPointerException if the name or object is null
     * @throws IllegalArgumentException if the scope is invalid
     *
     */
//    public void setAttribute( String name, Object o, int scope )
//    {
//    }

    /**
     * Return the object associated with the name in the page scope or null
     * if not found.
     *
     * @param name the name of the attribute to get
     * 
     * @throws NullPointerException if the name is null
     * @throws IllegalArgumentException if the scope is invalid
     */
//    public Object getAttribute( String name )
//    {
//    	return null;
//    }

    /**
     * Return the object associated with the name in the specified
     * scope or null if not found.
     *
     * @param name the name of the attribute to set
     * @param scope the scope with which to associate the name/object
     * 
     * @throws NullPointerException if the name is null
     * @throws IllegalArgumentException if the scope is invalid
     */
//    public Object getAttribute( String name, int scope )
//    {
//    	return null;
//    }

    /**
     * Searches for the named attribute in page, request, session (if valid),
     * and application scope(s) in order and returns the value associated or
     * null.
     *
     * @return the value associated or null
     */
//    public Object findAttribute( String name )
//    {
//    	return null;
//    }

    /**
     * Remove the object reference associated with the given name,
     * look in all scopes in the scope order.
     *
     * @param name The name of the object to remove.
     */
//    public void removeAttribute( String name )
//    {
//    }

    /**
     * Remove the object reference associated with the specified name
     * in the given scope.
     *
     * @param name The name of the object to remove.
     * @param scope The scope where to look.
     */
//    public void removeAttribute( String name, int scope )
//    {
//    }

    /**
     * Get the scope where a given attribute is defined.
     *
     * @return the scope of the object associated with the name specified or 0
     */
//    public int getAttributesScope( String name )
//    {
//    	return -1;
//    }

    /**
     * Enumerate all the attributes in a given scope
     *
     * @return an enumeration of names (java.lang.String) of all the attributes the specified scope
     */
//	public Enumeration getAttributeNamesInScope( int scope )
//	{
//		return null;
//	}

    /**
     * The current value of the out object (a JspWriter).
     *
     * @return the current JspWriter stream being used for client response
     */
	public CfmlJspWriter getOut()
	{
		return writer;
	}

    /**
     * The current value of the session object (an HttpSession).
     *
     * @return the HttpSession for this PageContext or null
     */
    public HttpSession getSession()
    {
    	return _session.REQ.getSession();
    }

    /**
     * The current value of the page object (a Servlet).
     *
     * @return the Page implementation class instance (Servlet)  associated with this PageContext
     */
//	public Object getPage()
//	{
//		return null;
//	}


    /**
     * The current value of the request object (a ServletRequest).
     *
     * @return The ServletRequest for this PageContext
     */
	public ServletRequest getRequest()
	{
		return _session.REQ;
	}

    /**
     * The current value of the response object (a ServletResponse).
     *
     * @return the ServletResponse for this PageContext
     */
	public ServletResponse getResponse()
	{
		return _session.RES;
	}

    /**
     * The current value of the exception object (an Exception).
     *
     * @return any exception passed to this as an errorpage
     */
//	public Exception getException()
//	{
//		return null;
//	}

    /**
     * The ServletConfig instance.
     *
     * @return the ServletConfig for this PageContext
     */
//	public ServletConfig getServletConfig()
//	{
//		return null;
//	}

    /**
     * The ServletContext instance.
     * 
     * @return the ServletContext for this PageContext
     */
	public ServletContext getServletContext()
	{
		return _session.CTX;
	}

    /**
     * <p>
     * This method is used to re-direct, or "forward" the current ServletRequest and ServletResponse to another
     * active component in the application.
     * </p>
     * <p>
     * If the <I> relativeUrlPath </I> begins with a "/" then the URL specified
     * is calculated relative to the DOCROOT of the <code> ServletContext </code>
     * for this JSP. If the path does not begin with a "/" then the URL 
     * specified is calculated relative to the URL of the request that was
     * mapped to the calling JSP.
     * </p>
     * <p>
     * It is only valid to call this method from a <code> Thread </code>
     * executing within a <code> _jspService(...) </code> method of a JSP.
     * </p>
     * <p>
     * Once this method has been called successfully, it is illegal for the
     * calling <code> Thread </code> to attempt to modify the <code>
     * ServletResponse </code> object.  Any such attempt to do so, shall result
     * in undefined behavior. Typically, callers immediately return from 
     * <code> _jspService(...) </code> after calling this method.
     * </p>
     *
     * @param relativeUrlPath specifies the relative URL path to the target resource as described above
     *
     * @throws ServletException
     * @throws IOException
     *
     * @throws IllegalArgumentException if target resource URL is unresolvable
     * @throws IllegalStateException if <code> ServletResponse </code> is not in a state where a forward can be performed
     * @throws SecurityException if target resource cannot be accessed by caller
     */
	public void forward( String relativeUrlPath ) throws cfmRunTimeException //ServletException
	{
		/**
		 * 	From the JSP 2.0 spec on <jsp:forward/> :
		 * 
		 * 	(1) If the page output is buffered, the buffer is cleared prior to forwarding.
		 *  (2) If the page output is buffered and the buffer was flushed, an attempt to
		 * 		forward the request will result in an IllegalStateException.
		 * 	(3) If the page output was unbuffered and anything has been written to it, an
		 * 		attempt to forward the request will result in an IllegalStateException.
		 * 
		 * 	These conditions are handled by the RequestDispatcher.forward() method,
		 * 	(note that item 3 doesn't apply to us since we're always buffered).
		 */

		//--[ Attempt to get the RequestDispatcher
		RequestDispatcher rd = _session.REQ.getRequestDispatcher( relativeUrlPath );
		if ( rd == null ) {
			throw new cfmRunTimeException( catchDataFactory.generalException( "errorCode.runtimeError",
																				"cfinclude.missingFile",
																				new String[]{relativeUrlPath} ) );
		}

		try {
			rd.forward( _session.REQ, _session.RES.getResponse() );
			_session.abortAfterForward();	// throws cfmAbortException
		} catch ( cfmAbortException aE ) {
			throw aE;  // catch and rethrow here so it's not caught as Exception below
		} catch ( IllegalStateException iE ) {
			cfCatchData catchData = new cfCatchData();
			catchData.setType( cfCatchData.TYPE_TEMPLATE );
			catchData.setDetail( "Unable to forward request because the page has been flushed" );
			catchData.setMessage( "Cannot forward request" );
			throw new cfmRunTimeException( catchData );
		} catch ( Exception sE ) { // ServletException, IOException
			throw new cfmRunTimeException( catchDataFactory.extendedException( "errorCode.runtimeError",
																				 "cfinclude.pageExecution",
																				 new String[]{relativeUrlPath},
																				 sE.getMessage()) );
		}
	}

    /**
     * <p>
     * Causes the resource specified to be processed as part of the current
     * ServletRequest and ServletResponse being processed by the calling Thread.
     * The output of the target resources processing of the request is written
     * directly to the ServletResponse output stream.
     * </p>
     * <p>
     * The current JspWriter "out" for this JSP is flushed as a side-effect
     * of this call, prior to processing the include.
     * </p>
     * <p>
     * If the <I> relativeUrlPath </I> begins with a "/" then the URL specified
     * is calculated relative to the DOCROOT of the <code> ServletContext </code>
     * for this JSP. If the path does not begin with a "/" then the URL 
     * specified is calculated relative to the URL of the request that was
     * mapped to the calling JSP.
     * </p>
     * <p>
     * It is only valid to call this method from a <code> Thread </code>
     * executing within a <code> _jspService(...) </code> method of a JSP.
     * </p>
     *
     * @param relativeUrlPath specifies the relative URL path to the target resource to be included
     *
     * @throws ServletException
     * @throws IOException
     *
     * @throws IllegalArgumentException if the target resource URL is unresolvable
     * @throws SecurityException if target resource cannot be accessed by caller
     *
     */
	public void include( String relativeUrlPath ) throws cfmRunTimeException //ServletException, IOException
	{
		//--[ Attempt to get the RequestDispatcher
		RequestDispatcher rd = _session.REQ.getRequestDispatcher( relativeUrlPath );
		if ( rd == null ) {
			throw new cfmRunTimeException( catchDataFactory.generalException( "errorCode.runtimeError",
																				"cfinclude.missingFile",
																				new String[]{relativeUrlPath} ) );
		}
		
		//---[ Now that the servlet has been found, trigger its execution
		try {
			_session.RES.setJspInclude( true );
			_session.REQ.setAttribute( cfSession.ATTR_NAME, _session );
            rd.include( _session.REQ, _session.RES );
        } catch ( ServletException se ) {
            try {
                // this is for WebLogic: try passing the original response object
                _session.RES.setJspInclude( false );
                _session.pageFlush();
                rd.include( _session.REQ, _session.RES.getResponse() );
            } catch ( ServletException e ) {
                throwIncludeError( e, relativeUrlPath );
            } catch ( IOException e ) {
                throwIncludeError( e, relativeUrlPath );
            }
		} catch ( IOException e ) {
            throwIncludeError( e, relativeUrlPath );
		} finally {
			_session.REQ.removeAttribute( cfSession.ATTR_NAME );
            _session.RES.setJspInclude( false );
		}
	}
    
    private static void throwIncludeError( Exception e, String relativeUrlPath ) throws cfmRunTimeException {
        throw new cfmRunTimeException( catchDataFactory.extendedException( "errorCode.runtimeError",
                                                                           "cfinclude.pageExecution",
                                                                            new String[]{relativeUrlPath},
                                                                            e.getMessage()) );
    }

    /**
     * <p>
     * This method is intended to process an unhandled "page" level exception
     * by redirecting the exception to either the specified error page for this
     * JSP, or if none was specified, to perform some implementation dependent
     * action.
     *
     * <p>
     * A JSP implementation class shall typically clean up any local state
     * prior to invoking this and will return immediately thereafter. It is
     * illegal to generate any output to the client, or to modify any 
     * ServletResponse state after invoking this call.
     *
     * <p>
     * This method is kept for backwards compatiblity reasons.  Newly
     * generated code should use PageContext.handlePageException(Throwable).
     *
     * @param e the exception to be handled
     *
     * @throws ServletException
     * @throws IOException
     *
     * @throws NullPointerException if the exception is null
     * @throws SecurityException if target resource cannot be accessed by caller
     *
     * @see #handlePageException(Throwable)
     */
//	public void handlePageException( Exception e ) throws ServletException, IOException
//	{
//		throw new ServletException( "Not Implemented" );
//	}

    /**
     * <p>
     * This method is identical to the handlePageException(Exception),
     * except that it accepts a Throwable.  This is the preferred method
     * to use as it allows proper implementation of the errorpage
     * semantics.
     *
     * <p>
     * This method is intended to process an unhandled "page" level exception
     * by redirecting the exception to either the specified error page for this
     * JSP, or if none was specified, to perform some implementation dependent
     * action.
     *
     * <p>
     * A JSP implementation class shall typically clean up any local state
     * prior to invoking this and will return immediately thereafter. It is
     * illegal to generate any output to the client, or to modify any 
     * ServletResponse state after invoking this call.
     *
     * @param t the throwable to be handled
     *
     * @throws ServletException
     * @throws IOException
     *
     * @throws NullPointerException if the exception is null
     * @throws SecurityException if target resource cannot be accessed by caller
     *
     * @see #handlePageException(Exception)
     */
//	public void handlePageException(Throwable t) throws ServletException, IOException
//	{
//		throw new ServletException( "Not Implemented" );
//	}
}


