package model;

import javax.swing.text.StringContent;

public class Attachment {
    private String name;
    private byte[] data;
    private String contentType;

    public Attachment(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    public Attachment(String name, byte[] data, String contentType) {
        this.name = name;
        this.data = data;
        this.contentType = contentType;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "name='" + name + '\'' +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}

