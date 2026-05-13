import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Filter transactions from last 30 days
    Exclude fraudulent transactions
    Group by customer
    For each customer:
    calculate total spend
    calculate average spend
    top 3 merchants by spend
    Return top 10 highest-spending customers
    Process in parallel efficiently
 */
public class DataParallelProcessor {

    // Single parallel pipeline to filter, exclude fraud, group by customer, sum spend and return top 3 customers
    public List<Map.Entry<String, Double>> top3CustomersBySpend(List<Transaction> transactions) {
        Map<String, List<Transaction>> grouped = processTransactionData(transactions);
        return topCustomersBySpend(grouped, 3);
    }
    //Filter transactions from last 30 days
    //Exclude fraudulent transactions
    //groupby customer
    public Map<String,List<Transaction>> processTransactionData(List<Transaction> transactions){
        return transactions.parallelStream()
        .filter(txn->txn.txnTime.isAfter(java.time.LocalDateTime.now().minusDays(30)))
        .filter(txn->txn.fraud==false)
        .collect(Collectors.groupingBy(txn->txn.customerId));
    }

    //calculate total spend for each customer
    public Map<String, Double> calculateTotalSpend(Map<String, List<Transaction>> groupedTransactions) {
        return groupedTransactions.entrySet().parallelStream()
        .collect(Collectors
            .toMap(Map.Entry::getKey, entry -> entry.getValue().stream().mapToDouble(txn -> txn.amount).sum()));
    }

    //calculate average spend
    public Map<String,Double> calculateAverageSpend(Map<String, List<Transaction>> groupedTransactions){
        return groupedTransactions.entrySet().parallelStream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().mapToDouble(txn -> txn.amount).average().orElse(0.0)));
    }

     //top 3 merchants by spend
     public List<Map.Entry<String, Double>> topMerchantsBySpend(List<Transaction> transactions, int topN) {
        return transactions.parallelStream()
                .collect(Collectors.groupingBy(t -> t.merchant, Collectors.summingDouble(t -> t.amount)))
                .entrySet()
                .parallelStream()
                .sorted(Map.Entry.<String, Double>comparingByValue(java.util.Comparator.reverseOrder()))
                .limit(topN)
                .collect(Collectors.toList());
    }

     //Return top 10 highest-spending customers
     public List<Map.Entry<String, Double>> topCustomersBySpend(Map<String, List<Transaction>> groupedTransactions, int topN) {
        return groupedTransactions.entrySet().parallelStream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().mapToDouble(txn -> txn.amount).sum()))
                .entrySet()
                .parallelStream()
                .sorted(Map.Entry.<String, Double>comparingByValue(java.util.Comparator.reverseOrder()))
                .limit(topN)
                .collect(Collectors.toList());
    }

  
}
