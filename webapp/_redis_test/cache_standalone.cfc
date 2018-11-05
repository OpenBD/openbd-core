component output="false" name="config.cache" {

	// $Id: $
	// Redis specific cache store setup with regions

	function init() {

		THIS.cache = {
			regions : {
				one : 'one',
				two : 'two',
				three : 'three',
				four : 'four',
				five : 'five',
				six : 'six',
				seven : 'seven',
				eight : 'eight',
				nine : 'nine',
				ten : 'ten'
			},
			props : {
				type : 'redis',
				server : 'redis://localhost:6379', // this is my local docker network url for redis -- change this according to your setup
				waittimeseconds : 5
			}
		};

		// set up the cache regions
		for ( key in THIS.cache.regions ) {

			var cacheprops = structNew();
			cacheprops[key] = structCopy( THIS.cache.props );

			cacheregionnew( region=key, props=cacheprops[key], throwonerror=false );

			// console( 'cache region: ' & key );
			// console( cachegetmetadata(key) );
		}

		return THIS;

	}

}
