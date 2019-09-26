package TestZk;

import org.apache.zookeeper.*;

import java.io.IOException;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

public class ZooKeeperApi implements Watcher {
    ZooKeeper zooKeeper;
    String hostPort;                 //ip和端口  格式 "a.a.a.a:port"

    public ZooKeeperApi(String hostPort) {
        this.hostPort = hostPort;
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public void startZk(){         //实例化zkclient
        try {
            zooKeeper=new ZooKeeper(hostPort,1500,this);  //使用自己设置的监控方法，下一步尝试着使用自定义的监控方法
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override   //设置实现watcher 接口 来获得事件 事件来自于client于server 之间的连接状态的变更，zk节点的改变（如果要获得节点事件，而需要通过exist设置监控点）
    public void process(WatchedEvent watchedEvent) {

        switch (watchedEvent.getState()){ //连接状态
            case SyncConnected:
                System.out.println("连接成功");
                break;
            case Disconnected:
                System.out.println("连接中断");
                break;
            default:
                break;
        }

        switch (watchedEvent.getType()){  //节点状态变更
            case NodeCreated: //exist 设置监控点，如果znode，则会zkclient 则会获得来自zkserver 通知，类型为NodeCreate 从而执行下面的程序。
                System.out.println("创建节点"+watchedEvent.getPath());
                break;
            case NodeDeleted:
                System.out.println("节点被删除"+watchedEvent.getPath());
                break;
            case NodeDataChanged:
                System.out.println("节点数据被改变"+watchedEvent.getPath());
                break;
            case NodeChildrenChanged:
                System.out.println("子节点事件"+watchedEvent.getType());
            default:
                break;
        }
    }

    public void stopZk(){ //关闭zkclient 与zkserver的连接
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            System.out.println("关闭失败");
            e.printStackTrace();
        }
    }
    public void CreateNode(){

    }

    public static void main(String[] args) {


        //获得zkclient
        ZooKeeperApi zooKeeperApi=new ZooKeeperApi("192.168.1.185:2181");
        zooKeeperApi.startZk();
        ZooKeeper zooKeeper=zooKeeperApi.getZooKeeper();



        try {

            zooKeeper.getChildren("/zklock", true); //设置监控点，监控子节点状态。
            zooKeeper.create("/zklock/task-","lock".getBytes(),OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);//创建临时顺序节点

        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
