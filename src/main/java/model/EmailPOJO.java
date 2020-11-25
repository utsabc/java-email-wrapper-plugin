package model;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

public class EmailPOJO {
    private String body;
    private String subject;
    private String toAddress;
    private String ccAddress;
    private String senderAddress;
    private String fileName;
    private Date sentDate;
    private byte[] data;
    private String strData;
    private int attachmentCount;
    private String contentType;
    private Collection<Attachment> attachments = new LinkedList<Attachment>();

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getCcAddress() {
        return ccAddress;
    }

    public void setCcAddress(String ccAddress) {
        this.ccAddress = ccAddress;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getStrData() {
        return strData;
    }

    public void setStrData(String strData) {
        this.strData = strData;
    }

    public int getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(int attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Collection<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Collection<Attachment> attachments) {
        this.attachments = attachments;
        this.attachmentCount = attachments.size();
    }

    public  void  addAttachment(Attachment attachment){
        this.attachments.add(attachment);
        this.attachmentCount++;
    }

    @Override
    public String toString() {
        return "EmailPOJO{" +
                "body='" + body + '\'' +
                ", subject='" + subject + '\'' +
                ", toAddress='" + toAddress + '\'' +
                ", ccAddress='" + ccAddress + '\'' +
                ", senderAddress='" + senderAddress + '\'' +
                ", fileName='" + fileName + '\'' +
                ", sentDate=" + sentDate +
                ", strData='" + strData + '\'' +
                ", attachmentCount=" + attachmentCount +
                ", contentType='" + contentType + '\'' +
                ", attachments=" + attachments +
                '}';
    }
}
