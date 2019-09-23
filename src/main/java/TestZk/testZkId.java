package TestZk;


import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class testZkId {
    private Logger logger= LoggerFactory.getLogger(this.getClass());
    private String zkadr;
    private String zkdig;
    private static ConcurrentMap<String,DistributedAtomicInteger> concurrentMap = new ConcurrentHashMap<>();//Concurrentmap 支持并发访问的map类 ，static 静态，全类共同使用一个


    public testZkId(String zkadr, String zkdig) {
        this.zkadr = zkadr;
        this.zkdig = zkdig;
        getCuratorFramework();
    }

    private CuratorFramework curatorFramework; //curator 客户端
    private ConnectionStateListener listener = new ConnectionStateListener() {
        public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
            if(connectionState==ConnectionState.LOST){
                logger.info("zk is closed");
                destory();
                getCuratorFramework();//执行函数重新创建CuratorFrameWork 函数
            }

        }
    };
    public void destory(){
        if(curatorFramework!=null){
            try {
                curatorFramework.close();
                curatorFramework=null;
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }
        }
    }

    private ReentrantLock INSTANCE_INIT_LOCK =new ReentrantLock(true);//true 为公平锁，优先执行等待时间最长的锁
    public CuratorFramework getCuratorFramework(){//为什么在执行创建CuratorFramework 就要进行加锁，而不是在创建序列号的时候进行操作
        if(curatorFramework==null){

            try {
                if(INSTANCE_INIT_LOCK.tryLock(2, TimeUnit.SECONDS)){
                    RetryPolicy retryPolicy=new ExponentialBackoffRetry(1000,10);
                    curatorFramework= CuratorFrameworkFactory
                            .builder()
                            .connectString(zkadr)
                            .sessionTimeoutMs(5000)
                            .retryPolicy(retryPolicy)
                            .build();
                    Listenable<ConnectionStateListener> connectionStateListenable = curatorFramework.getConnectionStateListenable();
                    connectionStateListenable.addListener(listener);
                    curatorFramework.start();
                }
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }finally {
                INSTANCE_INIT_LOCK.unlock();
            }
        }
        if(curatorFramework==null){
            throw  new NullPointerException("zkClient is not null");
        }
        return curatorFramework;
    }
    //上述全都是创建连接的过程

    public int getSeq(String key)
    {
        DistributedAtomicInteger distributedAtomicInteger =concurrentMap.get(key);
        int value=0;
        if(distributedAtomicInteger==null){
            concurrentMap.put(key,new DistributedAtomicInteger(getCuratorFramework(),"/Test",new RetryNTimes(1, 1)));
            distributedAtomicInteger=concurrentMap.get(key);
            try {
                while(value==0){
                    AtomicValue<Integer> increment = distributedAtomicInteger.increment();
                    value=increment.postValue();
                }

            } catch (Exception e) {
                logger.error("ZookeeperClient,getSeq:{}",e);
            }


        }
        return value;

    }

    public static void main(String[] args) {
        testZkId testZkId=new testZkId("192.168.1.230:2181","Test");
        int one = testZkId.getSeq("one");
        System.out.println(one);
    }






}


