<cfcomponent>

  <cffunction name="init" access="public" output="false" returntype="void">
    <cfreturn this />
  </cffunction>

	<cfscript>
	public void function init2(){
		return this;
	}
	</cfscript>

  <cffunction name="getBar" access="public" output="false" returntype="string">
    <cfreturn "Bar!" />
  </cffunction>

</cfcomponent>