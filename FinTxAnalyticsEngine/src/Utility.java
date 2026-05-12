public class Utility {
    // Using external Transaction class (defined in Transaction.java)

    /**
     * Generate mock transactions using IntStream.range(0, totalTransactions).
     * - lastDays controls date range (e.g. 60)
     * - numCustomers keeps homogeneous number of transactions per customer (totalTransactions must be divisible by numCustomers for exact homogeneity)
     * - merchantCount controls number of distinct merchants
     *
     * This generator intentionally skews amounts for a few merchants and a few customers so that
     * "top merchants by spend" and "top customers by spend" are clearly demonstrable.
     */
    public static java.util.List<Transaction> generateMockTransactions(int totalTransactions,
                                                                       int numCustomers,
                                                                       int merchantCount,
                                                                       int lastDays,
                                                                       long randomSeed) {
        if (totalTransactions <= 0 || numCustomers <= 0 || merchantCount <= 0) {
            throw new IllegalArgumentException("totalTransactions, numCustomers and merchantCount must be > 0");
        }
        java.util.Random rnd = new java.util.Random(randomSeed);
        java.util.List<String> merchants = new java.util.ArrayList<>(merchantCount);
        for (int m = 0; m < merchantCount; m++) {
            merchants.add("Merchant-" + m);
        }

        // Define some "hot" merchants (higher average spend) to ensure top merchants are predictable.
        java.util.Set<Integer> hotMerchants = java.util.Set.of(0, 1, 2);

        // Define some VIP customers who spend more on average (keeps transaction count homogeneous).
        int vipCount = Math.max(1, Math.min(100, numCustomers / 500)); // a small percent
        java.util.Set<Integer> vipCustomers = new java.util.HashSet<>();
        for (int i = 1; i <= vipCount; i++) vipCustomers.add(i); // customerIds 1..vipCount are VIPs

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.util.List<Transaction> tx = java.util.stream.IntStream.range(0, totalTransactions)
                .mapToObj(i -> {
                    long id = i;
                    // Homogeneous assignment of customerId: round-robin
                    int customerId = (i % numCustomers) + 1;

                    // Choose merchant pseudo-randomly but deterministic via hashing to avoid correlation across threads
                    int merchantIdx = Math.abs(Integer.hashCode(i * 31 + 7)) % merchantCount;
                    String merchant = merchants.get(merchantIdx);

                    // Base amount between 1 and 500
                    double base = 1 + (Math.abs(Integer.hashCode(i * 47 + 13)) % 500);

                    // Apply multipliers to force clear top merchants/customers
                    double merchantMultiplier = hotMerchants.contains(merchantIdx) ? 5.0 : 1.0;
                    double customerMultiplier = vipCustomers.contains(customerId) ? 10.0 : 1.0;

                    double amount = base * merchantMultiplier * customerMultiplier;

                    // Timestamp within lastDays uniformly distributed
                    int daysBack = Math.abs(Integer.hashCode(i * 97 + 23)) % lastDays;
                    int secondsInDay = Math.abs(Integer.hashCode(i * 83 + 19)) % (24 * 3600);
                    java.time.LocalDateTime ts = now.minusDays(daysBack).truncatedTo(java.time.temporal.ChronoUnit.DAYS)
                            .plusSeconds(secondsInDay);

                    // Categories and cities (deterministic selection)
                    String[] categories = new String[] { "Groceries", "Electronics", "Travel", "Restaurants", "Utilities", "Health", "Entertainment" };
                    String[] cities = new String[] { "New York", "London", "Paris", "Berlin", "Tokyo", "Sydney", "Mumbai" };
                    String category = categories[Math.abs(Integer.hashCode(i * 53 + 11)) % categories.length];
                    String city = cities[Math.abs(Integer.hashCode(i * 61 + 17)) % cities.length];

                    // Fraud flag: ~1% baseline, slightly higher for non-VIPs on certain hash patterns (deterministic)
                    int fraudHash = Math.abs(Integer.hashCode(i * 13 + 29)) % 1000;
                    boolean fraud = fraudHash < 10 || (hotMerchants.contains(merchantIdx) && fraudHash < 15);

                    // Use the no-arg constructor + setters which is compatible with most POJOs.
                    Transaction t = new Transaction();
                    t.setTxnId(Long.toString(id));
                    t.setCustomerId(Integer.toString(customerId));
                    t.setMerchant(merchant);
                    t.setCategory(category);
                    t.setAmount(amount);
                    t.setTxnTime(ts);
                    t.setCity(city);
                    t.setFraud(fraud);
                    return t;
                })
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

        return tx;
    }

    /** Returns top N merchants by total spend (merchant -> totalSpend). */
    public static java.util.List<java.util.Map.Entry<String, Double>> topMerchantsBySpend(java.util.List<Transaction> txs, int topN) {
        return txs.stream()
                .collect(java.util.stream.Collectors.groupingBy(t -> t.merchant, java.util.stream.Collectors.summingDouble(t -> t.amount)))
                .entrySet()
                .stream()
                .sorted(java.util.Map.Entry.<String, Double>comparingByValue(java.util.Comparator.reverseOrder()))
                .limit(topN)
                .collect(java.util.stream.Collectors.toList());
    }

    /** Returns top N customers by total spend (customerId -> totalSpend). */
    // public static java.util.List<java.util.Map.Entry<Integer, Double>> topCustomersBySpend(java.util.List<Transaction> txs, int topN) {
    public static java.util.List<java.util.Map.Entry<Integer, Double>> topCustomersBySpend(java.util.List<Transaction> txs, int topN) {
        return txs.stream()
                .collect(java.util.stream.Collectors.groupingBy(t -> Integer.valueOf(t.customerId), java.util.stream.Collectors.summingDouble(t -> t.amount)))
                .entrySet()
                .stream()
                .sorted(java.util.Map.Entry.<Integer, Double>comparingByValue(java.util.Comparator.reverseOrder()))
                .limit(topN)
                .collect(java.util.stream.Collectors.toList());
    }
    /** Demo using 1_000_000 transactions, 10_000 customers, 100 merchants and last 60 days. */
    public static void demoMillionTransactions() {
        int total = 1_000_000;
        int customers = 10_000; // divides 1_000_000 for homogeneity (100 tx per customer)
        int merchants = 100;
        int lastDays = 60;
        long seed = 12345L;

        System.out.println("Generating " + total + " transactions...");
        java.util.List<Transaction> txs = generateMockTransactions(total, customers, merchants, lastDays, seed);
        System.out.println("Generated transactions: " + txs.size());

        System.out.println("\nTop 3 merchants by spend:");
        topMerchantsBySpend(txs, 3).forEach(e -> System.out.println(e.getKey() + " -> " + e.getValue()));

        System.out.println("\nTop 10 customers by spend:");
        topCustomersBySpend(txs, 10).forEach(e -> System.out.println("Customer-" + e.getKey() + " -> " + e.getValue()));
    }
}
