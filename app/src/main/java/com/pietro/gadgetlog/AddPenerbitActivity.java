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
import com.pietro.gadgetlog.model.Penerbit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class AddPenerbitActivity extends AppCompatActivity {
    //    INSTANTIASI
    private DatabaseReference database;
    private Button btnSubmitPenerbit;
    private EditText etPenerbitName;
    private EditText etPenerbitCountry;
    private TextView tvTitle;
    private ImageView imgPenerbit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_penerbit);

//        INISIALISASI
        etPenerbitName = findViewById(R.id.etPenerbitName);
        etPenerbitCountry = findViewById(R.id.etPenerbitCountry);
        btnSubmitPenerbit = findViewById(R.id.btnSubmitPenerbit);
        tvTitle = findViewById(R.id.tvTitle);
        imgPenerbit = findViewById(R.id.imgPenerbit);
        database = FirebaseDatabase.getInstance().getReference();

//        GET DATA DEV FROM INTENT IF AVAILABLE
        final Penerbit penerbit = (Penerbit) getIntent().getSerializableExtra("data");

//        SET EVENT VARIABLE
        imgPenerbit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        if(penerbit != null) {
            tvTitle.setText("Edit Penerbit Data");
            etPenerbitName.setText(penerbit.getName());
            etPenerbitCountry.setText(penerbit.getCountry());
            Glide.with(getApplicationContext()).load(penerbit.getImg()).into(imgPenerbit);
            btnSubmitPenerbit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    penerbit.setName(etPenerbitName.getText().toString());
                    penerbit.setCountry(etPenerbitCountry.getText().toString());
                    uploadEdit(penerbit);
                }
            });
        } else {
            btnSubmitPenerbit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name = etPenerbitName.getText().toString();
                    String country = etPenerbitCountry.getText().toString();
                    uploadNew(name, country);
                }
            });
        }
    }

    //    SELECT IMAGE FUNCTION
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(AddPenerbitActivity.this);
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
                imgPenerbit.setImageBitmap(bm);
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

                imgPenerbit.setImageBitmap(thumbnail);
            }
        }
    }

    //    UPLOAD IMAGE TO STORAGE
    private void uploadNew(String name, String country) {
        imgPenerbit.setDrawingCacheEnabled(true);
        imgPenerbit.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imgPenerbit.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        String newName = name.replaceAll(" ", "").toLowerCase();
        StorageReference reference = storage.getReference("images/penerbit").child("IMG-"+ newName + "-" + new Date().getTime() + ".jpeg");
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
                                submitPenerbit(name, country, task.getResult().toString());
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Gagal", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadEdit(Penerbit penerbit) {
        imgPenerbit.setDrawingCacheEnabled(true);
        imgPenerbit.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imgPenerbit.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        String name = penerbit.getName().replaceAll(" ", "").toLowerCase(Locale.ROOT);
        StorageReference reference = storage.getReference("images/penerbit").child("IMG-"+ name + "-" + new Date().getTime() + ".jpeg");
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
                                FirebaseStorage.getInstance().getReferenceFromUrl(penerbit.getImg()).delete();
                                penerbit.setImg(task.getResult().toString());
                                updatePenerbit(penerbit);
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Gagal", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //    SAVE DEVELOPER DATA TO DB
    private void submitPenerbit(String name, String country, String img) {
        Penerbit penerbit = new Penerbit(name, country, img);
        String id = database.push().getKey();
        penerbit.setId(id);

        database.child("penerbit").child(id).setValue(penerbit).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                etPenerbitName.setText("");
                etPenerbitCountry.setText("");
                imgPenerbit.setImageResource(R.drawable.ic_baseline_image_24);
                Toast.makeText(getApplicationContext(), "Data Berhasil Disimpan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //    UPDATE DEVELOPER DATA TO DB
    private void updatePenerbit(Penerbit penerbit) {
        database.child("penerbit").child(penerbit.getId()).setValue(penerbit).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Data Berhasil Diupdate", Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }
}