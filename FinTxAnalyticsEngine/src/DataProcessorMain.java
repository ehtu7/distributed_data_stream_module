public class DataProcessorMain {
    public static void main(String[] args) {
        DataProcessor dataProcessor = new DataProcessor();
        java.util.List<Transaction> mockData = Utility.generateMockTransactions (
            1000,  // totalTransactions
            100,   // numCustomers
            50,   // merchantCount
            30,   // lastDays
            42L   // randomSeed
        );
        dataProcessor.processData(mockData);
    }
}
