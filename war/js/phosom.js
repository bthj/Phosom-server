
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
    
	
    
    ///// button events
	
	$('#btn-create-game').click(function(){
		
		$( '#phosom-game-creation' ).data( 'game-type', 'autoChallenge' );
		
		navigateToPageOrLoginIfNeeded( 'phosom-game-creation' );
	});
	
	$('#btn-game-overview').click(function(){
		
		navigateToPageOrLoginIfNeeded( 'phosom-challenges-overview' );
	});
	
	$('#btn-game-join').click(function(){
		
		navigateToPageOrLoginIfNeeded( 'phosom-game-join' );
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
		
		respondWithUrlToImage( $('#challenge-response-with-url').val() );
		
		return false;
	});
	
	$('#form-game-join').submit(function(){
		$.mobile.loading( 'show', { text: 'Joining game...', textVisible:true});
		
		var gameId = $(this).find('#text-game-id').val();
		
		gapi.client.autochallengegameendpoint.getAutoChallengeGame({
			'id': gameId
		}).execute(function(game){
			
			if( game ) {
				
				g_activeGame = game;
				
				addCurrentPlayerToCurrentGameAndShowChallenge();
			} else {
				alert("No game found with that ID");
			}
		});		
		
		return false;
	});
	
	function setCurrentGameIdFromChallengeLink(event, ui) {
		event.preventDefault();
		var gameId = $(this).data('gameid');
		gapi.client.autochallengegameendpoint.getAutoChallengeGame({
			'id': gameId
		}).execute(function(game){
			
			g_activeGame = game;
			
			$.mobile.changePage( '#phosom-challenge-result' );
		});
	}
	
	function respondWithUrlToImage( url ) {
		$.mobile.loading( 'show', { text: 'Sending...', textVisible:true});
		
		gapi.client.autoChallengeGameService.respondToChallengeWithUrl({
			'gameId':g_activeGame.key.id,
			'playerId':g_activeUser.key.id,
			'url': url
		}).execute(function(respUrlSent){
			
			console.log(respUrlSent);
			
			$.mobile.changePage( '#phosom-challenge-result' );
		});
	}
	
	function respondWithUrlFromLink( event, ui ) {
		event.preventDefault();
		var url = $(this).attr('href');
		respondWithUrlToImage(url);
	}
	
	$('#image-search').submit(function(){
		$.mobile.loading( 'show', { text: 'Finding some images...', textVisible:true});
		
		var $this = $(this);
		var query = $this.find('#input-image-search').val();
		
		gapi.client.autoChallengeGameService.searchForImagesAtBing({
			'query': encodeURIComponent(query)
		}).execute(function(searchResults){
			console.log(searchResults);
			var $gallery = $this.siblings('.gallery').first().empty();
			$.each(searchResults.items, function(index, oneImageResult) {
				var $a = $('<a/>', {'href':oneImageResult.fullSizeImageUrl, 'rel':'external', 'style':'padding:5px;'})
							.on('click', respondWithUrlFromLink);
				var $img = $('<img/>', {'src':oneImageResult.thumbnailUrl, 'alt':oneImageResult.altText});
				$a.append( $img );
				$gallery.append( $a );
			});
			$.mobile.loading( 'hide' );
		});
/*
        $.ajax({
            type: 'GET',
            url: 'https://api.datamarket.azure.com/Bing/Search/v1/Composite?Sources=%27image%27&Query=%27'+query+'%27&Adult=%27On%27',
            dataType: "json", 
            context: this,
            beforeSend: function(xhr){
                // base64 encoded: ignore:key
            	// as in http://social.msdn.microsoft.com/Forums/windowsazure/en-us/9f085915-81b6-488d-a348-1c3ca769d44f/migrating-to-windows-azure-bing-search-api-with-jquery-jsonp?forum=DataMarket
            	// and https://datamarket.azure.com/dataset/explore/bing/search
                xhr.setRequestHeader('Authorization', 'Basic OlpGeDI1Wmh1c0lUTGVPZ3JTd2FLSzhzTVVoUlJ4cGxPSjMvME10NGcvdWs=');
            },
            success: function(data,status){
            	
            	if( data.d !== undefined ) {
            		var $gallery = $this.siblings('.gallery').first().empty();
            		$.each(data.d.results, function(index, result){
            			$.each(result.Image, function(index2, image){
            				// <li><a href="images/full/001.jpg" rel="external"><img src="images/thumb/001.jpg" alt="Image 001" /></a></li>
//            				var $li = $('<li/>');
            				var $a = $('<a/>', {'href':image.MediaUrl, 'rel':'external', 'style':'padding:5px;'})
            							.on('click', respondWithUrlFromLink);
            				var $img = $('<img/>', {'src':image.Thumbnail.MediaUrl, 'alt':image.Title});
            				$a.append( $img );
//            				$li.append( $a );
            				$gallery.append( $a );
            			});
            		});
            		//var photoswipe = $gallery.find('a').photoSwipe({ enableMouseWheel: false , enableKeyboard: false });
            	}
            	$.mobile.loading( 'hide' );
            }
        });
*/
        return false;
	});
	
	
	
	///// page events
	
	function addCurrentPlayerToCurrentGameAndShowChallenge() {
		gapi.client.autoChallengeGameService.addPlayerToGame({
			'gameId':g_activeGame.key.id,
			'playerId':g_activeUser.key.id
		}).execute(function(playerAddedResp){
			
			$.mobile.loading( 'hide' );
			$.mobile.changePage('#phosom-one-challenge');
		});	
	}
	

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
					
					console.log(resp);
					
					g_activeGame = resp;
					console.log(resp);
					
					addCurrentPlayerToCurrentGameAndShowChallenge();
				});
