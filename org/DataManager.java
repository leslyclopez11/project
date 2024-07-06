import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class DataManager {

	private final WebClient client;
	private final Map<String, String> contributorNameC;
	public DataManager(WebClient client) {
		if (client == null) {
			throw new IllegalArgumentException("WebClient can't be null");
		}
		this.client = client;
		this.contributorNameC = new HashMap<>();
	}

	/**
	 * Attempt to log the user into an Organization account using the login and password.
	 * This method uses the /findOrgByLoginAndPassword endpoint in the API
	 * @return an Organization object if successful; null if unsuccessful
	 */
	public Organization attemptLogin(String login, String password) {
		//System.out.println("username: " + login);
		//System.out.println("password: " + password);
		if (login == null || password == null) {
			throw new IllegalArgumentException("Login or password can't be null");
		}
		try {
			System.out.println("username: " + login);
			System.out.println("password: " + password);
			Map<String, Object> map = new HashMap<>();
			map.put("login", login);
			map.put("password", password);
			//System.out.println("username: " + login);
			//System.out.println("password: " + password);
			String response = client.makeRequest("/findOrgByLoginAndPassword", map);
			//System.out.println("response: " + response);
			if (response == null) {
				throw new IllegalStateException("Response returned null");
			}
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String)json.get("status");


			if (status.equals("success")) {
				JSONObject data = (JSONObject)json.get("data");
				if (data == null) {
					return null;
				}
				String fundId = (String)data.get("_id");
				String name = (String)data.get("name");
				String description = (String)data.get("description");
				Organization org = new Organization(fundId, name, description);
				org.setPassword(password);
				JSONArray funds = (JSONArray)data.get("funds");
				Iterator it = funds.iterator();
				while(it.hasNext()){
					JSONObject fund = (JSONObject) it.next(); 
					fundId = (String)fund.get("_id");
					name = (String)fund.get("name");
					description = (String)fund.get("description");
					long target = (Long)fund.get("target");

					Fund newFund = new Fund(fundId, name, description, target);

					JSONArray donations = (JSONArray)fund.get("donations");
					List<Donation> donationList = new LinkedList<>();
					Iterator it2 = donations.iterator();
					while(it2.hasNext()){
						JSONObject donation = (JSONObject) it2.next();
						String contributorId = (String)donation.get("contributor");
						String contributorName = this.getContributorName(contributorId);
						long amount = (Long)donation.get("amount");
						String date = (String)donation.get("date");
						donationList.add(new Donation(fundId, contributorName, amount, date));
					}

					newFund.setDonations(donationList);

					org.addFund(newFund);

				}

				return org;
			} else {
				throw new IllegalStateException("Login failed: " + json.get("error"));
			}
		} catch (ParseException e) {
			throw new IllegalStateException("Failed to parse JSON respons0: " + e);
		} catch (Exception e) {
			throw new IllegalStateException("Error in communicating with server: " +  e);
		}
	}

	/**
	 * Look up the name of the contributor with the specified ID.
	 * This method uses the /findContributorNameById endpoint in the API.
	 * @return the name of the contributor on success; null if no contributor is found
	 */
	public String getContributorName(String id) {
		if (id == null) {
			throw new IllegalStateException("Contributor ID is null");
		}

		if (contributorNameC.containsKey(id)) {
			return contributorNameC.get(id);
		}
		try {

			Map<String, Object> map = new HashMap<>();
			map.put("id", id);
			String response = client.makeRequest("/findContributorNameById", map);
			if (response == null) {
				throw new IllegalStateException("Response returned null");
			}
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String)json.get("status");

			if (status.equals("success")) {
				String name = (String)json.get("data");
				contributorNameC.put(id, name);
				return name;
			} else {
				return null;
			}

		} catch (ParseException e) {
			throw new IllegalStateException("Failed to parse JSON response: " + e);
		} catch (Exception e) {
			throw new IllegalStateException("Error in communicating with server: " + e);
		}
			
	}


	

	/**
	 * This method creates a new fund in the database using the /createFund endpoint in the API
	 * @return a new Fund object if successful; null if unsuccessful
	 */
	public Fund createFund(String orgId, String name, String description, long target) {
		if (orgId == null || name == null || description == null) {
			throw new IllegalArgumentException("Organization ID, name, and description are null");
		}
		try {

			Map<String, Object> map = new HashMap<>();
			map.put("orgId", orgId);
			map.put("name", name);
			map.put("description", description);
			map.put("target", target);
			String response = client.makeRequest("/createFund", map);
			if (response == null) {
				throw new IllegalStateException("Response returned null");
			}
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String)json.get("status");

			if (status.equals("success")) {
				JSONObject fund = (JSONObject)json.get("data");
				String fundId = (String)fund.get("_id");
				return new Fund(fundId, name, description, target);
			} else {
				return null;
			} 

		} catch (ParseException e) {
			throw new IllegalStateException("Failed to parse JSON response2: " + e);
		} catch (Exception e) {
			throw new IllegalStateException("Error in communicating with server: " + e);
		}	
	}

	/*
	 * Create org
	 */

	public Organization createOrganization(String login, String password, String name, String description) {
		if (login == null) {
			throw new IllegalArgumentException("Login can't be null");
		}
		if (password == null) {
			throw new IllegalArgumentException("Password can't be null");
		}

		if (name == null) {
			throw new IllegalArgumentException("Name can't be null");
		}
		if (description == null) {
			throw new IllegalArgumentException("Description can't be null");
		}

		try {
			Map<String, Object> map = new HashMap<>();
			map.put("login", login);
			map.put("password", password);
			map.put("name", name);
			map.put("description", description);
			String response = client.makeRequest("/createOrganization", map);
			if (response == null) {
				throw new IllegalStateException("Response returned null");
			}
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String)json.get("status");

			if("success".equals(status)) {
				JSONObject data = (JSONObject) json.get("data");
                String orgId = (String) data.get("_id");
                return new Organization(orgId, name, description);
			} else {
				throw new IllegalStateException("Status: " + status);
			}
		} catch (ParseException e) {
			throw new IllegalStateException("Failed to parse JSON response2: " + e);
		} catch (Exception e) {
			throw new IllegalStateException("Error in communicating with server: " + e);
		}	

	}
	public boolean UsernameAv(String login) {
		if (login == null) {
			throw new IllegalArgumentException("login can't be null");
		}

		try {
			Map<String, Object> map = new HashMap<>();
			map.put("login", login);
			String response = client.makeRequest("/sameUsername", map);
			if (response == null) {
				throw new IllegalStateException("Response returned null");
			}
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String)json.get("status");

			if ("username already taken".equals(status)) {
				return false;
			} else if ("username available".equals(status)){
				return true;
			} else {
				throw new IllegalStateException("Status: " + status);
			}
		} catch (ParseException e) {
			throw new IllegalStateException("Failed to parse JSON response2: " + e);
		} catch (Exception e) {
			throw new IllegalStateException("Error in communicating with server: " + e);
		}	
	}

	public boolean changePassword(String orgId, String currentPassword, String newPassword) {
    	
    	// Throw exception
        if (client == null) {
            throw new IllegalStateException("Client is null!");
        }
        if (orgId == null) {
            throw new IllegalArgumentException("ID is null!");
        }
        if (currentPassword == null) {
            throw new IllegalArgumentException("Current password is null!");
        }
        if (newPassword == null) {
            throw new IllegalArgumentException("New password is null!");
        }

        try {
            Map<String, Object> map = new HashMap<>();
            map.put("orgId", orgId);
            map.put("currentPassword", currentPassword);
            map.put("newPassword", newPassword);

            String response = client.makeRequest("/changePassword", map);

            // Throw exception
            if (response == null) {
                throw new IllegalStateException("WebClient response is null!");
            }

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(response);
            String status = (String) json.get("status");

            if (status.equals("success")) {
				currentPassword = newPassword;
				// org.setPassword(currentPassword);
                return true;
            } else if (status.equals("incorrect password")) {
                throw new IllegalArgumentException("Current password is incorrect!");
            } else {
                throw new IllegalStateException("WebClient responded with an invalid status!");
            }
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Error while communicating with the server", e);
        }
    }

	public boolean editAccountInfo(String id, String newName, String newDescription) {

		// Throw exception
		if (client == null) {
			throw new IllegalStateException("Client is null!");
		}
		if (id == null) {
			throw new IllegalArgumentException("ID is null!");
		}
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("id", id);
			if (newName != null) {
				map.put("name", newName);
			}
			if (newDescription != null) {
				map.put("description", newDescription);
			}
			String response = client.makeRequest("/editAccount", map);
			// Throw exception
			if (response == null) {
				throw new IllegalStateException("WebClient response is null!");
			}

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(response);
			String status = (String) json.get("status");
			System.out.println(status);
			if (status.equals("success")) {
				return true;
			} else {
				throw new IllegalStateException("WebClient responded with an invalid status!");
			}
		} catch (Exception e) {
			throw new IllegalStateException("Error while communicating with the server", e);
		}
	}
	// public boolean changePassword(String orgId, String newPassword) {
	// 	if (orgId == null || newPassword == null) {
    //         throw new IllegalArgumentException("OrgID/ New Password cannot be null");
    //     }

    //     try{
    //        Map<String, Object> map = new HashMap<>(); 
    //        map.put("id", orgId);
    //        map.put("password", newPassword);
    //        String response = client.makeRequest("/changePassword", map);

    //        if (response == null) {
    //             throw new IllegalStateException("WebClient returned null");
    //         }

    //         JSONParser parser = new JSONParser();
    //         JSONObject json = (JSONObject) parser.parse(response);
    //         String status = (String) json.get("status");
    //         if ("success".equals(status)) {
    //             return true;
    //         } 
    //         else if ("change failed".equals(status)) {
    //             return false;
    //         }
    //         else {
    //             throw new IllegalStateException("WebClient returned error status: " + status);
    //         }

    //     } catch (ParseException e) {
    //         throw new IllegalStateException("Failed to parse JSON response", e);
    //     } catch (Exception e) {
    //         throw new IllegalStateException("Error in communicating with server", e);
    //     }
	// }

	// public String updateOrgsPassword(String orgId, String password) {
	// 	if (orgId == null || password == null) {
	// 		throw new IllegalArgumentException("OrgID or New Password cannot be null");
	// 	}

	// 	try {
	// 		Map<String, Object> map = new HashMap<>();
	// 		map.put("id", orgId);
	// 		map.put("password", password);
	// 		String response = client.makeRequest("/updatePassword", map);
	// 		JSONParser parser = new JSONParser();
	// 		JSONObject json = (JSONObject) parser.parse(response);
	// 		String status = (String) json.get("status");

	// 		return status.equals("success")? "success": "fail";
	// 	} catch (Exception e) {
	// 		System.out.println("Error when communicating with server.");
	// 		return "Error when communicating with server.";
	// 	}
	// }


}
