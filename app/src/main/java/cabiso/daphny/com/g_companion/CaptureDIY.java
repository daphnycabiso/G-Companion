package cabiso.daphny.com.g_companion;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by Lenovo on 8/22/2017.
 */

import cabiso.daphny.com.g_companion.Recommend.Bottle_Recommend;
import cabiso.daphny.com.g_companion.Recommend.DIYrecommend;
import cabiso.daphny.com.g_companion.Recommend.Paper_Recommend;
import cabiso.daphny.com.g_companion.Recommend.Rubber_Recommend;
import cabiso.daphny.com.g_companion.Recommend.Wood_Recommend;

public class CaptureDIY extends AppCompatActivity implements View.OnClickListener{

    private DatabaseReference databaseReference;
    private DatabaseReference userDatabaseReference;

    private StorageReference storageReference;

    private FirebaseUser mFirebaseUser;
    private String userID;
    private String imageFileName;

    private Uri diyPictureUri;

    private static final int SELECT_PHOTO = 100;
    private static final int MAX_LENGTH = 100;
    CheckBox bottle, paper, rubber, wood;
    Button submitButton;
    EditText name, material, procedure;
    ImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_diy);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = mFirebaseUser.getUid();

        name = (EditText)findViewById(R.id.add_diy_name);
        material = (EditText)findViewById(R.id.add_diy_material);
        procedure = (EditText) findViewById(R.id.add_diy_procedure);
        imgView = (ImageView) findViewById(R.id.add_product_image_plus_icon);
        bottle = (CheckBox)findViewById(R.id.cbBottle);
        paper = (CheckBox)findViewById(R.id.cbPaper);
        rubber = (CheckBox)findViewById(R.id.cbRubber);
        wood = (CheckBox)findViewById(R.id.cbWood);

        bottle.setOnClickListener(this);
        paper.setOnClickListener(this);
        rubber.setOnClickListener(this);
        wood.setOnClickListener(this);

        final ImageView addProductImagePlusIcon = (ImageView) findViewById(R.id.add_product_image_plus_icon);
        addProductImagePlusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        submitButton = (Button) findViewById(R.id.add_submit_diy);
        submitButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v==submitButton){
            if (bottle.isChecked()){
                //reference to database firebase
                databaseReference = FirebaseDatabase.getInstance().getReference().child("DIY_Methods")
                        .child("category").child("bottle");
                userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("DIYs_By_Users").child(userID);
                //generate random unique ID for image to database storage
                String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
                StringBuilder salt = new StringBuilder();
                Random rnd = new Random();
                while (salt.length() < 18) { // length of the random string.
                    int index = (int) (rnd.nextFloat() * SALTCHARS.length());
                    salt.append(SALTCHARS.charAt(index));
                }
                String saltStr = salt.toString();
                //reference to database storage
                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://g-companion.appspot.com/");
                storageReference.child("add_DIY").child("bottles").child(saltStr+"").putFile(diyPictureUri).addOnSuccessListener
                        (new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //get downloadUrl from storage
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                //get text input and save new object
                                DIYrecommend recommend = new DIYrecommend(name.getText().toString(), material.getText().toString()
                                        ,procedure.getText().toString(),taskSnapshot.getDownloadUrl().toString());
                                //assign string upload to database reference
                                String upload = databaseReference.push().getKey();
                                //upload data to DIY_Methods database reference
                                databaseReference.child(upload).setValue(recommend);
                                //upload data to DIYs by users
                                userDatabaseReference.child(upload).setValue(recommend);
                                Picasso.with(CaptureDIY.this).load(downloadUrl).into(imgView);
                                //direct to another activity
                                Intent intent = new Intent(CaptureDIY.this,Bottle_Recommend.class);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            }
            else if(paper.isChecked()){
                //reference to database firebase
                databaseReference = FirebaseDatabase.getInstance().getReference().child("DIY_Methods")
                        .child("category").child("paper");
                userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("DIYs_By_Users").child(userID);
                //generate random unique ID for image to database storage
                String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
                StringBuilder salt = new StringBuilder();
                Random rnd = new Random();
                while (salt.length() < 18) { // length of the random string.
                    int index = (int) (rnd.nextFloat() * SALTCHARS.length());
                    salt.append(SALTCHARS.charAt(index));
                }
                String saltStr = salt.toString();
                //reference to database storage
                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://g-companion.appspot.com/");
                storageReference.child("add_DIY").child("papers").child(saltStr+"").putFile(diyPictureUri).addOnSuccessListener
                        (new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {//get downloadUrl from storage
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                //get text input and save new object
                                DIYrecommend recommend = new DIYrecommend(name.getText().toString(), material.getText().toString()
                                        ,procedure.getText().toString(),taskSnapshot.getDownloadUrl().toString());
                                //assign string upload to database reference
                                String upload = databaseReference.push().getKey();
                                //upload data to database reference
                                databaseReference.child(upload).setValue(recommend);
                                //upload data to DIYs by users
                                userDatabaseReference.child(upload).setValue(recommend);
                                Picasso.with(CaptureDIY.this).load(downloadUrl).into(imgView);
                                //direct to another activity
                                Intent intent = new Intent(CaptureDIY.this,Paper_Recommend.class);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {Toast.makeText(CaptureDIY.this,"Error in uploading!",Toast.LENGTH_SHORT).show();
                                }
                        });
            }
            else if(rubber.isChecked()){
                //reference to DIY_Methods database firebase
                databaseReference = FirebaseDatabase.getInstance().getReference().child("DIY_Methods")
                        .child("category").child("rubber");
                //reference to Diys by users
                userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("DIYs_By_Users").child(userID);
                //generate random unique ID for image to database storage
                String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
                StringBuilder salt = new StringBuilder();
                Random rnd = new Random();
                while (salt.length() < 18) { // length of the random string.
                    int index = (int) (rnd.nextFloat() * SALTCHARS.length());
                    salt.append(SALTCHARS.charAt(index));
                }
                String saltStr = salt.toString();
                //reference to database storage
                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://g-companion.appspot.com/");
                storageReference.child("add_DIY").child("rubbers").child(saltStr+"").putFile(diyPictureUri).addOnSuccessListener
                        (new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //get downloadUrl from storage
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                //get text input and save new object
                                DIYrecommend recommend = new DIYrecommend(name.getText().toString(), material.getText().toString()
                                        ,procedure.getText().toString(),taskSnapshot.getDownloadUrl().toString());
                                //assign string upload to database reference
                                String upload = databaseReference.push().getKey();
                                //upload data to database reference
                                databaseReference.child(upload).setValue(recommend);
                                //upload data to DIYs by users
                                userDatabaseReference.child(upload).setValue(recommend);
                                Picasso.with(CaptureDIY.this).load(downloadUrl).into(imgView);
                                //direct to another activity
                                Intent intent = new Intent(CaptureDIY.this,Rubber_Recommend.class);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CaptureDIY.this,"Error in uploading!",Toast.LENGTH_SHORT).show();
                                }
                });
            }
            else if(wood.isChecked()){
                //reference to database firebase
                databaseReference = FirebaseDatabase.getInstance().getReference().child("DIY_Methods")
                        .child("category").child("wood");
                //reference to DIYs by users
                userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("DIYs_By_Users").child(userID);
                //generate random unique ID for image to database storage
                String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
                StringBuilder salt = new StringBuilder();
                Random rnd = new Random();
                while (salt.length() < 18) { // length of the random string.
                    int index = (int) (rnd.nextFloat() * SALTCHARS.length());
                    salt.append(SALTCHARS.charAt(index));
                }
                String saltStr = salt.toString();
                //reference to database storage
                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://g-companion.appspot.com/");
                storageReference.child("add_DIY").child("woods").child(saltStr+"").putFile(diyPictureUri).addOnSuccessListener
                        (new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //get downloadUrl from storage
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                //get text input and save new object
                                DIYrecommend recommend = new DIYrecommend(name.getText().toString(), material.getText().toString()
                                        ,procedure.getText().toString(),taskSnapshot.getDownloadUrl().toString());
                                //assign string upload to database reference
                                String upload = databaseReference.push().getKey();
                                //upload data to database reference
                                databaseReference.child(upload).setValue(recommend);
                                //upload data to DIYs by users
                                userDatabaseReference.child(upload).setValue(recommend);
                                Picasso.with(CaptureDIY.this).load(downloadUrl).into(imgView);
                                //direct to another activity
                                Intent intent = new Intent(CaptureDIY.this,Wood_Recommend.class);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CaptureDIY.this,"Error in uploading!",Toast.LENGTH_SHORT).show();
                                }
                });
            }
        }
    }

    public void selectImage(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    Toast.makeText(CaptureDIY.this,"Image selected, click on upload button",Toast.LENGTH_SHORT).show();
                    diyPictureUri = data.getData();
                    imgView.setImageURI(diyPictureUri);
                }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir );
        diyPictureUri = Uri.fromFile(image);
        return image;
    }

    @Override
    public void onStart(){
        super.onStart();
    }
}
