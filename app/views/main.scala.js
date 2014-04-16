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
		});
	yinyang.connect("@routes.Application.connect.webSocketURL()")
	
	$('#form-nickname').on('submit', function(event){
		event.preventDefault();
		yinyang.setUsername($('#nickname').val());
	});

	d3.xml("@routes.Assets.at("svg/yinyang.svg")", "image/svg+xml", function(xml) {
		d3.select("#game-board").node().appendChild(document.importNode(xml.documentElement, true));

		var data = [
				['b','w','w','w'],
				['b','w','w','w'],
				['b','b','b','w'],
				['b','b','b','w'],
				];
		rules = {
				white : [{hidden:true}, {hidden:true}, {hidden:true}, {hidden:true}],
				black : [{hidden:true}, {hidden:true}, {hidden:true}, {hidden:true}]
			};
		var token = [];
		for(var i = 0; i < 4; i++) {
			for(var j = 0; j < 4; j++) {
				token.push({key:i*4+j, color:data[i][j], row:i, col:j});
			}
		}

		var svg = d3.select("#game-board svg")
				.attr("height",500)
				.attr("width", 1000)
				.select("g#board")
					.append("g");
		var svg2 = d3.select("#game-board svg")
				.append("g")
					.attr("class", "whiteRules");
		var svg3 = d3.select("#game-board svg")
				.append("g")
					.attr("class", "blackRules");

		drawWhiteRules = function(whiteRules) {
			var svgWhiteRules = svg2.selectAll("g")
					.data(whiteRules, function(d, i) {return i});
			
			svgWhiteRules
				.enter()
					.append("g");
			svgWhiteRules
				.attr("transform", function(d, i) {return "translate(-500, " + (20 + 250 * i) + ")";});

			var oneRule = svgWhiteRules.selectAll("rect").data(function(d){return [d]});
			oneRule.enter().append("rect")
				.attr("width", "460")
				.attr("height", "210")
				.attr("x", "0")
				.attr("y", "0");
			oneRule.enter().append("use")
				.attr("xlink:href", "#yinyang")
				.attr("x", 130)
				.attr("y", 10);
		}

		drawBlackRules = function(blackRules) {
			var svgBlackRules = svg3.selectAll("g")
					.data(blackRules, function(d, i) {return i});
			
			svgBlackRules
				.enter()
					.append("g");
			svgBlackRules
				.attr("transform", function(d, i) {return "translate(1030, " + (20 + 250 * i) + ")";});
			
			var oneRule = svgBlackRules.selectAll("rect").data(function(d){return [d]});
			oneRule.enter().append("rect")
				.attr("width", "460")
				.attr("height", "210")
				.attr("x", "0")
				.attr("y", "0");
			oneRule.enter().append("use")
				.attr("xlink:href", "#yinyang")
				.attr("x", 130)
				.attr("y", 10);
		}

		drawMap = function(token) {
			var d3Token = svg.selectAll("circle").data(token, function(d){return d.key;});
			
			d3Token.enter().append("circle")
				.attr("opacity", 0.01)
				.attr("cx", 500)
				.attr("cy", -100);
			d3Token
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
			d3Token.exit()
				.attr("opacity", 1)
				.transition().duration(1000)
					.attr("opacity", 0.01)
				.attr("cx", 500)
				.attr("cy", -100)
					.remove()
				;
		}

		myToken = token;
		drawMap(token);
		drawWhiteRules(rules.white);
		drawBlackRules(rules.black);
	});
})(jQuery);
