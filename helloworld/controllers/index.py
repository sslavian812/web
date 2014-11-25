__author__ = 'Sergey'

import logging

from helloworld.lib.base import BaseController, render

log = logging.getLogger(__name__)

class IndexController(BaseController):

    def index(self):
        return render('/index.mako')

    def error(self):
        return 'error!'