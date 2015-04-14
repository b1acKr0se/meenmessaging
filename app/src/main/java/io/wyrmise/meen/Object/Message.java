package io.wyrmise.meen.Object;

public class Message implements Comparable<Message>, Cloneable {
    public String id;
    public String address;
    public String name;
    public String content;
    public String date;
    public int type;
    public int delivery;
    public int read;

    public Message() {

    }

    public String getDate() {
        return date;
    }

    @Override
    public int compareTo(Message another) {
        int r = date.compareTo(another.getDate());
        if (r == 1) return -1;
        else if (r == -1) return 1;
        return 0;
    }

   public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}