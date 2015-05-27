package io.wyrmise.meen;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Random;

/**
 * Created by wyrmise on 4/6/2015.
 */
public class NavAdapter extends RecyclerView.Adapter<NavAdapter.ViewHolder> {

    private static final int TYPE_HEADER = 0;  // Declaring Variable to Understand which View is being worked on
    // IF the view under inflation and population is header or Item
    private static final int TYPE_ITEM = 1;
    private String mNavTitles[]; // String Array to store the passed titles Value from MainActivity.java
    private int mIcons[];       // Int Array to store the passed icons resource value from MainActivity.java
    private String name;        //String Resource for header View Name
    private String id;       //String Resource for header view email
    private OnItemClickListener mListener;
    static ImageView header_image;
    static String path = "";


    /**
     * Interface for receiving click events from cells.
     */
    public interface OnItemClickListener {
        public void onClick(View view, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        int holderId;
        TextView textView;
        ImageView imageView;
        TextView Name;
        TextView id;
        View view;

        public ViewHolder(View itemView, int ViewType) {                 // Creating ViewHolder Constructor with View and viewType As a parameter
            super(itemView);
            // Here we set the appropriate view in accordance with the the view type as passed when the holder object is created
            view = itemView;
            if (ViewType == TYPE_ITEM) {
                textView = (TextView) itemView.findViewById(R.id.rowText); // Creating TextView object with the id of textView from item_row.xml
                imageView = (ImageView) itemView.findViewById(R.id.rowIcon);// Creating ImageView object with the id of ImageView from item_row.xml
                holderId = 1;                                               // setting holder id as 1 as the object being populated are of type item row
            } else {
                Name = (TextView) itemView.findViewById(R.id.name);         // Creating Text View object from header.xml for name
                id = (TextView) itemView.findViewById(R.id.id);       // Creating Text View object from header.xml for email
                holderId = 0;                                                // Setting holder id = 0 as the object being populated are of type header view
            }
        }
    }

    public NavAdapter(String Titles[], int Icons[], String Name, String id, OnItemClickListener listener) { // MyAdapter Constructor with titles and icons parameter
        mNavTitles = Titles;
        mIcons = Icons;
        name = Name;
        this.id = id;
        mListener = listener;
    }

    //Below first we ovverride the method onCreateViewHolder which is called when the ViewHolder is
    //Created, In this method we inflate the item_row.xml layout if the viewType is Type_ITEM or else we inflate header.xml
    // if the viewType is TYPE_HEADER
    // and pass it to the view holder

    @Override
    public NavAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_item, parent, false); //Inflating the layout

            ViewHolder vhItem = new ViewHolder(v, viewType); //Creating ViewHolder and passing the object of type view

            return vhItem; // Returning the created object

            //inflate your layout and pass it to view holder

        } else if (viewType == TYPE_HEADER) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header, parent, false); //Inflating the layout

            header_image = (ImageView) v.findViewById(R.id.header_image);

            SharedPreferences image = parent.getContext().getSharedPreferences("user_image",
                    Context.MODE_PRIVATE);
            path = image.getString("path","");

            if(path.equals("")) {
                Random r = new Random();
                int i = r.nextInt(4 - 1 + 1) + 1;
                switch (i) {
                    case 1:
                        header_image.setImageDrawable(parent.getContext().getResources().getDrawable(R.drawable.header_1));
                        break;
                    case 2:
                        header_image.setImageDrawable(parent.getContext().getResources().getDrawable(R.drawable.header_2));
                        break;
                    case 3:
                        header_image.setImageDrawable(parent.getContext().getResources().getDrawable(R.drawable.header_3));
                        break;
                    case 4:
                        header_image.setImageDrawable(parent.getContext().getResources().getDrawable(R.drawable.header_4));
                        break;
                }
            } else {
                loadImageFromStorage(path);
            }

            header_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 300);
                    intent.putExtra("aspectY", 178);
                    intent.putExtra("outputX", 300);
                    intent.putExtra("outputY", 178);

                    intent.putExtra("return-data", true);

                    ((Activity) parent.getContext()).startActivityForResult(Intent.createChooser(intent,
                            "Complete action using"), 1);
                }
            });

            ViewHolder vhHeader = new ViewHolder(v, viewType); //Creating ViewHolder and passing the object of type view

            return vhHeader; //returning the object created


        }
        return null;
    }

    public static void activityResult(Context context, int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            System.out.println("result ok");
            Bundle extra = data.getExtras();
            if (extra != null) {
                System.out.println("Change image");
                Bitmap photo = extra.getParcelable("data");
                path = saveToInternalSorage(context,photo);
                header_image.setImageBitmap(photo);
            }
        }
    }

    private static String saveToInternalSorage(Context context, Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "profile.jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SharedPreferences user_image = context.getSharedPreferences("user_image",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = user_image.edit();

        editor.putString("path", directory.getAbsolutePath());
        editor.commit();
        return directory.getAbsolutePath();
    }

    private void loadImageFromStorage(String path) {

        try {
            File f = new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            header_image.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    //Next we override a method which is called when the item in a row is needed to be displayed, here the int position
    // Tells us item at which position is being constructed to be displayed and the holder id of the holder object tell us
    // which view type is being created 1 for item row
    @Override
    public void onBindViewHolder(NavAdapter.ViewHolder holder, final int position) {
        if (holder.holderId == 1) {                              // as the list view is going to be called after the header view so we decrement the
            // position by 1 and pass it to the holder while setting the text and image
            holder.textView.setText(mNavTitles[position - 1]); // Setting the Text with the array of our Titles
            holder.imageView.setImageResource(mIcons[position - 1]);// Setting the image with array of our icons

        } else {
            holder.Name.setText(name);
            holder.id.setText(id);
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mListener.onClick(view, position);
            }
        });
    }

    // This method returns the number of items present in the list
    @Override
    public int getItemCount() {
        return mNavTitles.length + 1; // the number of items in the list will be +1 the titles including the header view.
    }

    // With the following method we check what type of view is being passed
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

}