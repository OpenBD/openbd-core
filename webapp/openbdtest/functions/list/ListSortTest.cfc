<cfcomponent extends="openbdtest.common.TestCase">
	<cffunction name="testNamingListSort">

		<cfset assertEquals("b,c,e,f,z,z" , ListSort("C,B,Z,z,f,e","textnocase","asc"))>
		<cfset assertEquals("b,c,e,f,z,z" , ListSort(list="C,B,Z,z,f,e",type="textnocase",order="asc"))>
		<cfset assertEquals("b,c,e,f,z,z" , ListSort(list="C,B,Z,z,f,e",order="asc",type="textnocase"))>
		<cfset assertEquals("b,c,e,f,z,z" , ListSort(type="textnocase",list="C,B,Z,z,f,e",order="asc"))>
		<cfset assertEquals("b,c,e,f,z,z" , ListSort(type="textnocase",order="asc",list="C,B,Z,z,f,e"))>
		<cfset assertEquals("b,c,e,f,z,z" , ListSort(order="asc",type="textnocase",list="C,B,Z,z,f,e"))>
		<cfset assertEquals("b,c,e,f,z,z" , ListSort(order="asc",list="C,B,Z,z,f,e",type="textnocase"))>

		<cfset assertEquals("b;c;e;f;z;z" , ListSort("C;B;Z;z;f;e","textnocase","asc",";"))>
		<cfset assertEquals("b;c;e;f;z;z" , ListSort(list="C;B;Z;z;f;e",type="textnocase",order="asc",delimiter=";"))>
		<cfset assertEquals("b;c;e;f;z;z" , ListSort(list="C;B;Z;z;f;e",type="textnocase",delimiter=";",order="asc"))>
		<cfset assertEquals("b;c;e;f;z;z" , ListSort(list="C;B;Z;z;f;e",type="textnocase",order="asc",delimiter=";"))>
		<cfset assertEquals("b;c;e;f;z;z" , ListSort(list="C;B;Z;z;f;e",order="asc",type="textnocase",delimiter=";"))>
		<cfset assertEquals("b;c;e;f;z;z" , ListSort(list="C;B;Z;z;f;e",order="asc",delimiter=";",type="textnocase"))>
		<cfset assertEquals("b;c;e;f;z;z" , ListSort(list="C;B;Z;z;f;e",type="textnocase",order="asc",delimiter=";"))>
		<cfset assertEquals("b;c;e;f;z;z" , ListSort(list="C;B;Z;z;f;e",type="textnocase",delimiter=";",order="asc"))>

		<cfset assertEquals("C,B,Z,z,f,e" , ListSort("C,B,Z,z,f,e","textnocase","asc",";"))>
		<cfset assertEquals("C,B,Z,z,f,e" , ListSort(list="C,B,Z,z,f,e",type="textnocase",order="asc",delimiter=";"))>

		<cfset assertEquals("z,z,f,e,c,b" , ListSort("C,B,Z,z,f,e","textnocase","desc"))>
		<cfset assertEquals("z,z,f,e,c,b" , ListSort(list="C,B,Z,z,f,e",type="textnocase",order="desc"))>

		<cfset assertEquals("B,C,Z,e,f,z" , ListSort("C,B,Z,z,f,e","text","asc"))>
		<cfset assertEquals("B,C,Z,e,f,z" , ListSort(list="C,B,Z,z,f,e",type="text",order="asc"))>

		<cfset assertEquals("z,f,e,Z,C,B" , ListSort("C,B,Z,z,f,e","text","desc"))>
		<cfset assertEquals("z,f,e,Z,C,B" , ListSort(list="C,B,Z,z,f,e",type="text",order="desc"))>

		<cfset assertEquals("8=6=4=2=1=1=0" , ListSort("1=2=8=0=6=4=1","numeric","desc","="))>
		<cfset assertEquals("8=6=4=2=1=1=0" , ListSort(list="1=2=8=0=6=4=1",type="numeric",order="desc",delimiter="="))>

	</cffunction>

</cfcomponent>



