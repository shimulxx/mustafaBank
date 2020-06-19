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

import com.example.mustafabank.Models.Loan;
import com.example.mustafabank.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class LoanAdapter extends RecyclerView.Adapter<LoanAdapter.ViewHolder> {
    private static final String TAG = "LoanAdapter";

    private Context context;
    private ArrayList<Loan> loans;
    private int number = -1;

    public LoanAdapter(Context context) {
        this.context = context;
        loans = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_loan, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: started");
        holder.name.setText(loans.get(position).getName());
        holder.initDate.setText(loans.get(position).getInit_date());
        holder.finishDate.setText(loans.get(position).getFinish_date());
        holder.amount.setText(String.valueOf(loans.get(position).getInit_amount()));
        holder.roi.setText(String.valueOf(loans.get(position).getMonthly_roi()));
        holder.remained_amount.setText(String.valueOf(loans.get(position).getRemained_amount()));
        holder.loss.setText(String.valueOf(getTotalLoss(loans.get(position))));
        holder.monthly_payment.setText(String.valueOf(loans.get(position).getMonthly_payment()));

        if(number == -1){ holder.parent.setCardBackgroundColor(context.getResources().getColor(R.color.light_green)); number = 1; }
        else{ holder.parent.setCardBackgroundColor(context.getResources().getColor(R.color.light_blue)); number = -1; }
    }

    private double  getTotalLoss(Loan loan){
        Log.d(TAG, "getTotalLoss for loan: " + loan.toString());
        double loss = 0.0;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try{
            Date initDate = simpleDateFormat.parse(loan.getInit_date());
            calendar.setTime(initDate);
            int initMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

            Date finishDate = simpleDateFormat.parse(loan.getFinish_date());
            calendar.setTime(finishDate);
            int finishMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

            int months = finishMonth - initMonth;
            for(int i = 0; i < months; ++i) loss += loan.getInit_amount() * loan.getMonthly_roi()/100;

        }catch (ParseException e) { e.printStackTrace(); }

        return loss;
    }

    @Override
    public int getItemCount() {
        return loans.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView name, initDate, finishDate, roi, loss, amount, remained_amount, monthly_payment;
        private CardView parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.textNameLoanListItem);
            initDate = (TextView) itemView.findViewById(R.id.textInitDateLoanListItem);
            finishDate = (TextView) itemView.findViewById(R.id.textFinishDateLoanListItem);
            roi = (TextView) itemView.findViewById(R.id.textMonthlyROILoanListItem);
            loss = (TextView) itemView.findViewById(R.id.textTotalExpectedLossLoanListItem);
            amount = (TextView) itemView.findViewById(R.id.textAmountLoanListItem);
            remained_amount = (TextView) itemView.findViewById(R.id.textRemainedAmountLoanListItem);
            monthly_payment = (TextView) itemView.findViewById(R.id.textMonthlyPaymentLoanListItem);

            parent = (CardView) itemView.findViewById(R.id.parentLoanListItem);
        }

    }

    public void setLoans(ArrayList<Loan> loans) {
        this.loans = loans;
        notifyDataSetChanged();
    }
}
