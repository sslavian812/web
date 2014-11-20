from sqlalchemy import Table, Column, Integer, String, MetaData, ForeignKey
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from sqlalchemy.ext.declarative import declarative_base
Base = declarative_base()



class User(Base):
    __tablename__ = 'users'
    _id        = Column(Integer, primary_key=True)
    login      = Column(String)
    password   = Column(String)
    email      = Column(String)
    first_name = Column(String)
    last_name  = Column(String)

    def __init__(self, login, password, email, first_name, last_name):
        self.login      = login
        self.password   = password
        self.email      = email
        self.first_name = first_name
        self.last_name  = last_name

    def __repr__(self):
        return "<User('%s', '%s', '%s','%s', '%s')>" % (
                self.login,
                self.password,
                self.email,
                self.first_name,
                self.last_name
        )


class List(Base):
    __tablename__ = 'lists'
    _id        = Column(Integer, primary_key=True)
    owner_id   = Column(Integer, ForeignKey(User._id))
    name       = Column(String)

    def __init__(self, owner_id, name):
        self.owner_id = owner_id
        self.name = name

    def __repr__(self):
        return "<List('%s', '%s')>" % (
                self.owner_id,
                self.name
        )

class Word(Base):
    __tablename__ = 'words'
    _id         = Column(Integer, primary_key=True)
    original    = Column(String)
    source      = Column(String)
    dest        = Column(String)
    translation = Column(String)
    list_id     = Column(Integer, ForeignKey(List._id))

    def __init__(self, original, source, dest, translation, owner_id):
        self.original    = original
        self.source      = source
        self.dest        = dest
        self.translation = translation
        self.owner_id    = owner_id

    def __repr__(self):
        return "<Word('%s' ['%s'->'%s'] '%s' : '%s' )>" % (
                self.original,
                self.source,
                self.dest,
                self.translation,
                self.list_id
        )


class DBAdaptor:
    def __init__(self):
        self.engine = create_engine('sqlite:///database.db', echo=True)
        #self.engine = create_engine('sqlite:///:memory:', echo=True)
        self.metadata = Base.metadata
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
