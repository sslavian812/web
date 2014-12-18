/*var url = "https://dictionary.yandex.net/api/v1/dicservice.json/lookup?key="
var key = "dict.1.1.20141116T162732Z.6367256efc30c377.79e985914e47344e3856d26681dce9dca74d9fd6";
url += key;*/
var address = "http://192.192.0.106:80"

$(document).ready(function(){		
	"use strict"
	
	$('#translate').click(translate);
	$('#original_word').live("keypress", function (event) {
		if(event.keyCode === 13) {
			translate();		
		}
	});
	my_window('sign_in', 360, 360);
	my_window('sign_up', 360, 380);
	my_window('add_to_dict', 250, 360);

	$('#close_sign_up').live('click', function() { 		
	  	$('#sign_up_fade ,  #sign_up_window').fadeOut(function() {
			clear_window();
			$('#sign_up_fade').remove();  
		});
		return false;
	});
	
	$('#close_sign_in').live('click', function() { 
		clear_window();
	  	$('#sign_in_fade ,  #sign_in_window').fadeOut(function() {
			$('#sign_in_fade').remove();  
		});
		return false;
	});
	
	$('#su_password, #sur_password').live('keyup', function() {
		correct_passwords();
		return false;
	});	
		
	$('#su_login').live('keyup', function() {
		correct_username($('#su_login').val());
		return false;
	});
	
	$('#sub_sign_up').live('click', function() {
		if(correct_username($('#su_login').val()) && correct_passwords()) {
			var url = address + "/signup?username="+$('#su_login').val()+"&password="+$('#su_password').val();	
			$.getJSON(url, function(response) {
				if (response.code === 301) {
					$('#username_alert').text("Username already exists.");	
					$('#username_alert').css({'visibility' : 'visible'});	
				} else if(response.code === 100) {					
					login($('#su_login').val());	
					auth(response.token);				
					$('#sign_up_fade ,  #sign_up_window').fadeOut(function() {
						clear_window();
						$('#sign_up_fade').remove();  
					});
				} else {
					window.alert("Something went wrong.");	
				}
				
			});
		}
		return false;
	});
	
	$('#sign_out').live('click', function () {
		logout();	
	});
	
});
var signin = function() {
	var url = address + "/signin";	
	$.getJSON(url, function(response) {
		console.log(response);		
	});
}

var auth = function(token) {
	var cookieName = "auth"
	var cookieValue = token;
	var nDays = 30;
	var today = new Date();
	var expire = new Date();
	if (nDays==null || nDays==0) nDays=1;
	expire.setTime(today.getTime() + 3600000*24*nDays);
	document.cookie = cookieName+"="+escape(cookieValue) + ";expires="+expire.toGMTString();
	return false;
}

var clear_window = function() {
	$('#su_login').val("");
	$('#su_password').val("");
	$('#si_login').val("");
	$('#si_password').val("");
	$('#sur_password').val("");
	$('#incorrect').css({'visibility' : 'hidden'});
	$('#username_alert').css({'visibility' : 'hidden'});
	$('#pass_dont_match').css({'visibility' : 'hidden'});	
}


var login = function(username) {
	$('#username').text($('#su_login').val());
	$('#sign_in').css({'display' : 'none'});	
	$('#sign_up').css({'display' : 'none'});
	$('#username').css({'display' : 'block'});	
	$('#sign_out').css({'display' : 'block'});
}

var logout = function() {
	$('#username').css({'display' : 'none'});	
	$('#sign_out').css({'display' : 'none'});	
	$('#sign_up').css({'display' : 'block'});
	$('#sign_in').css({'display' : 'block'});	
}

var correct_username = function(username) {
	if (username.length == 0) {
		$('#username_alert').text("Empty username.");	
		$('#username_alert').css({'visibility' : 'visible'});
		return false;
	}
 	if (legal_charaacters(username)) {
		$('#username_alert').css({'visibility' : 'hidden'});
		return true;	
	} else {
		$('#username_alert').text("Illegal characters in username.");	
		$('#username_alert').css({'visibility' : 'visible'});
		return false;	
	}
}

var correct_passwords = function() {
	if ($('#su_password').val().length === 0) {
		$('#pass_dont_match').text("Empty password.");	
		$('#pass_dont_match').css({'visibility' : 'visible'});
		return false;	
	}
	if (!legal_charaacters($('#su_password').val())) {
		$('#pass_dont_match').text("Illegal characters in password.");	
		$('#pass_dont_match').css({'visibility' : 'visible'});
		return false;	
	}
	if ($('#su_password').val() !== $('#sur_password').val()) {
		$('#pass_dont_match').text("Passwords don't match.");	
		$('#pass_dont_match').css({'visibility' : 'visible'});
		return false;
	} else {
		$('#pass_dont_match').css({'visibility' : 'hidden'});
		return true;
	}	
}

var legal_charaacters = function(string) {
 	for(var i = 0; i < string.length; i++) {
		var code = string.charCodeAt(i);
		if ( ((code >= 65) && (code <= 90)) || ((code >= 97) && (code <= 122)) || ((code >= 48) && (code <= 57))) {
			continue;
		} else {
			return false;	
		}
	}
	return true;	
}

var translate = function() {	
		var lang = "en-ru"; 
		url += "&lang=" + lang;
		url += "&text=" + $('#original_word').val();
		$.getJSON(url, function(response) {
			$('#word_article').html("");
			var $original = $("<div class=\"original\">");
			var $original_text = $("<span class=\"original_text\">");
			$original.append($original_text);
			if(response.def.length > 0) {
				
				$original_text.append(response.def[0].text);
				$original_text.append("<font color=\"#6f1014\"> ["+response.def[0].ts+"]</font>");				
				$('#word_article').append($original);
				
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
					$('#word_article').append($word);
				}
 			} else {				
				$original_text.append("No translation found.");
				
				$('#word_article').append($original);
			}
		});
		return false;
}
	
var my_window = function(id, width, height) {
	$('#'+id).click(function() {
		var cur_window = '#'+id+'_window';	
		$(cur_window).fadeIn().css({'width' : width, 'height' : height });;	
		var popMargTop = ($(cur_window).height() + 80) / 2;
		var popMargLeft = ($(cur_window).width() + 80) / 2;

		$(cur_window).css({ 
			'margin-top' : -popMargTop,
			'margin-left' : -popMargLeft
		});	
		$('body').append('<div class="fade" id='+id+'_fade></div>');
		$('.fade').css({'filter' : 'alpha(opacity=80)'}).fadeIn();
		
		return false;
	});

	$('#'+id+'_fade').live('click', function() { 
	  	$('#'+id+'_fade ,  #'+id+'_window').fadeOut(function() {
			clear_window();
			$('#'+id+'_fade').remove();  
		});
		return false;
	});
}