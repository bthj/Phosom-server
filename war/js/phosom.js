
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
    
    // TODO: move game behaviour into it's one object / enclosure!
    
	
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
    
	
	
	$('#btn-multi-auto-challenge').click(function(){
		
		$( '#phosom-game-creation' ).data( 'game-type', 'autoChallenge' );
		
		navigateToPageOrLoginIfNeeded( 'phosom-game-creation' );
	});
	
	
	$('#login').submit(function(){
		$.mobile.loading( 'show', { text: 'Going...', textVisible:true});
		
		gapi.client.playerfactory.createPlayerWithName(
				{'name':$('#name').val()}).execute(function(resp){
			
			g_activeUser = resp;
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
	
	
	// page events
	
	$( "div#phosom-one-challenge" ).on( "pagebeforeshow", function( event, ui ) {
		//$(this).find('h2').html(g_activeUser.playerScreenName + ", here's your challenge!");
	});
	

	$( "div#phosom-game-types" ).on( "pageshow", function( event, ui ) {
		// let's have the buttons disabled until all APIs have loaded
		if( ! g_allAPIsLoaded ) {
			$(this).find('[type="submit"], [type="button"]').each(function(){
				$(this).button('disable');
				$(this).button('refresh');
			});
		}
	});
	
	$(document).on('pagehide', '#phosom-game-creation, #phosom-one-challenge, #phosom-challenge-result', function(){ 
	    $(this).find('[data-role="content"]').empty();
	});
	
	$( "div#phosom-game-creation" ).on( "pageshow", function( event, ui ) {
		$.mobile.loading( 'show', { text: 'Creating a game...', textVisible:true});
		
		switch( $( '#phosom-game-creation' ).data('game-type') ) {
		case "autoChallenge":
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
			break;
			
		default:
			break;
		}
	});
	
	$( "div#phosom-one-challenge" ).on( "pagebeforecreate", function( event, ui ) {
		
		var $content = $( 'div#phosom-one-challenge div[data-role="content"]' );
		
		$content.prepend( $('<h2/>').text('Game # ' + g_activeGame.key.id + " - " +g_activeUser.playerScreenName+ ", here's your challenge!") );
		$content.append( $('<a/>', {
			'href':'#phosom-challenge-response', 'data-role':'button', 'text':'Respond to it!'}) );
		
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
	});
	
	$( "div#phosom-challenge-result" ).on( "pagebeforecreate", function( event, ui ) {
		var $content = $(this).find( 'div[data-role="content"]' );
		$content.append( $('<h2/>').text('Game # ' + g_activeGame.key.id + ' - results!') );
	});
	$( "div#phosom-challenge-result" ).on( "pageshow", function( event, ui ) {
		var $content = $(this).find( 'div[data-role="content"]' );
		$.mobile.loading( 'show', { text: 'Calculating the grade...', textVisible:true});
		
		gapi.client.autoChallengeGameService.getChallengeAndResponseInfo({
			'gameId':g_activeGame.key.id,
			'playerId':g_activeUser.key.id,
			'size':Math.round($content.parent().width() / 2.2)
		}).execute(function(urlResp){
			
			console.log(urlResp);
			
			$content.append( $('<img/>',{'src':urlResp.challengePhotoUrl}) );
			$content.append( $('<img/>',{'src':urlResp.responsePhotoUrl}) );
			$content.append( $('<h3/>',{'text':'Grade: ' + urlResp.score}) );
			
			$.mobile.loading( 'hide' );
		});
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
	
	apisToLoad = 3;
	
	var ENDPOINT_ROOT = '//' + window.location.host + '/_ah/api';
	gapi.client.load('playerfactory', 'v1', callback, ENDPOINT_ROOT);
	gapi.client.load('autoChallengeGameService', 'v1', callback, ENDPOINT_ROOT);
	gapi.client.load('gameService', 'v1', callback, ENDPOINT_ROOT);
}
