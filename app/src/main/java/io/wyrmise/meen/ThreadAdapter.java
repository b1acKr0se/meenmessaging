package io.wyrmise.meen;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;
import java.util.ArrayList;

import io.wyrmise.meen.Object.Message;

/**
 * @overview Custom Adapter using Message with modified getView() and add method
 * @attributes ctx Context messageListArray ArrayList<Message>
 * @author wyrmise
 *
 */
public class ThreadAdapter extends ArrayAdapter<Message> {
    public ArrayList<Message> messageListArray;
    private Context ctx;

    /**
     * @overview constructor method inherited from ArrayAdapter
     * @param context
     * @param textViewResourceId
     * @param messageListArray
     */
    public ThreadAdapter(Context context, int textViewResourceId,
                         ArrayList<Message> messageListArray) {
        super(context, textViewResourceId);
        this.messageListArray = messageListArray;
        this.ctx = context;
    }

    /**
     * @effect get the View generated by the current instance of MessageAdapter
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder holder; // holds the two TextView for better scrolling
        View convertView1 = convertView;
        LayoutInflater vi = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView1 == null) {
            holder = new Holder();
            convertView1 = vi.inflate(R.layout.list_view_thread, parent,
                    false);
            holder.messageContent = (TextView) convertView1
                    .findViewById(R.id.threadBody);
            holder.date = (TextView) convertView1.findViewById(R.id.threadDate);
            holder.wrapper = (LinearLayout) convertView1.findViewById(R.id.parent_wrapper);
            holder.inner_wrapper = (LinearLayout) convertView1.findViewById(R.id.inner_wrapper);
            holder.delivery_image = (ImageView) convertView1.findViewById(R.id.delivery_image);
            convertView1.setTag(holder);
        } else {
            holder = (Holder) convertView1.getTag();
        }
        Message message = getItem(position);

        if(message.delivery ==0 && message.name.equals("Me")){
            holder.delivery_image.setVisibility(ImageView.VISIBLE);
        } else {
            holder.delivery_image.setVisibility(ImageView.GONE);
        }

        try {
            if (message.name.equals("Me")) {
                holder.messageContent.setBackgroundResource(R.drawable.bubble_send);
                holder.messageContent.setTextColor(Color.BLACK);
                holder.wrapper.setGravity(Gravity.END);
                holder.inner_wrapper.setGravity(Gravity.END);
                Log.d("Delivery: ",holder.messageContent+" "+message.delivery);

            } else {
                SharedPreferences colorPref = ctx.getSharedPreferences("colors", ctx.MODE_PRIVATE);
                int color = colorPref.getInt("color",-1);
                switch (color) {
                        case -1:
                            holder.messageContent
                                    .setBackgroundResource(R.drawable.bubble_1);
                            holder.messageContent.setTextColor(Color.WHITE);
                            holder.wrapper.setGravity(Gravity.START);
                            holder.inner_wrapper.setGravity(Gravity.START);
                            break;
                        case 1:
                            holder.messageContent
                                    .setBackgroundResource(R.drawable.bubble_1);
                            holder.messageContent.setTextColor(Color.WHITE);
                            holder.wrapper.setGravity(Gravity.START);
                            holder.inner_wrapper.setGravity(Gravity.START);
                            break;
                        case 2:
                            holder.messageContent
                                    .setBackgroundResource(R.drawable.bubble_2);
                            holder.messageContent.setTextColor(Color.WHITE);
                            holder.wrapper.setGravity(Gravity.START);
                            holder.inner_wrapper.setGravity(Gravity.START);
                            break;
                        case 4:
                            holder.messageContent
                                    .setBackgroundResource(R.drawable.bubble_4);
                            holder.messageContent.setTextColor(Color.WHITE);
                            holder.wrapper.setGravity(Gravity.START);
                            holder.inner_wrapper.setGravity(Gravity.START);
                            break;
                        case 5:
                            holder.messageContent
                                    .setBackgroundResource(R.drawable.bubble_5);
                            holder.messageContent.setTextColor(Color.WHITE);
                            holder.wrapper.setGravity(Gravity.START);
                            holder.inner_wrapper.setGravity(Gravity.START);
                            break;
                        case 6:
                            holder.messageContent
                                    .setBackgroundResource(R.drawable.bubble_6);
                            holder.messageContent.setTextColor(Color.WHITE);
                            holder.wrapper.setGravity(Gravity.START);
                            holder.inner_wrapper.setGravity(Gravity.START);
                            break;
                        case 7:
                            holder.messageContent
                                    .setBackgroundResource(R.drawable.bubble_7);
                            holder.messageContent.setTextColor(Color.WHITE);
                            holder.wrapper.setGravity(Gravity.START);
                            holder.inner_wrapper.setGravity(Gravity.START);
                            break;
                        case 8:
                            holder.messageContent
                                    .setBackgroundResource(R.drawable.bubble_8);
                            holder.messageContent.setTextColor(Color.WHITE);
                            holder.wrapper.setGravity(Gravity.START);
                            holder.inner_wrapper.setGravity(Gravity.START);
                            break;
                        case 10:
                            holder.messageContent
                                    .setBackgroundResource(R.drawable.bubble_10);
                            holder.messageContent.setTextColor(Color.WHITE);
                            holder.wrapper.setGravity(Gravity.START);
                            holder.inner_wrapper.setGravity(Gravity.START);
                            break;
                        case 11:
                            holder.messageContent
                                    .setBackgroundResource(R.drawable.bubble_11);
                            holder.messageContent.setTextColor(Color.WHITE);
                            holder.wrapper.setGravity(Gravity.START);
                            holder.inner_wrapper.setGravity(Gravity.START);
                            break;
                        case 12:
                            holder.messageContent
                                    .setBackgroundResource(R.drawable.bubble_12);
                            holder.messageContent.setTextColor(Color.WHITE);
                            holder.wrapper.setGravity(Gravity.START);
                            holder.inner_wrapper.setGravity(Gravity.START);
                            break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.messageContent.setText(message.content);
        holder.date.setText(message.date);

        if(!MainActivity.hasBackground)
            holder.date.setTextColor(convertView1.getResources().getColor(R.color.black));
        else
            holder.date.setTextColor(convertView1.getResources().getColor(R.color.white));

        if(MainActivity.fontCode==2) {
            holder.messageContent.setTypeface(holder.face);
            holder.date.setTypeface(holder.face);
        } else if (MainActivity.fontCode==1){
            holder.messageContent.setTypeface(null);
            holder.date.setTypeface(null);
        }

        holder.messageContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.date.getVisibility() == TextView.GONE) {
                    holder.date.setVisibility(TextView.VISIBLE);
                } else {
                    holder.date.setVisibility(TextView.GONE);
                }
            }
        });
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
     * @effects set the given list as the default list of the adapter
     * @param messageList
     */
    public void setArrayList(ArrayList<Message> messageList) {
        this.messageListArray = messageList;
        notifyDataSetChanged();
    }

    /**
     * @effects
     * @param msg
     */
    public void addItem(Message msg) {
        this.messageListArray.add(msg);
        notifyDataSetChanged();
    }

    /**
     * @overview class to hold the references of the TextViews used in ListView
     * @author wyrmise
     *
     */
    private class Holder {
        public TextView messageContent, date;
        public ImageView delivery_image;
        public LinearLayout wrapper;
        public LinearLayout inner_wrapper;
        public Typeface face;
    }
}
