package xyz.gosick.design.pattern.reactor.framework;

import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author liukeshao
 * @date 2018/11/5 17:33
 */
public class ThreadPoolDispatcher implements Dispatcher {
    private final ExecutorService executorService;

    public ThreadPoolDispatcher(int poolSize) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    @Override
    public void onChannelReadEvent(AbstractNioChannel channel, Object readObject, SelectionKey key) {
        executorService.execute(() -> channel.getHandler().handleChannelRead(channel, readObject, key));
    }

    @Override
    public void stop() throws InterruptedException {
        executorService.shutdownNow();
        executorService.awaitTermination(4, TimeUnit.SECONDS);
    }
}
