package cn.gc.redis.lock;

import cn.gc.redis.util.RedisLockUtil;
import cn.gc.redis.util.RedisTemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


/**
 * @Author: gc
 * @Date: 2019/7/20
 * @Version: 1.0
 * @Description: redis分布式锁实现，有部分可能存在的问题未优化，详见readme
 **/
@Component
public class RedisLockDemo implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(RedisLockDemo.class);
     //redis锁的key
    private static String LOCK_KEY = "redis-lock-key";

    @Autowired
    private RedisLockUtil redisLockUtil;
    @Autowired
    private RedisTemplateUtil redisTemplateUtil;

    @Override
    public void run(String... args) throws Exception {
        redisTemplateUtil.remove(LOCK_KEY);
        int threadCount = 10;
        for(int i = 0; i < threadCount; i++){
            new RedisThread("线程"+i).start();
        }
    }

    class RedisThread extends Thread{
        RedisThread(String name){
            super.setName(name);
        }

        @Override
        public void run() {

            //是否可以使用此锁
            boolean useLock = redisLockUtil.getLock(RedisLockDemo.LOCK_KEY, this.getName(), 10000, 1000, 2000);
            if (useLock){
                logger.info("{}-----------{}-获取锁并使用",LocalDateTime.now(),this.getName());
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //使用完以后释放锁
                redisLockUtil.releaseLock(RedisLockDemo.LOCK_KEY);
            }else{
                logger.info("{}-----------{}-锁已被占用，暂时无法使用",LocalDateTime.now(),this.getName());
            }
        }
    }
}
