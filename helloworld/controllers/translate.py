import logging

from pylons import request, response, session, tmpl_context as c, url
from pylons.controllers.util import abort, redirect

from helloworld.lib.base import BaseController, render
from mako.template import Template

log = logging.getLogger(__name__)


class TranslateController(BaseController):
    def __call__(self, environ, start_response):
        start_response('200 OK', [('Content-Type', 'text/html')])
        word = None
        if 'word' in environ['wsgiorg.routing_args'][1]:
            word = environ['wsgiorg.routing_args'][1]['word']
        return render('/translate.mako', {'word':word})