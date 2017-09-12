# original source @https://bitbucket.org/sidharth/vtiger-webservice-clients/src
from urllib.parse import urlencode
from urllib.request import urlopen

import json
import hashlib


def md5(s):
    return hashlib.md5(s.encode()).hexdigest()


class WebserviceException(Exception):
    def __init__(self, code, message):
        self.code = code
        self.message = message

    def __repr__(self):
        return "VtigerWebserviceException(%s, %s)" % (self.code, self.message)


def exception(response):
    result = response['error']
    return WebserviceException(result['code'], result['message'])


class Webservice:
    # Connect to a vtiger instance.

    def __init__(self, serviceurl, username, accesskey):
        self.serviceUrl = serviceurl
        self.username = username
        self.accessKey = accesskey

    def doGet(self, **params):
        paramString = urlencode(params)
        response = urlopen('%s?%s' % (self.serviceUrl, paramString))

        try:
            return json.loads(response.read())
        except:
            return {'success': False, 'error': dict(code='Invalid JSON string', message='Invalid JSON string')}

    def doPost(self, **params):
        data = urlencode(params).encode()
        response = urlopen(self.serviceUrl, data = data)

        try:
            return json.loads(response.read())
        except:
            return {'success': False, 'error': dict(code='Invalid JSON string', message='Invalid JSON string')}

    def get(self, operation, **parameters):
        response = self.doGet(operation=operation, sessionName=self.sessionId, **parameters)
        if response['success']:
            return response['result']
        else:
            raise exception(response)

    def post(self, operation, **parameters):
        response = self.doPost(operation=operation, sessionName=self.sessionId, **parameters)
        if response['success']:
            return response['result']
        else:
            raise exception(response)

    def login(self):
        challengeResponse = self.doGet(operation='getchallenge', username=self.username)
        if challengeResponse['success']:
            token = challengeResponse['result']['token']
            encodedKey = md5(token + self.accessKey)
            loginResponse = self.doPost(operation='login', username=self.username, accessKey=encodedKey)
            if loginResponse['success']:
                self.sessionId = loginResponse['result']['sessionName']
                self.userId = loginResponse['result']['userId']
                return True
            else:
                raise exception(loginResponse)
        else:
            raise exception(challengeResponse)

    def logout(self):
        self.post('logout')

    def listtypes(self):
        return self.get('listtypes')

    def describe(self, name):
        return self.get('describe', elementType=name)

    def create(self, obj, objectType):
        objectJson = json.dumps(obj)
        return self.post('create', elementType=objectType, element=objectJson)

    def retrieve(self, id):
        return self.get('retrieve', id=id)

    def update(self, obj):
        objectJson = json.dumps(obj)
        return self.post('update', element=objectJson)

    def delete(self, id):
        return self.post('delete', id=id)

    def query(self, query):
        return self.get('query', query=query)
