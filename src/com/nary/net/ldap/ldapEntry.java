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

package com.nary.net.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;

/**
 * ldapEntry
 * 
 * A class for creating a directory entry for addition into the directory tree.
 * Used by ldapConnection.
 */

class ldapEntry implements DirContext {

	BasicAttributes attributes;

	ldapEntry(Attributes _attributes) {
		attributes = (BasicAttributes) _attributes;

	}// ldap()

	public Attributes getAttributes(String _name) throws NamingException {
		if (!_name.equals("")) {
			throw new NameNotFoundException();
		}
		return attributes;
	}// getAttributes()

	public Attributes getAttributes(Name _name) throws NamingException {
		return getAttributes(_name.toString());
	}// getAttributes()

	public Attributes getAttributes(String _name, String[] _ids) throws NamingException {
		if (!_name.equals("")) {
			throw new NameNotFoundException();
		}

		Attributes answer = new BasicAttributes(true);
		Attribute target;
		for (int i = 0; i < _ids.length; i++) {
			target = attributes.get(_ids[i]);
			if (target != null) {
				answer.put(target);
			}
		}
		return answer;
	}// getAttributes()

	public Attributes getAttributes(Name _name, String[] _ids) throws NamingException {
		return getAttributes(_name.toString(), _ids);
	}// getAttributes()

	public String toString() {
		return "blah";
	}

	public String getNameInNamespace() throws NamingException {
		throw new OperationNotSupportedException();
	}// getNameInNamespace()

	public Object lookup(Name _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// lookup()

	public Object lookup(String _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// lookup

	public void bind(Name _name, Object _obj) throws NamingException {
		throw new OperationNotSupportedException();
	}// bind

	public void bind(String _name, Object _obj) throws NamingException {
		throw new OperationNotSupportedException();
	}// bind()

	public void rebind(Name _name, Object _obj) throws NamingException {
		throw new OperationNotSupportedException();
	}// rebind()

	public void rebind(String _name, Object _obj) throws NamingException {
		throw new OperationNotSupportedException();
	}// rebind()

	public void unbind(Name _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// unbind()

	public void unbind(String _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// unbind()

	public void rename(Name _oldName, Name _newName) throws NamingException {
		throw new OperationNotSupportedException();
	}// rename()

	public void rename(String _oldName, String _newName) throws NamingException {
		throw new OperationNotSupportedException();
	}// rename()

	public NamingEnumeration list(Name _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// list()

	public NamingEnumeration list(String _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// list()

	public NamingEnumeration listBindings(Name _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// listBindings()

	public NamingEnumeration listBindings(String _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// listBindings()

	public void destroySubcontext(Name _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// destroySubcontext()

	public void destroySubcontext(String _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// destroySubcontext()

	public Context createSubcontext(Name _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// createSubcontext()

	public Context createSubcontext(String _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// createSubcontext()

	public Object lookupLink(Name _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// lookupLink()

	public Object lookupLink(String _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// lookupLink()

	public NameParser getNameParser(Name _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// getNameParser()

	public NameParser getNameParser(String _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// getNameParser()

	public String composeName(String _name, String _prefix) throws NamingException {
		throw new OperationNotSupportedException();
	}// composeName()

	public Name composeName(Name _name, Name _prefix) throws NamingException {
		throw new OperationNotSupportedException();
	}// composeName()

	public Object addToEnvironment(String _propName, Object _propVal) throws NamingException {
		throw new OperationNotSupportedException();
	}// addToEnvironment()

	public Object removeFromEnvironment(String _propName) throws NamingException {
		throw new OperationNotSupportedException();
	}// removeFromEnvironment()

	public Hashtable getEnvironment() throws NamingException {
		throw new OperationNotSupportedException();
	}// getEnvironment()

	public void close() throws NamingException {
		throw new OperationNotSupportedException();
	}// close()

	// -- DirContext
	public void modifyAttributes(Name _name, int _mod_op, Attributes _attrs) throws NamingException {
		throw new OperationNotSupportedException();
	}// modifyAttributes()

	public void modifyAttributes(String _name, int _mod_op, Attributes _attrs) throws NamingException {
		throw new OperationNotSupportedException();
	}// modifyAttributes()

	public void modifyAttributes(Name _name, ModificationItem[] _mods) throws NamingException {
		throw new OperationNotSupportedException();
	}// modifyAttributes()

	public void modifyAttributes(String _name, ModificationItem[] _mods) throws NamingException {
		throw new OperationNotSupportedException();
	}// modifyAttributes()

	public void bind(Name _name, Object _obj, Attributes _attrs) throws NamingException {
		throw new OperationNotSupportedException();
	}// bind()

	public void bind(String _name, Object _obj, Attributes _attrs) throws NamingException {
		throw new OperationNotSupportedException();
	}// bind()

	public void rebind(Name _name, Object _obj, Attributes _attrs) throws NamingException {
		throw new OperationNotSupportedException();
	}// rebind()

	public void rebind(String _name, Object _obj, Attributes _attrs) throws NamingException {
		throw new OperationNotSupportedException();
	}// rebind()

	public DirContext createSubcontext(Name name, Attributes attrs) throws NamingException {
		throw new OperationNotSupportedException();
	}// createSubcontext()

	public DirContext createSubcontext(String name, Attributes attrs) throws NamingException {
		throw new OperationNotSupportedException();
	}// createSubcontext()

	public DirContext getSchema(Name _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// getSchema()

	public DirContext getSchema(String _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// getSchema()

	public DirContext getSchemaClassDefinition(Name _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// getSchemaClassDefinition()

	public DirContext getSchemaClassDefinition(String _name) throws NamingException {
		throw new OperationNotSupportedException();
	}// getSchemaClassDefinition()

	public NamingEnumeration search(Name _name, Attributes _matchingAttributes, String[] _attributesToReturn) throws NamingException {
		throw new OperationNotSupportedException();
	}// search()

	public NamingEnumeration search(String _name, Attributes _matchingAttributes, String[] _attributesToReturn) throws NamingException {
		throw new OperationNotSupportedException();
	}// search()

	public NamingEnumeration search(Name _name, Attributes _matchingAttributes) throws NamingException {
		throw new OperationNotSupportedException();
	}// search()

	public NamingEnumeration search(String _name, Attributes _matchingAttributes) throws NamingException {
		throw new OperationNotSupportedException();
	}// search()

	public NamingEnumeration search(Name _name, String _filter, SearchControls _cons) throws NamingException {
		throw new OperationNotSupportedException();
	}// search()

	public NamingEnumeration search(String _name, String _filter, SearchControls _cons) throws NamingException {
		throw new OperationNotSupportedException();
	}// search()

	public NamingEnumeration search(Name _name, String _filterExpr, Object[] _filterArgs, SearchControls _cons) throws NamingException {
		throw new OperationNotSupportedException();
	}// search()

	public NamingEnumeration search(String _name, String _filterExpr, Object[] _filterArgs, SearchControls _cons) throws NamingException {
		throw new OperationNotSupportedException();
	}// search()

	public static void main(String[] args) {
		System.out.println("Hello World!");
	}

}// ldapEntry

