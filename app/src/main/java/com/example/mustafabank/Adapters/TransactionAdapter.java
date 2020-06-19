package com.example.mustafabank.Adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mustafabank.Models.Transaction;
import com.example.mustafabank.R;

import java.util.ArrayList;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private static final String TAG = "TransactionAdapter";

    private ArrayList<Transaction> transactions;

    public TransactionAdapter() {
        transactions = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: started");
        holder.textDate.setText(transactions.get(position).getDate());
        holder.textDesc.setText(transactions.get(position).getDescription());
        holder.textTransactionId.setText("Transaction ID: " + String.valueOf(transactions.get(position).get_id()));
        holder.textSender.setText(transactions.get(position).getRecipient());

        double amount = transactions.get(position).getAmount();
        if(amount > 0){
            holder.textAmount.setText("+" + amount);
            holder.textAmount.setTextColor(Color.GREEN);
        }else{
            holder.textAmount.setText(String.valueOf(amount));
            holder.textAmount.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView textAmount, textDesc, textDate, textSender, textTransactionId;
        private CardView parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textAmount = (TextView) itemView.findViewById(R.id.textamountListItem);
            textDesc = (TextView) itemView.findViewById(R.id.textDescriptionListItem);
            textDate = (TextView) itemView.findViewById(R.id.textDateListItem);
            textSender = (TextView) itemView.findViewById(R.id.textSenderListItem);
            textTransactionId = (TextView) itemView.findViewById(R.id.textTransactionListItem);
            parent = (CardView) itemView.findViewById(R.id.parentListItem);
        }
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }
}
