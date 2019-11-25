package utils;

import java.text.SimpleDateFormat;

class DateUtils implements Serializable {
    static String getFormattedDate(Date date) {
        def formattedDate = new SimpleDateFormat("yyyy_MM_dd")
        return formattedDate.format(date)
    }

    static String getFormattedTimeStamp(Date date) {
        def formattedTimestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm")
        return formattedTimestamp.format(date)
    }
}