package com.example.mustafabank.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mustafabank.Models.Investment;
import com.example.mustafabank.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class InvestmentAdapter extends RecyclerView.Adapter<InvestmentAdapter.ViewHolder> {
    private static final String TAG = "InvestmentAdapter";

    private ArrayList<Investment> investments;

    private int number = -1;

    private Context context;


    public InvestmentAdapter(Context context) {
        this.context = context;
        investments = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_investment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: started");
        holder.name.setText(investments.get(position).getName());
        holder.initDate.setText(String.valueOf(investments.get(position).get_id()));
        holder.finishDate.setText(investments.get(position).getFinish_date());
        holder.amount.setText(String.valueOf(investments.get(position).getAmount()));
        holder.roi.setText(String.valueOf(investments.get(position).getMonthly_roi()));
        holder.profit.setText(String.valueOf(getTotalProfit(investments.get(position))));

        if(number == -1){ holder.parent.setCardBackgroundColor(context.getResources().getColor(R.color.light_green)); number = 1; }
        else{ holder.parent.setCardBackgroundColor(context.getResources().getColor(R.color.light_blue)); number = -1; }

    }

    private double getTotalProfit(Investment investment){
        Log.d(TAG, "getTotalProfit: started");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        double profit = 0.0;

        try{
            calendar.setTime((simpleDateFormat.parse(investment.getInit_date())));
            int initMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);
            calendar.setTime(simpleDateFormat.parse(investment.getFinish_date()));
            int finishMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);
            int months = finishMonth - initMonth;
            for(int i = 0; i < months; ++i) profit += investment.getAmount() * investment.getMonthly_roi()/100;
        }catch (ParseException e){ e.printStackTrace(); }
        return profit;
    }

    @Override
    public int getItemCount() {
        return investments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView name, initDate, finishDate, roi, profit, amount;
        private CardView parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.textInvestmentNameItemInvestment);
            initDate = (TextView) itemView.findViewById(R.id.textInitDateItemInvestment);
            finishDate = (TextView) itemView.findViewById(R.id.textFinishDateItemInvestment);
            roi = (TextView) itemView.findViewById(R.id.textROIItemInvestment);
            profit = (TextView) itemView.findViewById(R.id.textProfitAmountItemInvestment);
            amount = (TextView) itemView.findViewById(R.id.textAmountItemInvestment);

            parent = (CardView) itemView.findViewById(R.id.parentListItemInvestment);

        }
    }

    public void setInvestments(ArrayList<Investment> investments) {
        this.investments = investments;
        notifyDataSetChanged();
    }
}
