

package coiner;

import java.io.*;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import quickfix.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Este programa es muy parecido a los otros y no lo comentare por que soy flojo.
 * @author omar
 */
public class Coiner {
    
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private Initiator initiator;
    private static Coiner caldera;
    private boolean initStarted = false;
    private static Logger log = LoggerFactory.getLogger(Coiner.class);
    
    Coiner() throws Exception{
        System.out.println("Ingresa el nombre del archivo de configuracion:");
        String input = new Scanner(System.in).next();
        
        InputStream inputS = new BufferedInputStream(
                                new FileInputStream(
                                new File("/home/omar/OMS/config/"+input+".cnf")));
                                //new File("/home/omar/OMS/config/GMIDemo00292str.cnf")));
     
        SessionSettings settings = new SessionSettings(inputS);
        
        inputS.close();
        
        CoinerApp application = new CoinerApp(settings.getString("UserName"),settings.getString("PassWord"),
               settings.getBool("Streaming"),settings.getBool("Recording"));
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true,true, true, true);
        MessageFactory messageFactory = new DefaultMessageFactory();
        
        initiator =  new SocketInitiator(application, messageStoreFactory, settings, 
                                        logFactory, messageFactory);   
        
    }
    
    public synchronized void logon(){
        
        if (!initStarted){
            try{
                initiator.start();
                initStarted = true;                
            }catch(Exception e){
                log.error("Colapso en Login", e);
            }   
        }else{
            Iterator<SessionID> sessionIds = initiator.getSessions().iterator();
            while(sessionIds.hasNext()){
                SessionID sessionId = (SessionID) sessionIds.next();
                Session.lookupSession(sessionId).logon();
            }
        }
    }
    public void stop(){
        shutdownLatch.countDown();
    }
    public static void main(String[] args) throws Exception{
        try{
            
        }catch(Exception e){
            log.info(e.getMessage(), e);
        }
        
        caldera =new Coiner();
        caldera.logon();
                
        shutdownLatch.await();
        
    }
}
