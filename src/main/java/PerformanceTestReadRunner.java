import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PerformanceTestReadRunner {
    public static void main(String[] args) {

        ExecutorService executor1 = Executors.newFixedThreadPool(1);
        ExecutorService executor2 = Executors.newFixedThreadPool(1);
        executor1.execute(new ReadCompressedData());
        executor2.execute(new ReadUncompressedData());

        executor1.shutdown();
        executor2.shutdown();
    }
}
