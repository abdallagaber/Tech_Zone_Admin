package com.example.techzoneadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditProduct1 extends AppCompatActivity {
    private String[] categories = {"PC","Laptop","PC Components","Accessories"};
    private ArrayAdapter<String> items;
    private AutoCompleteTextView categoryItems;
    private ImageView productImage;
    private EditText productName,productDesc,productPrice;
    private String category,productID,description,price,name,imageUri;
    private DatabaseReference ProductsRef;
    private Button editProductButton,deleteProductButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product1);

        productID = getIntent().getStringExtra("id");
        ProductsRef = FirebaseDatabase.getInstance().getReference("Products").child(productID);


        categoryItems = findViewById(R.id.editProductCategory);
        items = new ArrayAdapter<String>(this,R.layout.list_item, categories);
        categoryItems.setAdapter(items);
        categoryItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                category = adapterView.getItemAtPosition(i).toString();
                categoryItems.setError(null);
            }
        });

        productImage = findViewById(R.id.ProductIcon);
        productName = findViewById(R.id.editProductName);
        productDesc = findViewById(R.id.editProductDesc);
        productPrice = findViewById(R.id.editProductPrice);
        editProductButton = findViewById(R.id.editProductButton);
        deleteProductButton = findViewById(R.id.deleteProductButton);

        ProductInfo();

        editProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ApplyChanges();
            }
        });
        deleteProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProductsRef.removeValue();
                Toast.makeText(EditProduct1.this, "Product Deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    private void ApplyChanges() {
        name = productName.getText().toString();
        description = productDesc.getText().toString();
        price = productPrice.getText().toString();
        if (TextUtils.isEmpty(category)){
            categoryItems.setError("Choose Category");
            categoryItems.requestFocus();
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
            UpdateProductInformation();
        }
    }

    private void UpdateProductInformation() {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("description", description);
        productMap.put("category", category);
        productMap.put("price", price);
        productMap.put("name", name);

        ProductsRef.updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(EditProduct1.this, "Changes Applied", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void ProductInfo() {
        ProductsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    name = snapshot.child("name").getValue().toString();
                    price = snapshot.child("price").getValue().toString();
                    description = snapshot.child("description").getValue().toString();
                    category = snapshot.child("category").getValue().toString();
                    imageUri = snapshot.child("image").getValue().toString();

                    productName.setText(name);
                    productDesc.setText(description);
                    productPrice.setText(price);
                    categoryItems.setText(category,false);
                    Picasso.get().load(imageUri).into(productImage);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}