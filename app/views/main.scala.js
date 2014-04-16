@()(implicit request: play.api.mvc.Request[_])
(function($, undefined) {
	var $yinyang = $('#yinyang');

	yinyang = {wsSocket : undefined}

	yinyang.connect = function() {
		var self = this;
		this.wsSocket = new WebSocket("@routes.Application.connect.webSocketURL()");
		this.wsSocket.onmessage = this.receiveEvent;
		this.wsSocket.onopen = function(event) {
			console.log("open"); console.log(event)
			if(localStorage) {
				if(localStorage['username']) {
					self.setUsername(localStorage['username']);
				}
			}
		}
		this.wsSocket.onclose = function(event) {console.log("close"); console.log(event)}
	}

	yinyang.receiveEvent = function(event) {
		var data = JSON.parse(event.data);
		console.log(data);
		$yinyang.trigger('yy-' + data.type, data.content);
	}

	yinyang.setUsername = function(username) {
		if(localStorage) {
			localStorage['username'] = username;
		}
		var data = {
			type: 'username',
			content: username
		}
		this.wsSocket.send(JSON.stringify(data));
	}
	$yinyang
		.on('yy-self', function(event, data) {
			$('.yy-nickname').text(data.username);
		})
		.on('yy-game', function(event, data) {
			console.log(data)
			if($('#game').hasClass('hidden')) {
				$('.jumbotron').slideUp();
				$('#game').removeClass('hidden').slideDown();
			}
			if(data.white) {
				$('.yy-white-nickname').empty().text(data.white.username)
			} else {
				$('.yy-white-nickname').empty().text('En attente...')
			}
			if(data.black) {
				$('.yy-black-nickname').empty().text(data.black.username)
			} else {
				$('.yy-black-nickname').empty().text('En attente...')
			}
		});
	yinyang.connect()
	
	$('#form-nickname').on('submit', function(event){
		event.preventDefault();
		yinyang.setUsername($('#nickname').val());
	});
})(jQuery);
