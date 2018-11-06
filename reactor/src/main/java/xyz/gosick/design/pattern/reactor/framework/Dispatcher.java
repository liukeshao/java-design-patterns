package xyz.gosick.design.pattern.reactor.framework;

import java.nio.channels.SelectionKey;

/**
 * @author liukeshao
 * @date 2018/11/5 14:27
 */
public interface Dispatcher {

    void onChannelReadEvent(AbstractNioChannel channel, Object readObject, SelectionKey key);

    void stop() throws InterruptedException;
}
