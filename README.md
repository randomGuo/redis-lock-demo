#redis分布式锁实现demo
#####暂时只实验了单机环境，打印顺序可能会不同，log框架的原因，对比时间即可理解

#####1.0版本问题
    问题1、如果服务端（即线程）挂了，redis的锁时间没有过期，这段时间是无法获取到redis锁的。
    问题2、如果线程执行时间超出过期时间，锁可能被多个线程持有