package com.alistair.community;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueTest {

    public static void main(String[] args) {
        BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<Integer>(10);
        //模拟一个服务器多个客户端
        new Thread(new Producer(blockingQueue)).start();
        new Thread(new Consumer(blockingQueue)).start();
        new Thread(new Consumer(blockingQueue)).start();
        new Thread(new Consumer(blockingQueue)).start();
    }

}

class Producer implements Runnable{

    private BlockingQueue<Integer> queue;

    public Producer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try{
            for (int i=0;i<100;i++){
                //20ms模拟处理某个业务
                Thread.sleep(20);
                queue.put(i);
                System.out.println(Thread.currentThread().getName() + "生产了：" + i + "， 容器里还剩" + queue.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

class Consumer implements Runnable{
    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }
    @Override
    public void run() {
        try{
            // 有就取
            while (true){
                //模拟用户的行为
                Thread.sleep(new Random().nextInt(520));
                int i = queue.take();
                System.out.println(Thread.currentThread().getName() + "消费了：" + i + "， 容器里还剩" + queue.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}