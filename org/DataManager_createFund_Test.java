import static org.junit.Assert.*;

import java.util.Map;

import javax.management.RuntimeErrorException;

import org.junit.Test;

public class DataManager_createFund_Test {
	
	/*
	 * This is a test class for the DataManager.createFund method.
	 * Add more tests here for this method as needed.
	 * 
	 * When writing tests for other methods, be sure to put them into separate
	 * JUnit test classes.
	 */

	@Test
	public void testSuccessfulCreation() {

		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"success\",\"data\":{\"_id\":\"12345\",\"name\":\"new fund\",\"description\":\"this is the new fund\",\"target\":10000,\"org\":\"5678\",\"donations\":[],\"__v\":0}}";

			}
			
		});
		
		
		Fund f = dm.createFund("12345", "new fund", "this is the new fund", 10000);
		
		assertNotNull(f);
		assertEquals("this is the new fund", f.getDescription());
		assertEquals("12345", f.getId());
		assertEquals("new fund", f.getName());
		assertEquals(10000, f.getTarget());
		
	}
	@Test 
	public void testFailedCreation() {
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"error\",\"data\":{\"_id\":\"12345\",\"name\":\"new fund\",\"description\":\"this is the new fund\",\"target\":10000,\"org\":\"5678\",\"donations\":[],\"__v\":0}}";

			}
			
		});

		Fund f = dm.createFund("12345", "new fund", "this is the new fund", 10000);
		assertNull(f);

	}

	@Test 
	public void testExceptionThrownDuringRequest() {
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				throw new RuntimeException("can't make request");

			}
			
		});
		Fund f = dm.createFund("12345", "new fund", "this is the new fund", 10000);
		assertNull(f);

	}

	@Test 
	public void testInvalidJSONResponse() {
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "Invalid JSON";

			}
			
		});
		Fund f = dm.createFund("12345", "new fund", "this is the new fund", 10000);
		assertNull(f);

	}
	
	@Test 
	public void testUnexpectedStatus() {
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"nostatus\",\"data\":{\"_id\":\"12345\",\"name\":\"new fund\",\"description\":\"this is the new fund\",\"target\":10000,\"org\":\"5678\",\"donations\":[],\"__v\":0}}";

			}
			
		});
		Fund f = dm.createFund("12345", "new fund", "this is the new fund", 10000);
		assertNull(f);

	}
	
	@Test 
	public void testNullResponse() {
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return null;

			}
			
		});
		Fund f = dm.createFund("12345", "new fund", "this is the new fund", 10000);
		assertNull(f);
	}

	@Test
	public void testEmptyFundName() {
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"error\",\"message\":\"Fund name cannot be empty\"}";
			}
		});
		
		Fund f = dm.createFund("12345", "", "this is the new fund", 10000);
		
		assertNull(f);
	}

	@Test
	public void testNegativeTargetAmount() {
		DataManager dm = new DataManager(new WebClient("localhost", 3001) {
			@Override
			public String makeRequest(String resource, Map<String, Object> queryParams) {
				return "{\"status\":\"error\",\"message\":\"Target amount must be positive\"}";
			}
		});
		
		Fund f = dm.createFund("12345", "new fund", "this is the new fund", -100);
		
		assertNull(f);
	}


}
