import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.function.Function;
/**
 * DataProcessor provides methods to process transactions.
 */
public class DataProcessor {
    /**
     * Process a list of Transaction objects.
     *
     * @param transactions list of transactions to process
     */
    public Map<String,List<Transaction>> processData(List<Transaction> transactions) {
        return filterRecentTransactions(transactions);
    }

    /**
     * Example functional pipeline: filter -> group -> summarize
     * returns a map of CustomerSpendSummary per customer.
     */
    public Map<String, CustomerSpendSummary> processDataPipeline(List<Transaction> transactions) {
        Function<List<Transaction>, Map<String, List<Transaction>>> filterAndGroup = this::filterRecentTransactions;
        Function<Map<String, List<Transaction>>, Map<String, CustomerSpendSummary>> summarize = this::calculateCustomerSpend;

        return filterAndGroup.andThen(summarize).apply(transactions);
    }

    /**
     * Filter transactions from last 30 days
     */
    // private Map<String, List<Transaction>> filterRecentTransactions(Map<String, List<Transaction>> groupedTransactions) {
    //     return groupedTransactions.entrySet().stream()
    //         .filter(entry -> entry.getValue().stream().anyMatch(txn -> txn.txnTime.isAfter(LocalDateTime.now().minusDays(30))))
    //         .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    // }

    /**
     * Exclude fraudulent transactions
     */
    public Map<String,List<Transaction>> filterRecentTransactions(List<Transaction> transactions) {
        return transactions.stream()
            .filter(txn -> txn.txnTime.isAfter(LocalDateTime.now().minusDays(30)))
            .filter(txn -> !txn.fraud)
            .collect(java.util.stream.Collectors.groupingBy(txn -> txn.customerId));
    }


    /**
     * Group by customer
     */
    private Map<String, List<Transaction>> groupByCustomer(List<Transaction> transactions) {
        return transactions.stream()
            .collect(Collectors.groupingBy(txn -> txn.customerId));
    }

    
    
    /**
     * 
     * For each customer:
     * calculate total spend
     * calculate average spend
     */
    private Map<String, CustomerSpendSummary> calculateCustomerSpend(Map<String, List<Transaction>> groupedTransactions) {
        Map<String, CustomerSpendSummary> customerSpendMap = new HashMap<>();
        for (Map.Entry<String, List<Transaction>> entry : groupedTransactions.entrySet()) {
            String customerId = entry.getKey();
            List<Transaction> customerTxns = entry.getValue();
            double totalSpend = customerTxns.stream().mapToDouble(txn -> txn.amount).sum();
            double averageSpend = totalSpend / customerTxns.size();
            customerSpendMap.put(customerId, new CustomerSpendSummary(totalSpend, averageSpend));
        }
        return customerSpendMap;
        
    }

    
      
    // /**
    //  * Get top N merchants by spend
    //  */
    // public List<String> getTopMerchants(Map<String, CustomerSpendSummary> customerSpendMap, int n) {
    //     return customerSpendMap.entrySet().stream()
    //         .sorted((e1, e2) -> Double.compare(e2.getValue().getTotalSpend(), e1.getValue().getTotalSpend()))
    //         .limit(n)
    //         .map(Map.Entry::getKey)
    //         .collect(Collectors.toList());
    // }

    // /**
    //  * Get top N highest-spending customers
    //  */
    // public List<String> getTopCustomers(Map<String, CustomerSpendSummary> customerSpendMap, int n) {
    //     return customerSpendMap.entrySet().stream()
    //         .sorted((e1, e2) -> Double.compare(e2.getValue().getTotalSpend(), e1.getValue().getTotalSpend()))
    //         .limit(n)
    //         .map(Map.Entry::getKey)
    //         .collect(Collectors.toList());
    // }

    // /**
    //  * Process data in parallel
    //  */
    // public void processInParallel(List<Transaction> transactions) {
    //     transactions.parallelStream()
    //         .forEach(txn -> {
    //             // Process each transaction
    //         });
    // }

    //      For each customer:
    //  * calculate total spend
    //  * 
    private Map<String,Long> calculateTotalSpendForEachCustomer(Map<String, List<Transaction>> groupedTransactions){
        return groupedTransactions.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey(),
                entry -> Long.valueOf(Math.round(entry.getValue().stream().mapToDouble(x -> x.amount).sum()))
            ));
    }

    //      For each customer:
    //  * calculate total spend
    //  * calculate average spend
    private Map<String, Double> calculateAverageSpendForEachCustomer(Map<String, List<Transaction>> groupedTransactions){
           return groupedTransactions.entrySet().stream().collect(Collectors.toMap(
               entry -> entry.getKey(),
               entry -> {
                   List<Transaction> transactions = entry.getValue();
                   double totalSpend = transactions.stream().mapToDouble(x -> x.amount).sum();
                   return totalSpend / transactions.size();
               }
           ));
    }
}