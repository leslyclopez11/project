import java.util.List;
import java.util.Scanner;

public class UserInterface {
	
    private String cachedLogin;
    
    // Setter for cachedLogin
    public void setCachedLogin(String cachedLogin) {
        this.cachedLogin = cachedLogin;
    }
	private DataManager dataManager;
	private Organization org;
	private Scanner in = new Scanner(System.in);
	
	public UserInterface(DataManager dataManager, Organization org) {
		this.org = org;
		this.dataManager = dataManager;
	}
	
	public void start() {
				
		while (true) {
			System.out.println("\n\n");
            if (org == null) {
				boolean loggedBool = initialPrompt();
				if (!loggedBool) {
					System.out.println("Could not login or create organization.");
                    continue;
				}
			}

            System.out.println("\n\n");
			if (org.getFunds() == null) {
				System.out.println("Organization was null");
			}
			if (org.getFunds().size() > 0) {
				System.out.println("There are " + org.getFunds().size() + " funds in this organization:");
			
				int countFunds = 1;
				for (Fund f : org.getFunds()) {
					
					System.out.println(countFunds + ": " + f.getName());
					
					countFunds++;
				}
				System.out.println("Enter the fund number to see more information.");
			}
			System.out.println("Enter 0 to create a new fund");
            System.out.println("Enter 'editO' or 'edit organization' to edit organization's info!");
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
                //updateOrgPassword();
                continue;
            } else if (input.equals("editO") || input.equals("edit organization")) {
                //editAccountInfo(this.cachedLogin);
            }

            try {
                int option = Integer.parseInt(input);
                if (option == 0) {
                    createFund();
                } else if (option > 0 && option <= org.getFunds().size()) {
                    displayFund(option);
                } else {
                    System.out.println("Please enter a valid fund number.");
					System.out.println("Enter 0 to create a new fund, or 'q'/'quit' to exit.");
                }
            } catch (NumberFormatException e) {
                //System.out.println("Invalid input.");
            }
		}			
			
	}

	private void logout() {
		System.out.println("Loggin out!");
		org = null;
	}

    private boolean initialPrompt() {
        System.out.println("Enter 'l' to log in or 'c' to create a new organization:");
        String choice = in.nextLine().trim();

        if (choice.equals("l")) {
            return login();
        } else if (choice.equals("c")) {
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
        long target = -1;

        //fund name
        while (true) {
            System.out.print("Enter the fund name: ");
            fund_name = in.nextLine().trim();
            if (!fund_name.isEmpty()) {
                break;
            }
            System.out.println("Fund name can't be blank");
        }

        //fund description
        while (true) {
            System.out.print("Enter the fund description: ");
            description_fund = in.nextLine().trim();
            if (!description_fund.isEmpty()) {
                break;
            }
            System.out.println("Fund description can't be blank.");
        }

        // Prompt for fund target and ensure it's a positive number
        while (true) {
            System.out.print("Enter the fund target: ");
            String targetInput = in.nextLine().trim();
            try {
                target = Long.parseLong(targetInput);
                if (target > 0) {
                    break;
                }
                System.out.println("Please enter a valid fund target.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a numeric value for the fund target.");
            }
        }

        try {
            Fund fund = dataManager.createFund(org.getId(), fund_name, description_fund, target);
            if (fund != null) {
                org.getFunds().add(fund);
                System.out.println("Fund created successfully.");
            } else {
                System.out.println("Failed to create fund. Please try again.");
            }
        } catch (IllegalStateException e) {
            System.out.println("Error in communicating with server. Message: "  + e);
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
			System.out.println("* " + donation.getContributorName() + ": $" + donation.getAmount() + " on " + donation.getDate());
		}
	
		System.out.printf("Total donation amount: $%d (%.2f%% of target)%n", total_amount,
                ((double) total_amount / fund_num.getTarget()) * 100);

		System.out.println("Enter 'a' to see aggregated donations by contributor or press Enter to go back to the listing of funds");
		String input = in.nextLine().trim();
		if (input.equals("a")) {
			displayAggDonations(fund_num);
		}
		
	}
	
	public void displayAggDonations(Fund fund) {
        List<AggregateContributor> aggregatedDonations = fund.getAggregatedDonations();
        System.out.println("\n\nAggregated Donations:");
        for (AggregateContributor aggregate : aggregatedDonations) {
            System.out.printf("%s, %d donations, $%d total%n", aggregate.getContributorName(),
                    aggregate.getCountDonation(), aggregate.getTotalAmount());
        }
        System.out.println("Press the Enter key to go back to the listing of funds");
        in.nextLine();
    }

	public void createOrganization() {
		String name;
		String password;
		String orgName;
		String orgDescription;
		while (true) {
            System.out.print("Enter the username: ");
            name = in.nextLine().trim();

            if (name.isEmpty()) {
                System.out.println("Username cannot be blank. Please enter a valid username.");
                continue;
            }
            try {
                if (!dataManager.UsernameAv(name)) {
                    System.out.println("Username already taken. Please choose another username.");
                } else {
                    break;
                }
            }
            catch (IllegalStateException e)
            {
                System.out.println("Error in communicating with server. Please try again.");
            }
        }

        while (true) {
            System.out.print("Enter the password: ");
            password = in.nextLine().trim();
            if (!password.isEmpty()) {
                break;
            }
            System.out.println("Password cannot be blank. Please enter a valid password.");
        }

        // Prompt for organization name and ensure it's not blank
        while (true) {
            System.out.print("Enter the organization name: ");
            orgName = in.nextLine().trim();
            if (!orgName.isEmpty()) {
                break;
            }
            System.out.println("Organization name cannot be blank. Please enter a valid organization name.");
        }

        while (true) {
            System.out.print("Enter the organization description: ");
            orgDescription = in.nextLine().trim();
            if (!orgDescription.isEmpty()) {
                break;
            }
            System.out.println("Organization description cannot be blank. Please enter a valid organization description.");
        }

        try {
            Organization newOrg = dataManager.createOrganization(name, password, orgName, orgDescription);
            if (newOrg != null) {
                this.org = newOrg;
                System.out.println("Organization created successfully.");
            } else {
                System.out.println("Failed to create organization. Please try again.");
            }
        } catch (IllegalStateException e) {
            System.out.println("Error in communicating with server. Please try again.");
        }
		
	}



    // public void changePassword() {
    //     String currentPassword;
    //     String newPassword;
    //     String confirmPassword;

    //     while (true) {
    //         System.out.println("Enter current passwrod: ");
    //         currentPassword = in.nextLine().trim();
    //         try {
    //             Organization loggedInOrg = dataManager.attemptLogin(org.getLogin(), currentPassword);
    //             if (loggedInOrg == null) {
    //                 System.out.println("Incorrect current password. Please try again.");
    //                 return;
    //             }
    //         } catch (IllegalStateException e) {
    //             System.out.println("Error in communicating with server. Please try again.");
    //             return;
    //         }

    //         System.out.print("Enter new password: ");
    //         newPassword = in.nextLine().trim();

    //         System.out.print("Confirm new password: ");
    //         confirmPassword = in.nextLine().trim();

    //         if (!newPassword.equals(confirmPassword)) {
    //             System.out.println("New passwords do not match. Please try again.");
    //             return;
    //         }

    //         try {
    //             boolean success = dataManager.changePassword(org.getId(), newPassword);
    //             if (success) {
    //                 System.out.println("Password changed successfully.");
    //             } else {
    //                 System.out.println("Failed to change password. Please try again.");
    //             }
    //         } catch (IllegalStateException e) {
    //             System.out.println("Error in communicating with server. Please try again.");
    //         }

    //         break;
    //     }

    // }

    // public void updateOrgPassword() {
    //     System.out.println("Enter your current password: ");
    //     String currentPassword = in.nextLine().trim();

	// 	if (!currentPassword.equals(org.getPassword())) {
	// 		System.out.println("Your entered current password is incorrect.");
	// 		in.nextLine();
	// 		return;
	// 	}
	// 	System.out.print("Enter your new password: ");
	// 	String newPassword1 = in.nextLine().trim();
	// 	System.out.print("Enter your new password again: ");
	// 	String newPassword2 = in.nextLine().trim();

	// 	if (!newPassword1.equals(newPassword2)) {
	// 		System.out.println("The new passwords do not match.");
	// 		in.nextLine();
	// 		return;
	// 	}

	// 	try {
	// 		boolean result = dataManager.changePassword(org.getId(), currentPassword, newPassword1);
    //         if (result) {
    //             System.out.println("Password changed successfully.");
    //         }
	// 		// if (result.equals("success")) {
	// 		// 	System.out.println("Password changed successfully.");
	// 		// 	org.setPassword(newPassword1);
	// 		// 	in.nextLine();
	// 		// } else {
	// 		// 	System.out.println("Failed to change password.");
	// 		// 	in.nextLine();
	// 		// }
	// 	} catch (IllegalArgumentException e) {
    //         System.out.println(e.getMessage());
    //         System.out.println("Password change failed!  Please enter 'c' to try again.");
    //     } catch (Exception e) {
    //         System.out.println("Error: " + e.getMessage());
    //         System.out.println("Password change failed!  Please enter 'c' to try again.");
    //     }
    // }
	
    // private void editAccountInfo(String login) {
    //     Scanner scanner = new Scanner(System.in);
    //     System.out.println("Enter your password:");
    //     String password = scanner.nextLine();
    //     try {
    //         Organization tempOrg = dataManager.attemptLogin(login, password);
    //         System.out.println(login);
    //         System.out.println(password);
    //         if (tempOrg == null) {
    //             System.out.println("Password is wrong, go back to the previous menu.");
    //         } else {
    //             String newName = null;
    //             System.out.println("Edit name? (Enter Y/N)");
    //             String option = scanner.nextLine().strip().toUpperCase();
    //             while (!option.equals("Y") && !option.equals("N")) {
    //                 System.out.println("Invalid response! Edit name? (Enter Y/N)");
    //                 option = scanner.nextLine().strip().toUpperCase();
    //             }
    //             if (option.equals("Y")) {
    //                 System.out.println("Enter new name:");
    //                 newName = scanner.nextLine();
    //             }

    //             String newDescription = null;
    //             System.out.println("Edit Description? (Enter Y/N)");
    //             option = scanner.nextLine().strip().toUpperCase();
    //             while (!option.equals("Y") && !option.equals("N")) {
    //                 System.out.println("Invalid response! Edit Description? (Enter Y/N)");
    //                 option = scanner.nextLine().strip().toUpperCase();
    //             }
    //             if (option.equals("Y")) {
    //                 System.out.println("Enter new description:");
    //                 newDescription = scanner.nextLine();
    //             }
    //             boolean success = dataManager.editAccountInfo(org.getId(), newName, newDescription);
    //             if (success){
    //                 System.out.println("Editing Information is successful");
    //             }
    //         }
    //     } catch (Exception e) {
    //         System.out.println("Edit Information failed");
    //     }


    // }
	public static void main(String[] args) {
		
		DataManager ds = new DataManager(new WebClient("localhost", 3001));
		UserInterface ui = new UserInterface(ds, null);
        ui.start();
		
	}

}