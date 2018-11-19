package xyz.gosick.design.pattern.delegation;

/**
 * @author liukeshao
 * @date 2018/11/19 17:02
 */
public class PrinterController implements Printer {

    private final Printer printer;

    public PrinterController(Printer printer) {
        this.printer = printer;
    }

    @Override
    public void print(String message) {
        printer.print(message);
    }
}
