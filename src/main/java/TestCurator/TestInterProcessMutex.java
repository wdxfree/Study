package TestCurator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.security.spec.EncodedKeySpec;

//分布式可重用排它锁
public class TestInterProcessMutex {
    public static void main(String[] args) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1500,3); //失去连接的处理策略

        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("192.168.1.185:2181",retryPolicy);

        final InterProcessMutex mutex = new InterProcessMutex(curatorFramework,"/zkclock");

        for (int i=0;i<10;i++){
            new Thread(()->{
                try {
                    mutex.acquire();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

    }

}
