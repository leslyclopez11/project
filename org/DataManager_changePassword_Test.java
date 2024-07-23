import static org.junit.Assert.*;

import java.util.Map;
import org.junit.Test;

public class DataManager_changePassword_Test {

    @Test
    public void testSuccessfulChange() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "{\"status\": \"success\", \"data\": {\"_id\": \"667b79c7d381e21210a9b45d\", \"hello\": \"newPassword\"}}";
            }
        });

        boolean result = dm.changePassword("orgId", "currentPassword", "newPassword");
        assertTrue(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOrgIdNull() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001));
        dm.changePassword(null, "currentPassword", "newPassword");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewPasswordNull() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001));
        dm.changePassword("orgId", "currentPassword",null);
    }

    @Test(expected = IllegalStateException.class)
    public void testNullResponse() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return null;
            }
        });

        dm.changePassword("orgId", "currentPassword", "newPassword");
    }

    @Test(expected = IllegalStateException.class)
    public void testErrorStatus() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "{\"status\":\"error\"}";
            }
        });

        dm.changePassword("orgId", "currentPassword", "newPassword");
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidJSON() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "Invalid JSON response";
            }
        });

        dm.changePassword("orgId", "currentPassword", "newPassword");
    }

    @Test
    public void testPasswordChangeUnsuccessful() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "{\"status\":\"change failed\"}";
            }
        });

        boolean result = dm.changePassword("orgId", "currentPassword", "newPassword");
        assertFalse(result);
    }

    
}
