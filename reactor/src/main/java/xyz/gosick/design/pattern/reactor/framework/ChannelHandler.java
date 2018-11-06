package xyz.gosick.design.pattern.reactor.framework;

import java.nio.channels.SelectionKey;

/**
 * @author liukeshao
 * @date 2018/11/5 14:30
 */
public interface ChannelHandler {
    void handleChannelRead(AbstractNioChannel channel, Object readObject, SelectionKey key);
}
