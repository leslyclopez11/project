import java.util.List;
import java.util.Scanner;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


public class UserInterface {
    
    String cachedLogin;
    // set cachedLogin
    public void setCachedLogin(String cachedLogin) {
        this.cachedLogin = cachedLogin;
    }
	private DataManager dataManager;
	private Organization org;
	private Scanner in = new Scanner(System.in);
    private DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("MMMM d, yyyy"); 
	
	public UserInterface(DataManager dataManager, Organization org) {
		this.org = org;
		this.dataManager = dataManager;
	}
	
	public void start() {
				
		while (true) {
			System.out.println("\n");
            if (org == null) {
				boolean loggedBool = initialPrompt();
				if (!loggedBool) {
					System.out.println("Could not login or create organization.");
                    continue;
				}
			}

            System.out.println("\n");
			if (org.getFunds() == null) {
				System.out.println("Organization was null. Try again!");
			}
			if (org.getFunds().size() > 0) {
				System.out.println("There are " + org.getFunds().size() + " funds in this organization:");
			
				int countFunds = 1;
				for (Fund f : org.getFunds()) {
					System.out.println(countFunds + ": " + f.getName());
					countFunds++;
				}
				System.out.println("Please enter the fund number to see more information.");
			}
			System.out.println("Enter 0 to create a new fund");
            System.out.println("Enter 'cp' to change password or 'change password'");
            System.out.println("Enter 'l' or 'logout' to log out.");
            System.out.println("Enter 'q' or 'quit' to exit the program.");

            String input = in.nextLine().trim();

            if (input.equals("q") || input.equals("quit")) {
                System.out.println("Good bye!");
                break;
            } else if (input.equals("l") || input.equals("logout")) {
                if (org != null) {
					logout();
				} else {
					System.out.println("You are logged out");
				}
            } else if (input.equals("cp") || input.equals("change password")) {
                updateOrgPassword();
                continue;
            }

            try {
                int option = Integer.parseInt(input);
                if (option == 0) {
                    createFund();
                } else if (option > 0 && option <= org.getFunds().size()) {
                    displayFund(option);
                } else {
                    System.out.println("Please enter a valid fund number.");
					System.out.println("Enter 0 to create a new fund, 'cp'/'change password', 'l'/'logout', or 'q'/'quit' to exit.");
                }
            } catch (NumberFormatException e) {
                continue;
            }
		}			
			
	}

	private void logout() {
		System.out.println("Loggin out!");
		org = null;
	}

    private boolean initialPrompt() {
        System.out.println("Enter 'l' to log in or 'c' to create a new organization:");
        String input = in.nextLine().trim();

        if (input.equals("l")) {
            return login();
        } else if (input.equals("c")) {
            createOrganization();
            return org != null;
        } else {
            System.out.println("Invalid choice. Please enter 'l' to log in or 'c' to create a new organization.");
            return false;
        }
    }
	private boolean login() {
		System.out.println("Enter login: ");
		String login = in.nextLine().trim();
		System.out.println("Enter password: ");
		String password = in.nextLine().trim();

		try {
			org = dataManager.attemptLogin(login, password);
            setCachedLogin(login);

			if (org != null) {
				System.out.println("Login successful");
				return true;
			} else {
				System.out.println("Login failed");
				return false;
			}
			
		} catch (IllegalStateException e) {
			System.out.println("Error with server");
			return false;
		}
	}

	
	public void createFund() {
		String fund_name;
        String description_fund;
        long target_In = -1;

        //fund name
        while (true) {
            System.out.print("Enter the fund name: ");
            fund_name = in.nextLine().trim();
            //making sure to print a message if fund name is empty
            if (!fund_name.isEmpty()) {
                break;
            }
            System.out.println("Fund name can't be blank");
        }

        //fund description
        while (true) {
            System.out.print("Enter the fund description: ");
            description_fund = in.nextLine().trim();
            //making sure to print a message if description is empty
            if (!description_fund.isEmpty()) {
                break;
            }
            System.out.println("Fund description can't be blank.");
        }

        // Prompt for fund target
        while (true) {
            System.out.print("Enter the fund target: ");
            String targetInput = in.nextLine().trim();
            try {
                target_In = Long.parseLong(targetInput);
                //makes sure to print message if input is less than 0
                if (target_In > 0) {
                    break;
                }
                System.out.println("Please enter a valid fund target.");
            } catch (NumberFormatException e) {
                //input was not a number
                System.out.println("Please enter a numeric value for the fund target.");
            }
        }

        try {
            Fund fund = dataManager.createFund(org.getId(), fund_name, description_fund, target_In);
            if (fund != null) {
                org.getFunds().add(fund);
                System.out.println("Fund created successfully.");
            } else {
                System.out.println("Failed to create fund. Please try again.");
            }
        } catch (IllegalStateException e) {
            System.out.println("Error in communicating with server.");
        }

		
	}
	
	
	public void displayFund(int fund) {
		
		Fund fund_num = org.getFunds().get(fund - 1);
		long total_amount = 0;
		System.out.println("\n\n");
		System.out.println("Here is information about this fund:");
		System.out.println("Name: " + fund_num.getName());
		System.out.println("Description: " + fund_num.getDescription());
		System.out.println("Target: $" + fund_num.getTarget());
		
		List<Donation> donationsList = fund_num.getDonations();
		System.out.println("Number of donations: " + donationsList.size());
		for (Donation donation : donationsList) {
			total_amount += donation.getAmount();
            String formatDay = Instant.parse(donation.getDate()).atZone(ZoneId.systemDefault()).format(formatDate);
			System.out.println("* " + donation.getContributorName() + ": $" + donation.getAmount() + " on " + formatDay);
		}
	
		System.out.printf("Total donation amount: $%d (%.2f%% of target)%n", total_amount,
                ((double) total_amount / fund_num.getTarget()) * 100);

        //Aggregated Donations
		System.out.println("Enter 'a' to see aggregated donations by contributor or press Enter to go back to the listing of funds");
		String input = in.nextLine().trim();
    
		if (input.equals("a")) {
			displayAggDonations(fund_num);
            System.out.printf("Total donation amount: $%d (%.2f%% of target)%n", total_amount,
                ((double) total_amount / fund_num.getTarget()) * 100);
		}
        System.out.println("Press the Enter key to go back to the listing of funds");
        in.nextLine();
		
	}
	
