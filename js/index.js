var url = "https://dictionary.yandex.net/api/v1/dicservice.json/lookup?key="
var key = "dict.1.1.20141116T162732Z.6367256efc30c377.79e985914e47344e3856d26681dce9dca74d9fd6";
url += key;

var translate = function() {	
		var lang = "en-ru"; 
		url += "&lang=" + lang;
		url += "&text=" + $('#original_word').val();
		$.getJSON(url, function(response) {
			$('#word_section').html("");
			var $original = $("<div class=\"original\">");
			var $original_text = $("<span class=\"original_text\">");
			$original.append($original_text);
			if(response.def.length > 0) {
				
				$original_text.append(response.def[0].text);
				$original_text.append("<font color=\"#6f1014\"> ["+response.def[0].ts+"]</font>");				
				$('#word_section').append($original);
				
				for(var i = 0; i < response.def.length; i++) {
					var type = response.def[i];
					var $word = $("<div class=\"word\">");
					var $pos = $("<span class=\"pos\">");
					$pos.append("/"+type.tr[0].pos+"/");
					$word.append($pos);	
				
					for(var j = 0; j < type.tr.length; j++) {
						var translation = type.tr[j]; 
						var $tran = $("<div class=\"tran\">");
						var $tran_text = $("<span class=\"tran_text\">");
						$tran_text.append("<font color=\"#0d7248\" size=\"+2\">"+translation.text+"</font>");
						
						if (translation.syn) {
							for(var k = 0; k < translation.syn.length; k++) {
								$tran_text.append(", "+translation.syn[k].text);	
							}
						}
						$tran.append($tran_text);
						
						if (translation.mean && translation.mean.length > 0) {
							var $tran_mean = $("<span class=\"tran_mean\">");
							$tran_mean.append("("+translation.mean[0].text);
							for(var k = 1; k < translation.mean.length; k++) {
								$tran_mean.append(", "+translation.mean[k].text);	
							}
							$tran_mean.append(")");
							$tran.append($tran_mean);
						}
						if (translation.ex && translation.ex.length > 0) {
							var $ex = $("<div class=\"ex\">");
							for(var k = 0; k < translation.ex.length; k++) {
								var $tran_ex = $("<span class=\"tran_ex\">");
								$tran_ex.append(translation.ex[k].text + " - " + translation.ex[k].tr[0].text);
								$ex.append($tran_ex);	
							}
							$tran.append($ex);
						}
						
						$word.append($tran);			
					}
					$('#word_section').append($word);
				}
 			} else {				
				$original_text.append("No translation found.");
				
				$('#word_section').append($original);
			}
		});
	}

$(document).ready(function(){		
	"use strict"
	

	
	$('#translate').click(translate);
	$('#original_word').live("keypress", function (event) {
		if(event.keyCode === 13) {
			translate();		
		}
	});
	
	$('#sign_in').click(function() {
			
		$('#sign_in_window').fadeIn().css({'width' : 360, 'height' : 360 });;	
		var popMargTop = ($('#sign_in_window').height() + 80) / 2;
		var popMargLeft = ($('#sign_in_window').width() + 80) / 2;

		$('#sign_in_window').css({ 
			'margin-top' : -popMargTop,
			'margin-left' : -popMargLeft
		});	
		$('body').append('<div id="fade"></div>');
		$('#fade').css({'filter' : 'alpha(opacity=80)'}).fadeIn();
		
		return false;
	});

	$('#close_sign_up, #close_sign_in, #fade').live('click', function() { 
	  	$('#fade ,  #sign_up_window, #sign_in_window').fadeOut(function() {
			$('#fade').remove();  
		});
		return false;
	});
	
	$('#sign_up').click(function() {			
		$('#sign_up_window').fadeIn().css({'width' : 360, 'height' : 360 });;	
		var popMargTop = ($('#sign_up_window').height() + 80) / 2;
		var popMargLeft = ($('#sign_up_window').width() + 80) / 2;
 
		$('#sign_up_window').css({ 
			'margin-top' : -popMargTop,
			'margin-left' : -popMargLeft
		});	
		$('body').append('<div id="fade"></div>');
		$('#fade').css({'filter' : 'alpha(opacity=80)'}).fadeIn();
		return false;
	});	
});

