package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * ClassName: ThreadTest
 * Package: com.atguigu.gulimall.search.thread
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/1 上午 01:17
 * @Version 1.0
 */
public class ThreadTest {
    //利用JUC裡面快速的建立線程池
    //newFixedThreadPool:固定數量的線程池
    //當前系統中池只有一兩個，所以用全局靜態
    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("start.......");

        //異步編排1 無返回值runAsync()
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//            System.out.println("當前線程:" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("運行結果：" + i);
//        }, executor);

        //異步編排2 有返回值 supplyAsync()
        /**
         * 方法完成後的感知
         */
//        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("當前線程:" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("運行結果：" + i);
//            return i;
//        }, executor).whenComplete((result, exception)->{
//            //雖然能得到異常信息，卻不能修改返回數據，類似監聽器
//            System.out.println("異步任務完成了，結果是：" + result + "，異常是：" + exception);
//        }).exceptionally((throwable -> {
//            //可以感知異常，同時返回默認值
//            return 10;
//        }));
//        System.out.println(future2.get());


        //handle()方法執行完成後的處理 (無論成功完成還是失敗完成)，就算有異常，也想要結果
        /**
         * 方法執行完成後的處理
         */
//        CompletableFuture<Integer> future2_1 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("當前線程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("結果運行：" + i);
//            return i;
//        }, executor).handle((res, thr) -> {
//            if (res != null) {
//                return res * 2;
//            }
//            if (thr != null) {
//                return 0;
//            }
//            return 0;
//        });
//        System.out.println(future2_1.get());

        /**
         * 串行化 A任務完成後 -> B任務執行
         * 帶Async的意思是：再開一個線程； 否則和A線程共用一個線程
         */
        //thenRunAsync() 不能獲取到上一步的執行結果，無返回值
//        CompletableFuture<Void> future1 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("當前線程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("結果運行：" + i);
//            return i;
//        }, executor).thenRunAsync(()->{
//            System.out.println("任務2啟動了....");
//        },executor);


        //thenAcceptAsync() 能接受上一個任務的結果，但是無返回值
//        CompletableFuture.supplyAsync(() -> {
//            System.out.println("當前線程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("結果運行：" + i);
//            return i;
//        }, executor).thenAcceptAsync((result)->{
//            System.out.println("任務2啟動了" + "上一步執行的結果是：" + result);
//        }, executor);


        //thenAcceptAsync() 能接受上一個任務的結果，有返回值
//        CompletableFuture<String> future2_2 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("當前線程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("結果運行：" + i);
//            return i;
//        }, executor).thenApplyAsync((result) -> {
//            System.out.println("任務2啟動了..." + "上一步執行的結果是：" + result);
//            return "Hello " + result;
//        }, executor);
//        System.out.println("main....end...."+future2_2.get());

        /**
         * 兩個任務組合
         * 兩個任務都完成 然後執行第三個 A + B -> C
         */
//        CompletableFuture<Object> future3_1 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任務1開始。當前線程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("任務1結束...");
//            return i;
//        }, executor);
//
//        CompletableFuture<Object> future3_2 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任務2開始。當前線程：" + Thread.currentThread().getId());
//            try {
//                Thread.sleep(3000);
//                System.out.println("任務2結束...");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return "Hello";
//        }, executor);

        //runAfterBothAsync()不能感知前兩步的執行結果 自己也沒有返回值
//        future3_1.runAfterBothAsync(future3_2,()->{
//            System.out.println("任務3開始。");
//        },executor);

        //thenAcceptBothAsync()能感知前兩步的執行結果 自己沒有返回值
//        future3_1.thenAcceptBothAsync(future3_2, (f1, f2) -> {
//            System.out.println("任務3開始...之前的結果：f1= " + f1 + "； f2= " + f2);
//        }, executor);

        //thenCombineAsync()能感知前兩步的執行結果， 還能處理前面兩個任務的返回值，並生成返回值 自己有返回值
//        CompletableFuture<String> future3_3 = future3_1.thenCombineAsync(future3_2, (f1, f2) -> {
//            return f1 + ": " + f2 + " -> HaHa";
//        }, executor);
//        System.out.println("main....end...." + future3_3.get());

        /**
         * 兩個任務 只要有一個完成就行 就能執行第三個任務 A || B = C
         */
        //runAfterEitherAsync() 不感知前面任務的結果，自己也沒有返回值
//        future3_1.runAfterEitherAsync(future3_2, () -> {
//            System.out.println("任務3開始執行。");
//        }, executor);


        //acceptEitherAsync() 能感知前面任務的結果，自己沒有返回值
//        future3_1.acceptEitherAsync(future3_2, (result) -> {  //要求任務1、2的返回類型必須相同
//            System.out.println("任務3開始執行..." + "上一步執行的結果是：" + result);
//        }, executor);


        //applyToEitherAsync() 能感知前面任務的結果，自己有返回值
//        CompletableFuture<String> future3_4 = future3_1.applyToEitherAsync(future3_2, (result) -> {//要求任務1、2的返回類型必須相同
//            System.out.println("任務3開始執行..." + "上一步執行的結果是：" + result);
//            return result.toString() + " 哈哈";
//        }, executor);
//        System.out.println("main....end...." + future3_4.get());

