package com.pietro.gadgetlog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pietro.gadgetlog.adapter.KategoriAdapter;
import com.pietro.gadgetlog.model.Kategori;
import com.pietro.gadgetlog.model.Social;
import com.pietro.gadgetlog.model.Penerbit;
import com.pietro.gadgetlog.model.Science;

import java.util.ArrayList;
import java.util.List;

public class ListKategoriActivity extends AppCompatActivity {
    private TextView tvTitle;
    private DatabaseReference database;
    private List<Kategori> kategoriList;
    private RecyclerView rvKategori;
    private KategoriAdapter adapter;
    private FloatingActionButton btnAddKategori;
    private Button btnScience, btnSocial;
    String kategoriType, penerbitId, penerbitName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_kategori);

        btnAddKategori = findViewById(R.id.btnAddKategori);
        tvTitle = findViewById(R.id.tvTitle);
        rvKategori = findViewById(R.id.rvKategori);
        btnScience = findViewById(R.id.btnScience);
        btnSocial = findViewById(R.id.btnSocial);
        kategoriList = new ArrayList<>();

        rvKategori.setLayoutManager(new GridLayoutManager(this, 2));

        Penerbit penerbit = (Penerbit) getIntent().getSerializableExtra("data");
        penerbitId = penerbit.getId();
        penerbitName = penerbit.getName();
        kategoriType = "science";

        tvTitle.setText("Katalog " + penerbit.getName());
        database = FirebaseDatabase.getInstance().getReference("kategori").child(penerbitId);

        database.child(kategoriType).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                kategoriList.clear();
                if(snapshot.exists()) {
                    for(DataSnapshot data : snapshot.getChildren()) {
                        Science science = data.getValue(Science.class);
                        kategoriList.add(science);
                    }
                }
                adapter = new KategoriAdapter(ListKategoriActivity.this, kategoriList, penerbitId, kategoriType);
                rvKategori.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnAddKategori.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListKategoriActivity.this, AddKategoriActivity.class);
                intent.putExtra("id", penerbitId);
                intent.putExtra("type", kategoriType);
                intent.putExtra("name", penerbitName);
                startActivity(intent);
            }
        });

        btnScience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnScience.setBackgroundColor(getResources().getColor(R.color.white));
                btnScience.setTextColor(getResources().getColor(R.color.purple_500));
                btnSocial.setBackgroundColor(getResources().getColor(R.color.purple_500));
                btnSocial.setTextColor(getResources().getColor(R.color.white));
                kategoriType = "science";
                database.child(kategoriType).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        kategoriList.clear();
                        if(snapshot.exists()) {
                            for(DataSnapshot data : snapshot.getChildren()) {
                                Science science = data.getValue(Science.class);
                                kategoriList.add(science);
                            }
                        }
                        adapter = new KategoriAdapter(ListKategoriActivity.this, kategoriList, penerbitId, kategoriType);
                        rvKategori.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        btnSocial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSocial.setBackgroundColor(getResources().getColor(R.color.white));
                btnSocial.setTextColor(getResources().getColor(R.color.purple_500));
                btnScience.setBackgroundColor(getResources().getColor(R.color.purple_500));
                btnScience.setTextColor(getResources().getColor(R.color.white));
                kategoriType = "Social";
                database.child(kategoriType).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        kategoriList.clear();
                        if(snapshot.exists()) {
                            for(DataSnapshot data : snapshot.getChildren()) {
                                Social social = data.getValue(Social.class);
                                kategoriList.add(social);
                            }
                        }
                        adapter = new KategoriAdapter(ListKategoriActivity.this, kategoriList, penerbitId, kategoriType);
                        rvKategori.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }
}