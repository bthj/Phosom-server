
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
    
	
	// page events
	
	$('#btn-single-auto-challenge').click(function(){
		if( g_activeUser ) {
			$.mobile.changePage('#phosom-one-challenge');
		} else {
			g_pageAfterLogin = 'phosom-one-challenge';
			$.mobile.changePage('#phosom-get-user');
		}
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
});


// cloud endpoint things

function endpointinit() {
	var ENDPOINT_ROOT = '//' + window.location.host + '/_ah/api';
	gapi.client.load('playerfactory', 'v1', function(){
		
	}, ENDPOINT_ROOT);
}
function getPlayerWithName( name ) {

}
