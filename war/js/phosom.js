
var g_allAPIsLoaded;
var g_activeUser;
var g_activeGame;
var g_pageAfterLogin;

$( document ).ready(function(){
	
    var isLocalStorage;
    try {
        isLocalStorage = ('localStorage' in window && window.localStorage !== null);
    } catch (e) {
        isLocalStorage = false;
    }
    var userStorageKey = 'phosomUser';
    function getUserFromLocalStorage() {
    	var userData = undefined;
    	if( isLocalStorage ) {
    		var userString = localStorage[userStorageKey];
    		if( userString) {
    			userData = JSON.parse( userString );
    		}
    	}
    	return userData;
    }
    function saveUserToLocalStorage( userData ) {
    	if( isLocalStorage ) {
    		localStorage[userStorageKey] = JSON.stringify( userData );
    	}
    }
    
    g_activeUser = getUserFromLocalStorage();
    
    // TODO: move game behaviour into it's own object / enclosure!
    
	
    function navigateToPageOrLoginIfNeeded( pageId ) {
		if( g_activeUser ) {
			$.mobile.changePage( '#'+pageId );
		} else {
			g_pageAfterLogin = pageId;
			$.mobile.changePage('#phosom-get-user');
		}    	
    }
    
    function getFileNameFromURL( url ) {
    	return url.substring(url.lastIndexOf('/')+1, url.length)
    }
    
	
    
    // button events
	
	$('#btn-create-game').click(function(){
		
		$( '#phosom-game-creation' ).data( 'game-type', 'autoChallenge' );
		
		navigateToPageOrLoginIfNeeded( 'phosom-game-creation' );
	});
	
	$('#btn-game-overview').click(function(){
		
		navigateToPageOrLoginIfNeeded( 'phosom-challenges-overview' );
	});
	
	
	$('#login').submit(function(){
		$.mobile.loading( 'show', { text: 'Going...', textVisible:true});
		
		gapi.client.playerfactory.createPlayerWithName(
				{'name':$('#name').val()}).execute(function(resp){
			
			g_activeUser = resp;
			saveUserToLocalStorage(g_activeUser);
			console.log(resp);
			
			$.mobile.changePage( '#'+g_pageAfterLogin );
		});
		
		return false;
	});
	
	$('#respond-with-url').submit(function(){
		$.mobile.loading( 'show', { text: 'Sending...', textVisible:true});
		
		gapi.client.autoChallengeGameService.respondToChallengeWithUrl({
			'gameId':g_activeGame.key.id,
			'playerId':g_activeUser.key.id,
			'url':$('#challenge-response-with-url').val()
		}).execute(function(respUrlSent){
			
			console.log(respUrlSent);
			
			$.mobile.changePage( '#phosom-challenge-result' );
		});
		
		return false;
	});
	
	function setCurrentGameIdFromChallengeLink(event, ui) {
		event.preventDefault();
		gapi.client.autochallengegameendpoint.getAutoChallengeGame({
			'id': $(this).data('gameid')
		}).execute(function(game){
			
			g_activeGame = game;
			
			$.mobile.changePage( '#phosom-challenge-result' );
		});
	}
	
	
	// page events
	
	$( "div#phosom-one-challenge" ).on( "pagebeforeshow", function( event, ui ) {
		//$(this).find('h2').html(g_activeUser.playerScreenName + ", here's your challenge!");
	});
	

	$( "div#phosom-index" ).on( "pageshow", function( event, ui ) {
		// let's have the buttons disabled until all APIs have loaded
		if( ! g_allAPIsLoaded ) {
			$(this).find('[type="submit"], [type="button"]').each(function(){
				$(this).button('disable');
				$(this).button('refresh');
			});
		}
	});
	
	$(document).on(
			'pagehide', 
			'#phosom-game-creation, #phosom-one-challenge, #phosom-challenge-result, #phosom-challenges-overview', 
			function(){ 
	    $(this).find('[data-role="content"]').empty();
	});
	
	$( "div#phosom-game-creation" ).on( "pageshow", function( event, ui ) {
		$.mobile.loading( 'show', { text: 'Creating a game...', textVisible:true});
		
//		switch( $( '#phosom-game-creation' ).data('game-type') ) {
//			case "autoChallenge":
				// create a game with automatic challenge photo
				gapi.client.autoChallengeGameService.createGame().execute(function(resp){
					
					g_activeGame = resp;
					console.log(resp);
					
					gapi.client.autoChallengeGameService.addPlayerToGame({
						'gameId':g_activeGame.key.id,
						'playerId':g_activeUser.key.id
					}).execute(function(playerAddedResp){
						
						console.log(playerAddedResp);
						
						$.mobile.loading( 'hide' );
						$.mobile.changePage('#phosom-one-challenge');
					});
				});
//				break;
//				
//			default:
//				break;
//		}
	});
	

	$( "div#phosom-one-challenge" ).on( "pagebeforeshow", function( event, ui ) {
		
		var $content = $( 'div#phosom-one-challenge div[data-role="content"]' );
		
		$content.prepend( $('<h2/>').text('Game # ' + g_activeGame.key.id + " - " +g_activeUser.playerScreenName+ ", here's your challenge!") );
		$content.append( $('<a/>', {
			'href':'#phosom-challenge-response', 
			'data-role':'button',
			'text':'Respond to it!'}) );
		
		$.mobile.loading( 'show', { text: 'Fetching the challenge...', textVisible:true});
		gapi.client.gameService.getChallengePhotoUrl({
			'bucket':'auto-challenge-photos', 
			'filename': getFileNameFromURL( g_activeGame.challengeUrl ),
			'size':$content.parent().width() - 20
		}).execute(function(urlResp){
		
			console.log(urlResp);

			$content.append( $('<img/>',{'src':urlResp.challengePhotoUrl}) );
			$.mobile.loading( 'hide' );
		});
		$content.trigger('create');
	});
	
	$( "div#phosom-challenge-response" ).on( "pagebeforeshow", function( event, ui ) {
		var $content = $(this).find( 'div[data-role="content"]' );
		$content.find('#challenge-response-with-url').val('');
	});
	
	$( "div#phosom-challenge-result" ).on( "pageshow", function( event, ui ) {
		var $content = $(this).find( 'div[data-role="content"]' );
		$.mobile.loading( 'show', { text: 'Getting grades...', textVisible:true});
		
		gapi.client.autoChallengeGameService.getChallengeAndResponseInfo({
			'gameId':g_activeGame.key.id,
			'playerId':g_activeUser.key.id,
			'size':Math.round($content.parent().width() / 2.2)
		}).execute(function(challengesInfo){
			
			console.log(challengesInfo);
			
			$content.append( $('<h2/>').text('Game # ' + g_activeGame.key.id + ' - results!') );
			
			var $listview = $('<ul>', {'data-role':'listview', 'data-inset':'true'});
			$.each( challengesInfo.items, function(index, oneChallenge){
				var $oneLI = $('<li/>');
				var $oneDIV = $('<div/>');
				
				$oneDIV.append( $('<img/>',{
					'src':oneChallenge.challengePhotoUrl, 'style':'padding:5px;' }) );
				$oneDIV.append( $('<img/>',{
					'src':oneChallenge.responsePhotoUrl, 'style':'padding:5px;'}) );
				$oneDIV.append( $('<h3/>',{'text':'Grade: ' + oneChallenge.score}) );
				
				$oneLI.append( $oneDIV );
				$listview.append( $oneLI );
			});
			
			$content.append( $listview );

			$content.append( $('<a/>', {
				'href':'#phosom-game-creation', 'data-role':'button', 'data-theme':'b', 
				'text':'Have another go!'}) );
			$content.append( $('<a/>', {
				'href':'#phosom-index', 'data-role':'button', 'text':'Go home...'}) );
			
			$.mobile.loading( 'hide' );
			
			$content.trigger('create');
		});
	});
	
	$( "div#phosom-challenges-overview" ).on( "pageshow", function( event, ui ) {
		$.mobile.loading( 'show', { text: 'Fetching challenges...', textVisible:true});
		
		var $content = $(this).find( 'div[data-role="content"]' );
		
		var $listview = $('<ul>', {'data-role':'listview', 'data-inset':'true'});
		
		gapi.client.autoChallengeGameService.listChallengesPlayedByPlayer({
			'playerId':g_activeUser.key.id
		}).execute(function(challengesInfo){
			
			console.log(challengesInfo);
			
			$.each( challengesInfo.items, function(index, challenge){
				var $oneLI = $('<li/>');
				var $oneAnchor = $('<a/>', {
					'href': '#phosom-challenge-result', 'data-gameid': challenge.parentGameId
					}).on('click', setCurrentGameIdFromChallengeLink);
				$oneAnchor.append( $('<img/>', {'src': challenge.challengePhotoUrl}) );
				$oneAnchor.append( $('<h2/>', {'text': challenge.gameInfo}) );
				$oneLI.append( $oneAnchor );
				$listview.append( $oneLI );
			});
			
			$content.append( $listview );
			$content.trigger('create');

			$.mobile.loading( 'hide' );
		});
	});
	
    // hidden page to clear (user) info stored in localStorage
    $( document ).delegate("#reset", "pageinit", function() {
        if( isLocalStorage ) {
            localStorage.removeItem(userStorageKey);
        }
    });
	
});


// cloud endpoint things

function endpointinit() {
	$.mobile.loading( 'show', { text: 'Phone home...', textVisible:true});
	var apisToLoad;
	var callback = function() {
		if( --apisToLoad == 0 ) {
			// let's enable buttons now that all APIs have loaded
			$('#'+$.mobile.activePage.attr('id')).find('[type="submit"], [type="button"]').each(function(){
				$(this).button('enable');
				$(this).button('refresh');
				g_allAPIsLoaded = true;
				$.mobile.loading( 'hide' );
			});
			
			// TODO: signing things... see https://developers.google.com/appengine/docs/java/endpoints/consume_js
		}
	}
	
	apisToLoad = 4;
	
	var ENDPOINT_ROOT = '//' + window.location.host + '/_ah/api';
	gapi.client.load('playerfactory', 'v1', callback, ENDPOINT_ROOT);
	gapi.client.load('autochallengegameendpoint', 'v1', callback, ENDPOINT_ROOT);
	gapi.client.load('autoChallengeGameService', 'v1', callback, ENDPOINT_ROOT);
	gapi.client.load('gameService', 'v1', callback, ENDPOINT_ROOT);
}
