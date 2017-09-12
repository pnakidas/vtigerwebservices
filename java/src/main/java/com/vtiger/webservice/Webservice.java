package com.vtiger.webservice;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sidharth
 */
@SuppressWarnings({"unchecked", "unused", "rawtypes"})
public class Webservice {
	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	public static class TypeInfo {
		private boolean isEntity;
		private String label;
		private String singular;
		private Map<String, Object> information;

		public TypeInfo(Map<String, Object> fields) {
			information = fields;
			isEntity = (Boolean) fields.get("isEntity");
			label = (String) fields.get("fields");
			singular = (String) fields.get("singular");
		}

		/**
		 * @return the isEntity
		 */
		public boolean isEntity() {
			return isEntity;
		}

		/**
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * @return the singular
		 */
		public String getSingular() {
			return singular;
		}

	}

	public static class DescribeTypeInfo {
		private String name;
		private Map<String, Object> information;

		public DescribeTypeInfo(Map<String, Object> info) {
			name = (String) info.get("name");
			information = info;
		}

	}

	public static class DescribeFieldInfo {
		private boolean editable;
		private String label;
		private boolean mandatory;
		private String name;
		private boolean nullable;
		private DescribeTypeInfo type;

		private Map<String, Object> information;

		public DescribeFieldInfo(Map<String, Object> info) {
			information = info;

			editable = (Boolean) info.get("editable");
			label = (String) info.get("label");
			mandatory = (Boolean) info.get("mandatory");
			name = (String) info.get("name");
			nullable = (Boolean) info.get("nullable");
			type = new DescribeTypeInfo((Map<String, Object>) info.get("type"));
		}

		/**
		 * @return the editable
		 */
		public boolean isEditable() {
			return editable;
		}

		/**
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * @return the mandatory
		 */
		public boolean isMandatory() {
			return mandatory;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the nullable
		 */
		public boolean isNullable() {
			return nullable;
		}

		/**
		 * @return the type
		 */
		public DescribeTypeInfo getType() {
			return type;
		}

	}

	public static class DescribeInfo {
		private String labelFields;
		private boolean deleteable;
		private boolean entity;
		private String label;
		private boolean retrieveable;
		private boolean createable;
		private boolean updateable;
		private String name;
		private String idPrefix;
		private Map<String, DescribeFieldInfo> fields;
		private Map<String, Object> information;

		public DescribeInfo(Map<String, Object> info) {
			information = info;

			labelFields = (String) info.get("labelFields");
			deleteable = (Boolean) info.get("deleteable");
			entity = (Boolean) info.get("isEntity");
			label = (String) info.get("label");
			retrieveable = (Boolean) info.get("retrieveable");
			createable = (Boolean) info.get("createable");
			updateable = (Boolean) info.get("updateable");
			name = (String) info.get("name");
			idPrefix = (String) info.get("idPrefix");

			Map<String, DescribeFieldInfo> fieldInfo = new HashMap<String, DescribeFieldInfo>();
			List<Map<String, Object>> fields = (List<Map<String, Object>>) info.get("fields");
			for (Map<String, Object> field : fields) {
				fieldInfo.put((String) field.get("name"), new DescribeFieldInfo(field));
			}
			this.fields = Collections.unmodifiableMap(fieldInfo);
		}

		/**
		 * @return the labelFields
		 */
		public String getLabelFields() {
			return labelFields;
		}

		/**
		 * @return the deleteable
		 */
		public boolean isDeleteable() {
			return deleteable;
		}

		/**
		 * @return the entity
		 */
		public boolean isEntity() {
			return entity;
		}

		/**
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * @return the retrieveable
		 */
		public boolean isRetrieveable() {
			return retrieveable;
		}

		/**
		 * @return the createable
		 */
		public boolean isCreateable() {
			return createable;
		}

		/**
		 * @return the updateable
		 */
		public boolean isUpdateable() {
			return updateable;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the idPrefix
		 */
		public String getIdPrefix() {
			return idPrefix;
		}

		/**
		 * @return the fields
		 */
		public Map<String, DescribeFieldInfo> getFields() {
			return fields;
		}

	}

	private String serviceUrl;
	private String username;
	private String accessKey;

	private String userId;
	private String sessionName;

	Json json = new SimpleJson();

	/**
	 * Create a new webservice instance
	 * 
	 * @param serviceUrl
	 *            - The url for the webservice
	 * @param username
	 *            - The name of the user to log in as
	 * @param accessKey
	 *            - The user's access key from the My Preferences page.
	 */
	public Webservice(String serviceUrl, String username, String accessKey) {
		this.serviceUrl = serviceUrl;
		this.username = username;
		this.accessKey = accessKey;
	}

	/**
	 * Login to the webservice.
	 *
	 * This operation must be carried out before any of the other operations.
	 *
	 * The operation will throw a WebserviceException if it fails.
	 * 
	 * @return true if the login is a succes
	 * @throws com.vtiger.webservice.WebserviceException
	 */
	public boolean login() throws WebserviceException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("operation", "getchallenge");
		params.put("username", username);
		Map<String, Object> challengeResponse = doGet(params);
		if (((Boolean) challengeResponse.get("success")) == true) {
			Map<String, Object> challengeResult = (Map<String, Object>) challengeResponse.get("result");
			String token = (String) challengeResult.get("token");
			params = new HashMap();
			params.put("operation", "login");
			params.put("username", username);
			params.put("accessKey", Util.md5(token + accessKey));
			Map<String, Object> loginResponse = doPost(params);
			if (((Boolean) loginResponse.get("success") == true)) {
				Map<String, Object> loginResult = (Map<String, Object>) loginResponse.get("result");
				userId = (String) loginResult.get("userId");
				sessionName = (String) loginResult.get("sessionName");
				return true;
			} else {
				throw new WebserviceException("Login Failed");
			}
		} else {
			throw new WebserviceException("Login Failed");
		}
	}

