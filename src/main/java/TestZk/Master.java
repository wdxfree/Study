package TestZk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class Master implements Watcher {

    ZooKeeper zk;
    String hostPort;
    Master(String hostPort){
        this.hostPort=hostPort;
    }
    void  startZk(){
        try {
            zk=new ZooKeeper(hostPort,15000,this);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void process(WatchedEvent event){

        System.out.println(event);
        System.out.println("创建一个zookeeper");

    }

    public static void main(String[] args) throws Exception{
        Master m= new Master("127.0.0.1:2181");
        m.startZk();
        Thread.sleep(60000);
    }


}
