package com.example.techzoneadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddProduct extends AppCompatActivity {
//    define variables
    private String[] categories = {"PC","Laptop","Graphic Card"};
    private ArrayAdapter<String> items;
    private String category,description,price,name,saveCurrentDate,saveCurrentTime,productRandomKey,downloadImageUrl;
    private AutoCompleteTextView categoryItems;
    private Button button;
    private ImageView productImage;
    private EditText productName,productDesc,productPrice;
    private static final int GalleryPick = 1;
    private Uri imageUri;
    private StorageReference ProductImagesRef;
    private DatabaseReference ProductsRef;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        loadingBar = new ProgressDialog(this);

        // Database Define
        ProductImagesRef = FirebaseStorage.getInstance().getReference().child("Product Images");
        ProductsRef = FirebaseDatabase.getInstance().getReference().child("Products");

        // Category Box
        categoryItems = findViewById(R.id.autoCompleteTextView);
        items = new ArrayAdapter<String>(this,R.layout.list_item, categories);
        categoryItems.setAdapter(items);
        categoryItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                category = adapterView.getItemAtPosition(i).toString();
                categoryItems.setError(null);
            }
        });

        // Product Details
        productImage = findViewById(R.id.selectImage);
        productName = findViewById(R.id.editTextProductName);
        productDesc = findViewById(R.id.editTextProductDesc);
        productPrice = findViewById(R.id.editTextProductPrice);

        // Select Product Image
        productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        // Add Product Button
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateProductData();
            }
        });
    }

    private void ValidateProductData() {
        name = productName.getText().toString();
        description = productDesc.getText().toString();
        price = productPrice.getText().toString();

        if (TextUtils.isEmpty(category)){
            categoryItems.setError("Choose Category");
            categoryItems.requestFocus();
        }
        else if (imageUri == null) {
            Toast.makeText(this, "Product image is required...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(name)) {
            productName.setError("Please write product name...");
            productName.requestFocus();
        }
        else if (TextUtils.isEmpty(description)) {
            productDesc.setError("Please write product description...");
            productDesc.requestFocus();
        }
        else if (TextUtils.isEmpty(price)) {
            productPrice.setError("Please write product Price...");
            productPrice.requestFocus();
        }
        else {
            StoreProductInformation();
        }
    }

    private void StoreProductInformation() {
        loadingBar.setTitle("Add New Product");
        loadingBar.setMessage("Dear Admin, please wait while we are adding the new product.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        productRandomKey = saveCurrentDate + saveCurrentTime;

        final StorageReference filePath = ProductImagesRef.child(imageUri.getLastPathSegment() + productRandomKey + ".jpg");
        final UploadTask uploadTask = filePath.putFile(imageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(AddProduct.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                    {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }

                        downloadImageUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if (task.isSuccessful())
                        {
                            downloadImageUrl = task.getResult().toString();
                            SaveProductInfoToDatabase();
                        }
                    }
                });
            }
        });
    }

    private void SaveProductInfoToDatabase() {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("id", productRandomKey);
        productMap.put("date", saveCurrentDate);
        productMap.put("time", saveCurrentTime);
        productMap.put("description", description);
        productMap.put("image", downloadImageUrl);
        productMap.put("category", category);
        productMap.put("price", price);
        productMap.put("name", name);

        ProductsRef.child(productRandomKey).updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    loadingBar.dismiss();
                    Toast.makeText(AddProduct.this, "Product is added successfully..", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.dismiss();
                    String message = task.getException().toString();
                    Toast.makeText(AddProduct.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick  &&  resultCode==RESULT_OK  &&  data!=null)
        {
            imageUri = data.getData();
            productImage.setImageURI(imageUri);
        }
    }
}