package xyz.gosick.design.pattern.delegation;

import xyz.gosick.design.pattern.delegation.printers.CanonPrinter;
import xyz.gosick.design.pattern.delegation.printers.EpsonPrinter;
import xyz.gosick.design.pattern.delegation.printers.HpPrinter;

/**
 * @author liukeshao
 * @date 2018/11/19 09:01
 */
public class App {
    private static final String MESSAGE_TO_PRINT = "hello world";

    public static void main(String[] args) {
        PrinterController hpPrinterController = new PrinterController(new HpPrinter());
        PrinterController canonPrinterController = new PrinterController(new CanonPrinter());
        PrinterController epsonPrinterController = new PrinterController(new EpsonPrinter());

        hpPrinterController.print(MESSAGE_TO_PRINT);
        canonPrinterController.print(MESSAGE_TO_PRINT);
        epsonPrinterController.print(MESSAGE_TO_PRINT);
    }
}
