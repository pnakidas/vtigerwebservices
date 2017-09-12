# original source @https://bitbucket.org/sidharth/vtiger-webservice-clients/src
import unittest
import testconfig

from vtigerwebservices import Webservice, WebserviceException


class TestSequenceFunctions(unittest.TestCase):
    def setUp(self):
        self.instance = Webservice(testconfig.serviceUrl, testconfig.username, testconfig.accessKey)
        self.instance.login()

    def testLogin(self):
        self.assertTrue(self.instance.login())

    def testInvalidLogin(self):
        instance = Webservice(testconfig.serviceUrl, 'user', 'key')
        self.assertRaises(WebserviceException, instance.login)

    def testListTypes(self):
        result = self.instance.listtypes()
        # To keep this simple just check for the contact module
        self.assertTrue('Contacts' in result['types'])
        contacts = result['information']['Contacts']
        expected = dict(isEntity=True, singular='Contact', label='Contacts')
        self.assertEqual(expected, contacts)

    def testDescribe(self):
        result = self.instance.describe('Contacts')
        self.assertEqual('Contacts', result['name'])
        # TODO - might want to add one more field assertion here

    def testCrud(self):
        inst = self.instance
        data = dict(firstname='Pinaki', lastname='Das', email='vtigerdevel@gmail.com', assigned_user_id=inst.userId)
        contact = inst.create(data, 'Contacts')
        self.assertTrue(contact['id'] is not None)
        self.assertEqual('Pinaki', contact['firstname'])
        self.assertEqual('Das', contact['lastname'])
        contactId = contact['id']

        contact = inst.retrieve(contactId)
        self.assertEqual(contactId, contact['id'])
        self.assertEqual('Pinaki', contact['firstname'])
        self.assertEqual('Das', contact['lastname'])

        contact['description'] = 'Updated Description'
        contact = inst.update(contact)
        self.assertEqual('Updated Description', contact['description'])

        contact = inst.retrieve(contactId)
        self.assertEqual('Updated Description', contact['description'])

        # TODO - check if we need to add a test case of revise
        inst.delete(contactId)
        self.assertRaises(WebserviceException, inst.retrieve, contactId)

    def testQuery(self):
        inst = self.instance
        data = dict(firstname='Pinaki', lastname='Das', email='vtigerdevel@gmail.com', assigned_user_id=inst.userId)
        contact = inst.create(data, 'Contacts')
        contactId = contact['id']
        result = inst.query("select firstname, lastname from Contacts where id='%s';" % contactId)
        expected = [{'lastname': 'Das', 'id': contactId, 'firstname': 'Pinaki'}]
        self.assertEqual(expected, result)
        inst.delete(contactId)


if __name__ == '__main__':
    unittest.main()
