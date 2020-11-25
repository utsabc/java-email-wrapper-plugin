package factory;


import config.EmailConfiguration;
import exception.PluginInternalException;
import model.Attachment;
import model.EmailPOJO;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * The Wrapper API Class which is used to manage opening , closing and transformation of E-mails to POJO.
 * This is the base layer which is require to be instantiated by passing the pre-built EmailConfiguration Object
 * @author Utsab Chowdhury
 */
public class EmailFactory {
    static Logger log = Logger.getLogger(EmailFactory.class.getName());

    private static  final int BUFFER_SIZE =  1024*4;

    private Store store = null;
    private Folder folder = null;
    private Message[] messages = null;
    private boolean isOpen = false;
    private boolean setUnreadFlag = true;
    private int totalMessages;
    private int startIndex;
    private int endIndex;
    EmailConfiguration configuration;
    Integer openMode;

    /**
     * The Parameterized constructor used to set up internal connection-apis using the configuration object. The openMode
     * param is used to set open mode for the email inbox (NULL by default will set it to READ MODE) @see javax.mail.event.ConnectionEvent.
     *  Example:
     *  EmailFactory factory = new EmailFactory(new EmailConfiguration.ConfigBuilder()
     *                     .setUser("user@email.com")
     *                     .setPassword("Password")
     *                     .setConnection("IMAP")
     *                     .setHost("imap.gmail.com")
     *                     .setPort("993").build(),null);
     *             factory.setStartIndex(0);
     *             factory.setEndIndex(100);
     *             Collection<EmailPOJO> emails =  factory.read("Inbox");
     *
     * @param builder
     * @param openMode
     *
     * @author Utsab Chowdhury
     */
    public EmailFactory(EmailConfiguration builder, Integer openMode){
         this.configuration = builder;
         this.openMode = openMode;
    }

