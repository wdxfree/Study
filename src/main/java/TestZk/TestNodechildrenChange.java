package TestZk;


import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TestNodechildrenChange {
    private Logger logger= LoggerFactory.getLogger(this.getClass());



    private String hostPort;
    RetryPolicy retryPolicy =new BoundedExponentialBackoffRetry(2,100,1);
    public CuratorFramework curatorFramework;
    PathChildrenCache pathChildrenCache =null;
    TreeCache treeCache=null;

    public PathChildrenCache getPathChildrenCache() {
        return pathChildrenCache;
    }

    public TestNodechildrenChange(String hostPort) {
        this.hostPort = hostPort;
        curatorFramework= CuratorFrameworkFactory.newClient(hostPort,retryPolicy);
        pathChildrenCache = new PathChildrenCache(curatorFramework,"/Test",true);
        treeCache=new TreeCache(curatorFramework,"/worker");

    }

    public TreeCache getTreeCache() {
        return treeCache;
    }

    void setPathChildrenCache(){

        try {

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public CuratorFramework getCuratorFramework() {
        return curatorFramework;
    }

    ConnectionStateListener connectionStateListener=new ConnectionStateListener() {  //怎样将这个函数改造成lambda 表达式
        @Override
        public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
            if(connectionState.isConnected()==true){
                System.out.println("连接");
            }

        }
    };
    public void Test(){
        try {
            curatorFramework.getData().inBackground().forPath("/d");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    CuratorListener curatorListener =new CuratorListener() { //设置监控点 CuratorListener 只能 对后台任务有监控任务  background task
        public void eventReceived(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
            try{
                switch (curatorEvent.getType()){
                    case CHILDREN:
                        break;
                    case CREATE:
                        System.out.println("创建子节点"+curatorEvent.getName());
                        break;
                    case DELETE:
                        System.out.println("删除节点");
                        break;
                    case GET_DATA:
                        System.out.println("获得节点数据");
                        default:
                            break;
                }

            }catch (Exception e){
                logger.error("",e);
                curatorFramework.close();
            }
        }
    };


    public void setlistener(){
        curatorFramework.getCuratorListenable().addListener(curatorListener);
        curatorFramework.getConnectionStateListenable().addListener(connectionStateListener);//设置注册监控点
    }
    public static void main(String[] args) throws Exception {
        TestNodechildrenChange testNodechildrenChange=new TestNodechildrenChange("192.168.1.230:2181");
        CuratorFramework curatorFramework = testNodechildrenChange.getCuratorFramework();
        testNodechildrenChange.setlistener();
        curatorFramework.start();
        TreeCache treeCache =testNodechildrenChange.getTreeCache();
        treeCache.getListenable().addListener((CuratorFramework curatorFramewor, TreeCacheEvent treeCacheEvent)->{
            if(treeCacheEvent.getData()!=null){
                System.out.println(1);
                if(treeCacheEvent.getData().getData()!=null){
                    System.out.println(new String(treeCacheEvent.getData().getData()));
                    System.out.println(treeCacheEvent.getType());
                    System.out.println(2);
                }
            }


        });
        treeCache.start();






        /*
        *pathChildrenCache的使用 */

//        PathChildrenCache pathChildrenCache=testNodechildrenChange.getPathChildrenCache();

//        pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
//        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
//            @Override
//            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
//                System.out.println("事件类型："  + event.getType() + "；操作节点：" + event.getData().getPath());
//                System.out.println("测试");
//            }
//        });



        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}
