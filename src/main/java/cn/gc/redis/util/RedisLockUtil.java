package cn.gc.redis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Author: gc
 * @Date: 2019/7/20
 * @Description: redis锁工具类
 **/
@Component
public class RedisLockUtil {
    private static Logger log = LoggerFactory.getLogger(RedisLockUtil.class);

    @Autowired
    private RedisTemplateUtil redisTemplate;

    private static final long LOCK_TRY_INTERVAL = 50L;// 默认多久尝试获取一次锁, 需考虑redis服务器压力

    private static final long LOCK_TRY_TIMEOUT = 200L;// 默认尝试多久, 需考虑并发压力

    private static final long DEFAULT_EXPIRE_TIME = 3000L;   // 默认key过期时间, 需考虑业务执行时长

    private static final String LOCK_SUCCESS = "OK";    // set方法执行成功后的返回值

    private static final String SET_IF_NOT_EXIST = "NX";    // SET IF NOT EXIST，key存在，进行set操作。若key已经存在，则不做任何操作

    private static final String SET_WITH_EXPIRE_TIME = "PX";    // 当设置为PX，表示设置一个过期时间

    private static final String DEFAULT_VALUE = "v"; // set方法的value字段, 这里默认设置v

    /**
     * 尝试获取全局锁
     *
     * @param key 锁名
     * @return true 获取成功，false获取失败
     */
    public boolean tryLock(String key) {
        return getLock(key, DEFAULT_VALUE, LOCK_TRY_TIMEOUT, LOCK_TRY_INTERVAL, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 尝试获取全局锁
     *
     * @param key     锁名
     * @param timeout 获取超时时间 单位ms
     * @return true 获取成功，false获取失败
     */
    public boolean tryLock(String key, long timeout) {
        return getLock(key, DEFAULT_VALUE, timeout, LOCK_TRY_INTERVAL, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 尝试获取全局锁
     *
     * @param key         锁名
     * @param timeout     获取锁的超时时间
     * @param tryInterval 多少毫秒尝试获取一次
     * @return true 获取成功，false获取失败
     */
    public boolean tryLock(String key, long timeout, long tryInterval) {
        return getLock(key, DEFAULT_VALUE, timeout, tryInterval, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 尝试获取全局锁
     *
     * @param key            锁名
     * @param timeout        获取锁的超时时间
     * @param tryInterval    多少毫秒尝试获取一次
     * @param lockExpireTime 锁的过期
     * @return true 获取成功，false获取失败
     */
    public boolean tryLock(String key, long timeout, long tryInterval, long lockExpireTime) {
        return getLock(key, DEFAULT_VALUE, timeout, tryInterval, lockExpireTime);
    }

    /**
     * 尝试获取全局锁, 只尝试一次
     *
     * @param key 锁名
     * @return true 获取成功，false获取失败
     */
    public boolean onceTryLock(String key) {
        return getLock(key, DEFAULT_VALUE, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 尝试获取全局锁, 只尝试一次
     *
     * @param key            锁名
     * @param lockExpireTime 锁的过期
     * @return true 获取成功，false获取失败
     */
    public boolean onceTryLock(String key, long lockExpireTime) {
        return getLock(key, DEFAULT_VALUE, lockExpireTime);
    }

    /**
     * 获取全局锁
     *
     * @param key         锁名
     * @param value       锁value, 如果要保证加锁和解锁是同一个客户端的话, 这个参数用来指定特定客户端
     * @param expireTime  锁的超时时间
     * @param timeout     获取锁的超时时间
     * @param tryInterval 多少ms尝试一次
     * @return
     */
    public boolean getLock(String key, String value, long timeout, long tryInterval, long expireTime) {
        try {
            // 锁如果为空, 获取锁失败
            if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
                return false;
            }
            long startTime = System.currentTimeMillis();  // 开始时间戳
            do {
                Boolean result = redisTemplate.setStrIfAbsent(key, value, expireTime);
                if (result) {  // 返回成功，表示加锁成功
                    return true;
                }
                if (System.currentTimeMillis() - startTime > timeout) { // 尝试超过了设定超时时间后直接跳出循环，获取锁失败
                    log.info("{}-----------{}-获取锁超时: {}",LocalDateTime.now(), Thread.currentThread().getName(), System.currentTimeMillis() - startTime);
                    return false;
                }
//                log.info("锁被占用中，{}后尝试重新获取", tryInterval);
                Thread.sleep(tryInterval);  // 循环时设置时间差
            }
            while (true);   // 只要锁存在，循环
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 获取全局锁(无超时后循环重试机制，拿不到直接返回false)
     *
     * @param key        锁名
     * @param value      锁value, 如果要保证加锁和解锁是同一个客户端的话, 这个参数用来指定特定客户端
     * @param expireTime 超时时间
     * @return
     */
    public boolean getLock(String key, String value, long expireTime) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return false;
        }
        // 参数: key, value, key不存在set操作存在就不做任何操作, 可设置超时时间, 具体超时时间
        return redisTemplate.setStrIfAbsent(key, value, expireTime);
    }

    /**
     * 释放锁
     *
     * @param key 锁名
     */
    public void releaseLock(String key) {
            if (!StringUtils.isEmpty(key)) {
                boolean del = redisTemplate.remove(key);
                log.info("{}-----------{}-是否释放成功：{}",LocalDateTime.now(), Thread.currentThread().getName(), del);
            }
    }
}
