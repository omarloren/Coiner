

package coiner;

/**
 *
 * @author omar
 */
public class fixToJson {

    public static String parseVela(String moneda, double open, double high, double low, double close,
            double vol) {
        Date date = GMTDate.getDate();
        int min = (date.getMinute() - 1) < 0 ? 0 : date.getMinute();
        String fecha = date.getYear() + "" + (date.getMonth() < 10 ? "0" + date.getMonth() : date.getMonth())
                + "" + (date.getDay() < 10 ? "0" + date.getDay() : date.getDay());
        String hour = date.getHour() < 10 ? "0" + date.getHour() + "" + (min < 10 ? "0" + min : min) + "00"
                : date.getHour() + "" + (min < 10 ? "0" + min : min) + "00";
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"date\":\"" + fecha + "\",");
        buffer.append("\"hour\": " + hour + ",");
        buffer.append("\"Open\":" + open + ",");
        buffer.append("\"High\":" + high + ",");
        buffer.append("\"Low\":" + low + ",");
        buffer.append("\"Close\":" + close + ",");
        buffer.append("\"Volume\":" + (int) vol);
        buffer.append("}");
        return buffer.toString();
    }
    
    public static String parseTick(double precio, char tipo){
        StringBuffer buffer = new StringBuffer();
        Date date = GMTDate.getDate();
        int min = (date.getMinute()-1)<0?0:date.getMinute();
        String secs = date.getSecond()<10?"0"+date.getSecond():""+date.getSecond();
        String milis = date.getMillis()<10?"0"+date.getMillis():""+date.getMillis();
        String fecha = date.getYear() +  "" + (date.getMonth()<10?"0"+date.getMonth():date.getMonth())
            + "" + (date.getDay()<10?"0"+date.getDay():date.getDay()) ;
        String hora = date.getHour()<10?"0"+date.getHour():""+date.getHour();
        String type = tipo == '1'?"bid":"ask";
        
        buffer.append("{");
        buffer.append("\"date\": "+ fecha + ",");
        buffer.append("\"tipo\":\""+ type + "\",");
        buffer.append("\"time\": "+ (hora+""+min+""+secs+"."+milis)+ ",");
        buffer.append("\"precio\":" + precio );
        buffer.append("}");
        return buffer.toString();
    }
   
}
