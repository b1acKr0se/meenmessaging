package io.wyrmise.meen;


import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Iterator;

import io.wyrmise.meen.Object.Message;

/**
 * @author wyrmise
 * @overview Custom Adapter using Message with modified getView() and add method
 * @attributes ctx Context messageListArray ArrayList<Message>
 */
public class MessageAdapter extends ArrayAdapter<Message> {
    public ArrayList<Message> messageListArray;
    Holder holder;
    private Context ctx;
    private SparseBooleanArray mSelectedItemsIds;

    /**
     * @param context
     * @param textViewResourceId
     * @param messageListArray
     * @overview constructor method inherited from ArrayAdapter
     */
    public MessageAdapter(Context context, int textViewResourceId,
                          ArrayList<Message> messageListArray) {
        super(context, textViewResourceId);
        mSelectedItemsIds = new SparseBooleanArray();
        this.messageListArray = messageListArray;
        this.ctx = context;
    }

    public static String removeDiacriticalMarks(String string) {
        return Normalizer.normalize(string, Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    @Override
    public int getViewTypeCount() {
        if (getCount() != 0)
            return getCount();
        return 1;
    }

    /**
     * @effect get the View generated by the current instance of MessageAdapter
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View convertView1 = convertView;
        LayoutInflater vi = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView1 == null) {
            holder = new Holder();
            convertView1 = vi.inflate(R.layout.list_view_main, parent,
                    false);
            holder.messageTo = (TextView) convertView1
                    .findViewById(R.id.address);
            holder.messageContent = (TextView) convertView1
                    .findViewById(R.id.previewMsg);
            holder.date = (TextView) convertView1.findViewById(R.id.showDate);
            holder.imgView = (ImageView) convertView1
                    .findViewById(R.id.quickBadge);
            convertView1.setTag(holder);
        } else {
            holder = (Holder) convertView1.getTag();
        }
        Message message = getItem(position);
        if (!MainActivity.contactPictureID.containsKey(message.name)) {
            holder.imgView.setImageResource(getDisplayPicture(message));
        } else
            holder.imgView.setImageDrawable(MainActivity.contactPictureID.get(message.name));

        holder.messageTo.setText(message.name);
        holder.messageContent.setText(message.content);
        holder.date.setText(message.date);

        if (MainActivity.fontCode == 2) {
            holder.messageTo.setTypeface(holder.bold);
            holder.messageContent.setTypeface(holder.face);
            holder.date.setTypeface(holder.face);
        } else if (MainActivity.fontCode == 1) {
            holder.messageTo.setTypeface(null);
            holder.messageContent.setTypeface(null);
            holder.date.setTypeface(null);
        }

        if (MainActivity.isNightMode) {
            holder.messageTo.setTextColor(convertView1.getResources().getColor(R.color.white));
            holder.messageContent.setTextColor(convertView1.getResources().getColor(R.color.white));
            holder.date.setTextColor(convertView1.getResources().getColor(R.color.gray));
        } else {
            if (!MainActivity.hasBackground) {
                holder.messageTo.setTextColor(convertView1.getResources().getColor(R.color.black));
                holder.messageContent.setTextColor(convertView1.getResources().getColor(R.color.black));
                holder.date.setTextColor(convertView1.getResources().getColor(R.color.black));
            } else {
                holder.messageTo.setTextColor(convertView1.getResources().getColor(R.color.white));
                holder.messageContent.setTextColor(convertView1.getResources().getColor(R.color.white));
                holder.date.setTextColor(convertView1.getResources().getColor(R.color.gray));
            }
        }
        if (message.read == 0) {
            setColor(convertView1);
        } else {
            if (MainActivity.isNightMode)
                holder.messageContent.setTextColor(convertView1.getResources().getColor(R.color.white));
            else
                holder.messageContent.setTextColor(convertView1.getResources().getColor(R.color.black));
        }
        return convertView1;
    }

    /**
     * @effects returns the size of the array of adapter
     */
    @Override
    public int getCount() {
        return messageListArray.size();
    }

    /**
     * @effects returns the item at specific position
     */
    @Override
    public Message getItem(int position) {
        return messageListArray.get(position);
    }

    /**
     * @param messageList
     * @effects set the given list as the default list of the adapter
     */
    public void setArrayList(ArrayList<Message> messageList) {
        this.messageListArray = messageList;
        notifyDataSetChanged();
    }

    /**
     * @param msg
     * @effects iterate through the current list to find and remove older entry
     * add new entry to the first position notify data changed
     */
    public void addItem(Message msg) {
        Iterator<Message> iter = messageListArray.iterator();
        while (iter.hasNext()) {
            Message message = iter.next();
            if (message.name.equals(msg.name))
                iter.remove();
        }
        this.messageListArray.add(0, msg);
        notifyDataSetChanged();
    }

    public int getDisplayPicture(Message msg) {
        if (!msg.name.substring(0, 1).matches("[0-9]")
                && !msg.name.startsWith("+")) {
            int color = MainActivity.colorCode;
            String first = msg.name.substring(0, 1)
                    .toLowerCase();
            if (first.equals("đ"))
                first = "d";
            String firstLetter = removeDiacriticalMarks(first);
            String fileName = "letters_" + firstLetter + "_" + color;
            int id = ctx.getResources().getIdentifier(fileName, "drawable",
                    ctx.getPackageName());
            return id;
        } else {
            int color = MainActivity.colorCode;
            String fileName = "default_" + color;
            int id = ctx.getResources().getIdentifier(fileName, "drawable",
                    ctx.getPackageName());
            return id;
        }
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    private void setColor(View view) {
        int color = MainActivity.colorCode;
        switch (color) {
            case -1:
                break;
            case 1:
                holder.messageContent.setTextColor(view.getResources().getColor(R.color.green));
                break;
            case 2:
                holder.messageContent.setTextColor(view.getResources().getColor(R.color.light_green));
                break;
            case 3:
                holder.messageContent.setTextColor(view.getResources().getColor(R.color.lime));
                break;
            case 4:
                holder.messageContent.setTextColor(view.getResources().getColor(R.color.light_blue));
                break;
            case 5:
                holder.messageContent.setTextColor(view.getResources().getColor(R.color.cyan));
                break;
            case 6:
                holder.messageContent.setTextColor(view.getResources().getColor(R.color.teal));
                break;
            case 7:
                holder.messageContent.setTextColor(view.getResources().getColor(R.color.red));
                break;
            case 8:
                holder.messageContent.setTextColor(view.getResources().getColor(R.color.orange));
                break;
            case 9:
                holder.messageContent.setTextColor(view.getResources().getColor(R.color.amber));
                break;
            case 10:
                holder.messageContent.setTextColor(view.getResources().getColor(R.color.purple));
                break;
            case 11:
                holder.messageContent.setTextColor(view.getResources().getColor(R.color.pink));
                break;
            case 12:
                holder.messageContent.setTextColor(view.getResources().getColor(R.color.brown));
                break;
        }
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }


    /**
     * @author wyrmise
     * @overview class to hold the references of the TextViews used in ListView
     */
    private class Holder {
        public TextView messageTo, messageContent, date;
        public ImageView imgView;
        public Typeface face, bold;
    }

}
