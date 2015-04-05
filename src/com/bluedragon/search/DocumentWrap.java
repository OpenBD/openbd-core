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
 *  http://openbd.org/
 *  
 *  $Id: DocumentWrap.java 2404 2013-09-22 21:51:40Z alan $
 */

package com.bluedragon.search;

import java.io.File;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;


/**
 * This is a loose wrapper to the lucene document.  Its primary purpose is to ensure all the keys
 * that CFML requires are consistent.  This class stores very little data, with all the real field
 * data being stored in the actual Lucene document.
 * 
 * This controls which fields are we are passing through the analyzer and also which ones we are storing in full
 * 
 */
public class DocumentWrap {

	public static final String	ID						= "id";
	public static final String	URL						= "url";
	public static final String	CONTENTS			= "contents";
	public static final String	SUMMARY				= "summary";
	public static final String	NAME					= "name";
	public static final String	AUTHOR				= "author";
	public static final String	TYPE					= "type";
	public static final String	SIZE					= "size";
	public static final String	CATEGORY			= "category";
	public static final String	CATEGORYTREE	= "categorytree";

	private Document	doc						= null;
	private float			score					= 0f;

	
	/**
	 * Used for create new documents for inserting into an index.  The lucene document is created
	 * to be used with the setXXX() methods for setting the various fields
	 */
	public DocumentWrap(){
		doc	= new Document();
	}

	
	/**
	 * Used when retrieving results from a search
	 * @param doc
	 * @throws Exception
	 */
	public DocumentWrap(Document doc) throws Exception {
		this.doc = doc;
		checkValidity();
	}

	
	/**
	 * Makes sure the minimum set of keys are set for this document.  used in both indexing and the creation of documents
	 * @throws Exception
	 */
	public void checkValidity() throws Exception {
		if (doc.getField(ID) == null)
			throw new Exception("Invalid Document, missing " + ID + " field.");
	}
	
	
	/**
	 * Returns the underlying Lucene document
	 * @return
	 */
	public Document getDocument(){
		return doc;
	}



	public String getId() {
		return get(ID);
	}

	public void setId( String id ){
		id = id.replace(File.separatorChar, '/');
    doc.add( new StringField( ID, id, Field.Store.YES) );
	}

	public String getAuthor(){
		return get(AUTHOR);
	}

	public void setAuthor( String author ){
		if ( author != null && author.length() > 0 )
			doc.add( new StringField( AUTHOR, author, Field.Store.YES) );
	}

	public String getType(){
		return get(TYPE);
	}

	public void setType( String type ){
		if ( type != null )
			doc.add( new StringField( TYPE, type, Field.Store.YES) );
	}

	public String getSize() {
		return get(SIZE);
	}

	public void setSize( int size ){
		if ( size > 0 )
			doc.add( new IntField( SIZE, size, Field.Store.YES) );
	}

	public String getURL() {
		return get(URL);
	}

	public void setURL( String url ){
		if ( url != null )
			doc.add( new StringField( URL, url, Field.Store.YES ) );
	}

	public void deleteField( String name ){
		doc.removeField(name);
	}
	
	public String getName(){
		return get(NAME);
	}

	public void setName( String name ){
		if ( name != null && name.length() > 0 )
			doc.add( new StringField( NAME, name, Field.Store.YES) );
	}

	public String getSummary() {
		return get(SUMMARY);
	}

	public void setSummary( String summary ){
		if ( summary == null || summary.length() > 0 ) return;
		
    int summaryLen = ( summary.length() >= 256 ? 256 : summary.length() );
    doc.add( new StoredField( SUMMARY, summary.substring(0,summaryLen) ) );
	}

	public String getCategoryTree(){
		return get(CATEGORYTREE);
	}

	public void setCategoryTree( String categorytree ){
		if ( categorytree != null )
			doc.add( new TextField( CATEGORYTREE, categorytree, Field.Store.YES) );
	}

	public String[] getCategories(){
		return doc.getValues(CATEGORY);
	}

	public void setCategories( String[] categories ){
		for ( int x=0; x < categories.length; x++ ){
			if ( categories[x].trim().length() > 0 )
				doc.add( new TextField( CATEGORY, categories[x].trim(), Field.Store.YES) );
		}
	}

	public String getAttribute(String attribName){
		return get(attribName);
	}

	public void setAttribute( String name, String value ){
		if ( value != null && value.trim().length() > 0 )
			doc.add( new TextField( name.toLowerCase(), value.trim(), Field.Store.YES) );
	}
	
	public void setContent( String value, boolean bStored ){
		doc.add( new TextField( CONTENTS, value, bStored ? Field.Store.YES : Field.Store.NO) );
	}

	protected String get(String key) {
		String rtn = null;
		if (doc != null)
			rtn = doc.get(key);
		return rtn;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}
}