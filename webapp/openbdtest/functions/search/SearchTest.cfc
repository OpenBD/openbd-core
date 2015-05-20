<cfcomponent extends="openbdtest.common.TestCase">


	<cffunction name="testSearchCollection">

		<cfscript>
		var collection = "testCollection";
		
		// Clear up the old one
		try{
			CollectionDelete( collection );
		}catch(any e){}
		
		// Create a new one
		CollectionCreate( collection=collection, storebody=true );
		
		var q = queryNew("collection, category, id");
		queryAddRow(q);
		querySetCell(q,"collection","1", 1);
		querySetCell(q,"category","img", 1);
		querySetCell(q,"id","108", 1);
		queryAddRow(q);
		querySetCell(q,"collection","2", 2);
		querySetCell(q,"category","doc", 2);
		querySetCell(q,"id","118", 2);
		
		
		
		// Add some elements to it
		CollectionIndexCustom(
		   	collection=collection,
		   	query = q,
			category = "category",
			categoryTree = "id",
			key = "id",
			title = "id",
			body = "id"
		);
		
		// search
		var x = CollectionSearch(
   			collection=collection, 
   			criteria="108" 
		);
		
		assertTrue( x.recordcount == 1 );
		assertTrue( x.key == "108" );
		
		// Clear up the collection
		try{
		//	CollectionDelete( collection );
		}catch(any e){}
		</cfscript>
	</cffunction>
		




	<cffunction name="testSearch">

		<cfscript>
		var collection = "testCollection";
		
		// Clear up the old one
		try{
			CollectionDelete( collection );
		}catch(any e){}
		
		// Create a new one
		CollectionCreate( collection=collection, storebody=true );
		
		// Add some elements to it
		CollectionIndexCustom(
		   	collection=collection, 
   			key="abc1", 
   			body="By training two space telescopes on a supermassive black hole with the mass of a billion Suns, they measured the strength of its ferocious winds.", 
   			title="Title for ABC1" 
		);
		
		CollectionIndexCustom(
		   	collection=collection, 
   			key="abc2", 
   			body="The team also confirmed that these winds blow outwards in every direction, an idea that had been tricky to prove.", 
   			title="Title for ABC2"
		);
		
		
		// search
		var x = CollectionSearch(
   			collection=collection, 
   			criteria="billion" 
		);
		
		assertTrue( x.recordcount == 1 );
		assertTrue( x.key == "abc1" );
		
		
		// Clear up the collection
		try{
			CollectionDelete( collection );
		}catch(any e){}
		</cfscript>
	</cffunction>
		
</cfcomponent>