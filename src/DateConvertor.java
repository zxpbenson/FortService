import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateConvertor {
    
    public static final String dateFormatExp = "yyyy-MM-dd HH:mm:ss";
    public static final DateFormat standDateFormator = new SimpleDateFormat(DateConvertor.dateFormatExp);
    
    public static Date stringToDate(String dateString, DateFormat dateFormat) throws ParseException{
        return dateFormat.parse(dateString);
    }

    public static Date stringToDate(String dateString, String dateFormatExp) throws ParseException{
        DateFormat formator = new SimpleDateFormat(dateFormatExp);
        return stringToDate(dateString, formator);
    }

    public static Date stringToDate(String dateString) throws ParseException{
        return stringToDate(dateString, DateConvertor.standDateFormator);
    }
    
    public static String dateToString(Date date, DateFormat dateFormat){
        return dateFormat.format(date);
    }
    
    public static String dateToString(Date date, String dateFormatExp){
        DateFormat formator = new SimpleDateFormat(dateFormatExp);
        return dateToString(date, formator);
    }
    
    public static String dateToString(Date date){
        return dateToString(date, DateConvertor.standDateFormator);
    }
    
    public static long dateToMillis(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getTimeInMillis();
    }
    
    public static Date millisToDate(long millis){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.getTime();
    }
    
    public static long stringToMillis(String dateString, DateFormat dateFormat) throws ParseException{
        Date date = stringToDate(dateString, dateFormat);
        return dateToMillis(date);
    }
    
    public static long stringToMillis(String dateString, String dateFormatExp) throws ParseException{
        Date date = stringToDate(dateString, dateFormatExp);
        return dateToMillis(date);
    }

    public static long stringToMillis(String dateString) throws ParseException{
        Date date = stringToDate(dateString);
        return dateToMillis(date);
    }
    
    public static String millisToString(long millis, DateFormat dateFormat){
        Date date = millisToDate(millis);
        return dateToString(date, dateFormat);
    }

    public static String millisToString(long millis, String dateFormatExp){
        Date date = millisToDate(millis);
        return dateToString(date, dateFormatExp);
    }

    public static String millisToString(long millis){
        Date date = millisToDate(millis);
        return dateToString(date);
    }
    
    public static String stringToString(String dateString, DateFormat dateFormatFrom, DateFormat dateFormatTo) throws ParseException{
        Date dateFrom = stringToDate(dateString, dateFormatFrom);
        return dateToString(dateFrom, dateFormatTo);
    }

    public static String stringToString(String dateString, String dateFormatExpFrom, String dateFormatExpTo) throws ParseException{
        DateFormat formatorFrom = new SimpleDateFormat(dateFormatExpFrom);
        DateFormat formatorTo = new SimpleDateFormat(dateFormatExpTo);
        return stringToString(dateString, formatorFrom, formatorTo);
    }

}