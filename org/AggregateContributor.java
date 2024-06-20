public class AggregateContributor {
    private String contributorName;
    private int countDonation;
    private long totalAmount;

    public AggregateContributor(String contributorName) {
        this.contributorName = contributorName;
        this.countDonation = 0;
        this.totalAmount = 0;
    }

    public void aggregateDonation(Donation donation) {
        this.countDonation++;
        this.totalAmount += donation.getAmount();
    }

    public String getContributorName() {
        return contributorName;
    }

    public int getCountDonation() {
        return countDonation;
    }

    public long getTotalAmount() {
        return totalAmount;
    }
}
