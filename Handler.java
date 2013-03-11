

package coiner;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import quickfix.FieldNotFound;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.field.*;
import quickfix.fix42.MarketDataRequest;
import quickfix.fix42.MessageCracker;

/**
 *
 * @author omar
 */

public class Handler extends MessageCracker{
    //Este array indica las monedas de las que queremos precios.
    private final String[] MONEDAS = {"EUR/USD","USD/CHF", "USD/JPY", "GBP/USD", "EUR/GBP"};
    //private final String[] MONEDAS = {"USD/JPY"};
    private ArrayList<Candle> velas = new ArrayList();
    private MongoConnection mongo = MongoConnection.getInstance();
    private DBCollection coll;
    private boolean streaming;
    private boolean recording;
    /**
     * 
     * @param streaming
     * @param recording 
     */    
    public Handler(boolean streaming, boolean recording){
        this.streaming =  streaming;
        this.recording = recording;
    }
    
    @Override
    public void onMessage(quickfix.fix42.TradingSessionStatus status,
            SessionID sessionID) {
        for (int i=0; i<MONEDAS.length;i++){
            this.sendIncrementalRefresh(MONEDAS[i], sessionID);
            velas.add(new Candle(MONEDAS[i],this.streaming, this.recording));
        }        
    }
    
    @Override
    public void onMessage(quickfix.fix42.MarketDataIncrementalRefresh msj,
            SessionID sessionID) {
       //vela de la moneda con la que vamos a trabajar.
       Candle current;
       String temp;
       try{
            NoMDEntries nomdentries = new NoMDEntries();
        quickfix.fix42.MarketDataIncrementalRefresh.NoMDEntries group = new quickfix.fix42.MarketDataIncrementalRefresh.NoMDEntries();
        msj.get(nomdentries);
        if (nomdentries.getValue() == 1) {
            
            msj.getGroup(1, group);
            if(group.getMDEntryType().getValue() == '0'){
                current = this.getVela(group.getSymbol().getValue());            
                current.onTick(group);
                this.sendPrice(group.getSymbol().getValue(), "bid", group.getMDEntryPx().getValue());
                temp=fixToJson.parseTick(group.getMDEntryPx().getValue(),'1');
                this.savetick(group.getSymbol().getValue(), temp);
            }else if(group.getMDEntryType().getValue() == '1'){
                this.sendPrice(group.getSymbol().getValue(), "ask", group.getMDEntryPx().getValue());
                temp=fixToJson.parseTick(group.getMDEntryPx().getValue(),'2');
                this.savetick(group.getSymbol().getValue(), temp);
            }
        }else if (nomdentries.getValue() == 2){
            for (int i = 1; i <= 2; i++) {
                msj.getGroup(i, group);
                if(group.getMDEntryType().getValue() =='0'){
                    current = this.getVela(group.getSymbol().getValue());            
                    current.onTick(group);
                    this.sendPrice(group.getSymbol().getValue(), "bid", group.getMDEntryPx().getValue());
                     temp=fixToJson.parseTick(group.getMDEntryPx().getValue(),'1');
                     this.savetick(group.getSymbol().getValue(), temp);
                }else if(group.getMDEntryType().getValue() == '1'){
                    this.sendPrice(group.getSymbol().getValue(), "ask", group.getMDEntryPx().getValue());
                    temp=fixToJson.parseTick(group.getMDEntryPx().getValue(),'2');
                    this.savetick(group.getSymbol().getValue(), temp);
                }
            }
        }
       }catch(FieldNotFound fnf){
           System.err.println("Colapso:No se encontro campo!");
       }
    }
    
    
    private void sendIncrementalRefresh(String order, SessionID sessionID){
         quickfix.fix42.MarketDataRequest mdr = new quickfix.fix42.MarketDataRequest();
        CoinerApp.sessionID = sessionID;
        mdr.set(new MDReqID("A"));
        mdr.set(new SubscriptionRequestType('1'));
        mdr.set(new MarketDepth(1));
        mdr.set(new MDUpdateType(1));
        mdr.set(new AggregatedBook(true));
        
        MarketDataRequest.NoRelatedSym relatedSymbols = new MarketDataRequest.NoRelatedSym();        
        relatedSymbols.set(new Symbol(order));
        
        MarketDataRequest.NoMDEntryTypes mdEntryTypes = new MarketDataRequest.NoMDEntryTypes();
        mdEntryTypes.set(new MDEntryType('0')); // bid
        mdr.addGroup(mdEntryTypes);
        mdEntryTypes.set(new MDEntryType('1')); // Offer = Ask
        
        mdr.addGroup(mdEntryTypes);
        mdr.addGroup(relatedSymbols);        
        try {
            
            Session.sendToTarget(mdr, sessionID);
        } catch (SessionNotFound ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Candle getVela(String symbol){
        Candle temp = null;
        for(int i=0; i<velas.size();i++){
            
            if(velas.get(i).getSymbol().equals(symbol)){
                temp = velas.get(i);
            }
        }
        return temp;
    }
    private void savetick(String symbol, String json){
        /**
         * Solo enviamos aperturas ticks si esta habilitado el Recording en el 
         * Archivo de configuracion. 
         */
        if(this.recording){
            BasicDBObject doc;
            coll = mongo.tickDB.getCollection( unSlash(symbol));
            doc = (BasicDBObject)JSON.parse(json);
            coll.insert(doc);
        }        
    }
    private void sendPrice(String symbol, String tipo, double precio){
        /**
         * Solo enviamos aperturas ticks si esta habilitado el Streaming en el 
         * Archivo de configuracion. 
         */
        if(this.streaming){
            String json="{ \"type\" : \"tick\", \"symbol\":\""+ unSlash(symbol) +"\",\"entry\":\""+tipo+"\",\"precio\" : "+ precio +"}";
            try {
                //esperamos 1 milis pa' que no se peguen los mensajes.
                Thread.sleep(1);
                Node.send(json);
            } catch (IOException ex) {
                this.reconnect();
            }catch (InterruptedException ex) {
                Logger.getLogger(Candle.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void reconnect(){
        try {
            System.err.println("Node desconectado esperado reconectar ..");
            Thread.sleep(5000);
            Node.connect();
        } catch (InterruptedException ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Metodo que quita el / en un symbol, EUR/USD resulta en EURUSD ya que en
     * mongo los nombres de las monedas se encuentran así.
     * mongo los nombres de las monedas se encuentran así.
     *
     * @param symbol
     * @return cadena formateada
     */
    static String unSlash(String symbol) {
        StringBuffer str = new StringBuffer(symbol.length() - 1);
        str.append(symbol.substring(0, 3)).append(symbol.substring(4));
        return str.toString();
    }
}
