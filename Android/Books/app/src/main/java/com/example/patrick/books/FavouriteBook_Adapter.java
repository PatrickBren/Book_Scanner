package com.example.patrick.books;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FavouriteBook_Adapter extends ArrayAdapter<Book> {

    private static final String TAG = "Book_Adapter";

    private Context mContext;
    private ArrayList<Book> books = new ArrayList<>();

    int mResource;

    public FavouriteBook_Adapter(Context context, int resource, ArrayList<Book> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        String title = getItem(position).getTitle();
        String author = getItem(position).getAuthor();
        String id = getItem(position).getImg_id();
        Boolean is_custom = getItem(position).getIs_custom();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        final TextView tv_title  = (TextView) convertView.findViewById(R.id.tv_title);
        final TextView tv_author = (TextView) convertView.findViewById(R.id.tv_author);
        ImageView iv_book_main = (ImageView) convertView.findViewById(R.id.iv_book_main);

        tv_title.setText(title);
        tv_author.setText(author);

        //if book is custom user firebase storage custom image
        if(is_custom == true){
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/book-scann.appspot.com/o/book_covers%2FkbNojTrH0OWz1bTPIByY9OuIBbE3%2F"+id+"?alt=media&token=f946069c-da0f-4731-b49b-374b0405ccb6").into(iv_book_main);
        }
        //if book isn't custom it uses the google books cover image
        else if(is_custom == false){
            Picasso.get().load("https://books.google.com/books/content/images/frontcover/"+id+"?fife=w500-h900").into(iv_book_main);
        }


        return convertView;
    }

}
