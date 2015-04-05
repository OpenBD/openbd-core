<cfcomponent extends="openbdtest.common.TestCase">

	<cffunction name="testcfloopArray">
	
		<cfset var arr = ["alan", "andy", "ceri", "jamie"]>
		
		<cfloop array="#arr#" item="item" index="index">
		
			<cfif index == 1 && item != "alan">
				<cfset fail("invalid index")>
			</cfif>
		</cfloop>	
	
	</cffunction>



	<cffunction name="testcfloopArraySimple">
	
		<cfset var arr = ["alan", "andy", "ceri", "jamie"]>
		
		<cfset x = 1>
		<cfloop array="#arr#" index="index">
		
			<cfif x == 1 && index != "alan">
				<cfset fail("invalid index")>
			</cfif>
			
			<cfset x = x + 1>
		</cfloop>	
	
	</cffunction>

</cfcomponent>