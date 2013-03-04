package coiner;

import java.text.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GMTDate {
    private static Date date;
    static java.util.Date gmt;
    public static Date getDate()  {
        
        SimpleDateFormat gmtDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        gmtDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        date = new Date();
        
        //return - Tiempo actual en GMT        
        return date;
    }
    
    public static Integer getTime(){
        
        int hora = getDate().getHour();
        int min = getDate().getMinute();
        int seg = getDate().getSecond();
        //String time = hora +""+ min +""+ ( (seg<9)? ("0"+seg):seg);
        String time = hora +""+ ((min<=9)? ("0"+min):min);
        return (new Integer(time));
    }
    
    public static Integer getTimeFin(int i){
        
        int hora = getDate().getHour();
        int min = getDate().getMinute()+i;
        int seg = getDate().getSecond();
        //String time = hora +""+ min +""+ ( (seg<9)? ("0"+seg):seg);
        String time = hora +""+ ((min<9)? ("0"+(min)):min);
        return (new Integer(time));
    }
    
    public static String getGMT(){
        
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm");  
        df.setTimeZone(TimeZone.getTimeZone("GMT"));  
        java.util.Date date = new java.util.Date();
        return df.format(date).toString();
    }
    
    public static Integer getSeconds(){
        return getDate().getSecond();
    }
}