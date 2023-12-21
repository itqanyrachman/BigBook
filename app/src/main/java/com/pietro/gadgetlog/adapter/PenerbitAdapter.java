package com.pietro.gadgetlog.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.pietro.gadgetlog.AddPenerbitActivity;
import com.pietro.gadgetlog.ListKategoriActivity;
import com.pietro.gadgetlog.R;
import com.pietro.gadgetlog.model.Penerbit;

import java.util.List;

public class PenerbitAdapter extends RecyclerView.Adapter<PenerbitAdapter.PenerbitViewHolder> {
    private Context context;
    private List<Penerbit> penerbitList;

    public PenerbitAdapter(Context context, List<Penerbit> penerbitList) {
        this.context = context;
        this.penerbitList = penerbitList;
    }

    @NonNull
    @Override
    public PenerbitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_penerbit, parent, false);
        return new PenerbitViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PenerbitViewHolder holder, int position) {
        holder.tvPenerbitName.setText(penerbitList.get(position).getName());
        if(penerbitList.get(position).getImg() != null) {
            Glide.with(context).load(penerbitList.get(position).getImg()).into(holder.imgPenerbit);
        }

        holder.cvPenerbit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ListKategoriActivity.class);
                intent.putExtra("data", penerbitList.get(holder.getAdapterPosition()));
                context.startActivity(intent);
            }
        });

        holder.cvPenerbit.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final CharSequence[] items = { "Edit", "Delete", "Cancel" };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals("Edit")) {
                            Intent intent = new Intent(context, AddPenerbitActivity.class);
                            intent.putExtra("data", penerbitList.get(holder.getAdapterPosition()));
                            context.startActivity(intent);
                        } else if (items[item].equals("Delete")) {
                            delete(penerbitList.get(holder.getAdapterPosition()));
                        } else if (items[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return penerbitList.size();
    }

    private void delete(Penerbit penerbit) {
        FirebaseStorage.getInstance().getReferenceFromUrl(penerbit.getImg()).delete();
        FirebaseDatabase.getInstance().getReference("kategori").child(penerbit.getId()).removeValue();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("penerbit");
        database.child(penerbit.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Data Berhasil Dihapus", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class PenerbitViewHolder extends RecyclerView.ViewHolder {
        TextView tvPenerbitName;
        ImageView imgPenerbit;
        CardView cvPenerbit;
        public PenerbitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPenerbitName = itemView.findViewById(R.id.tvPenerbitName);
            imgPenerbit = itemView.findViewById(R.id.imgPenerbit);
            cvPenerbit = itemView.findViewById(R.id.cvPenerbit);
        }
    }
}

