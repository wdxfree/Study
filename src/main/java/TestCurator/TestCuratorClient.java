package TestCurator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestCuratorClient {
    private final Logger logger = LoggerFactory.getLogger(TestCuratorClient.class);

    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework zkc ;
    public TestCuratorClient(){
        zkc=CuratorFrameworkFactory.newClient("192.168.1.154:2181", retryPolicy);
    }
    public void startZk(){
        zkc.start();
    }
    public void CreateNode(){
        try {
            zkc.create().withMode(CreateMode.PERSISTENT).forPath("/Testone");
        } catch (Exception e) {
           System.out.println("Node is exist");
        }
    }
    public void Test(){
        System.out.println("retryPolicy");
    }

    public static void main(String[] args) throws InterruptedException {
        TestCuratorClient testCuratorClient = new TestCuratorClient();
        if(testCuratorClient!=null){
            testCuratorClient.startZk();
            testCuratorClient.CreateNode();
        }
        Thread.sleep(6000);
    }
}