	public boolean logout() throws WebserviceException {
		post("logout", Collections.<String, String>emptyMap());
		return true;
	}

	/**
	 * Get a list of types accessable by the current user.
	 *
	 * @return A list of TypeTnfo objects
	 * @throws com.vtiger.webservice.WebserviceException
	 */
	public Map<String, TypeInfo> listTypes() throws WebserviceException {
		Map<String, Object> result = (Map<String, Object>) get("listtypes", Collections.EMPTY_MAP);
		Map<String, Map<String, Object>> information = (Map<String, Map<String, Object>>) result.get("information");
		Map<String, TypeInfo> out = new HashMap<String, TypeInfo>();
		for (Map.Entry<String, Map<String, Object>> entry : information.entrySet()) {
			String key = entry.getKey();
			final Map<String, Object> value = entry.getValue();
			out.put(key, new TypeInfo(value));
		}
		return out;
	}

	/**
	 * Describe a particular type.
	 *
	 * @param name
	 *            The name of the type.
	 * @return A DescribeInfo object
	 * @throws com.vtiger.webservice.WebserviceException
	 */
	public DescribeInfo describe(String name) throws WebserviceException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("elementType", name);
		Map<String, Object> result = (Map<String, Object>) get("describe", params);
		return new DescribeInfo(result);
	}

	/**
	 * Create a new entity.
	 *
	 * The returned data will conaing a field "id" which will be the new
	 * entity's id.
	 *
	 * @param name
	 *            The name of the type to save as.
	 * @param data
	 *            The data to save.
	 * @return A map representing the saved entity.
	 * @throws com.vtiger.webservice.WebserviceException
	 */
	public Map<String, Object> create(String name, Map<String, Object> data) throws WebserviceException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("elementType", name);
		params.put("element", json.write(data));
		return (Map<String, Object>) post("create", params);
	}

	/**
	 * Retrieve an entity
	 *
	 * @param id
	 *            The id of the entity to retrieve.
	 * @return
	 * @throws com.vtiger.webservice.WebserviceException
	 */
	public Map<String, Object> retrieve(String id) throws WebserviceException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", id);
		return (Map<String, Object>) get("retrieve", params);

	}

	/**
	 * Update an existing entity.
	 *
	 * @param data
	 *            The entity to save.
	 * @return The updated entity.
	 * @throws com.vtiger.webservice.WebserviceException
	 */
	public Map<String, Object> update(Map<String, Object> data) throws WebserviceException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("element", json.write(data));
		return (Map<String, Object>) post("update", params);
	}

	/**
	 * Delete an entity.
	 *
	 * @param id
	 *            The id of the entity to delete.
	 * @return true if successful false otherwise.
	 * @throws com.vtiger.webservice.WebserviceException
	 */
	public boolean delete(String id) throws WebserviceException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", id);
		Map<String, Object> data = (Map<String, Object>) post("delete", params);
		boolean status = data.get("status").equals("successful");
		return status;
	}

	/**
	 * Execute a vtiger webservices query.
	 *
	 * @param query - The query to execute
	 * @return A list of Maps representing the queried fields.
	 * @throws com.vtiger.webservice.WebserviceException
	 */
	public List<Map<String, Object>> query(String query) throws WebserviceException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("query", query);
		return (List<Map<String, Object>>) get("query", params);
	}

	private Object get(String operation, Map<String, String> parameters) throws WebserviceException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("operation", operation);
		map.put("sessionName", sessionName);
		map.putAll(parameters);
		Map<String, Object> result = doGet(map);
		if ((Boolean) result.get("success")) {
			return result.get("result");
		} else {
			throw new WebserviceException(result.get("error").toString());
		}
	}

	public Object post(String operation, Map<String, String> parameters) throws WebserviceException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("operation", operation);
		map.put("sessionName", sessionName);
		map.putAll(parameters);
		Map<String, Object> result = doPost(map);
		if ((Boolean) result.get("success")) {
			return result.get("result");
		} else {
			throw new WebserviceException(result.get("error").toString());
		}
	}

	public Map<String, Object> doGet(Map<String, String> parameters) throws WebserviceException {
		try {
			String paramString = paramString(parameters);
			URL url = new URL(serviceUrl + "?" + paramString);
			URLConnection con = url.openConnection();
			String data = Util.readStream(con.getInputStream());
			return (Map<String, Object>) json.read(data);
		} catch (MalformedURLException ex) {
			throw new WebserviceException("Invalid service url", ex);
		} catch (IOException ex) {
			throw new WebserviceException("Unable to connect to service url", ex);
		}
	}

	public Map<String, Object> doPost(Map<String, String> parameters) throws WebserviceException {
		try {
			String paramString = paramString(parameters);
			URL url = new URL(serviceUrl);
			URLConnection con = url.openConnection();

			con.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			wr.write(paramString);
			wr.flush();
			String data = Util.readStream(con.getInputStream());
			return (Map<String, Object>) json.read(data);
		} catch (MalformedURLException ex) {
			throw new WebserviceException("Invalid service url", ex);
		} catch (IOException ex) {
			throw new WebserviceException("Unable to connect to service url", ex);
		}
	}

	private String paramString(Map<String, String> parameters) {
		try {
			List<String> parts = new ArrayList<String>();
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				String key = URLEncoder.encode(entry.getKey(), "utf-8");
				String value = URLEncoder.encode(entry.getValue(), "utf-8");
				parts.add(key + "=" + value);

			}
			String paramString = Util.join("&", parts);
			return paramString;
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("This system doesn't support utf-8 encodeing");
		}
	}
}
