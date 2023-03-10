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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder>{

    private Context mContext;
    private List<Product> mListProduct;
    private List<String> employeeList;

    public ProductAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<Product> list, List<String> employeeList){
        this.mListProduct = list;
        this.employeeList = employeeList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_single_product,parent,false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = mListProduct.get(position);
        if(product == null){
            return;
        }
        holder.tv_product_name.setText(product.getName());
        holder.tv_barcode.setText(product.getCode());
        holder.tv_date.setText(product.getTimeSubmit());
        holder.tv_wh.setText(product.getBelongsTo());
        holder.tv_product_qty.setText(product.getQuantity());
    }

    @Override
    public int getItemCount() {
        if(mListProduct != null) return mListProduct.size();
        return 0;
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder{

        private TextView tv_product_qty,tv_product_name,tv_barcode,tv_wh,tv_date;
        private RelativeLayout layout_single_product;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_product_name = itemView.findViewById(R.id.tv_product_name);
            tv_product_qty = itemView.findViewById(R.id.tv_product_qty);
            tv_barcode = itemView.findViewById(R.id.tv_barcode);
            tv_wh = itemView.findViewById(R.id.tv_wh);
            tv_date = itemView.findViewById(R.id.tv_date);
            layout_single_product = itemView.findViewById(R.id.layout_single_product);
            layout_single_product.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //open a pop up window
                    AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                    View customView = LayoutInflater.from(mContext).inflate(R.layout.popup_detail_recycler,null);

                    TextView des_pro_name = customView.findViewById(R.id.des_pro_name);
                    des_pro_name.setText(mListProduct.get(getAdapterPosition()).getName());
                    TextView des_pro_id = customView.findViewById(R.id.des_pro_id);
                    des_pro_id.setText(mListProduct.get(getAdapterPosition()).getCode());
                    TextView des_created_on = customView.findViewById(R.id.des_created_on);
                    des_created_on.setText(mListProduct.get(getAdapterPosition()).getTimeSubmit());
                    TextView des_warehouse = customView.findViewById(R.id.des_warehouse);
                    des_warehouse.setText(mListProduct.get(getAdapterPosition()).getBelongsTo());
                    TextView des_quantity = customView.findViewById(R.id.des_quantity);
                    des_quantity.setText(mListProduct.get(getAdapterPosition()).getQuantity());
                    TextView des_person = customView.findViewById(R.id.des_person);
                    des_person.setText(employeeList.get(getAdapterPosition()));

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
