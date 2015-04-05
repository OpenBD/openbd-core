/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  $Id: CollectionListCategoryFunction.java 1638 2011-07-31 16:08:50Z alan $
 */


package com.bluedragon.search.collection;

import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.search.IndexSearcher;

import com.bluedragon.search.DocumentWrap;
import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


public class CollectionListCategoryFunction extends functionBase {
	private static final long	serialVersionUID	= 1L;

	public CollectionListCategoryFunction(){
		min = 1;
		max = 1;
		setNamedParams( new String[]{ "collection" } );
	}
	
	
  public String[] getParamInfo(){
		return new String[]{
			"the name of the collection"
		};
  }
	
  
	public java.util.Map getInfo(){
		return makeInfo(
				"search", 
				"Lists all the categories this collection uses and the counts for each one.  A struct with all the counts are returned", 
				ReturnType.STRUCTURE );
	}

	
	public cfData execute(cfSession _session, cfArgStructData argStruct) throws cfmRunTimeException {
		String name	= getNamedStringParam(argStruct, "collection", null );
		if ( name == null )
			throwException(_session, "please specifiy the 'collection' attribute" );
		
		if ( !CollectionFactory.isCollection(name) )
			throwException(_session, "collection, " + name + ", does not exist");
		
		Collection col	= CollectionFactory.getCollection(name);

		// Collection up the stats
		Map<String, CountWrapper> catList 		= new FastMap<String, CountWrapper>();
		Map<String, CountWrapper> catTreeList = new FastMap<String, CountWrapper>();

		try{

			// Run around all the documents counting up the categories
			IndexSearcher indexsearcher = col.getIndexSearcher();
			for ( int d=0; d < col.getTotalDocs(); d++ ){
				DocumentWrap	docWrap	= new DocumentWrap( indexsearcher.doc(d) );
				
				String [] categories = docWrap.getCategories();
				if ( categories != null ){
					for ( int c = 0; c < categories.length; c++ ){
						incrementCount( catList, categories[c] );
					}
				}

				String catTree = docWrap.getCategoryTree();
				if ( catTree != null )
					incrementCount( catTreeList, catTree );
			}
			
			// divide the categories trees into subcategory tree counts
			Object [] keys = catTreeList.keySet().toArray();
			for ( int k = 0; k < keys.length; k++ ){
				String nextCatTree = keys[k].toString();
				int indx = nextCatTree.indexOf( '/' );
				while ( indx != -1 && indx != (nextCatTree.length()+1) ){
					String subTree = nextCatTree.substring( 0, indx+1 );
					incrementCount( catTreeList, subTree );
					indx = nextCatTree.indexOf( '/', indx+1 );
				}
			}
		
			// create cfStructDatas from the results
			cfStructData cats = new cfStructData();
			Iterator<String> catsIterator = catList.keySet().iterator();
			while ( catsIterator.hasNext() ){
				String nextCat = catsIterator.next();
				cats.setData(  nextCat, new cfNumberData( catList.get( nextCat ).value ) );
			}
			
			// Collect up the categorytree counts
			cfStructData catTrees = new cfStructData();
			Iterator<String> catTreesIterator = catTreeList.keySet().iterator();
			while ( catTreesIterator.hasNext() ){
				String nextCatTree = catTreesIterator.next();
				catTrees.setData(  nextCatTree, new cfNumberData( catTreeList.get( nextCatTree ).value ) );
			}

			cfStructData catListResult = new cfStructData();
			catListResult.setData( "CATEGORIES", cats );
			catListResult.setData( "CATEGORYTREES", catTrees );

			return catListResult;
			
		}catch(Exception ioe){
			throwException(_session, ioe.getMessage() );
		}
		
		return cfBooleanData.TRUE;
	}
	
	private void incrementCount( Map<String, CountWrapper> _map, String _key ){
		CountWrapper count = _map.get( _key );
		if ( count == null ){
			count = new CountWrapper();
			count.value = 1;
			_map.put( _key, count );
		}else{
			count.value++;
		}
	}

	private class CountWrapper{
		public int value = 0;
	}
}
