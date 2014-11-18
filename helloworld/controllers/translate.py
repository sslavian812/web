import logging

from pylons import request, response, session, tmpl_context as c, url
from pylons.controllers.util import abort, redirect

from helloworld.lib.base import BaseController, render

log = logging.getLogger(__name__)

class TranslateController(BaseController):

    def index(self):
        return render('/translate.mako')

    def argh(self):
        return 'aargh!!'