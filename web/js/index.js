var address = "http://localhost:80"
var $active_dict = null;
var logon = false;
var cur_word = null;

function if_enter_key(then) {
    if (event.keyCode === 13) {
        then()
    }
}

function start_login() {
    if (legal_characters($('#si_login').val()) && legal_characters($('#si_password').val())) {
        var url = address + "/signin?username=" + $('#si_login').val() + "&password=" + $('#si_password').val();
        $.getJSON(url, function (response) {
            if (response.code === 301) {
                $('#incorrect').text("Incorrect username or password.");
                $('#incorrect').css({'visibility': 'visible'});
            } else if (response.code === 100) {
                auth(response.token);
                fill_all($('#si_login').val());
                $('#sign_in_fade ,  #sign_in_window').fadeOut(function () {
                    clear_window();
                    $('#sign_in_fade').remove();
                });
            } else {
                window.alert("Something went wrong.");
            }
        });
    }
    return false;
}

function start_signup() {
    if (correct_username($('#su_login').val()) && correct_passwords()) {
        var url = address + "/signup?username=" + $('#su_login').val() + "&password=" + $('#su_password').val();
        $.getJSON(url, function (response) {
            if (response.code === 301) {
                $('#username_alert').text("Username already exists.");
                $('#username_alert').css({'visibility': 'visible'});
            } else if (response.code === 100) {
                auth(response.token);
                fill_all($('#su_login').val());
                $('#sign_up_fade ,  #sign_up_window').fadeOut(function () {
                    clear_window();
                    $('#sign_up_fade').remove();
                });
            } else {
                window.alert("Something went wrong.");
            }

        });
    }
    return false;
}

