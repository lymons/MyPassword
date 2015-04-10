package cn.xing.mypassword.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;
import android.content.Context;
import cn.xing.mypassword.R;

public class DateUtils {
    
    private static final SimpleDateFormat simpleDateFormatYear = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat simpleDateFormatMonth = new SimpleDateFormat("MM-dd", Locale.getDefault());
    /** 一天含有的秒数 */
    private static final long DAY = 1000 * 60 * 60 * 24;
    
    public static String formatDate(Context context, long createDate) {
        String result = "";
        long currentTime = System.currentTimeMillis();
        long distance = currentTime - createDate;
        if (createDate > currentTime) {
            result = simpleDateFormatYear.format(createDate);
        } else if (distance < 1000 * 60) {
            result = context.getString(R.string.just);
        } else if (distance < 1000 * 60 * 60) {
            String dateString = context.getString(R.string.minute_ago);
            result = String.format(Locale.getDefault(), dateString, distance / (1000 * 60));
        } else if (distance < DAY) {
            String dateString = context.getString(R.string.hour_ago);
            result = String.format(Locale.getDefault(), dateString, distance / (1000 * 60 * 60));
        } else if (distance < DAY * 365) {
            result = simpleDateFormatMonth.format(createDate);
        } else {
            result = simpleDateFormatYear.format(createDate);
        }

        return result;
    }

}
