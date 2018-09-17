package red.point.checkpoint.util;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class NumberUtil {

    public static String getFormattedNumber(double value) {
        DecimalFormatSymbols custom = new DecimalFormatSymbols();
        custom.setDecimalSeparator(',');
        custom.setGroupingSeparator('.');

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setDecimalFormatSymbols(custom);
        return decimalFormat.format(value);
    }

    public static String getFormattedNumber(long value) {
        DecimalFormatSymbols custom = new DecimalFormatSymbols();
        custom.setDecimalSeparator(',');
        custom.setGroupingSeparator('.');

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setDecimalFormatSymbols(custom);
        return decimalFormat.format(value);
    }
    
}