//				break;
//				
//			default:
//				break;
//		}
	});
	

	$( "div#phosom-one-challenge" ).on( "pageshow", function( event, ui ) {
		
		var $content = $( 'div#phosom-one-challenge div[data-role="content"]' );
		
		$.mobile.loading( 'show', { text: 'Fetching the challenge...', textVisible:true});
		gapi.client.gameService.getChallengePhotoUrl({
			'bucket':'auto-challenge-photos', 
			'filename': getFileNameFromURL( g_activeGame.challengeUrl ),
			'size':$content.parent().width() - 20
		}).execute(function(urlResp){
		
			console.log(urlResp);
			
			$content.prepend( $('<h2/>').text('Game # ' + g_activeGame.key.id + " - " +g_activeUser.playerScreenName+ ", here's your challenge!") );
			$content.append( $('<a/>', {
				'href':'#phosom-challenge-response', 
				'data-role':'button',
				'text':'Respond to it!'}) );

			$content.append( $('<img/>',{'src':urlResp.challengePhotoUrl}) );
			
			$content.waitForImages(function(){
				
				$.mobile.loading( 'hide' );
			});
			
			$content.trigger('create');
		});
	});
	
	$( "div#phosom-challenge-response" ).on( "pagebeforeshow", function( event, ui ) {
		var $content = $(this).find( 'div[data-role="content"]' );
		$content.find('#challenge-response-with-url').val('');
	});
	
	function compareResultListsByScore( a, b ) {
		if( a.score < b.score ) {
			return 1;
		}
		if( a.score > b.score ) {
			return -1;
		}
		return 0;
	}
	function getGradeFromJSHammingDistance( distance ) {
		// let's assume 0.400 is the maximum distance
		var maxDistance = 0.400;
		if( distance > maxDistance ) {
			distance = maxDistance;
		}
		var percentageOfMaximumDistance = distance / maxDistance;
		var grade = Math.round(1000 * (1 - percentageOfMaximumDistance));
		return grade;
	}
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
			var listToSort = [];
			$.each( challengesInfo.items, function(index, oneChallenge){
				var $oneLI = $('<li/>');
				var $oneDIV = $('<div/>');
				
				if( oneChallenge.playerId == g_activeUser.key.id ) {
					$oneDIV.append( $('<h3/>', {'text': "Your response"}) );
				} else {
					$oneDIV.append( $('<h3/>', {'text': oneChallenge.playerName + "'s response"}) );
				}
				$oneDIV.append( $('<img/>',{
					'src':oneChallenge.challengePhotoUrl, 'style':'padding:5px;' }) );
				$oneDIV.append( $('<img/>',{
					'src':oneChallenge.responsePhotoUrl, 'style':'padding:5px;'}) );
				$oneDIV.append( $('<h3/>',{'text':'Grade: ' + oneChallenge.score}) );
				
				if( oneChallenge.playerId == g_activeUser.key.id ) {
					var $collapsibleSetDIV = $('<div/>', {'data-role': 'collapsible-set'});
					var $collapsibleDIV = $('<div/>', {'data-role':'collapsible', 'data-collapsed':'true'});
					$collapsibleDIV.append( $('<h3/>', {'text': 'Alternative scores'}) );
					$collapsibleDIV.append( oneChallenge.extraScoreInfo );
					$collapsibleSetDIV.append( $collapsibleDIV );
					$oneDIV.append( $collapsibleSetDIV );
				}
				
				$oneLI.append( $oneDIV );
				listToSort.push( {'score':oneChallenge.score, 'liMarkup':$oneLI} );
			});
			
			listToSort.sort(compareResultListsByScore);
			
			$.each(listToSort, function(index, oneEntry){
				$listview.append( oneEntry.liMarkup );
			});
			
			$content.append( $listview );

			$content.append( $('<a/>', {
				'href':'#phosom-game-creation', 'data-role':'button', 'data-theme':'b', 
				'text':'Have another go!'}) );
			$content.append( $('<a/>', {
				'href':'#phosom-index', 'data-role':'button', 'text':'Go home...'}) );
			
			$listview.waitForImages(function(){
				
//				$(this).children('li').each(function(){
//					var img1 = $(this).find('img')[0];
//					var img2 = $(this).find('img')[1];
//					img1.crossOrigin="anonymous";
//					img2.crossOrigin="anonymous";
//					var hammingDistance = simi.compare(img1, img2);
//					var jsGrade = getGradeFromJSHammingDistance(hammingDistance);
//					$(this).find('h3:eq(1)').after($('<h3/>',{'text':'JS Grade: ' + jsGrade + ' (calculated distance: ' + hammingDistance + ')'}));
//				});
				
				$.mobile.loading( 'hide' );
			});
			
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
