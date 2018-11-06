package xyz.gosick.design.pattern.reactor.framework;

import java.nio.channels.SelectionKey;

/**
 * @author liukeshao
 * @date 2018/11/5 17:32
 */
public class SameThreadDispatcher implements Dispatcher {
    @Override
    public void onChannelReadEvent(AbstractNioChannel channel, Object readObject, SelectionKey key) {
        channel.getHandler().handleChannelRead(channel, readObject, key);
    }

    @Override
    public void stop() throws InterruptedException {

    }
}