    /**
     * The entry point Method to get Access to Emails. It returns a collection of Emails within the reading boundary provided by the user
     * , Internally uses A threadPool size of 10 for Email-to-Pojo transformation. Returns collection once all thread execution is complete.
     *
     * @param folder
     * @return
     * @throws PluginInternalException
     *
     * @author Utsab Chowdhury
     */
    public Collection<EmailPOJO> read(String folder) throws PluginInternalException{
        Collection<EmailPOJO> emailList = new ArrayList<>();
        List<Future<?>> futures = new ArrayList<>();
        try {
            openInbox(folder);
            log.info("message array length is  " + (this.messages != null ? this.messages.length : 0));
            ExecutorService executor = Executors.newFixedThreadPool(10);
            if (this.messages != null && this.messages.length > 0) {

                    List<Message> messageQueue = Arrays.asList(this.messages);
                    messageQueue.forEach(message -> {
                        Future<?> f  = executor.submit(()->{
                            try{
                                EmailPOJO emailPOJO = transformaMessagetoEmailPojo(message);
                                emailList.add(emailPOJO);
                            }catch (Exception ex){
                                log.error("Error : ", ex);
                            }

                        });
                        futures.add(f);
                    });



            }
        }catch (Exception ex){
            log.error("Error while reading", ex);
        }
        for(Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception ex) {
                log.error("Exception retrieving task", ex);
            }
        }
        log.info("closing email api for inbox for folder " +  folder);
        closeInbox();
        Comparator<EmailPOJO> compareByTS = (EmailPOJO o1, EmailPOJO o2) -> o1.getSentDate().compareTo(o2.getSentDate());
        Collections.sort((List)emailList,compareByTS);
        Collections.reverse((List)emailList);
        return emailList;
    }


    private void openInbox(String folder) throws PluginInternalException, MessagingException{
        if(!this.isOpen){
            log.info("Inside openInbox folder : "+ folder);
            this.store = configuration.init();
            if (this.store != null && this.store.isConnected()){
                this.folder = configuration.openFolder(folder,this.store,openMode);
                this.totalMessages = this.folder.getMessageCount();
                log.info("Total read count messages : "+ startIndex +" Total Messages :"+ totalMessages);
                this.setUnreadFlag = true;
                this.endIndex = (this.endIndex!=0)?this.endIndex:this.totalMessages;
                if(!(startIndex<endIndex)){
                    setUnreadFlag = false;
                    return;
                }
                log.info("Reading boundary ["+this.startIndex+" "+this.endIndex+"]");
                this.messages = this.folder.getMessages(this.startIndex+1, this.endIndex);
                log.info("Inbox unread item count "+ this.messages.length);
                this.isOpen = true;
            }
        }
        else{
            log.info("Unable to Open the email Store");
        }
    }

    private void closeInbox() throws PluginInternalException {
        log.info(" Inside close inbox , status of isopen flag :"+ this.isOpen);
        if(this.isOpen) {
            this.isOpen = false;
            if(setUnreadFlag){
                setStartIndex(this.totalMessages);
            }
            configuration.closeFolder(this.folder);
            configuration.closeStore(this.store);
            log.info("Connection closed");
        }
    }


    private EmailPOJO transformaMessagetoEmailPojo(Message message) throws MessagingException, IOException, PluginInternalException {
        EmailPOJO emailPOJO = new EmailPOJO();
        try{
            if(message!=null){
                emailPOJO.setSenderAddress(getAdressesAsText(message.getFrom()));
                emailPOJO.setSubject((message.getSubject()));
                emailPOJO.setSentDate(message.getSentDate());
                emailPOJO.setToAddress(getAdressesAsText(message.getAllRecipients()));
                emailPOJO.setContentType(message.getContentType());
                String fileName = null;
                Object messageContent = message.getContent();
                log.info("Message Content Type : " + messageContent.getClass().getName());
                if(javax.mail.internet.MimeMultipart.class.isInstance(messageContent)) {
                    Multipart multipart  = (Multipart) message.getContent();
                    log.debug("multipart count "+ multipart.getCount());
                    for(int partCount = 0; partCount < multipart.getCount(); partCount++){
                        MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(partCount);
                        if(Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || Part.INLINE.equalsIgnoreCase(part.getDisposition())) {
                            fileName = MimeUtility.decodeText(part.getFileName());
                            log.info("Attachment "+ fileName);
                            byte[] content = getByteArrayForAttachment(part.getInputStream());
                            emailPOJO.setData(content);
                            emailPOJO.addAttachment(new Attachment(fileName, content));
                        } else {
                            emailPOJO.setBody(getText(part));
                        }
                    }

                } else if (message.getContentType().contains("text/plain")|| message.getContentType().contains("text/html")){
                    Object content = message.getContent();
                    if(content != null) {
                        emailPOJO.setBody(Jsoup.parse(content.toString()).text());
                    }
                }
            }
        }catch (MessagingException ex){
            throw ex;
        }catch (UnsupportedEncodingException ex){
            throw ex;
        }
        catch (Exception ex){
            log.error("Error while transformation ");
            throw new PluginInternalException(ex.getMessage());
        }

        return emailPOJO;
    }


    private String getAdressesAsText(Address... addresses){
        StringBuilder stringBuilder = new StringBuilder();
        if(addresses != null && addresses.length > 0){
            for(Address address: addresses){
                stringBuilder.append(address.toString());
            }
        }
        return stringBuilder.toString();
    }

    private byte[] getByteArrayForAttachment(InputStream input) throws IOException{
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            for(int buf ; (buf = input.read(buffer)) != -1;){
                baos.write(buffer,0, buf);
            }
            return baos.toByteArray();
        }catch (Exception ex){
            log.error("error while getting byte content from inputstream", ex);
        }
        return null;
    }

    private String getText(Part part) throws MessagingException, IOException{
        log.info("Inside getText of EmailFactory");
        if(part.isMimeType("text/*")){
            log.info("returning text for mime-type text/*");
            return Jsoup.parse((String)part.getContent()).text();
        }
        if(part.isMimeType("multipart/alternative")|| part.isMimeType("multipart/*")){
            Multipart multipart = (Multipart) part.getContent();
            StringBuilder sb = new StringBuilder(1024);
            log.info("email body has "+ multipart.getCount()+ " multipart count");
            for(int index = 0;index <multipart.getCount(); index++){
                BodyPart bodyPart = multipart.getBodyPart(index);
                if(bodyPart.isMimeType("text/plain")
                        || bodyPart.isMimeType("text/html")
                        || bodyPart.isMimeType("text/*")) {
                    sb.append(Jsoup.parse((String)bodyPart.getContent()).text());
                }
            }
            return sb.toString();
        }
        return null;
    }

    /**
     * Returns the starting index by default its set to 0
     * @return
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * Sets the starting index
     * @param startIndex
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * Returns the ending index by default its set to inbox Email count
     * @return
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * Sets the ending index
     * @param endIndex
     */
    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }
}