	public void displayAggDonations(Fund fund) {
        List<AggregateContributor> aggregatedDonations = fund.getAggregatedDonations();
        System.out.println("\n\nAggregated Donations:");
        for (AggregateContributor aggregate : aggregatedDonations) {
            System.out.printf("%s, %d donations, $%d total%n", aggregate.getContributorName(),
                    aggregate.getCountDonation(), aggregate.getTotalAmount());
        }
    }

	public void createOrganization() {
		String name;
		String password;
		String orgName;
		String orgDescription;

        //Prompts for name and ensure is available
		while (true) {
            System.out.print("Enter the username: ");
            name = in.nextLine().trim();
            //making sure name is not empty and prints message
            if (name.isEmpty()) {
                System.out.println("Username cannot be blank. Try again!");
                continue;
            }
            try {
                //makes sure the username is available
                if (!dataManager.UsernameAv(name)) {
                    System.out.println("Username already taken. Please choose another username.");
                } else {
                    break;
                }
            }
            catch (IllegalStateException e){
                System.out.println("Error in communicating with server.");
            }
        }

        //Prompts for password
        while (true) {
            System.out.print("Enter the password: ");
            password = in.nextLine().trim();
            //makes sure password is not empty and prints message
            if (!password.isEmpty()) {
                break;
            }
            System.out.println("Password cannot be blank. Try again!");
        }

        // Prompt for organization's name
        while (true) {
            System.out.print("Enter the organization name: ");
            orgName = in.nextLine().trim();
            //makes sure organization name is not empty and prints message
            if (!orgName.isEmpty()) {
                break;
            }
            System.out.println("Organization name cannot be blank. Try again!");
        }

        //Prompt for organization's description
        while (true) {
            System.out.print("Enter the organization description: ");
            orgDescription = in.nextLine().trim();
            //makes sure description is not empty and prints message
            if (!orgDescription.isEmpty()) {
                break;
            }
            System.out.println("Organization description cannot be blank. Try again!");
        }

        //Creates organization
        try {
            Organization newOrg = dataManager.createOrganization(name, password, orgName, orgDescription);
            if (newOrg != null) {
                this.org = newOrg;
                System.out.println("Organization created successfully.");
            } else {
                System.out.println("Failed to create organization. Try again!");
            }
        } catch (IllegalStateException e) {
            System.out.println("Error in communicating with server. Try again!");
        }
		
	}

    public void updateOrgPassword() {
        System.out.println("Enter your current password: ");
        String currentPassword = in.nextLine().trim();

        //validates user's password
		if (!currentPassword.equals(org.getPassword())) {
			System.out.println("Your entered current password is incorrect.");
			in.nextLine();
			return;
		}
		System.out.print("Enter your new password: ");
		String newPassword1 = in.nextLine().trim();
		System.out.print("Enter your new password again: ");
		String newPassword2 = in.nextLine().trim();

        //makes sure the two inputs match
		if (!newPassword1.equals(newPassword2)) {
			System.out.println("The new passwords do not match.");
			in.nextLine();
			return;
		}

        //updates the org's password
		try {
			boolean result = dataManager.changePassword(org.getId(), currentPassword, newPassword1);
            if (result) {
                System.out.println("Password changed successfully.");
            } else {
                System.out.println("Password change failed. Please enter 'cp' or 'change password' to try again.");
            }
		} catch (IllegalStateException e) {
            System.out.println("Error in communicating with server. Try again!");
        }
    }




	public static void main(String[] args) {
		
		DataManager ds = new DataManager(new WebClient("localhost", 3001));
		UserInterface ui = new UserInterface(ds, null);
        ui.start();
		
	}

}