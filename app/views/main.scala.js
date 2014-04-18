@()(implicit request: play.api.mvc.Request[_])
(function($, undefined) {
	var $yinyang = $('#yy-yinyang');

	$yinyang
		.on('yy-self', function(event, data) {
			$yinyang.data('user-id', data.id)
			$('.yy-nickname').text(data.username);
			if (data.id == $('.yy-white-nickname').data('user-id')) {
				$('#game-board').addClass('user-white')
			} else if (data.id == $('.yy-black-nickname').data('user-id')){
				$('#game-board').addClass('user-black')
			}
			$('.yy-username').filter(function(){
				return $(this).data('user-id') == data.id
			}).text(data.username);
		})
		.on('yy-game', function(event, data) {
			$('#game-board')
				.removeClass('user-white')
				.removeClass('user-black');
			if(data.white) {
				$('.yy-white-nickname')
					.empty()
					.text(data.white.username)
					.data('user-id', data.white.id);
				if(data.white.id == $yinyang.data('user-id')) {
					$('#game-board').addClass('user-white')
				}
			} else {
				$('.yy-white-nickname')
					.empty()
					.text('En attente...')
					.removeData('user-id');
			}
			if(data.black) {
				$('.yy-black-nickname')
					.empty()
					.text(data.black.username)
					.data('user-id', data.black.id);
				if(data.black.id == $yinyang.data('user-id')) {
					$('#game-board').addClass('user-black')
				}
			} else {
				$('.yy-black-nickname')
					.empty()
					.text('En attente...')
					.removeData('user-id');
			}
		})
		.on('yy-username', function(event, data) {
			$('.yy-username').filter(function(){
				return $(this).data('user-id') == data.id
			}).text(data.username);
		})
		.on('yy-board', function(event, data) {
			if(yinyang.game) {
				yinyang.ui.clearBoard(function(){yinyang.ui.initBoard(data)});
			} else {
				yinyang.ui.initBoard(data);
			}
		});

	yinyang.connect("@routes.Application.connect.webSocketURL()")
	
	$('#form-nickname').on('submit', function(event){
		event.preventDefault();
		yinyang.setUsername($('#nickname').val());
	});

	yinyang.ui = {}

	yinyang.ui.clearBoard = function (callback) {
		d3.select("g.board-map")
			.selectAll("circle")
			.data([])
			.exit()
				.transition().duration(1000)
				.attr("opacity", 0.01)
				.attr("cx", 500)
				.attr("cy", -100)
				.remove();
		d3.select("svg")
			.selectAll("g.rule")
			.data([])
			.exit()
			.transition().duration(1000)
				.attr("transform", "translate(270,-200)")
				.attr("opacity", 0.01)
				.remove();
		d3.select("body").transition().duration(1000).each("end", callback)
		yinyang.game = undefined;
	}

	yinyang.ui.initBoard = function(rawGame) {
		var game = {}
		
		// Init tokens
		game.token = [];
		game.tokenIdx = 0
		for(var i = 0; i < 4; i++) {
			for(var j = 0; j < 4; j++) {
				if(rawGame.board[i*4+j] != '.') {
					game.token.push({key: game.tokenIdx++, color: rawGame.board[i*4+j], row:i, col:j});
				}
			}
		}
		var d3Board = d3.select("#game-board svg g.board-map");
		var d3Token = d3Board.selectAll("circle").data(game.token, function(d){return d.key;});
		
		d3Token.enter()
			.append("circle")
				.attr("opacity", 0.01)
				.attr("cx", 500)
				.attr("cy", -100)
				.attr("r", 100)
				.attr("fill", function(d){
					return d.color == 'b' ? "url(#rgBlack)": "url(#rgWhite)";
				})
				.attr("stroke", "gray")
				.transition().duration(1000)
					.attr("opacity", 1)
					.attr("cx", function(d){
						return d.col * 250 + 125;
					})
					.attr("cy", function(d){
						return d.row * 250 + 125;
					});

		// Init white Rules
		game.whiteRules = []
		for(var i = 0; i < 4; i++) {
			var rule = {}
			rule.key = i;
			if(rawGame.whiteRules[i]) {
				rule.hidden = false;
				// TODO If the rule is already defined
			} else {
				rule.hidden = true;
			}
			game.whiteRules.push(rule);
		}

		var svgWhiteRules = d3.select("#game-board svg g.whiteRules").selectAll("g")
			.data(game.whiteRules, function(d) {return d.key});

		svgWhiteRules
			.enter()
				.append("g")
				.attr("class", "white rule")
				.attr('opacity', 0.01)
				.attr("transform", "translate(270,-200)")
				.transition().duration(1000)
					.attr("transform", function(d) {return "translate(-500, " + (20 + 250 * d.key) + ")";})
					.attr('opacity', 1);

		var oneRule = svgWhiteRules.selectAll("rect").data(function(d){return [d]});
		oneRule.enter()
			.append("rect")
				.attr("width", "460")
				.attr("height", "210")
				.attr("x", "0")
				.attr("y", "0");
		oneRule.enter()
			.append("use")
				.attr("xlink:href", "#yinyang")
				.attr("x", 130)
				.attr("y", 10);

		// Init black Rules
		game.blackRules = []
		for(var i = 0; i < 4; i++) {
			var rule = {}
			rule.key = i;
			if(rawGame.blackRules[i]) {
				rule.hidden = false;
				// TODO If the rule is already defined
			} else {
				rule.hidden = true;
			}
			game.blackRules.push(rule);
		}

		var svgBlackRules = d3.select("#game-board svg g.blackRules").selectAll("g")
			.data(game.blackRules, function(d) {return d.key});

		svgBlackRules
			.enter()
				.append("g")
				.attr("class", "black rule")
				.attr('opacity', 0.01)
				.attr("transform", "translate(270,-200)")
				.transition().duration(1000)
					.attr("transform", function(d) {return "translate(1030, " + (20 + 250 * d.key) + ")";})
					.attr('opacity', 1);

		var oneRule = svgBlackRules.selectAll("rect").data(function(d){return [d]});
		oneRule.enter()
			.append("rect")
				.attr("width", "460")
				.attr("height", "210")
				.attr("x", "0")
				.attr("y", "0");
		oneRule.enter()
			.append("use")
				.attr("xlink:href", "#yinyang")
				.attr("x", 130)
				.attr("y", 10);

		if($('yy-nickname').data('user-id') == $('yy-black-nickname').data('user-id')) {
			game.user = 'black';
		} else if($('yy-nickname').data('user-id') == $('yy-white-nickname').data('user-id')) {
			game.user = 'white';
		} else {
			game.user = undefined
		}
		game.state = rawGame.state;
		yinyang.game = game;
		yinyang.ui.refresh();
	};

	yinyang.ui.refresh = function() {
		yinyang.ui.refreshModel();
		yinyang.ui.refreshUI();
	}

	yinyang.ui.refreshModel = function() {
		if(yinyang.game) {
			var canSelectBlackRules = yinyang.game.user == 'black' && yinyang.game.state == 'Init';
			for(var i = 0; i < 4; i++) {
				yinyang.game.blackRules[i].available = true;
			}
		}
	}

	yinyang.ui.refreshUI = function() {
		var blackRules = d3.select("#game-board svg g.blackRules").selectAll("g")
			.data(yinyang.game.blackRules.filter(function(d){return !d.selected}), function(d, i) {return d.key});

		blackRules.enter().append("g")
			.attr("class", "black rule")
			.attr('opacity', 1)
			.attr("transform", function(d) {return "translate(1030, " + (20 + 250 * d.key) + ") scale(1)";});
		blackRules
			.transition().duration(1000)
			.attr('opacity', 1)
			.attr("transform", function(d) {return "translate(1030, " + (20 + 250 * d.key) + ") scale(1)";});
		blackRules.on("click", function(d){
			d.selected = true;
			yinyang.ui.refreshUI();
		});
		blackRules.exit().remove();
		var oneRule = blackRules.selectAll("rect").data(function(d){return [d]});
		oneRule.enter()
			.append("rect")
				.attr("width", "460")
				.attr("height", "210")
				.attr("x", "0")
				.attr("y", "0");
		oneRule.enter()
			.append("use")
				.attr("xlink:href", "#yinyang")
				.attr("x", 130)
				.attr("y", 10);
		var oneRuleAvailable = oneRule.selectAll("animateTransform").data(function(d){
			if(d.available) {
				return [d];
			}
		})
		oneRuleAvailable.enter()
			.append("animate")
				.attr("attributeName", "stroke-width")
				.attr("type", "XML")
				.attr("begin", "0s")
				.attr("dur", "1s")
				.attr("from", "0")
				.attr("to", "10")
				.attr("repeatCount", "indefinite");

		var svgBackdrop = d3.select("#game-board svg g.selection").selectAll("rect.backdrop").data(yinyang.game.blackRules.filter(function(d){return d.selected}), function(d, i) {return d.key});
		svgBackdrop.enter()
			.append("rect")
			.attr('class', 'backdrop')
			.attr('width', 2020)
			.attr('height', 1020)
			.attr('x', -510)
			.attr('y', -10)
			.style('fill', '#fff')
			.attr('opacity', '0.01')
			.transition().duration(1000)
				.attr('opacity', '0.6');
		svgBackdrop.exit()
			.transition().duration(1000)
				.attr('opacity', '0.01')
				.remove();
		var svgBlackRuleSelected = d3.select("#game-board svg g.selection").selectAll("g")
			.data(yinyang.game.blackRules.filter(function(d){return d.selected}), function(d, i) {return d.key});
		svgBlackRuleSelected
			.enter()
				.append("g")
				.attr("class", "black rule")
				.attr('opacity', 1)
				.attr("transform", function(d) {return "translate(1030, " + (20 + 250 * d.key) + ") scale(1)";})
				.transition().duration(1000)
					.attr("transform", function(d) {return "translate(-200, 180) scale(3)";});
		svgBlackRuleSelected.exit()
			.transition().duration(1000)
				.attr("transform", function(d) {return "translate(1030, " + (20 + 250 * d.key) + ") scale(1)";})
				.remove();
		var oneRuleSelected = svgBlackRuleSelected.selectAll("rect").data(function(d){return [d]});
		oneRuleSelected.enter()
			.append("rect")
				.attr("width", "460")
				.attr("height", "210")
				.attr("x", "0")
				.attr("y", "0");
		oneRuleSelected.enter()
			.append("use")
				.attr("xlink:href", "#yinyang")
				.attr("x", 130)
				.attr("y", 10);
	}

	d3.xml("@routes.Assets.at("svg/yinyang.svg")", "image/svg+xml", function(xml) {
		d3.select("#game-board").node().appendChild(document.importNode(xml.documentElement, true));

		d3.select("#game-board svg")
			.attr("height",500)
			.attr("width", 1000)
			.select("g#board")
				.append("g")
					.attr('class', 'board-map');
		d3.select("#game-board svg")
			.append("g")
				.attr("class", "whiteRules");
		d3.select("#game-board svg")
			.append("g")
				.attr("class", "blackRules");
		d3.select("#game-board svg")
		.append("g")
			.attr("class", "selection");
	});
})(jQuery);
