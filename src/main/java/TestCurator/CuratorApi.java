package TestCurator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.apache.curator.framework.recipes.nodes.PersistentNode;
import org.apache.curator.framework.recipes.nodes.PersistentTtlNode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

public class CuratorApi {

    public static void main(String[] args) {


        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1500,3); //失去连接的处理策略
        String hostPort="192.168.1.185:2181";   // zkServer 的ip和端口
        CuratorFramework curatorFramework=CuratorFrameworkFactory.newClient(hostPort,retryPolicy); //通过工厂创建curatorFramework

        curatorFramework.start(); //启动

        /*
        *       设置NodeCache监控  是用来监听节点的数据变化的 ,能不能直接监控指定的事件      */

        NodeCache nodeCache = new NodeCache(curatorFramework,"/study");   // 实例化NodeCache 参数：CuratorFramework 客户端，path 监控节点
        NodeCacheListener nodeCacheListener = new NodeCacheListener() {  //设置获取通知后的处理方式，可以采用lambda的方式直接使用
            @Override
            public void nodeChanged() throws Exception {
                System.out.println("节点发生改变");
            }
        };
        nodeCache.getListenable().addListener(nodeCacheListener); //注册监控


        /*
        *       设置PathChildrenCache  是用来监听指定节点 的子节点变化情况。  */
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework,"/study",true);//实例化PathChildrenCache
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {  //可以按照lambd的方式
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                switch (pathChildrenCacheEvent.getType()){
                    case CHILD_ADDED:                      //事件情况
                        System.out.println("pathChildrenCache+添加子节点");
                        break;
                        default:
                            System.out.println("其他的方法");
                            break;
                }

            }
        });


        /*
        *      设置 TreeCache  既能够监听自身节点的变化、也能够监听子节点的变化。*/
        TreeCache treeCache=new TreeCache(curatorFramework,"/study");//采用lambda 的方式来创建
        treeCache.getListenable().addListener((CuratorFramework curatorFramework1,TreeCacheEvent treeCacheEvent)->{
            switch (treeCacheEvent.getType()){
                case NODE_ADDED:
                    System.out.println("TreeCache+创建节点");
                    break;
                case NODE_UPDATED:
                    System.out.println("TreeCache+更新节点"+treeCacheEvent.getData().getData().toString());
                    break;
                    default:
                        System.out.println("TreeCache");
                        break;
            }
        });




        try {



            curatorFramework.setData().forPath("/study","Test".getBytes()); //修改节点数据

            try {
                curatorFramework.create().withMode(CreateMode.PERSISTENT).forPath("/study/study_one","study_one".getBytes()); //创建节点
            }catch (KeeperException e){
                System.out.println("节点已经存在");
            }


            byte[] bytes = curatorFramework.getData().forPath("/study/studytest");
            System.out.println(new String(bytes));

        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}
