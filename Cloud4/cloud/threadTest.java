package cloud;

public class threadTest extends Thread {

    public static void main(String[] args) {

        int threadCount = 16;

        threadTest[] t = new threadTest[threadCount];
        for (int i = 0; i < threadCount; i++) {
            t[i] = new threadTest();
            t[i].start();
        }
    }

    public void run(){
        System.out.println("thread is running...");
        for (long i = 0L; i < 100000000000L; i++) {
            int b = 10000*1000000;
        }
        System.out.println("thread is Done...");
    }
}
