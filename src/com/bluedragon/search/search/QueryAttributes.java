/*
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: QueryAttributes.java 2523 2015-02-22 16:23:11Z alan $
 */

package com.bluedragon.search.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;

import com.bluedragon.search.AnalyzerFactory;
import com.bluedragon.search.DocumentWrap;
import com.bluedragon.search.collection.Collection;
import com.bluedragon.search.collection.CollectionFactory;




/**
 * Manages all the attributes and creation of the Lucene query object 
 * for running against the various collections 
 * 
 */
public class QueryAttributes extends Object {

	private List<Collection> collectionsList;
	private Query	query;
	private float	minScore = 0;
	private int startRow	= 0, maxRows = Integer.MAX_VALUE;
	private boolean	bContent = true;
	private String uniqueColumn = null;
	
	private	int ContextBytes = 300, ContextPassages = 0;
	private String ContextHighlightStart = "<b>", ContextHighlightEnd = "</b>";
  
	public Query getQuery(){
		return query;
	}
	
	
	
	
	/**
	 * Sets the collections to run this query against; it supports multiple ones
	 * 
	 * @param _collection
	 * @return
	 */
	public boolean setCollection(String _collection) {
		if ( _collection == null || _collection.length() == 0 )
			return false;
		
		String[] collections	= _collection.toLowerCase().split(",");
		
		collectionsList	= new ArrayList<Collection>();
		for ( int x=0; x < collections.length; x++ ){
			Collection	col	= CollectionFactory.getCollection( collections[x].trim() );
			if ( col != null )
				collectionsList.add( col );
		}
		
		return ( collectionsList.size() != 0 );
	}
	
	public Iterator<Collection>	getCollectionIterator(){
		return collectionsList.iterator();
	}
	
	public boolean setCriteria( String _critera, String type, boolean _allowLeadingWildcard ) throws ParseException {
		if ( _critera == null || _critera.length() == 0 )
			return false;
	
		// If this is 'simple' then we OR up all parameters
		if ( ( type == null || type.equalsIgnoreCase("simple") ) && _critera.indexOf(",") != -1 ){
			String[] tokens	= _critera.split(",");
			_critera	= tokens[0];
			for ( int x=1; x < tokens.length; x++ )
				_critera += " OR " + tokens[x];
		}
		
		if (_critera.equals("*")) // Special Case, this allows you to retrieve all results from the collection
			query = new MatchAllDocsQuery();
		else {
			QueryParser qp = new QueryParser( DocumentWrap.CONTENTS, AnalyzerFactory.get( collectionsList.get(0).getLanguage() ) );
			qp.setAllowLeadingWildcard( _allowLeadingWildcard );
			query = qp.parse(_critera);
		}
		
		return true;
	}

	public boolean setCategory( String category ) throws ParseException{
		if ( category == null || category.length() == 0 )
			return false;
	
		String[] categories = category.split(",");
		
		Query catQuery = null;
		QueryParser qp = new QueryParser(DocumentWrap.CATEGORY, AnalyzerFactory.get( collectionsList.get(0).getLanguage() ));
		if (categories.length == 1) {
			catQuery = qp.parse(categories[0]);
		} else {
			catQuery = new BooleanQuery();
			for (int i = 0; i < categories.length; i++) {
				((BooleanQuery) catQuery).add(qp.parse(categories[i]), BooleanClause.Occur.SHOULD);
			}
		}
		
		this.query = combineQueries(catQuery, this.query);
		return true;
	}
	
	
	public boolean setCategoryTree( String categoryTree ){
		if ( categoryTree == null || categoryTree.length() == 0 )
			return false;

		Query catTreeQuery = new PrefixQuery(new Term(DocumentWrap.CATEGORYTREE, categoryTree));
		this.query = combineQueries(this.query, catTreeQuery);
		return true;
	}
	
	public void setMinScore( float _score ){
		minScore = _score;
	}
	
	public float getMinScore(){
		return minScore;
	}
	
	public int getMaxRows(){
		return maxRows;
	}
	
	public int getStartRow(){
		return startRow;
	}

	public void setMaxRows(int _maxrows) {
		this.maxRows	= _maxrows;
	}

	public void setStartRow(int _startrow) {
		this.startRow = _startrow;
	}
	
	public void setContentFlag( boolean _bContent ){
		bContent	= _bContent;
	}
	
	public boolean	getContentFlag(){
		return bContent;
	}
	
	public String getUniqueColumn(){
		return uniqueColumn;
	}
	
	public void setUniqueColumn( String _uniqueColumn ){
		if ( _uniqueColumn != null )
			uniqueColumn = _uniqueColumn.toLowerCase();
	}
	
	private Query combineQueries(Query q1, Query q2) {
		if (q1 == null) {
			return q2;
		} else if (q2 == null) {
			return q1;
		} else {
			BooleanQuery combinedQry = new BooleanQuery();
			combinedQry.add(q1, BooleanClause.Occur.MUST);
			combinedQry.add(q2, BooleanClause.Occur.MUST);
			return combinedQry;
		}
	}


	public void setContextPassages(int namedIntParam) {
		ContextPassages = namedIntParam;
	}

	public int getContextPassages(){
		return ContextPassages;
	}

	public void setContextBytes(int namedIntParam) {
		ContextBytes = namedIntParam;
	}
	
	public int getContextBytes(){
		return ContextBytes;
	}

	public void setContext(String namedStringParam, String namedStringParam2) {
		if ( namedStringParam != null )
			ContextHighlightStart = namedStringParam;
		
		if ( namedStringParam2 != null )
			ContextHighlightEnd		= namedStringParam2;
	}
	
	public String getContextHighlightStart(){
		return ContextHighlightStart;
	}
	
	public String getContextHighlightEnd(){
		return ContextHighlightEnd;
	}
	
}