$(document).ready(function () {
    "use strict";

    $('#translate').click(translate);
    $('#original_word').live("keypress", function (event) {
        if_enter_key(function () {
            var $original_word = $('#original_word');
            translate($original_word.val());
            $original_word.val("");
        });
    });

    $('#si_password, #si_login').live("keypress", function (event) {
        if_enter_key(start_login)
    });

    my_window('sign_in', 360, 360);
    my_window('sign_up', 360, 380);
    my_window('add_to_dict', 250, 360);

    $('#close_sign_up').live('click', function () {
        $('#sign_up_fade ,  #sign_up_window').fadeOut(function () {
            clear_window();
            $('#sign_up_fade').remove();
        });
        return false;
    });

    $('#close_sign_in').live('click', function () {
        clear_window();
        $('#sign_in_fade ,  #sign_in_window').fadeOut(function () {
            $('#sign_in_fade').remove();
        });
        return false;
    });

    $('#close_add_to_dict_window').live('click', function () {
        $('#add_to_dict_fade ,  #add_to_dict_window').fadeOut(function () {
            $('#add_to_dict_fade').remove();
        });
        return false;
    });

    $('#su_password, #sur_password').live('keyup', function () {
        correct_passwords();
        return false;
    });

    $('#su_password, #sur_password, #su_login').live('keypress', function (event) {
        if_enter_key(start_signup)
    });

    $('#su_login').live('keyup', function () {
        correct_username($('#su_login').val());
        return false;
    });

    $('#si_password').live('keyup', function () {
        if (!legal_characters($('#si_password').val())) {
            $('#incorrect').text("Illegal characters.");
            $('#incorrect').css({'visibility': 'visible'});
        } else {
            $('#incorrect').css({'visibility': 'hidden'});
        }
        return false;
    });

    $('#si_login').live('keyup', function () {
        if (!legal_characters($('#si_login').val())) {
            $('#incorrect').text("Illegal characters.");
            $('#incorrect').css({'visibility': 'visible'});
        } else {
            $('#incorrect').css({'visibility': 'hidden'});
        }
        return false;
    });

    $('#sub_sign_up').live('click', start_signup);

    $('#sub_sign_in').live('click', start_login);

    $('#sign_out').live('click', function () {
        logout();
    });

    $('#new_dict').live('keyup', function () {
        if (legal_characters($('#new_dict').val())) {
            $('#add_dict_alert').css({'visibility': 'hidden'});
        } else {
            $('#add_dict_alert').text("Illegal characters.");
            $('#add_dict_alert').css({'visibility': 'visible'});
        }
    });

    $('#new_dict').live('keypress', function(event) {
        if_enter_key(start_add_dict)
    })

    function start_add_dict() {
        var name = $('#new_dict').val();
        if (name.length === 0) {
            $('#add_dict_alert').text("Empty.");
            $('#add_dict_alert').css({'visibility': 'visible'});
        } else if (legal_characters(name)) {
            $('#add_dict_alert').css({'visibility': 'hidden'});
            var url = address + "/do?object=list&action=add&list=" + name + "&token=" + getToken();
            $.post(url, null, function (data, textStatus) {
                if (data !== null && data.code && data.code === 100) {
                    if (cur_word !== null)
                        add_to_dict_checker();
                    $('#new_dict').val("");
                    $('#dict_list').append($('<span class="dict_name">' + name + '</span>'));
                }
            }, "json");
        } else {
            $('#add_dict_alert').text("Illegal characters.");
            $('#add_dict_alert').css({'visibility': 'visible'});
        }
    }

    $('#add_dict').live('click', start_add_dict);

    $('.dict_name').live('click', function (event) {
        $active_dict.removeClass("active");
        $active_dict = $(event.target);
        $active_dict.addClass("active");
        draw_word_list($active_dict.text());
    });

    $('.dict_name_in_window').live('click', function (event) {
        var $dict = $(event.target);
        $dict.addClass("active");
        var dictName = $dict.text();
        var url = address + "/do?object=word&action=add&list=" + $dict.text() + "&word=" + cur_word + "&token=" + getToken();
        $.post(url, null, function (data, textStatus) {
            if (data !== null && data.code && data.code === 100) {
                $('#add_to_dict_fade ,  #add_to_dict_window').fadeOut(function () {
                    clear_window();
                    $('#add_to_dict_fade').remove();
                });
                if ($active_dict.text() === dictName)
                    draw_word_list(dictName);
                add_to_dict_checker();
            }
        }, "json");
    });

    $('.dict_element').live('click', function (event) {
        var $word = $(event.target);
        translate($word.data("word"));
    });

    signin();

})
;

var signin = function () {
    var url = address + "/signin";
    $.getJSON(url, function (response) {
        if (response.code === 101) {
            fill_all(response.username);
        }
    });
};

var fill_all = function (username) {
    login(username);
    url = address + "/do?object=list&action=get&token=" + getToken();
    $.getJSON(url, function (response) {
        draw_dict_list(response);
        draw_word_list(response[0]);
    });
};

var draw_dict_list = function (dict_list) {
    var $dict_list = $('#dict_list');
    $dict_list.html("");
    for (var i = 0; i < dict_list.length; i++) {
        var $dict = $('<span class="dict_name">' + dict_list[i] + '</span>');
        if (i === 0) {
            $active_dict = $dict;
            $active_dict.addClass("active");
        }
        $dict_list.append($dict);
    }
};

var draw_word_list = function (dict_name) {
    url = address + "/do?object=word&action=get&list=" + dict_name + "&token=" + getToken();
    $.getJSON(url, function (word_list) {
        var $word_list = $('#word_list');
        $word_list.html("");
        for (var i = 0; i < word_list.length; i++) {
            var $word = $('<span class="dict_element" data-word="' + word_list[i].word + '">' + word_list[i].word + '<font color="#99c1b9"  data-word="' + word_list[i].word + '"> - ' + word_list[i].translation + '</font></span>');
            $word_list.append($word);
        }
    });
};

var auth = function (token) {
    var cookieName = "auth";
    var nDays = 30;
    var today = new Date();
    var expire = new Date();
    if (nDays == null || nDays == 0) nDays = 1;
    expire.setTime(today.getTime() + 3600000 * 24 * nDays);
    document.cookie = cookieName + "=" + token + ";expires=" + expire.toGMTString();
    return false;
};

