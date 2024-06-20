import static org.junit.Assert.*;

import java.beans.Transient;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.junit.Test;

public class DataManager_getContributorName_Test {
     @Test
    public void testGetContributorNameSuccess() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override 
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "{\"name\":\"Lesly\"}";
            }
        });

        String contributor_Name = dm.getContributorName("6668db089d6aab58f8cf8d8d");
        assertEquals("Lesly", contributor_Name);
    }

    @Test 
    public void testGetContributorNameException() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override 
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                throw new RuntimeException("exception");
            }
        });
        
        String contributor_Name = dm.getContributorName("6668db089d6aab58f8cf8d8d");
        assertNull(contributor_Name);

    }

    @Test 
    public void testGetContributorNameFail() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001) {
            @Override 
            public String makeRequest(String resource, Map<String, Object> queryParams) {
                return "{\"status\":\"fail\"}";
            }
        });

        String contributor_Name = dm.getContributorName("6668db089d6aab58f8cf8d8d");
        assertNull(contributor_Name);
    }
}
