package config;

import com.sun.mail.imap.IMAPSSLStore;
import com.sun.mail.pop3.POP3SSLStore;
import com.sun.mail.pop3.POP3Store;
import exception.PluginInternalException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.mail.*;
import java.util.Properties;

/**
 * Configuration Class for internal service connection using java-mail apis.
 * @author Utsab Chowdhury
 */
public class EmailConfiguration {
    static Logger log = Logger.getLogger(EmailConfiguration.class.getName());

    private String user = StringUtils.EMPTY;
    private String password = StringUtils.EMPTY;
    private String connection = StringUtils.EMPTY;
    private String host = StringUtils.EMPTY;
    private String port = StringUtils.EMPTY;

    /**
     * Constructor - object to be instantiated using ConfigBuilder.
     * Argument to be built with property set [user, password, connection, host, port]
     *
     *    Example : new EmailConfiguration.ConfigBuilder()
     *                           .setUser("user@email.com")
     *                           .setPassword("Password")
     *                           .setConnection("IMAP")
     *                           .setHost("imap.gmail.com")
     *                           .setPort("993").build()
     *
     *
     * @param builder
     * @throws PluginInternalException
     *
     * @author Utsab Chowdhury
     */
    public EmailConfiguration(ConfigBuilder builder) throws PluginInternalException{
        this.user = builder.user;
        this.password = builder.password;
        this.connection = builder.connection;
        this.host = builder.host;
        this.port = builder.port;
        init();
    }

    public static class ConfigBuilder {

        private String user = StringUtils.EMPTY;
        private String password = StringUtils.EMPTY;
        private String connection = StringUtils.EMPTY;
        private String host = StringUtils.EMPTY;
        private String port = StringUtils.EMPTY;

        public ConfigBuilder setUser(String user){
            this.user = user;
            return this;
        }

        public ConfigBuilder setPassword(String password){
            this.password = password;
            return this;
        }

        public  ConfigBuilder setConnection(String connection){
            this.connection = connection;
            return this;
        }

        public ConfigBuilder setHost(String host){
            this.host = host;
            return this;
        }

        public ConfigBuilder setPort(String port){
            this.port = port;
            return  this;
        }

        public EmailConfiguration build() throws  PluginInternalException{
            return new EmailConfiguration(this);
        }

    }

    public Store init() throws PluginInternalException {
        if( !(port.equals("110") || port.equals("993") ||  port.equals("995")))
            throw  new PluginInternalException("Invalid Port Provided");
        return getSpecificEmailConnection(this.connection);
    }

    private Store getSpecificEmailConnection(String network){
        Store store = null;
        if(StringUtils.equals(network,"POP3"))
            store = getPOP3Connection();
        else if(StringUtils.equals(network,"IMAP"))
            store = getIMAPConnection();
        else throw new RuntimeException("Custom exception");

        return  store;
    }

    private POP3Store getPOP3Connection(){
        Properties pop3Properties = new Properties();
        pop3Properties.setProperty("mail.pop3.socketFactory.class","javax.net.SocketFactory");
        pop3Properties.setProperty("mail.pop3.socketFactory.fallback","true");
        pop3Properties.setProperty("mail.pop3.port",port);
        pop3Properties.setProperty("mail.pop3.socketFactory.port",port);
        pop3Properties.setProperty("mail.pop3.forgettopheaders","true");
        Session session = Session.getInstance(pop3Properties);
        URLName url  = new URLName("pop3s",host,Integer.parseInt(port),"",user,password);
        log.info("Connection URL >> " + url.toString());
        POP3SSLStore store = new POP3SSLStore(session,url);
        try{
            store.connect();
        }catch (MessagingException e){
            log.error("Unable to connect to  POP store : ",e);
            if(store.isConnected()){
                try{
                    store.close();
                }catch (MessagingException ex){
                    log.error("Unable to close store correctly : ",ex);
                }
            }else{
                log.info("Store not connected, closing connection handle");
                try{
                    store.close();
                }catch (MessagingException ex){
                    log.error("Unable to close store correctly : ",ex);
                }
            }
        }
        return store;
    }

    private IMAPSSLStore getIMAPConnection(){
        Properties imapProperties = new Properties();
        imapProperties.setProperty("mail.imap.socketFactory.class","IMAP_SSL_FACTORY");
        imapProperties.setProperty("mail.imap.socketFactory.fallback","false");
        imapProperties.setProperty("mail.imap.port",port);
        imapProperties.setProperty("mail.imap.socketFactory.port",port);
        Session session = Session.getInstance(imapProperties);

        URLName url = new URLName("IMAP",host,Integer.parseInt(port),"",user,password);
        log.info("Connection URL >> "+ url.toString());
        IMAPSSLStore store = new IMAPSSLStore(session,url);
        try{
            store.connect();
        }catch (MessagingException e){
            log.error("Unable to connect to IMAP store : ",e);
            if(store.isConnected()){
                try{
                    store.close();
                }catch (MessagingException ex){
                    log.error("Unable to close store correctly : ",ex);
                }
            }else{
                log.info("Store not connected, closing connection handle");
                try{
                    store.close();
                }catch (MessagingException ex){
                    log.error("Unable to close store correctly : ",ex);
                }
            }
        }
        return store;

    }

    public Folder openFolder(String folderName , Store store, Integer mode) throws PluginInternalException {
        Folder folder = null;

        try{
            folder = store.getFolder(folderName);
            if(folder.exists()) {
                log.info("Email Connection Session : trying to open folder " + folder.getFullName());
                folder.open((null == mode || mode == 0)?Folder.READ_ONLY:mode);
            }else{
                throw new PluginInternalException("Invalid Folder Name");
            }

        }catch (MessagingException ex) {
            log.error("Invalid Folder Name");
            throw new PluginInternalException("Invalid Folder Name");
        }
        return folder;
    }

    public  void  closeFolder(Folder folder) throws PluginInternalException{
        try{
            if(null != folder && folder.isOpen()){
                folder.close(true);
            }else{
                log.error("Folder was null or folder was not open");
            }
        }catch (MessagingException ex){
            log.error("Error while closing folder ", ex);
            throw new PluginInternalException("Unable to close folder");
        }
    }

    public  void closeStore(Store store) throws  PluginInternalException{
        try{
            if(store != null){
                store.close();
            }else{
                log.error("Store was null");
            }
        }catch (MessagingException ex){
            log.error("Error while closing store ", ex);
            throw new PluginInternalException("Could not close store");
        }

    }
}
