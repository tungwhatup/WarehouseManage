package com.example.warehousemanage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class InStorageAdapter extends RecyclerView.Adapter<InStorageAdapter.InStorageViewHolder>{

    private Context mContext;
    private List<Map<String,String>> maps;
    private List<String> names;

    public InStorageAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<Map<String,String>> maps,List<String> names){
        this.maps = maps;
        this.names = names;
    }

    @NonNull
    @Override
    public InStorageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_single_in_storage,parent,false);
        return new InStorageAdapter.InStorageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InStorageViewHolder holder, int position) {
        Map<String,String> product = maps.get(position);
        String barcode = names.get(position);
        holder.tv_product_name.setText(product.get("Title"));
        holder.tv_barcode.setText(barcode);
        holder.tv_nums.setText(product.get("Price"));
        holder.tv_zone.setText(product.get("In"));
        holder.tv_product_qty.setText(product.get("In Storage")+" items");
    }

    @Override
    public int getItemCount() {
        if(maps != null) return maps.size();
        return 0;
    }

    public class InStorageViewHolder extends RecyclerView.ViewHolder{

        private TextView tv_product_name,tv_barcode,tv_nums,tv_zone,tv_product_qty;
        private RelativeLayout layout_single_product;

        public InStorageViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_product_name = itemView.findViewById(R.id.tv_product_name);
            tv_barcode = itemView.findViewById(R.id.tv_barcode);
            tv_nums = itemView.findViewById(R.id.tv_nums);
            tv_zone = itemView.findViewById(R.id.tv_zone);
            tv_product_qty = itemView.findViewById(R.id.tv_product_qty);
            layout_single_product = itemView.findViewById(R.id.layout_single_product);
            layout_single_product.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                    View customView = LayoutInflater.from(mContext).inflate(R.layout.popup_storage,null);
                    TextView des_pro_name = customView.findViewById(R.id.des_pro_name);
                    des_pro_name.setText(maps.get(getAdapterPosition()).get("Title"));
                    TextView des_pro_id = customView.findViewById(R.id.des_pro_id);
                    des_pro_id.setText(names.get(getAdapterPosition()));
                    TextView des_oldest = customView.findViewById(R.id.des_oldest);
                    des_oldest.setText(maps.get(getAdapterPosition()).get("Since"));
                    TextView des_zone = customView.findViewById(R.id.des_zone);
                    des_zone.setText(maps.get(getAdapterPosition()).get("In"));
                    TextView des_quantity = customView.findViewById(R.id.des_quantity);
                    des_quantity.setText(maps.get(getAdapterPosition()).get("In Storage"));
                    TextView des_manufacturer = customView.findViewById(R.id.des_manufacturer);
                    des_manufacturer.setText(maps.get(getAdapterPosition()).get("Manufacturer"));
                    TextView des_price = customView.findViewById(R.id.des_price);
                    des_price.setText(maps.get(getAdapterPosition()).get("Price"));
                    TextView des_currency = customView.findViewById(R.id.des_currency);
                    des_currency.setText(maps.get(getAdapterPosition()).get("Currency"));

                    Button btnDismiss = customView.findViewById(R.id.btnDismiss);
                    builder.setView(customView);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    btnDismiss.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                }
            });

        }
    }
}
