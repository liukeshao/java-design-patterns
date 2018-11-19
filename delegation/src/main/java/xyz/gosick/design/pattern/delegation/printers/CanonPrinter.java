package xyz.gosick.design.pattern.delegation.printers;

import lombok.extern.slf4j.Slf4j;
import xyz.gosick.design.pattern.delegation.Printer;

/**
 * @author liukeshao
 * @date 2018/11/19 17:00
 */
@Slf4j
public class CanonPrinter implements Printer {

    @Override
    public void print(String message) {
        log.info("Canon Printer : {}", message);
    }
}
