package com.example.mustafabank.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mustafabank.Models.Item;
import com.example.mustafabank.R;

import java.util.ArrayList;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

    private static final String TAG = "ItemsAdapter";
    private ArrayList<Item> items = new ArrayList<>();
    private Context context;
    private DialogFragment dialogFragment;

    public interface GetItem{
        void onGettingItemResult(Item item);
    }

    public GetItem getItem;

    public ItemsAdapter() {
    }

    public ItemsAdapter(Context context, DialogFragment dialogFragment) {
        this.context = context;
        this.dialogFragment = dialogFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_items,parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: started");
        holder.name.setText(items.get(position).getName());
        Glide.with(context)
                .asBitmap()
                .load(items.get(position).getImage_url())
                .into(holder.image);

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    getItem = (GetItem) dialogFragment;
                    getItem.onGettingItemResult(items.get(position));
                }catch (ClassCastException e) { e.printStackTrace(); }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView image;
        private TextView name;
        private CardView parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.itemImageItems);
            name = (TextView) itemView.findViewById(R.id.itemNameItems);
            parent = (CardView) itemView.findViewById(R.id.parentItems);
        }
    }
}
