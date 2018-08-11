package org.study.jim;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SelectThreadPoolSockets  extends  SelectSockets{
    //定义线程池
    private static final int POOL_SIZE = 5;
    private ThreadPool pool = new ThreadPool(POOL_SIZE);
    public static void main(String[] args) throws IOException {
        new SelectThreadPoolSockets().go();
    }
    protected void readDataFromSocket(SelectionKey key) throws Exception
    {
        WorkerThread worker = pool.getWorker();
        if (worker == null)
        {
            // No threads available. Do nothing. The selection
            // loop will keep calling this method until a
            // thread becomes available. This design could
            // be improved.
            return;
        }
        // Invoking this wakes up the worker thread, then returns
        worker.serviceChannel(key);
    }
    /*自定义线程池对象，提供同步的获取线程工作类和添加线程的方法
     */
    private class ThreadPool{
        //初始化线程集合
        private List idle = new LinkedList();
        public ThreadPool(int  poolSize) {
            for (int i = 0; i < poolSize; i++)
            {
                WorkerThread thread = new WorkerThread(this);
                // Set thread name for debugging. Start it.
                thread.setName("Worker" + (i + 1));
                thread.start();
                idle.add(thread);
            }
        }
        /**
         * Find an idle worker thread, if any. Could return null.
         */
        WorkerThread getWorker() {
            WorkerThread worker = null;
            synchronized (idle)
            {
                if (idle.size() > 0)
                {
                    worker = (WorkerThread) idle.remove(0);
                }
            }
            return (worker);
        }
        /**
         * Called by the worker thread to return itself to the idle pool.
         */
        void returnWorker(WorkerThread worker)
        {
            synchronized (idle)
            {
                idle.add(worker);
            }
        }
    }

    /**
     * 线程工作类：
     * 从Selector中获取就绪通道，执行响应
     */
    private class WorkerThread extends Thread{
        private ByteBuffer buffer = ByteBuffer.allocate(1024);
        private ThreadPool pool;
        private SelectionKey key;
        WorkerThread(ThreadPool pool)
        {
            this.pool = pool;
        }
        @Override
        public synchronized void run() {
            System.out.println(this.getName()+" is reading run ");
            while (true){
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    this.interrupt();
                }
                if(key == null){
                    continue;
                }
                System.out.println(this.getName()+" has been awakened ");
                try
                {
                    drainChannel(key);
                }
                catch (Exception e)
                {
                    System.out.println("Caught '" + e + "' closing channel");
                    // Close channel and nudge selector
                    try
                    {
                        key.channel().close();
                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                    key.selector().wakeup();
                }
                key = null;
                // Done. Ready for more. Return to pool
                this.pool.returnWorker(this);
            }
        }

        void drainChannel(SelectionKey key) throws Exception
        {
            SocketChannel channel = (SocketChannel) key.channel();
            int count;
            buffer.clear(); // 清空buffer
            // Loop while data is available; channel is nonblocking
            while ((count = channel.read(buffer)) > 0)
            {
                buffer.flip(); // make buffer readable
                // Send the data; may not go all at once
                while (buffer.hasRemaining())
                {
                    channel.write(buffer);
                }
                // WARNING: the above loop is evil.
                // See comments in superclass.
                buffer.clear(); // Empty buffer
            }
            if (count < 0)
            {
                // Close channel on EOF; invalidates the key
                channel.close();
                return;
            }
            // Resume interest in OP_READ
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            // Cycle the selector so this key is active again
            key.selector().wakeup();
        }
        synchronized void serviceChannel(SelectionKey key)
        {
            this.key = key;
            key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
            this.notify(); // Awaken the thread
        }
    }
}
