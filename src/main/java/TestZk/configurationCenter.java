package TestZk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.Properties;

//配置中心
public class configurationCenter {
    private CuratorFramework curatorFramework;
    private String hostPort;
    private RetryPolicy retryPolicy;

    public void setlistener(){
        NodeCache nodeCache =new NodeCache(curatorFramework,"/study");
        try {
            nodeCache.start();
            nodeCache.getListenable().addListener(()->{
                byte[] data = nodeCache.getCurrentData().getData();
                System.out.println("数据发生变化"+new String());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public configurationCenter(String hostPort){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        curatorFramework= CuratorFrameworkFactory.newClient(hostPort,retryPolicy);
    }

    public CuratorFramework getCuratorFramework() {
        return curatorFramework;
    }

    public void Start(){
        curatorFramework.start();
    }

    public static void main(String[] args) {
        configurationCenter configurationCenter =new configurationCenter("192.168.1.185:2181");
        CuratorFramework curatorFramework=configurationCenter.getCuratorFramework();
        curatorFramework.start();
        configurationCenter.setlistener();
        try {
            curatorFramework.setData().forPath("/study","sss".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



}
