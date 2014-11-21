from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from classes import metadata, User, Word, List


class DBAdaptor:
    def __init__(self):
        self.engine = create_engine('sqlite:///database.db', echo=True)
        #self.engine = create_engine('sqlite:///:memory:', echo=True)
        self.metadata = metadata
        self.metadata.create_all(self.engine)
        self.Session = sessionmaker(bind = self.engine)

    def create_session(self):
        session = self.Session()
        return session

    def add_user(self, login, password):
        session = self.create_session()
        session.add(User(login, password, '-', login, '-'))
        session.commit()

    def add_word(self, original, source, dest, translation, list_id):
        session = self.create_session()
        session.add(Word(original, source, dest, translation, list_id))
        session.commit()

    def add_list(self, owner_id, name):
        session = self.create_session()
        session.add(List(owner_id, name))
        session.commit()

    ####################

    def drop_users(self):
        session = self.create_session()
        for u in session.query(User).all():
            session.delete(u)
        session.commit()

    def drop_words(self):
        session = self.create_session()
        for w in session.query(Word).all():
            session.delete(w)
        session.commit()

    def drop_words(self):
        session = self.create_session()
        for l in session.query(List).all():
            session.delete(l)
        session.commit()

    ####################

    def get_user(self, login):
        session = self.create_session()
        return session.query(User).filter_by(login=login).first()

    def get_user(self, login, password):
        session = self.create_session()
        return session.query(User).filter_by(
            login=login,
            password=password
        ).first()


adaptor = DBAdaptor()