        /**
         * 多任務組合
         */
        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查詢商品的圖片信息……");
            return "hello.jpg";
        }, executor);
        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查詢商品的屬性……");
            return "黑色+256G";
        }, executor);
        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
                System.out.println("查詢商品的介紹……");//模擬業務時間超長
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "華為";
        }, executor);

        //allOf() 所有任務結束後才能繼續執行
//        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureAttr, futureDesc);
//        allOf.get();//等待上面3個任務完成，才不可以打印
//        System.out.println(futureImg.get() + " => " + futureAttr.get() + " => " + futureDesc.get());

//        //anyOf() 只要一個任務，結束就可以繼續執行
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureAttr, futureDesc);
        anyOf.get();//只要一個任務完成，就打印
        System.out.println(anyOf.get());


        System.out.println("end.........");
    }


    public static void thread(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main....start....");
        /**
         * 1)、繼承thread
         *         Thread01 thread = new Thread01();
         *         thread.start();
         *         System.out.println("main....end....");
         * 2)、實現Runnable接口
         *         Runable01 runable01 = new Runable01();
         *         new Thread(runable01).start();
         * 3)、實現Callable接口 + FutureTask (可以拿到返回結果，可以處理異常)
         *         FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
         *         new Thread(futureTask).start();
         *         //阻塞等待整個線程執行完成，獲取返回結果
         *         Integer integer = futureTask.get();
         * 4)、線程池
         *          給線程池直接提交任務。
         *          service.execute(new Runable01());
         *        1)、創建:
         *              1)、Executors
         *              2)、new ThreadPoolExecutor();
         *
         *        Future: 可以獲取到異步結果
         *
         *
         * 區別：
         *      1、2   不能得到返回值
         *      3      可以獲取返回值
         *      1、2、3 都不能控制資源
         *      4      可以控制資源，性能穩定
         */
        //我們以後的業務代碼裡面，以上三種啟動線程的方式禁止使用。
        //【將所有多線異步任務都交給線程池執行】
        // new Thread(()->System.out.println("hello")).start();

        //當前系統中池只有一兩個，每個異步任務，提交給線程池讓他自己去執行就行
        /**
         * 七大參數
         * corePoolSize : [5] 核心線程數 [一直存在除非(allowCoreThreadTimeOut)]; 線程池，創建好以後就準備就緒的線程數量，就等待來接受異步任務去執行。
         *                 5個 Thread thread = new Thread();  thread.start();
         *
         * maximumPoolSize：[200] 最大線程數量; 控制資源
         *
         * keepAliveTime : 存活時間。 如果當前的線程數量大於core的數量。
         *          什麼時候釋放空閒的線程( maximumPoolsize-corePoolSize ) ? 只要線程空閒大於指定的keepAliveTime
         *
         * unit:時間單位
         *
         * BlockingQUeue<Runnable> workQueue : 阻塞隊列，如果任務有很多 就會將目前多的任務放在隊列裡面。
         *                                     只要有線程空閒，就會去隊列裡面取出新的任務繼續執行
         * threadFactory : 線程創建工廠
         *
         * RejectedExecutionHandler handler: 如果隊列滿了，按照我們指定的拒絕策略拒絕執行任務
         *
         * 工作順序
         * 1)、線程池創建好 準備好core數量的核心線程，準備接受任務
         * 1.1、core滿了 就將在進來的任務放入阻塞隊列中。空閒的core就會自己去阻塞隊列獲取任務執行
         * 1.2、阻塞隊列滿了，就直接開新線程執行，最大隻能開到max指定數量
         * 1.3、max滿了就用 RejectedExecutionHandler 拒絕任務
         * 1.4、max都執行完成，有很多空閒。 在指定時間以後 keepAliveTime以後，釋放max-core(195)這些線程 200-5=195
         *                             最大線程數量 - 核心線程數量 = 可釋放的線程數量 ，核心線程是不能被釋放的，因為他們一直存在除非有(allowCoreThreadTimeOut)
         *
         *       new LinkedBlockingQueue<>() 默認是Integer最大值。容易導致我們的內存不夠，所以最好自定義數量
         *
         *       面試題:
         *       一個線程池 core:7 max:20 queue:50 100並發進來怎麼分配
         *       7個會立即得到執行 50個進入隊列 再開13個(20-7來的)進行執行，剩下的30個就使用拒絕策略，一般都是丟棄。
         *       如果不想拋棄還要執行 CallerRunsPolicy ;
         */

        //最原始的方式：
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5,
                200,
                10, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

        //快速创建线程池
        Executors.newCachedThreadPool(); //core是0，所有都可回收。 core(核心)
        Executors.newFixedThreadPool(10);//固定大小，core= max ; 都不可以回收
        Executors.newScheduledThreadPool(10); //定時任務的線程池
        Executors.newSingleThreadExecutor(); //單線程的線程池,後台從隊列裡面獲取任務，挨個執行

        System.out.println("main....end....");

    }

    //方法一
    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("當前線程:" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("運行結果：" + i);
        }
    }

    //方法二
    public static class Runable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("當前線程:" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("運行結果：" + i);
        }
    }

    //方法三
    public static class Callable01 implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            System.out.println("當前線程:" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("運行結果：" + i);
            return i;
        }
    }
}
