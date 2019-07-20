package cn.gc.redis.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: gc
 * @Date: 2019/4/2
 * @Description: redisTemPlate操作工具类
 **/
@Component
public class RedisTemplateUtil {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 保存字符串类型值
     * @param key
     *      key
     * @param value
     *      value
     * @param <K>
     * @param <V>
     */
    public <K, V> void saveStr(K key, V value){
        redisTemplate.opsForValue().set(key,value);
    }
    /**
     * 保存字符串类型值
     * @param key
     *      key
     * @param value
     *      value
     * @param time
     *      保留时间长度
     * @param timeUnit
     *      时间单位
     * @param <K>
     * @param <V>
     */
    public <K, V> void saveStr(K key, V value, long time, TimeUnit timeUnit){
        redisTemplate.opsForValue().set(key,value,time,timeUnit);
    }

    /**
     * 获取字符串类型缓存
     * @param key
     *      key
     * @param <K>
     * @param <T>
     * @return
     */
    public <K,T> T getStr(K key){
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 保存hash
     * @param key
     *      key
     * @param field
     *      field
     * @param value
     *      value
     * @param <K>
     * @param <F>
     * @param <V>
     */
    public <K,F,V> void saveHash(K key, F field, V value){
        redisTemplate.opsForHash().put(key,field, value);
    }

    /**
     * 获取hash结构中指定key下所有field的数据键值对
     * @param key
     *      key
     * @param <K>
     * @param <V>
     * @return
     */
    public <K,V> V getHash(K key){
        return (V) redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取hash数据结构中指定key的指定field的值
     * @param key
     *      key
     * @param field
     *      field
     * @param <K>
     * @param <F>
     * @param <V>
     * @return
     */
    public <K,F,V> V getHash(K key, F field){
        return (V) redisTemplate.opsForHash().get(key,field);
    }

    /**
     * 获取hash数据结构中指定key的多个field的值
     * @param key
     *      key
     * @param fields
     *      fields
     * @param <K>
     * @param <V>
     * @return
     */
    public <K,V> List<V> getHash(K key, Collection<Object> fields){
        return (List<V>)redisTemplate.opsForHash().multiGet(key,fields);
    }

    /**
     * 批量插入hash数据
     * @param key
     *      key
     * @param fields
     *      fields
     * @param <V>
     * @return
     */
    public <V> void setHash(String key, Map<String, V> fields){
        redisTemplate.executePipelined((RedisConnection connection) -> {
            byte[] keyByte = key.getBytes();
            for (Map.Entry<String,V> entry : fields.entrySet()){
                byte[] field = entry.getKey().getBytes();
                byte[] value = entry.getValue().toString().getBytes();
                connection.hSet(keyByte,field,value);
            }
            return null;
        });
    }

    /**
     * 批量插入list数据到hash结构中
     * @param key
     *      key
     * @param fields
     *      fields
     * @param <K>
     * @param <V>
     * @return
     */
    public <K,F,V> void setListHash(String key, Map<String, List<String>> fields){
        redisTemplate.executePipelined((RedisConnection connection) -> {
            byte[] keyByte = key.getBytes();
            for (Map.Entry<String,List<String>> entry : fields.entrySet()){
                byte[] field = entry.getKey().getBytes();
                byte[] value = StringUtils.listToStr(entry.getValue()).getBytes();
                connection.hSet(keyByte,field,value);
            }
            return null;
        });
    }

    /**
     * 删除redis缓存信息
     * @param key
     *      key
     * @param <K>
     */
    public <K> boolean remove(K key){
        return redisTemplate.delete(key);
    }

    /**
     * 删除redis缓存信息
     * @param key
     *      key
     * @param <K>
     */
    public <K> void remove(Collection<K> key){
        redisTemplate.delete(key);
    }

    /**
     * 通过前缀匹配的key集合set
     * @param prefix
     *      前缀
     * @return
     */
    public Set<Object> getKetSetPrefix(String prefix){
        return redisTemplate.keys(prefix + "*");
    }

    /**
     * 添加字符串
     * @param key
     * @param value
     */
    public void setStr(String key, String value) {
        redisTemplate.opsForValue().set(key,value);
    }

    /**
     * 添加字符串，成功返回true，失败返回false
     * @param key
     * @param value
     */
    public boolean setStrIfAbsent(String key, String value) {
        return redisTemplate.opsForValue().setIfAbsent(key,value);
    }

    /**
     * 获取原来key键对应的值并重新赋新值。
     * @param key
     * @param value
     * @return
     */
    public Object getAndSetStr(String key, String value) {
        return redisTemplate.opsForValue().getAndSet(key, value);
    }

    /**
     * 添加字符串并设置过期时间，成功返回true，失败返回false
     * @param key
     * @param value
     */
    public Boolean setStrIfAbsent(String key, String value, long expireTime) {
        return redisTemplate.opsForValue().setIfAbsent(key,value, Duration.ofMillis(expireTime));
    }
}
