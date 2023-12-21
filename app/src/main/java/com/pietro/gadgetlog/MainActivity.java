package com.pietro.gadgetlog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pietro.gadgetlog.adapter.PenerbitAdapter;
import com.pietro.gadgetlog.model.Penerbit;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //    INSTANTIASI
    private FloatingActionButton btnAddPenerbit;
    private DatabaseReference database;
    private List<Penerbit> penerbitList;
    private RecyclerView rvPenerbit;
    private PenerbitAdapter adapter;
    private Button buttonLogout;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        INISIALISASI
        btnAddPenerbit = findViewById(R.id.btnAddPenerbit);
        rvPenerbit = findViewById(R.id.rvPenerbit);
        database = FirebaseDatabase.getInstance().getReference();
        penerbitList = new ArrayList<>();
        buttonLogout = findViewById(R.id.buttonLogout);

//        SET VARIABLE
        rvPenerbit.setLayoutManager(new GridLayoutManager(this, 2));

        btnAddPenerbit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddPenerbitActivity.class);
                startActivity(intent);
            }
        });

        buttonLogout.setOnClickListener(v ->{
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

//        GET DATA FROM DB
        database.child("penerbit").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                penerbitList.clear();
                if(snapshot.exists()) {
                    for(DataSnapshot data : snapshot.getChildren()) {
                        Penerbit dev = data.getValue(Penerbit.class);
                        penerbitList.add(dev);
                    }
                }
                adapter = new PenerbitAdapter(MainActivity.this, penerbitList);
                rvPenerbit.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}