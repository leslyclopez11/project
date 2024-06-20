import static org.junit.Assert.*;

import java.beans.Transient;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.junit.Test;
public class DataManager_attemptLogin_Test {
    /*
	 * This is a test class for the DataManager.createFund method.
	 */

     @Test 
     public void testSuccessfulLogin() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"success\",\"data\":{\"_id\":\"66578ea0d659812ff80f7c2d\",\"name\":\"Develop for Snoppy\",\"description\":\"organization for snoppy\",\"funds\":[{\"_id\":\"66578eedd659812ff80f7c32\",\"name\":\"Fund for cats\",\"description\":\"help cats\",\"target\":500,\"donations\":[{\"contributor\":\"contributorId\",\"amount\":100,\"date\":\"2023-01-01\"}]}]}}";

			}
			
		});

        Organization org = dm.attemptLogin("snoopy","bestsnoopy");
        assertNotNull(org);
        assertEquals("66578ea0d659812ff80f7c2d", org.getId());
        assertEquals("Develop for Snoppy", org.getName());


     }

     @Test 
     public void testFailedLogin() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"failure\"}";

			}
			
		});
        Organization org = dm.attemptLogin("snoopy", "123");
        assertNull(org);
     }

     @Test 
     public void testLoginthrowException() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				throw new RuntimeException("exception");

			}
			
		});
        Organization org = dm.attemptLogin("snoopy", "123");
        assertNull(org);
     }

     @Test(expected = IllegalStateException.class)
	public void testInvalidJsonResponse() {
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "Invalid JSON response";
			}
		});
		
		Organization org = dm.attemptLogin("testLogin", "testPassword");
		
		assertNull(org);
	}
}
