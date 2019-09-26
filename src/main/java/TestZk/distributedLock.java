package TestZk;


import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

public class distributedLock implements Lock, Watcher { //实现了lock和watcher 接口
    private ZooKeeper zooKeeper;
    private String hostPort;
    private String current;
    private String path;
    private String pre;

    @Override
    public String toString() {
        return "distributedLock{" +
                "zooKeeper=" + zooKeeper +
                '}';
    }

    public distributedLock(String hostPort,String path) {
        this.hostPort = hostPort;
        this.path=path;
    }
    public void startZk(){
        try {
            zooKeeper=new ZooKeeper(hostPort,1500,this);
            Stat stat=zooKeeper.exists("/zklock",this);
            if(stat==null){
                zooKeeper.create("/zklock","".getBytes(),OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
    public boolean tryLock(long time, TimeUnit unit){
        return false;
    }



    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()){
            case NodeDeleted:
                synchronized (pre){
                    System.out.println("唤醒线程"+Thread.currentThread().getName());
                    pre.notify(); //唤醒线程
                }
                break;
                default:
                    break;

        }


    }

    //获取子节点列表
    public void getList(String path){
        try {
            List<String> children = zooKeeper.getChildren(path, true);

            System.out.println(children);
        } catch (KeeperException e) {

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String createNode(String path){ //创建临时顺序节点
        String name=null;
        try {

            name=zooKeeper.create(path,"Lock node".getBytes(),OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return name;

    }


    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public void stopZk(){ //关闭zkclient 与zkserver的连接
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            System.out.println("关闭失败");
            e.printStackTrace();
        }
    }

    @Override
    public void lock() {
        if (tryLock()) {

            System.out.println(Thread.currentThread().getName() + " 获取锁成功");
            System.out.println("正在执行程序，请稍等");
            unlock();
            return;
        }
        try {
            waitForLock();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

    }

    private void  waitForLock() throws KeeperException, InterruptedException {

        Stat stat = zooKeeper.exists("/zklock/"+pre, true);
        if (stat != null) {
            System.out.println(Thread.currentThread().getName() + "正在等待");
            synchronized (pre) {
                Long curentTime = System.currentTimeMillis();
                pre.wait(10000); //设置超时时间
                Long now = System.currentTimeMillis();
                if (now - curentTime >= 10000) {
                    System.out.println(Thread.currentThread().getName() + "超过等待时间，退出竞争");
                    unlock();

                }
                System.out.println(Thread.currentThread().getName() + "获取锁成功");

            }
        }
    }


    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock(){
        current=createNode("/zklock/");
        List<String> list=null;
//        System.out.println(current);

        System.out.println("当前线程"+Thread.currentThread().getName()+"创建节点"+current);
        try {
            list = zooKeeper.getChildren(path, true);
            list.sort(Comparator.naturalOrder()); //对list中存在的的节点进行排序。
//            System.out.println(list.toString());
            int i =list.indexOf(current.substring(8));
//            System.out.println(i);
            if(i==0){


                return true;
            }
            else{
//                System.out.println(list.);
                pre=list.get(i-1);
                System.out.println(Thread.currentThread().getName()+"监控"+"/zklock/"+pre);
                zooKeeper.exists("/zklock/"+pre,true);
                return false;
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println();
        return false;
    }


    @Override
    public void unlock() {
//        System.out.println("释放锁");
        try {
            System.out.println(Thread.currentThread().getName()+"释放锁");
            zooKeeper.delete(current, -1);
            current = null;
            zooKeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();


        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }


    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(10);
        for(int i = 0;i<3;i++){
            new Thread(() -> {

                distributedLock dispathLock  = null;
                try {

                     dispathLock = new distributedLock("192.168.1.185:2181","/zklock");
                     dispathLock.startZk();
                     dispathLock.lock(); //锁

                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if (dispathLock!=null){
                        dispathLock.unlock();
                    }
                }
            }).start();

        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    }



