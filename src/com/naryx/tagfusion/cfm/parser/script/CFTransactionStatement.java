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
import java.util.Map;

import org.antlr.runtime.Token;

import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.CFContext;
import com.naryx.tagfusion.cfm.parser.CFException;
import com.naryx.tagfusion.cfm.parser.CFExpression;
import com.naryx.tagfusion.cfm.sql.cfTRANSACTION;
import com.naryx.tagfusion.cfm.sql.cfTransactionCache;
import com.naryx.tagfusion.cfm.tag.cfTag;


public class CFTransactionStatement extends CFParsedAttributeStatement implements Serializable {

	private static final long serialVersionUID = 1L;

	private CFScriptStatement body;

	private static HashSet<String> supportedAttributes;

	static {
		supportedAttributes = new HashSet<String>();
		supportedAttributes.add( "ACTION" );
		supportedAttributes.add( "SAVEPOINT" );
		supportedAttributes.add( "ISOLATION" );
	}


	public CFTransactionStatement( Token _t, Map<String, CFExpression> _attr, CFScriptStatement _body ) {
		super( _t, _attr );

		body = _body;
	}


	@Override
	public CFStatementResult Exec( CFContext context ) throws cfmRunTimeException {
		setLineCol( context );

		String action = getAttributeValue( context, "ACTION", true ).getString().toUpperCase();
		if ( !action.equals( cfTRANSACTION.ACTION_BEGIN ) && !action.equals( cfTRANSACTION.ACTION_COMMIT ) && !action.equals( cfTRANSACTION.ACTION_ROLLBACK ) )
			throw new CFException( "Invalid type for ACTION.  BEGIN/COMMIT/ROLLBACK only allowed.", context );

		cfTransactionCache tCache = (cfTransactionCache) context.getSession().getDataBin( cfTRANSACTION.DATA_BIN_KEY );

		if ( action.equals( cfTRANSACTION.ACTION_BEGIN ) ) {

			if ( tCache != null )
				throw new CFException( "You may not embed a CFTRANSACTION within another CFTRANSACTION", context );

			if ( containsAttribute( "ISOLATION" ) ) {
				String isolationLevel = getAttributeValueString( context, "ISOLATION" ).toLowerCase();

				if ( isolationLevel.equals( "read_uncommitted" ) )
					tCache = new cfTransactionCache( java.sql.Connection.TRANSACTION_READ_UNCOMMITTED );
				else if ( isolationLevel.equals( "read_committed" ) )
					tCache = new cfTransactionCache( java.sql.Connection.TRANSACTION_READ_COMMITTED );
				else if ( isolationLevel.equals( "repeatable_read" ) )
					tCache = new cfTransactionCache( java.sql.Connection.TRANSACTION_REPEATABLE_READ );
				else if ( isolationLevel.equals( "serializable" ) )
					tCache = new cfTransactionCache( java.sql.Connection.TRANSACTION_SERIALIZABLE );
				else
					throw new CFException( "Invalid ISOLATION level: " + isolationLevel, context );

			} else {
				tCache = new cfTransactionCache();
			}

			context.getSession().setDataBin( cfTRANSACTION.DATA_BIN_KEY, tCache ); // --[ Insert the Transaction cache in the session

			try {
				body.Exec( context );
				tCache.commit();
			} catch ( cfmRunTimeException e ) {
				tCache.rollback();
				throw e;
			} finally {
				context.getSession().deleteDataBin( cfTRANSACTION.DATA_BIN_KEY ); // --[ Remove the transaction cache
			}

		} else if ( action.equals( cfTRANSACTION.ACTION_COMMIT ) ) {

			if ( tCache != null )
				tCache.commit();

		} else if ( action.equals( cfTRANSACTION.ACTION_ROLLBACK ) ) {

			if ( tCache != null )
				tCache.rollback();

		}

		return null;
	}


	@Override
	public void setHostTag( cfTag _parentTag ) {
		super.setHostTag( _parentTag );
		body.setHostTag( _parentTag );
	}


	@Override
	public String Decompile( int indent ) {
		StringBuilder sb = new StringBuilder();
		sb.append( "transaction " );
		DecompileAttributes( sb );
		if ( body == null ) {
			sb.append( ";" );
		} else {
			sb.append( body.Decompile( 0 ) );
		}

		return sb.toString();
	}

}
