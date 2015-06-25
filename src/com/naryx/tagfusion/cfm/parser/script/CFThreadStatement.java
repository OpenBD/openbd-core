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
package com.naryx.tagfusion.cfm.parser.script;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.antlr.runtime.Token;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfThreadData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.parser.CFCall;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFException;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.ext.thread.cfTHREAD;
import com.naryx.tagfusion.cfm.tag.ext.thread.cfThreadRunner;
import com.naryx.tagfusion.cfm.tag.ext.thread.cfThreadScriptRunner;


public class CFThreadStatement extends CFParsedAttributeStatement implements Serializable {

	private static final long serialVersionUID = 1L;

	private final CFScriptStatement body;

	private static HashSet<String> supportedAttributes;

	static {
		supportedAttributes = new HashSet<String>();
		supportedAttributes.add( "NAME" );
		supportedAttributes.add( "ATTRIBUTECOLLECTION" );
		supportedAttributes.add( "OUTPUT" );
		supportedAttributes.add( "ACTION" );
		supportedAttributes.add( "PRIORITY" );
		supportedAttributes.add( "DURATION" );
		supportedAttributes.add( "TIMEOUT" );
	}


	public CFThreadStatement( Token _t, Map<String, CFExpression> _attr, CFScriptStatement _body ) {
		super( _t, _attr );

		body = _body;
	}


	@Override
	public CFStatementResult Exec( CFContext context ) throws cfmRunTimeException {
		setLineCol( context );

		String action = getAttributeValueString( context, "ACTION", "RUN" ).toUpperCase();

		if ( action.equals( "RUN" ) ) {
			return renderRun( context );
		} else if ( action.equals( "SLEEP" ) ) {
			return renderSleep( context );
		} else if ( action.equals( "JOIN" ) ) {
			return renderJoin( context );
		} else if ( action.equals( "TERMINATE" ) ) {
			return renderTerminate( context );
		} else {
			throw new CFException( "Invalid ACTION attribute value: " + action +
					". Valid values include \"JOIN\", \"RUN\", \"SLEEP\" and \"TERMINATE\"", context );
		}
	}


	@Override
	public String Decompile( int indent ) {
		StringBuilder sb = new StringBuilder();
		sb.append( "thread " );
		DecompileAttributes( sb );
		if ( body == null ) {
			sb.append( ";" );
		} else {
			sb.append( body.Decompile( 0 ) );
		}

		return sb.toString();
	}


	@Override
	public void setHostTag( cfTag _parentTag ) {
		super.setHostTag( _parentTag );

		if ( body != null )
			body.setHostTag( _parentTag );
	}


	/*
	 * creates a new thread
	 */
	private CFStatementResult renderRun( CFContext _context ) throws dataNotSupportedException, cfmRunTimeException {
		String name = null;

		if ( containsAttribute( "NAME" ) ) {
			name = getAttributeValueString( _context, "NAME" );

			// check if thread already exists
			cfData existingThread = _context.getSession().getQualifiedData( variableStore.CFTHREAD_SCOPE ).getData( name );
			if ( existingThread instanceof cfThreadData ) {
				throw new CFException( "Invalid NAME attribute value. You cannot create multiple threads with the same name in a single request", _context );
			}
		}

		// get priority of thread
		cfSession session = _context.getSession();
		int priority = cfTHREAD.getPriority( session, getAttributeValueString( _context, "PRIORITY", "normal" ) );


		cfStructData attributes = packageUpAttributes( _context );
		boolean keepOutput = ( getAttributeValueBoolean( _context, "OUTPUT", true ) && name != null );
		cfSession threadSession = cfTHREAD.createVirtualSession( session, name, attributes, keepOutput );
		cfThreadRunner thread = new cfThreadScriptRunner( body, name, threadSession.getCFContext(), priority );
		if ( name != null ) { // named threads have associated data
			cfStructData threadScope = session.getQualifiedData( variableStore.CFTHREAD_SCOPE );
			threadScope.setData( name, thread.getThreadData() );
			CFCall call = threadSession.enterCFThread( attributes, thread.getThreadData() );
			// copy the attributes to the local scope
			call.putAll( attributes, null );
		}
		thread.start();

		return null;
	}


	/*
	 * joins the named threads, with a given timeout
	 */
	private CFStatementResult renderJoin( CFContext _context ) throws dataNotSupportedException, cfmRunTimeException {
		String names = getAttributeValue( _context, "NAME", true ).getString();

		int timeout = getAttributeValueInt( _context, "TIMEOUT", 0 );
		if ( timeout < 0 ) {
			throw new CFException( "Invalid TIMEOUT attribute value. The specified value must not be negative", _context );
		}

		cfTHREAD.join( _context.getSession(), names, timeout );
		return null;
	}


	/*
	 * sleep for the given duration
	 */
	private CFStatementResult renderSleep( CFContext _context ) throws dataNotSupportedException, cfmRunTimeException {
		int duration = getAttributeValue( _context, "DURATION" ).getInt();
		if ( duration <= 0 ) {
			throw new CFException( "Invalid DURATION attribute value. The specified value must be greater than 0", _context );
		}

		try {
			Thread.sleep( duration );
		} catch ( InterruptedException ignore ) {}

		return null;
	}


	/*
	 * terminate the named threads
	 */
	private CFStatementResult renderTerminate( CFContext _context ) throws cfmRunTimeException {
		String name = getAttributeValue( _context, "NAME", true ).getString();
		cfTHREAD.terminate( _context.getSession(), name );
		return null;
	}


	private cfStructData packageUpAttributes( CFContext _context ) throws cfmRunTimeException {
		cfStructData attributeValues = new cfStructData();

		Iterator<String> iter = getAttributeKeyIterator();
		while ( iter.hasNext() ) {
			String key = iter.next();
			if ( !supportedAttributes.contains( key.toUpperCase() ) ) {
				cfData data = getAttributeValue( _context, key ).duplicate();
				attributeValues.setData( key, data );
			}
		}

		return attributeValues;
	}
}
