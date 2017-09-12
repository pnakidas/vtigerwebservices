package com.vtiger.webservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import junit.framework.TestCase;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;

/**
 * @author sidharth
 */
@SuppressWarnings("unchecked")
public class WebserviceTest extends TestCase {

	public WebserviceTest(String testName) {
		super(testName);
	}

	@Override
	protected void setUp() throws Exception {
		setupTrustManager();
		super.setUp();
	}
	
	/**
	 * Create a trust manager that does not validate certificate chains
	 * required for testing SSL connections
	 * Note - it would be better to get the crt file for the server and install rather than bypassing this completely
	 */
	private void setupTrustManager() {
		try {
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };

			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private Map<String, String> getLoginDetails() {
		Properties prop = new Properties();
		InputStream input = null;
		Map<String, String> loginDetails = new HashMap<String, String>();
		
		try {
	        input = new FileInputStream("config.properties");
	        prop.load(input);
	        
			loginDetails.put("url", prop.getProperty("url"));
			loginDetails.put("username", prop.getProperty("username"));
			loginDetails.put("accesskey", prop.getProperty("accesskey"));
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    } finally {
	        if (input != null) {
	            try {
	                input.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
		return loginDetails;
	}

	/**
	 * Test of login method, of class Webservice.
	 */
	public void testLogin() throws Exception {
		System.out.println("login");

		Map<String, String> loginInfo = getLoginDetails();
		Webservice ws = new Webservice(loginInfo.get("url"), loginInfo.get("username"), loginInfo.get("accesskey"));
		ws.login();
	}

	public void testRetrieveInventory() throws Exception {
		System.out.println("retrieve inventory");

		Webservice ws = instance();
		String salesorderId = "6x1335";
		Map<String, Object> salesorder = ws.retrieve(salesorderId);

		ArrayList<Map<String, String>> lineItems = (ArrayList<Map<String, String>>) salesorder.get("LineItems");
		for (Map<String, String> lineItem : lineItems) {
			assertEquals(salesorderId, lineItem.get("parent_id"));
		}
		ws.logout();
	}

	public void testQuery() throws Exception {
		System.out.println("query");
		Webservice ws = instance();
		List<Map<String, Object>> contactsList = ws.query("select * from Contacts where firstname='Pinaki' limit 2;");
		Map<String, Object> contact = contactsList.get(1);
		String firstname = (String) contact.get("firstname");
		assertTrue("Pinaki".equalsIgnoreCase(firstname));
		ws.logout();
	}

	public void testListTypes() throws Exception {
		System.out.println("listTypes");
		Webservice ws = instance();
		Map<String, Webservice.TypeInfo> map = ws.listTypes();
		Webservice.TypeInfo contacts = map.get("Contacts");
		assertEquals("Contact", contacts.getSingular());
		assertTrue(contacts.isEntity());
		ws.logout();
	}

	public void testDescribe() throws Exception {
		System.out.println("describe");
		Webservice ws = instance();
		Webservice.DescribeInfo desc = ws.describe("Contacts");
		assertEquals("Contacts", desc.getName());
		assertEquals("firstname", desc.getFields().get("firstname").getName());
	}

	public void testCRUD() throws Exception {
		System.out.println("CRUD");
		Webservice ws = instance();
		Map<String, Object> data = new HashMap<String, Object>();
		
		String firstname = generateRandomString(6);
		String lastname = generateRandomString(6);
		data.put("firstname", firstname);
		data.put("lastname", lastname);
		data.put("email", "vtigerdevel@gmail.com");
		data.put("assigned_user_id", ws.getUserId());

		Map<String, Object> created = ws.create("Contacts", data);

		String id = (String) created.get("id");
		assertNotNull(id);

		Map<String, Object> retrieved = ws.retrieve(id);
		assertEquals(created, retrieved);
		
		lastname = lastname + "-updated";
		created.put("lastname", lastname);
		Map<String, Object> updated = ws.update(created);
		assertEquals(lastname, updated.get("lastname"));

		ws.delete(id);
	}

	private Webservice instance() throws WebserviceException {
		Map<String, String> loginInfo = getLoginDetails();
		Webservice ws = new Webservice(loginInfo.get("url"), loginInfo.get("username"), loginInfo.get("accesskey"));
		ws.login();
		return ws;
	}
	
	/**
	 * if you need to generate a random email/name for testing
	 */
	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private String generateRandomString(int len) {
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		}
		return sb.toString();
	}
}