var getToken = function () {
    var arr = document.cookie.split("=");
    if (arr.length === 2 && arr[0] === "auth")
        return arr[1];
};

var clear_window = function () {
    $('#su_login').val("");
    $('#su_password').val("");
    $('#si_login').val("");
    $('#si_password').val("");
    $('#sur_password').val("");
    $('#incorrect').css({'visibility': 'hidden'});
    $('#username_alert').css({'visibility': 'hidden'});
    $('#pass_dont_match').css({'visibility': 'hidden'});
};

var login = function (username) {
    logon = true;
    $('#word_article').html("");
    $('#original_word').val("");
    $('#word_list').css({'visibility': 'visible'});
    $('#dict').css({'visibility': 'visible'});
    $('#sign_in').css({'display': 'none'});
    $('#sign_up').css({'display': 'none'});
    $('#username').text(username);
    $('#username').css({'display': 'block'});
    $('#sign_out').css({'display': 'block'});
};

var logout = function () {
    logon = false;
    cur_word = null;
    document.cookie = "auth=;expires=Thu, 01 Jan 1970 00:00:01 GMT";
    $('#word_article').html("");
    $('#original_word').val("");
    $('#add_dict_alert').css({'visibility': 'hidden'});
    $('#add_to_dict').css({'visibility': 'hidden'});
    $('#dict').css({'visibility': 'hidden'});
    $('#dict_list').html("");
    $('#word_list').css({'visibility': 'hidden'});
    $('#word_list').html("");
    $('#new_dict').val("");
    $('#username').css({'display': 'none'});
    $('#sign_out').css({'display': 'none'});
    $('#sign_up').css({'display': 'block'});
    $('#sign_in').css({'display': 'block'});
};

var add_to_dict_checker = function () {
    var url = address + "/do?object=list&action=get&not_have_word=" + cur_word + "&token=" + getToken();
    $.getJSON(url, function (list) {
        if (list.length > 0) {
            draw_dict_list_in_window(list);
            $('#add_to_dict').css({'visibility': 'visible'});
        } else {
            $('#add_to_dict').css({'visibility': 'hidden'});
        }
    });
};

var draw_dict_list_in_window = function (list) {
    $dict_list = $('#dict_list_in_window');
    $dict_list.html("");
    for (var i = 0; i < list.length; i++) {
        var $dict = $('<span class="dict_name_in_window">' + list[i] + '</span>');
        $dict_list.append($dict);
    }
};

var correct_username = function (username) {
    if (username.length == 0) {
        $('#username_alert').text("Empty username.");
        $('#username_alert').css({'visibility': 'visible'});
        return false;
    }
    if (legal_characters(username)) {
        $('#username_alert').css({'visibility': 'hidden'});
        return true;
    } else {
        $('#username_alert').text("Illegal characters in username.");
        $('#username_alert').css({'visibility': 'visible'});
        return false;
    }
};

var correct_passwords = function () {
    if ($('#su_password').val().length === 0) {
        $('#pass_dont_match').text("Empty password.");
        $('#pass_dont_match').css({'visibility': 'visible'});
        return false;
    }
    if (!legal_characters($('#su_password').val())) {
        $('#pass_dont_match').text("Illegal characters in password.");
        $('#pass_dont_match').css({'visibility': 'visible'});
        return false;
    }
    if ($('#su_password').val() !== $('#sur_password').val()) {
        $('#pass_dont_match').text("Passwords don't match.");
        $('#pass_dont_match').css({'visibility': 'visible'});
        return false;
    } else {
        $('#pass_dont_match').css({'visibility': 'hidden'});
        return true;
    }
};

var legal_characters = function (string) {
    /*for (var i = 0; i < string.length; i++) {
        var code = string.charCodeAt(i);
        if (((code >= 65) && (code <= 90)) || ((code >= 97) && (code <= 122)) || ((code >= 48) && (code <= 57))) {
            continue;
        } else {
            return false;
        }
    }*/
    return string.indexOf("<") == -1 && string.indexOf(">") == -1;
};

