/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  
 *  $Id: QueryRun.java 2404 2013-09-22 21:51:40Z alan $
 */

package com.bluedragon.search.search;

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

import com.bluedragon.search.AnalyzerFactory;
import com.bluedragon.search.DocumentWrap;
import com.bluedragon.search.collection.Collection;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStringData;


public class QueryRun extends Object {
	private static final String[] QUERY_COLUMNS = new String[] { "KEY", "TITLE", "SCORE", "SEARCHCOUNT", "RECORDSSEARCHED", "RANK" };

	private QueryAttributes queryAttributes;
	private cfQueryResultData	queryResultData;
	private Map<String, Integer>	activeColumns;
	private Set<String>	uniqueSet = null;
	
	public QueryRun( QueryAttributes _queryAttributes ){
		queryAttributes	= _queryAttributes;
		queryResultData	=  new cfQueryResultData( QUERY_COLUMNS, "SEARCH" );
		
		if ( queryAttributes.getUniqueColumn() != null )
			uniqueSet	= new HashSet<String>();
			
		activeColumns	= new HashMap<String,Integer>();
		for ( int x=0; x<QUERY_COLUMNS.length; x++ )
			activeColumns.put(QUERY_COLUMNS[x].toLowerCase(), x+1 );
		
		activeColumns.put( "id", 		1 );
		activeColumns.put( "name", 	2 );
	}

	public cfQueryResultData getQueryResultData(){
		return queryResultData;
	}

	public void run() throws CorruptIndexException, Exception{
		int potentialRows = 0;

		Iterator<Collection> it =	queryAttributes.getCollectionIterator();

		while ( it.hasNext() && queryResultData.getSize() <= queryAttributes.getMaxRows() ){
			Collection collection	= it.next();

			IndexSearcher	searcher	= collection.getIndexSearcher();
			TopDocs hits = searcher.search( queryAttributes.getQuery(), collection.getTotalDocs() );

			ScoreDoc[] scorehits	= hits.scoreDocs;
			for ( int x=0; x < scorehits.length; x++ ){
				if ( scorehits[x].score > queryAttributes.getMinScore() ){
					potentialRows++;
					
					if ( potentialRows >= queryAttributes.getStartRow() ){
						addRow( searcher, scorehits[x].doc, scorehits[x].score, x, scorehits.length, collection.getTotalDocs() );
						if ( queryResultData.getSize() == queryAttributes.getMaxRows() )
							break;
					}
					
				}
			}
		}
		
		queryResultData.reset();
		
		if ( uniqueSet != null )
			uniqueSet.clear();
	}

	
	private void addRow(IndexSearcher	searcher, int docid, float score, int rank, int searchCount, int recordsSearched ) throws CorruptIndexException, Exception {
		DocumentWrap document = new DocumentWrap( searcher.doc(docid) );
		
		queryResultData.addRow(1);
		queryResultData.setCurrentRow( queryResultData.getSize() );
		
		// Add in the standard columns that we know we have for every search
		queryResultData.setCell( 1, new cfStringData(document.getId()) );
		queryResultData.setCell( 2, new cfStringData(document.getName()) );
		queryResultData.setCell( 3, new cfNumberData(score) );
		queryResultData.setCell( 4, new cfNumberData(searchCount) );
		queryResultData.setCell( 5, new cfNumberData(recordsSearched) );
		queryResultData.setCell( 6, new cfNumberData(rank+1) );
		
		String uC	= queryAttributes.getUniqueColumn();
		
		// Now we do the custom ones
		List<IndexableField> fields 	= document.getDocument().getFields();
		Iterator<IndexableField> it	= fields.iterator();
		while ( it.hasNext() ){
			IndexableField	fieldable	= it.next();
			
			String fieldName	= fieldable.name().toLowerCase();
			
			// Check for the unique
			if ( uniqueSet != null && fieldName.equals( uC ) ){
				if ( uniqueSet.contains(fieldable.stringValue()) ){
					queryResultData.deleteRow( queryResultData.getSize() );
					return;
				}else
					uniqueSet.add(fieldable.stringValue());
			}
			
			// Check to see if we have this column
			if ( fieldName.equals("contents") && !queryAttributes.getContentFlag() )
				continue;
			
			if ( !activeColumns.containsKey( fieldName ) ){
				int newcolumn = queryResultData.addColumnData( fieldable.name().toUpperCase(), cfArrayData.createArray(1), null );
				activeColumns.put( fieldName, newcolumn );
			}
			
			int column	= activeColumns.get( fieldName );
			if ( column <= 6 )
				continue;
			
			queryResultData.setCell( column, new cfStringData( fieldable.stringValue() ) );
		}

		
		// Do the context stuff if enable
		if ( queryAttributes.getContextPassages() > 0 ){
			
	    Scorer scorer									= new QueryScorer( queryAttributes.getQuery() );
	    SimpleHTMLFormatter formatter = new SimpleHTMLFormatter( queryAttributes.getContextHighlightStart(), queryAttributes.getContextHighlightEnd() );
	    Highlighter highlighter 			= new Highlighter( formatter, scorer );
	    Fragmenter fragmenter 				= new SimpleFragmenter( queryAttributes.getContextBytes() );
	    highlighter.setTextFragmenter( fragmenter );

    	String nextContext = "";
      String contents = document.getAttribute( DocumentWrap.CONTENTS );
      
      if ( contents != null ){
	      TokenStream tokenStream = AnalyzerFactory.get("simple").tokenStream( DocumentWrap.CONTENTS, new StringReader( contents ) );
	      String [] fragments = null;
	      try {
					fragments = highlighter.getBestFragments( tokenStream, contents, queryAttributes.getContextPassages() );
					if ( fragments.length == 1 ){
						nextContext = fragments[0] + "...";
					}else{
						StringBuilder context = new StringBuilder();
						for ( int f = 0; f < fragments.length; f++ ){
							context.append( "..." );
							context.append( fragments[f] );
						}
						context.append( "..." );
						nextContext = context.toString();
					}
				} catch (Exception e) {
				}
				
				
				// Add in the context
				if ( !activeColumns.containsKey( "context" ) ){
					int newcolumn = queryResultData.addColumnData( "CONTEXT", cfArrayData.createArray(1), null );
					activeColumns.put( "context", newcolumn );
				}
				
				queryResultData.setCell( activeColumns.get( "context" ), new cfStringData( nextContext ) );
      }
		}
	}
}