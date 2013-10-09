
var g_activeUser;
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
    
	// page events
	
	$('#btn-single-auto-challenge').click(function(){
		
		navigateToPageOrLoginIfNeeded( 'phosom-one-challenge' );
	});
	
	$('#btn-multi-auto-challenge').click(function(){
		
		$( '#phosom-game-created' ).data( 'game-type', 'autoChallenge' );
		
		navigateToPageOrLoginIfNeeded( 'phosom-game-created' );
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
	
	$( "div#phosom-one-challenge" ).on( "pagebeforeshow", function( event, ui ) {
		$(this).find('h2').html('Welcome, ' + g_activeUser.playerScreenName + ', to your challenge!');
	});
	
//	$( "div#phosom-game-created" ).on( "pagebeforecreate", function( event, ui ) {
//		$( 'div#phosom-game-created div[data-role="content"]' ).empty();
//		$( 'div#phosom-game-created div[data-role="content"]' ).trigger("create");
//	});
	$(document).on('pagehide', '#phosom-game-created', function(){ 
	    $(this).find('[data-role="content"]').empty();
	});
	$( "div#phosom-game-created" ).on( "pageshow", function( event, ui ) {
		$.mobile.loading( 'show', { text: 'Creating a game...', textVisible:true});
		
		switch( $( '#phosom-game-created' ).data('game-type') ) {
		case "autoChallenge":
			gapi.client.gamefactory.createGame(
					{'type':'autoChallenge'}).execute(function(resp){
				
				console.log(resp);
				
				$( 'div#phosom-game-created div[data-role="content"]' ).append( $('<img/>',{'src':resp.challengeUrl}) );
				
				$.mobile.loading( 'hide' );
			});
			break;
			
		default:
			break;
		}
	});
});


// cloud endpoint things

function endpointinit() {
	var ENDPOINT_ROOT = '//' + window.location.host + '/_ah/api';
	gapi.client.load('playerfactory', 'v1', function(){
		
	}, ENDPOINT_ROOT);
	gapi.client.load('gamefactory', 'v1', function(){
		
	}, ENDPOINT_ROOT);
}
function getPlayerWithName( name ) {

}
