package io.wyrmise.meen;

public class Message implements Comparable<Message>, Cloneable{
    public String messageID;
    public String originalAddress;
    public String messageNumber;
    public String messageContent;
    public String messageDate;
    public int readState;

    public Message(){

    }

    public String getDate()
    {
        return messageDate;
    }

    @Override
    public int compareTo(Message another) {
        int r = messageDate.compareTo(another.getDate());
        if(r==1) return -1;
        else if(r==-1) return 1;
        return 0;
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}