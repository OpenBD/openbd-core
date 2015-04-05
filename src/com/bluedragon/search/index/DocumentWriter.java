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
 *  $Id: DocumentWriter.java 2404 2013-09-22 21:51:40Z alan $
 */

package com.bluedragon.search.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;

import com.bluedragon.search.DocumentWrap;


/**
 * This object is designed to be used as a collection point for all the documents, so we send documents
 * to the indexwriter in batches instead of one at a time.   This makes lucene more efficient.
 * 
 * This also lets us run a number of threads when indexing files/web sites
 *  
 */
public class DocumentWriter extends Object {
	private com.bluedragon.search.collection.Collection collection;
	private Collection<Document>	pageCollection;
	
	private int	PAGE_SIZE		= 100;
	
	public DocumentWriter( com.bluedragon.search.collection.Collection collection ){
		this.collection		= collection;
		pageCollection		= new ArrayList<Document>();
	}
	
	
	/**
	 * Adds a new document to the collection 
	 * 
	 * @param docwrap
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public synchronized void add(DocumentWrap docwrap) throws CorruptIndexException, IOException{
		clearExistingDoc(docwrap);
		pageCollection.add( docwrap.getDocument() );

		if ( pageCollection.size() == PAGE_SIZE )
			commit();
	}

	
  private void clearExistingDoc(DocumentWrap docwrap) throws IOException {
  	collection.deleteDocument( docwrap, false );
  }

	

	/**
	 * Commits the collection to the underlying indexwriter 
	 * 
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public synchronized void commit() throws CorruptIndexException, IOException{
		if ( pageCollection.size() == 0 )
			return;
		
		collection.addDocuments( pageCollection );
		
		pageCollection.clear();
	}
}