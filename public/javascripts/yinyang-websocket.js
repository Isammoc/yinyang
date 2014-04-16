(function($, undefined) {
	var $yinyang = $('#yy-yinyang');

	yinyang = {
		wsSocket : undefined
	}

	yinyang.connect = function(wsUrl) {
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
			console.log("close");
			console.log(event)
		}
	}

	yinyang.receiveEvent = function(event) {
		var data = JSON.parse(event.data);
		$yinyang.trigger('yy-' + data.type, data.content);
	}

	yinyang.setUsername = function(username) {
		if (localStorage) {
			localStorage['username'] = username;
		}
		var data = {
			type : 'username',
			content : username
		}
		this.wsSocket.send(JSON.stringify(data));
	}
})(jQuery);