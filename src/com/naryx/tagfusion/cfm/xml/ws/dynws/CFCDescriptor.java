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

package com.naryx.tagfusion.cfm.xml.ws.dynws;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfComponentData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfDataFactory;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class CFCDescriptor {
	private cfStructData md = null;
	private cfStructData[] fxns = null;
	private cfArrayData[] parms = null;
	private cfStructData[] props = null;
	private cfSession session = null;

	public CFCDescriptor(cfStructData md, cfSession sess) {
		this.md = md;
		this.session = sess;
		init();
	}

	protected void init() {
		cfStructData localMd = null;
		List prevParents = new LinkedList();

		// Get the functions, parameters
		localMd = this.md;
		prevParents.clear();
		Map fMap = new FastMap();
		while (localMd != null) {
			cfStructData f = null;
			cfArrayData tmp = null;
			cfStringData axs = null;
			cfData fName = null;
			cfData fReturnType = null;
			cfArrayData allFxns = (cfArrayData) localMd.getData("FUNCTIONS");
			if (allFxns != null) {
				for (int i = 0; i < allFxns.size(); i++) {
					f = (cfStructData) allFxns.getElement(i + 1);
					axs = (cfStringData) f.getData("ACCESS");
					fName = f.getData("NAME");
					fReturnType = f.getData("RETURNTYPE");
					if (axs != null && axs.toString().toLowerCase().equalsIgnoreCase("remote") && fName != null && !fMap.containsKey(fName.toString())) {
						// If the function doesn't specify a returnType then throw an
						// exception. Fix for bug NA#2934.
						if (fReturnType == null)
							throw new IllegalStateException("The function '" + fName + "' must specify a returnType attribute.");
						tmp = (cfArrayData) f.getData("PARAMETERS");
						if (tmp == null)
							tmp = cfArrayData.createArray(1);
						fMap.put(fName.toString(), new Object[] { f, tmp });
					}
				}
			}
			localMd = getSuperMetaData(localMd, prevParents);
		}
		fxns = new cfStructData[fMap.size()];
		parms = new cfArrayData[fMap.size()];
		Iterator itr = fMap.values().iterator();
		for (int i = 0; itr.hasNext(); i++) {
			Object[] fp = (Object[]) itr.next();
			fxns[i] = (cfStructData) fp[0];
			parms[i] = (cfArrayData) fp[1];
		}

		// Get the properties
		localMd = this.md;
		prevParents.clear();
		Map prMap = new HashMap();
		while (localMd != null) {
			cfStructData pr = null;
			cfArrayData allProps = (cfArrayData) localMd.getData("PROPERTIES");
			if (allProps != null) {
				for (int i = 0; i < allProps.size(); i++) {
					pr = (cfStructData) allProps.getElement(i + 1);
					cfData prName = pr.getData("NAME");
					if (prName != null && !prMap.containsKey(prName.toString()))
						prMap.put(prName.toString(), pr);
				}
			}
			localMd = getSuperMetaData(localMd, prevParents);
		}
		props = (cfStructData[]) prMap.values().toArray(new cfStructData[prMap.values().size()]);
	}

	protected cfStructData getSuperMetaData(cfStructData localMd, List prevParents) {
		cfStructData smd = (cfStructData) localMd.getData("EXTENDS");
		if (smd != null) {
			if (!prevParents.contains(smd.getData("NAME").toString())) {
				prevParents.add(smd.getData("NAME").toString());
				return smd;
			}
		}
		return null;
	}

	public File getFile() {
		return new File(md.getData("PATH").toString());
	}

	public String getPath() {
		return md.getData("PATH").toString();
	}

	public String getName() {
		return md.getData("NAME").toString();
	}

	public String getDescription() {
		if (md.getData("HINT") != null)
			return md.getData("HINT").toString();
		else
			return null;
	}

	public int getPropertyCount() {
		return props.length;
	}

	public String getPropertyName(int ndx) {
		cfStructData pr = getProperty(ndx);
		if (pr != null && pr.getData("NAME") != null)
			return pr.getData("NAME").toString();
		else
			return null;
	}

	public String getPropertyDescription(int ndx) {
		cfStructData pr = getProperty(ndx);
		if (pr != null && pr.getData("HINT") != null)
			return pr.getData("HINT").toString();
		else
			return null;
	}

	public String getPropertyType(int ndx, DynamicWebServiceTypeGenerator gen) {
		cfStructData pr = getProperty(ndx);
		if (pr != null && pr.getData("TYPE") != null)
			return findType(pr.getData("TYPE").toString().trim(), gen);
		else
			return null;
	}

	public int getFunctionCount() {
		return fxns.length;
	}

	public String getFunctionName(int ndx) {
		cfStructData f = getFunction(ndx);
		if (f != null && f.getData("NAME") != null)
			return f.getData("NAME").toString();
		else
			return null;
	}

	public String getFunctionDescription(int ndx) {
		cfStructData f = getFunction(ndx);
		if (f != null && f.getData("HINT") != null)
			return f.getData("HINT").toString();
		else
			return null;
	}

	public String getFunctionReturnType(int ndx, DynamicWebServiceTypeGenerator gen) {
		cfStructData f = getFunction(ndx);
		if (f != null && f.getData("RETURNTYPE") != null)
			return findType(f.getData("RETURNTYPE").toString().trim(), gen);
		else
			return null;
	}

	public int getFunctionParameterCount(int ndx) {
		cfArrayData parms = getParameters(ndx);
		if (parms != null)
			return parms.size();
		else
			return 0;
	}

	public String getFunctionParameterName(int fxnNdx, int parmNdx) {
		cfStructData p = getParameter(fxnNdx, parmNdx);
		if (p != null && p.getData("NAME") != null)
			return p.getData("NAME").toString();
		else
			return null;
	}

	public String getFunctionParameterType(int fxnNdx, int parmNdx, DynamicWebServiceTypeGenerator gen) {
		cfStructData p = getParameter(fxnNdx, parmNdx);
		if (p != null && p.getData("TYPE") != null)
			return findType(p.getData("TYPE").toString().trim(), gen);
		else
			return null;
	}

	protected cfStructData getFunction(int ndx) {
		if (ndx < fxns.length)
			return fxns[ndx];
		else
			return null;
	}

	protected cfArrayData getParameters(int ndx) {
		if (ndx < parms.length)
			return parms[ndx];
		else
			return null;
	}

	protected cfStructData getParameter(int ndx1, int ndx2) {
		cfArrayData parmsArray = getParameters(ndx1);
		if (parmsArray != null && ndx2 < parmsArray.size())
			return (cfStructData) parmsArray.getElement(ndx2 + 1);
		else
			return null;
	}

	protected cfStructData getProperty(int ndx) {
		if (ndx < props.length)
			return props[ndx];
		else
			return null;
	}

	protected String findType(String rawType, DynamicWebServiceTypeGenerator gen) {
		// Try a standard type
		Class typ = cfDataFactory.getDatatypeJavaClass(rawType);
		if (typ != null) {
			if (typ.isArray())
				return typ.getComponentType().getName() + "[]";
			else
				return typ.getName();
		}
		// Try a CFC type
		String cfcT = findCFCType(rawType, gen);
		if (cfcT != null)
			return cfcT;

		// Handle void types
		if (rawType.equalsIgnoreCase("void"))
			return rawType;

		// We don't know this type, so...
		throw new IllegalArgumentException("Unrecognized type encountered: " + rawType);
	}

	protected String findCFCType(String rawType, DynamicWebServiceTypeGenerator gen) {
		cfComponentData cfc = null;
		String nsLocal = null;
		String fullyQual = rawType;
		if (rawType.indexOf(".") == -1)
			nsLocal = getName().substring(0, getName().lastIndexOf(".") + 1) + rawType;

		// Try ns local if we have it
		if (nsLocal != null) {
			try {
				cfc = new cfComponentData(session, nsLocal);
			} catch (cfmRunTimeException ex) {
			}
		}
		if (cfc != null)
			return genCFCType(cfc, gen);

		// Try fully qualified
		try {
			cfc = new cfComponentData(session, fullyQual);
		} catch (cfmRunTimeException ex) {
		}
		if (cfc != null)
			return genCFCType(cfc, gen);
		return null;
	}

	protected String genCFCType(cfComponentData cfc, DynamicWebServiceTypeGenerator gen) {
		String type = cfc.getMetaData().getData("NAME").toString().trim();
		String full = DynamicWebServiceTypeGenerator.getFQName(type);
		String knownType = gen.getKnownType(full);
		if (knownType == null)
			gen.addType(new CFCDescriptor(cfc.getMetaData(), session));
		else
			full = knownType;
		return full;
	}
}
