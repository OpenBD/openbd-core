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

/*
 * Created on 18-Feb-2005
 *
 */
package com.naryx.tagfusion.cfm.sql.platform.java;

import java.io.Serializable;
import java.util.List;

import com.naryx.tagfusion.cfm.sql.preparedData;

public class queryBatchSQL  implements Serializable {
  static final long serialVersionUID = 1;

  private String sqlString;
  private String datasourceName;
  private String datasourceUser;
  private String datasourcePass;
  private List<preparedData>	 queryParams;
  
  
  public static long getSerialVersionUID() {
    return serialVersionUID;
  }
  public String getDatasourceName() {
    return datasourceName;
  }
  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;
  }
  public String getDatasourcePass() {
    return datasourcePass;
  }
  public void setDatasourcePass(String datasourcePass) {
    this.datasourcePass = datasourcePass;
  }
  public String getDatasourceUser() {
    return datasourceUser;
  }
  public void setDatasourceUser(String datasourceUser) {
    this.datasourceUser = datasourceUser;
  }
  public List<preparedData> getQueryParams() {
    return queryParams;
  }
  public void setQueryParams(List<preparedData> queryParams) {
    this.queryParams = queryParams;
  }
  public String getSqlString() {
    return sqlString;
  }
  public void setSqlString(String sqlString) {
    this.sqlString = sqlString;
  }
}
