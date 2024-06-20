import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Fund {

	private String id;
	private String name;
	private String description;
	private long target;
	private List<Donation> donations;
	private List<AggregateContributor> aggregatedDonations;
	
	public Fund(String id, String name, String description, long target) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.target = target;
		donations = new LinkedList<>();
		aggregatedDonations = null;
	}

	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public long getTarget() {
		return target;
	}

	public void setDonations(List<Donation> donations) {
		this.donations = donations;
		this.aggregatedDonations = null;
	}
	
	public List<Donation> getDonations() {
		return donations;
	}

	public List<AggregateContributor> getAggregatedDonations() {
		if (aggregatedDonations == null) {
			Map<String, AggregateContributor> map_aggregate = new HashMap<>();
			for (Donation donation: donations) {
				String contributorName = donation.getContributorName();
				AggregateContributor aggregate = map_aggregate.getOrDefault(contributorName, new AggregateContributor(contributorName));
				aggregate.aggregateDonation(donation);
				map_aggregate.put(contributorName, aggregate);
			}

			aggregatedDonations = new LinkedList<>(map_aggregate.values());
			Collections.sort(aggregatedDonations, Comparator.comparingLong(AggregateContributor::getTotalAmount).reversed());

		}
		return aggregatedDonations;
	}
	
	
	
}
