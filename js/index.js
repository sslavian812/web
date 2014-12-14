$(document).ready(function(){					   		   
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