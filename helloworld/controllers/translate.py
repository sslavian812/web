import logging
import urllib

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
        if word is None and 'QUERY_STRING' in environ:
            query = environ['QUERY_STRING']
            parts = str(query).split('&')
            for p in parts:
                if p.split('=')[0] == 'text':
                    word = urllib.unquote(p.split('=')[1])
                    break
        return render('/translate.mako', {'word': word})