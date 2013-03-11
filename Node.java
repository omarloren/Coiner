/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coiner;

import com.mongodb.BasicDBObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author omar
 */
public class Node {
    
    public static Socket clientSocket;
    static DataOutputStream outNode;
    public static void  connect() throws IOException {
        String sentence = "";
        String modifiedSentence;
        System.out.println("Conectando con Node");
        Node.clientSocket = new Socket("127.0.0.1", 3000);
        Node.outNode = new DataOutputStream(Node.clientSocket.getOutputStream());
        outNode.writeUTF("{\"type\" : \"login\",\"name\" : \"SERVIDOR_PRECIOS\" }\n");
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
                
    }
    public static void send(String message) throws IOException{
        Node.outNode = new DataOutputStream(Node.clientSocket.getOutputStream());
        outNode.writeUTF(""+message+"\n");
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void send(Double open, String moneda) throws IOException{
        Node.outNode = new DataOutputStream(Node.clientSocket.getOutputStream());
        //outNode.writeUTF("{\"type\" : \"open\", \"symbol\": \"" + message.get("Moneda") + "\", \"precio\" :" + message.get("Open").toString() + "}\n");
        
        outNode.writeUTF("{\"type\" : \"open\", \"data\": { \"Open\":" + open + ", \"Moneda\":\"" +moneda +"\"}}\n");
        try {
            Thread.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void reconnect(){
        
    }
}
