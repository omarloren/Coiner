package coiner;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import quickfix.FieldNotFound;

/**
 *
 * @author omar
 */
public class Candle {
    
    private Integer strTime = 0;
    private Integer endTime = 0;
    private Integer velas = 0;
    private String symbol;
    private ArrayList temp = new ArrayList();
    private ArrayList<Double> precios = new ArrayList();
    private MongoConnection mongo;
    private DBCollection coll;
    private double open, high, low, close; // no me gusta hacer esto pero... qué más da...
    private String vela;
    private String json;
    BasicDBObject doc;
    private boolean streaming;
    private boolean recording;
    
    Candle(String symbol, boolean streaming, boolean recording){
        this.symbol = symbol;
        this.streaming =  streaming;
        this.recording = recording;
        mongo = MongoConnection.getInstance();
        coll = mongo.db.getCollection(this.formatString(symbol));
        doc = new BasicDBObject();
    }
    
    public void onTick(quickfix.fix42.MarketDataIncrementalRefresh.NoMDEntries msj) throws FieldNotFound{
        if(GMTDate.getTime().intValue() == endTime){
            open = precios.get(0);
            high = Collections.max(precios);
            low = Collections.min(precios);
            close = precios.get(precios.size()-1);
            if(this.recording){
                vela = fixToJson.parseVela(this.symbol, open,high, low,close, (msj.getMDEntrySize().getValue()/100000));
                doc = (BasicDBObject)JSON.parse(vela);
                coll.insert(doc);
            }
            
            if(this.streaming){
                try {
                    Node.send(msj.getMDEntryPx().getValue(), this.symbol);
                } catch (IOException ex) {
                    Logger.getLogger(Candle.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            precios.clear();
        }
        
        endTime = this.checkTime(GMTDate.getTime()+1);
        precios.add(msj.getMDEntryPx().getValue());
    }
    
    public String getSymbol(){
        return this.symbol;
    }
    
    private String formatString(String moneda){
    
        return moneda.replace("/", "");
    }
    private int checkTime(int hora){
        int prim;
        int seg;
        String result;
        
        if (hora>0){
            String uno = String.valueOf(hora).substring(2);
            String dos = String.valueOf(hora).substring(0, 2);
            
            if (Integer.parseInt(uno) == 60){
                seg = new Integer(dos)+1;
                if(seg >= 24){
                    seg=0;
                }
                result = ""+ seg + "00";
                return new Integer(result);
            }
            else return hora; 
            
        }else return hora;   
    }
}
