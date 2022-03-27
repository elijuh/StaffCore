package me.elijuh.staffcore.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MathUtil {

    public double roundTo(double value, int decimals) {
        double divisor = Math.pow(10, decimals);
        return Math.round(value * divisor) / divisor;
    }

    public long parseDate(String input) throws NumberFormatException {
        long amount = Integer.parseInt(input.substring(0, input.length() - 1));
        switch (input.toCharArray()[input.length() - 1]) {
            case 'y': {
                return amount * 31536000000L;
            }
            case 'M': {
                return amount * 2592000000L;
            }
            case 'w': {
                return amount * 604800000L;
            }
            case 'd': {
                return amount * 86400000L;
            }
            case 'h': {
                return amount * 3600000L;
            }
            case 'm': {
                return amount * 60000L;
            }
            case 's': {
                return amount * 1000L;
            }
            default: {
                throw new NumberFormatException("Did not follow format.");
            }
        }
    }
}
