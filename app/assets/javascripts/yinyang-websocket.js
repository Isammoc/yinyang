(function($, undefined) {
	var $yinyang = $('#yy-yinyang');

	var ws = {
		wsSocket: undefined
	}

	ws.connect = function(wsUrl) {
		var self = this;
		this.wsSocket = new WebSocket(wsUrl);
		this.wsSocket.onmessage = this.receiveEvent;
		this.wsSocket.onopen = function(event) {
			if (localStorage) {
				if (localStorage['username']) {
					self.setUsername(localStorage['username']);
				}
			}
		}
		this.wsSocket.onclose = function(event) {
			$('#modal-disconnected').modal();
		}
	}

	ws.receiveEvent = function(event) {
		console.log(event)
		var data = JSON.parse(event.data);
		$yinyang.trigger('yy-' + data.type, data.content);
	}

	ws.setUsername = function(username) {
		if (localStorage) {
			localStorage['username'] = username;
		}
		var data = {
			type : 'username',
			content : username
		}
		this.wsSocket.send(JSON.stringify(data));
	}
	$yinyang.data('ws', ws)
})(jQuery);
