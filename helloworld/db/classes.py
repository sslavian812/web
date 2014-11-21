from sqlalchemy import Column, Integer, String, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
Base = declarative_base()


class User(Base):
    __tablename__ = 'users'
    _id        = Column(Integer, primary_key=True)
    login      = Column(String)
    password   = Column(String)
    email      = Column(String)
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


metadata = Base.metadata