package com.pietro.gadgetlog.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pietro.gadgetlog.AddKategoriActivity;
import com.pietro.gadgetlog.R;
import com.pietro.gadgetlog.model.Kategori;
import com.pietro.gadgetlog.model.Penerbit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class KategoriAdapter extends RecyclerView.Adapter<KategoriAdapter.KategoriViewHolder> {
    private Context context;
    private List<Kategori> kategoriList;
    private String penerbitId;
    private String kategoriType;

    public KategoriAdapter(Context context, List<Kategori> kategoriList, String penerbitId, String kategoriType) {
        this.context = context;
        this.kategoriList = kategoriList;
        this.penerbitId = penerbitId;
        this.kategoriType = kategoriType;
    }

    @NonNull
    @Override
    public KategoriViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_kategori, parent, false);
        return new KategoriViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull KategoriViewHolder holder, int position) {
        holder.tvKategoriName.setText(kategoriList.get(position).getName());
        Glide.with(context).load(kategoriList.get(position).getImg()).into(holder.imgKategori);
        if(kategoriList.get(position).getPrice() != null) {
            double price = Double.parseDouble(kategoriList.get(position).getPrice());
            NumberFormat kursId = NumberFormat.getCurrencyInstance(Locale.ITALY);
            String newPrice = kursId.format(price);
            newPrice = newPrice.substring(0, newPrice.length()-5);
            holder.tvKategoriPrice.setText("IDR "+ newPrice);
        }

        holder.cvKategori.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final CharSequence[] items = { "Edit", "Delete", "Cancel", "Download"};

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals("Edit")) {
                            Intent intent = new Intent(context, AddKategoriActivity.class);
                            intent.putExtra("data", kategoriList.get(holder.getAdapterPosition()));
                            intent.putExtra("id", penerbitId);
                            intent.putExtra("type", kategoriType);
                            context.startActivity(intent);
                        } else if (items[item].equals("Delete")) {
                            delete(kategoriList.get(holder.getAdapterPosition()));
                        } else if (items[item].equals("Cancel")) {
                            dialog.dismiss();
                        } else if (items[item].equals("Download")) {
                            downloadImage(kategoriList.get(holder.getAdapterPosition()));
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
        return kategoriList.size();
    }
    private void delete(Kategori kategori) {
        FirebaseStorage.getInstance().getReferenceFromUrl(kategori.getImg()).delete();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("kategori").child(penerbitId).child(kategoriType);
        database.child(kategori.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Data Berhasil Dihapus", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadImage(Kategori kategori) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String name = kategori.getName().replaceAll(" ", "").toLowerCase(Locale.ROOT);
        StorageReference reference = storage.getReference("/images/kategori/-NmBxpH_wKa9Ost-qy4L/science/")
                .child("IMG-matematikakelasixkurikulum2013-1703175536097.jpeg");

        File localFile = new File(context.getFilesDir(), "downloaded_image.jpeg");

        reference.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(context, "Berhasil mengunduh gambar", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            // Handle failure
            Toast.makeText(context, "Gagal mengunduh gambar", Toast.LENGTH_SHORT).show();
        });
    }

    class KategoriViewHolder extends RecyclerView.ViewHolder {
        TextView tvKategoriName;
        TextView tvKategoriPrice;
        ImageView imgKategori;
        CardView cvKategori;
        public KategoriViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKategoriName = itemView.findViewById(R.id.tvKategoriName);
            imgKategori = itemView.findViewById(R.id.imgKategori);
            cvKategori = itemView.findViewById(R.id.cvKategori);
            tvKategoriPrice = itemView.findViewById(R.id.tvKategoriPrice);
        }
    }
}