var translate = function (word) {
    var url = address + "/translate?word=" + word + "&token=" + getToken();
    $('#add_to_dict').css({'visibility': 'hidden'});
    $word_article = $('#word_article');
    $.getJSON(url, function (response) {
        response = response.article_json;
        $word_article.html("");
        var $original = $("<div class=\"original\">");
        var $original_text = $("<span class=\"original_text\">");
        $original.append($original_text);
        if (response.def.length > 0) {
            $original_text.append(response.def[0].text);
            if (response.def[0].ts)
                $original_text.append("<font color=\"#6f1014\"> [" + response.def[0].ts + "]</font>");
            $word_article.append($original);

            for (var i = 0; i < response.def.length; i++) {
                var type = response.def[i];
                var $word = $("<div class=\"word\">");
                var $pos = $("<span class=\"pos\">");
                $pos.append("/" + type.tr[0].pos + "/");
                $word.append($pos);

                for (var j = 0; j < type.tr.length; j++) {
                    var translation = type.tr[j];
                    var $tran = $("<div class=\"tran\">");
                    var $tran_text = $("<span class=\"tran_text\">");
                    $tran_text.append("<font color=\"#0d7248\" size=\"+2\">" + translation.text + "</font>");

                    if (translation.syn) {
                        for (var k = 0; k < translation.syn.length; k++) {
                            $tran_text.append(", " + translation.syn[k].text);
                        }
                    }
                    $tran.append($tran_text);

                    if (translation.mean && translation.mean.length > 0) {
                        var $tran_mean = $("<span class=\"tran_mean\">");
                        $tran_mean.append("(" + translation.mean[0].text);
                        for (var k = 1; k < translation.mean.length; k++) {
                            $tran_mean.append(", " + translation.mean[k].text);
                        }
                        $tran_mean.append(")");
                        $tran.append($tran_mean);
                    }
                    if (translation.ex && translation.ex.length > 0) {
                        var $ex = $("<div class=\"ex\">");
                        for (var k = 0; k < translation.ex.length; k++) {
                            var $tran_ex = $("<span class=\"tran_ex\">");
                            $tran_ex.append(translation.ex[k].text + " - " + translation.ex[k].tr[0].text);
                            $ex.append($tran_ex);
                        }
                        $tran.append($ex);
                    }

                    $word.append($tran);
                }
                $word_article.append($word);
            }
            if (logon) {
                cur_word = response.def[0].text;
                if ($active_dict.text() === "history")
                    draw_word_list("history");
                add_to_dict_checker();
            }
        } else {
            $original_text.append("No translation found.");
            $word_article.append($original);
            cur_word = null;
        }
    });
    return false;
};

var my_window = function (id, width, height) {
    $('#' + id).click(function () {
        var cur_window = '#' + id + '_window';
        $(cur_window).keyup(function (e) {
            if (e.keyCode == 27) { $('#' + id + '_fade').click(); }     // enter
        });
        document.onkeydown = function(evt) {
            evt = evt || window.event;
            if (evt.keyCode == 27) {
                $('#' + id + '_fade').click();
            }
        };
        $(cur_window).fadeIn().css({'width': width, 'height': height});
        var popMargTop = ($(cur_window).height() + 80) / 2;
        var popMargLeft = ($(cur_window).width() + 80) / 2;

        $(cur_window).css({
            'margin-top': -popMargTop,
            'margin-left': -popMargLeft
        });
        $('body').append('<div class="fade" id=' + id + '_fade></div>');
        $('.fade').css({'filter': 'alpha(opacity=80)'}).fadeIn();

        return false;
    });

    $('#' + id + '_fade').live('click', function () {
        $('#' + id + '_fade ,  #' + id + '_window').fadeOut(function () {
            clear_window();
            $('#' + id + '_fade').remove();
        });
        return false;
    });
};