component output=false {

	THIS.name = 'redis-check';

	ConsoleOutput(true);

	VARIABLES.cacheSetup = new cache_standalone().cache;

	THIS.sessionmanagement = true;
	THIS.sessiontimeout = CreateTimeSpan(0,2,0,0);
	THIS.sessionstorage = cacheSetup.props.server;

	function onApplicationStart() {

		APPLICATION.cache = VARIABLES.cacheSetup;
	}

	function onRequestStart( pageuri ) {

		if ( !isdefined('APPLICATION.cache') ) {
			APPLICATION.cache = VARIABLES.cacheSetup;
		}
	}

}
