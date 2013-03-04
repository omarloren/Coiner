

package coiner;

import java.io.IOException;
import quickfix.*;
import quickfix.field.MsgType;
import quickfix.field.Password;
import quickfix.field.ResetSeqNumFlag;
import quickfix.field.Username;

/**
 *
 * @author omar
 */
public class CoinerApp implements Application{

    private String passWord;
    private String userName;
    /*MarketPool mp = new MarketPool();
    public static PriceBeat pricebeat = new PriceBeat();
    PriceSensitive pricesense = new PriceSensitive();
    public static MongoConnection mongo;*/
    public static SessionID sessionID;
    private Handler handler;
    private boolean streaming;
    private boolean recording;
    /*
     * Constructor 
     */
    public CoinerApp(String userName, String passWord, boolean streaming, boolean recording){
        this.userName = userName;
        this.passWord = passWord;
        
        this.handler = new Handler(streaming, recording);
    }
    @Override
    public void onCreate(SessionID sid) {
        
    }

    @Override
    public void onLogon(SessionID sid) {
        try {
            Node.connect();
        } catch (IOException ex) {
            System.err.println("Error al conectar con Node, esta Node corriendo?");
        }
    }

    @Override
    public void onLogout(SessionID sid) {
        
    }

    @Override
    public void toAdmin(Message msg, SessionID sid) {
        
        final Message.Header header = msg.getHeader();
        try{
            if(header.getField(new MsgType()).valueEquals(MsgType.LOGON)){
                msg.setField(new Username(userName));
                msg.setField(new Password(passWord));
                msg.setField( new ResetSeqNumFlag(true));
            }
        }catch(FieldNotFound e){
            System.out.println(msg.getHeader()+ " " + e);
        }
    }

    @Override
    public void fromAdmin(Message msg, SessionID sid) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        
    }

    @Override
    public void toApp(Message msg, SessionID sid) throws DoNotSend {
        
    }

    @Override
    public void fromApp(Message msg, SessionID sid) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        
        handler.crack(msg,sid);
    }
}
