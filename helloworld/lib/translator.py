# coding=utf-8
import urllib
import functools

__author__ = 'Sergey'
__see__ = """
        http://api.yandex.com/dictionary/doc/dg/reference/lookup.xml
        http://api.yandex.com/translate/doc/dg/reference/translate.xml
"""


class Translation:
    meanings = []
    synonyms = []
    examples = []
    position = ""
    text = ""

    def __init__(self, synonyms, meanings, examples, position, text):
        self.synonyms = synonyms
        self.meanings = meanings
        self.examples = examples
        self.position = position
        self.text = text


class DictionaryEntry:
    word = ""
    transcription = ""
    position = ""
    translations = []

    def __init__(self, word, transcription, position, translations):
        self.word = word
        self.transcription = transcription
        self.position = position
        self.translations = translations


class Article:
    def __init__(self, original, main_translation):
        self.original = original
        self.main_translation = main_translation

    original = ""
    main_translation = ""


class DictionaryArticle(Article):
    translations = []
    transcription = ""
    entries = []

    def __init__(self, original, main_translation, transcription, entries):
        Article.__init__(self, original, main_translation)
        self.transcription = transcription
        self.entries = entries


translation_direction_ru_en = "ru-en"
translation_direction_en_ru = "en-ru"
translation_direction_default = translation_direction_en_ru

url_dict = 'https://dictionary.yandex.net/api/v1/dicservice.json/lookup?key={0}&lang={1}&text={2}'
url_trnsl = 'https://translate.yandex.net/api/v1.5/tr.json/translate?key={0}&lang={1}&text={2}'

dict_api_key = 'dict.1.1.20141116T162732Z.6367256efc30c377.79e985914e47344e3856d26681dce9dca74d9fd6'
trnsl_api_key = 'trnsl.1.1.20131003T161157Z.7dadb380384bbab6.321816b6e72f632f1a1f5bbab86b2799ed0d63d6'

import requests


class Translator:
    def __init__(self):
        pass

    @staticmethod
    def __get_translation_direction(text):
        for c in text:
            if 'A' <= c <= 'Z' or 'a' <= c <= 'z':
                return translation_direction_en_ru
            elif 'А' <= c <= 'Я' or 'а' <= c <= 'я':
                return translation_direction_ru_en
        return translation_direction_default

    @staticmethod
    def __get_dict_article(word):
        url = url_dict.format(dict_api_key, Translator.__get_translation_direction(word), urllib.quote(word))
        try:
            response = requests.get(url).json()
        except (requests.ConnectionError, ValueError):
            return None
        entries = []
        for d in (response['def'] if 'def' in response else []):
            translations = []
            for tr in (d['tr'] if 'tr' in d else []):
                t = Translation(map(lambda x: x['text'], tr['syn'] if 'syn' in tr else []),
                                map(lambda x: x['text'], tr['mean'] if 'mean' in tr else []),
                                map(lambda x: (x['text'], x['tr']), tr['ex'] if 'ex' in tr else []),
                                tr['pos'] if 'pos' in tr else None,
                                tr['text'] if 'text' in tr else None)
                translations.append(t)
            entry = DictionaryEntry(d['text'], d['tr'], d['pos'], translations)
            entries.append(entry)
        if len(entries) == 0:
            return None
        result = DictionaryArticle(word, entries[0].translations[0].text, entries[0].transcription, entries)
        if result.original == result.main_translation:
            return None
        return result

    @staticmethod
    def __get_trnsl_article(word):
        url = url_trnsl.format(trnsl_api_key, Translator.__get_translation_direction(word), urllib.quote(word))
        try:
            response = requests.get(url).json()
        except (requests.ConnectionError, ValueError):
            return None
        if 'text' in response:
            return Article(word,
                           functools.reduce(lambda acc, c: (acc + "\n" if len(acc) > 0 else "") + c,
                                            response['text'], ""))
        return None

    @staticmethod
    def get_any_article(word):
        result = Translator.__get_dict_article(word)
        if result is None or len(result.entries) == 0:
            result = Translator.__get_trnsl_article(word)
        return result


def test():
    result = Translator.get_any_article("time")
    assert result.main_translation == u"время"
    result = Translator.get_any_article("You've got time")
    assert result.main_translation == u'У вас есть время'


test()