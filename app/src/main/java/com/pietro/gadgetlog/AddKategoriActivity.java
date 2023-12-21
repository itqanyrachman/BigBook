package com.pietro.gadgetlog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pietro.gadgetlog.model.Kategori;
import com.pietro.gadgetlog.model.Social;
import com.pietro.gadgetlog.model.Science;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class AddKategoriActivity extends AppCompatActivity {
    //    INSTANTIASI
    private DatabaseReference database;
    private Button btnSubmitKategori;
    private EditText etKategoriName;
    private EditText etKategoriBrand;
    private EditText etKategoriPrice;
    private TextView tvTitle;
    private ImageView imgKategori;
    String penerbitId, penerbitName;
    String kategoriType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_kategori);

//        INISIALISASI
        etKategoriName = findViewById(R.id.etKategoriName);
        etKategoriBrand = findViewById(R.id.etKategoriBrand);
        btnSubmitKategori = findViewById(R.id.btnSubmitKategori);
        imgKategori = findViewById(R.id.imgKategori);
        penerbitId = getIntent().getStringExtra("id");
        kategoriType = getIntent().getStringExtra("type");
        penerbitName = getIntent().getStringExtra("name");
        tvTitle = findViewById(R.id.tvTitle);
        etKategoriPrice = findViewById(R.id.etKategoriPrice);

        database = FirebaseDatabase.getInstance().getReference("kategori").child(penerbitId).child(kategoriType);
        Kategori kategori;

        if(kategoriType.equals("science")) {
            kategori = (Science) getIntent().getSerializableExtra("data");
        } else {
            kategori = (Social) getIntent().getSerializableExtra("data");
        }

        tvTitle.setText("Tambah Buku " + kategoriType);
        etKategoriBrand.setText(penerbitName);

        if(kategori != null) {
            tvTitle.setText("Edit " + kategoriType + " Data");
            etKategoriName.setText(kategori.getName());
            etKategoriBrand.setText(kategori.getBrand());
            etKategoriPrice.setText(kategori.getPrice());
            Glide.with(getApplicationContext()).load(kategori.getImg()).into(imgKategori);
            btnSubmitKategori.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    kategori.setName(etKategoriName.getText().toString());
                    kategori.setBrand(etKategoriBrand.getText().toString());
                    kategori.setPrice(etKategoriPrice.getText().toString());
                    uploadEdit(kategori);
                }
            });
        } else {
            btnSubmitKategori.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name = etKategoriName.getText().toString();
                    String brand = etKategoriBrand.getText().toString();
                    String price = etKategoriPrice.getText().toString();
                    upload(name, brand, price);
                }
            });
        }

//        SET VARIABLE EVENT
        imgKategori.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
    }

    //    SELECT IMAGE FUNCTION
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(AddKategoriActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 10);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);//
                    startActivityForResult(Intent.createChooser(intent, "Select File"),20);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    //    GET IMAGE CHOOSEN
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 20) {
                Bitmap bm = null;
                if (data != null) {
                    try {
                        bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                imgKategori.setImageBitmap(bm);
            } else if (requestCode == 10) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");

                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                imgKategori.setImageBitmap(thumbnail);
            }
        }
    }

    //    UPLOAD IMAGE TO STORAGE
    private void upload(String name, String brand, String price) {
        imgKategori.setDrawingCacheEnabled(true);
        imgKategori.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imgKategori.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        String newName = name.replaceAll(" ", "").toLowerCase();
        StorageReference reference = storage.getReference("images/kategori").child(penerbitId).child(kategoriType).child("IMG-"+ newName + "-" + new Date().getTime() + ".jpeg");
        UploadTask uploadTask = reference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if(taskSnapshot.getMetadata().getReference() != null) {
                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.getResult() != null) {
                                submitKategori(name, brand, task.getResult().toString(), price);
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Gagal", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadEdit(Kategori kategori) {
        imgKategori.setDrawingCacheEnabled(true);
        imgKategori.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imgKategori.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        String name = kategori.getName().replaceAll(" ", "");
        StorageReference reference = storage.getReference("images/kategori").child(penerbitId).child(kategoriType).child("IMG-"+ name + "-" + new Date().getTime() + ".jpeg");
        UploadTask uploadTask = reference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if(taskSnapshot.getMetadata().getReference() != null) {
                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.getResult() != null) {
                                FirebaseStorage.getInstance().getReferenceFromUrl(kategori.getImg()).delete();
                                kategori.setImg(task.getResult().toString());
                                updateKategori(kategori);
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Gagal", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //    SAVE GAME TO DB
    private void submitKategori(String name, String genre, String img, String price) {
        Kategori kategori;
        if(kategoriType.equals("science")) {
            kategori = new Science(name, genre, img, price);
        } else {
            kategori = new Social(name, genre, img, price);
        }
        String id = database.push().getKey();
        kategori.setId(id);

        database.child(id).setValue(kategori).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                etKategoriName.setText("");
                etKategoriBrand.setText("");
                etKategoriPrice.setText("");
                imgKategori.setImageResource(R.drawable.ic_baseline_image_24);
                Toast.makeText(getApplicationContext(), "Data Berhasil Disimpan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateKategori(Kategori kategori) {
        database.child(kategori.getId()).setValue(kategori).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Data Berhasil Diubah", Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }
}