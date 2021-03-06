package cabiso.daphny.com.g_companion;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cabiso.daphny.com.g_companion.Adapter.CommunityAdapter;
import cabiso.daphny.com.g_companion.Model.CommunityItem;
import cabiso.daphny.com.g_companion.Model.Constants;
import cabiso.daphny.com.g_companion.Model.DBMaterial;
import cabiso.daphny.com.g_companion.Model.DIYSell;
import cabiso.daphny.com.g_companion.Model.DIYnames;
import cabiso.daphny.com.g_companion.Model.QuantityItem;
import cabiso.daphny.com.g_companion.Model.TagClass;
import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;

/**
 * Created by Lenovo on 8/22/2017.
 */


public class CaptureDIY extends AppCompatActivity implements View.OnClickListener{

    private DatabaseReference databaseReference;
    private DatabaseReference dbRef;
    private Task<Void> materialsReference;
    private Task<Void> proceduresReference;
    private FirebaseDatabase database;
    private FirebaseAuth mFirebaseAuth;

    private FirebaseStorage mStorage;
    private StorageReference storageReference, imageRef;

    private StorageReference storageRef, imgRef;


    private FirebaseUser mFirebaseUser;
    private String userID;
    private String imageFileName;

    private Uri diyPictureUri;

    static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 11;
    private List<String> tags = new ArrayList<>();
    private List<String> extras = new ArrayList<>();
    private List<String> validWords = new ArrayList<>();

    final ClarifaiClient client;

    public CaptureDIY() {
        client = new ClarifaiBuilder("cb169e9d3f9e4ec5a7769cc0422f3162").buildSync();
    }

    private static final int SELECT_PHOTO = 100;
    private static final int MAX_LENGTH = 100;
    private Button submitButton, sellButton;
    private ImageButton btnAddMaterial, btnAddProcedure;
    private TagView tagGroup;
    private EditText name, material, procedure;
    private ImageView imgView;
    private ListView materialsList;
    private ListView proceduresList;
    private CommunityAdapter pAdapter;
    private CommunityAdapter mAdapter;
    ArrayList<CommunityItem> itemMaterial;
    ArrayList<CommunityItem> itemMat;
    ArrayList<CommunityItem> itemForMaterials;
    ArrayList<CommunityItem> itemProcedure;
    ArrayList<QuantityItem> itemQuantity;
    ArrayList<DBMaterial> dbMaterials;
    ArrayList<QuantityItem> itemUnit;
    private List<String> diys = new ArrayList<String>();

    private ProgressDialog progressDialog;
    UploadTask uploadTask;

    private ArrayList<TagClass> tagList;
    String[] unitOfMeasurement;
    String[] quantity;
    String spinner_item_um;
    String spinner_item_q;
    SpinnerAdapter umAdapter;
    SpinnerAdapter1 qAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_diy);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = mFirebaseUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("diy_by_tags");

        dbRef = FirebaseDatabase.getInstance().getReference().child("Sell DIY");

        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl("gs://g-companion.appspot.com/").child("diy_by_tags");

        storageRef = mStorage.getReferenceFromUrl("gs://g-companion.appspot.com/").child("Sell DIY");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarAddDIY);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationIcon(R.drawable.back_btn);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent im = new Intent(CaptureDIY.this,MainActivity.class);
                startActivity(im);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        name = (EditText) findViewById(R.id.add_diy_name);

        material = (EditText) findViewById(R.id.etMaterials);
        tagGroup = (TagView) findViewById(R.id.tag_group);

        unitOfMeasurement = getResources().getStringArray(R.array.UM);
        umAdapter=new SpinnerAdapter(getApplicationContext());

        quantity = getResources().getStringArray(R.array.qty);
        qAdapter=new SpinnerAdapter1(getApplicationContext());


        material.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setTags(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tagGroup.setOnTagLongClickListener(new TagView.OnTagLongClickListener() {
            @Override
            public void onTagLongClick(Tag tag, int position) {
                Toast.makeText(CaptureDIY.this, "Long Click: " + tag.text, Toast.LENGTH_SHORT).show();
            }
        });

        tagGroup.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(Tag tag, int position) {
                material.setText(tag.text);
                material.setSelection(tag.text.length());//to set cursor position

            }
        });
        tagGroup.setOnTagDeleteListener(new TagView.OnTagDeleteListener() {

            @Override
            public void onTagDeleted(final TagView view, final Tag tag, final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CaptureDIY.this, R.style.AppTheme);
                builder.setMessage("\"" + tag.text + "\" will be delete. Are you sure?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        view.remove(position);
                        Toast.makeText(CaptureDIY.this, "\"" + tag.text + "\" deleted", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();

            }
        });


        procedure = (EditText) findViewById(R.id.etProcedures);
        imgView = (ImageView) findViewById(R.id.add_product_image_plus_icon);
        materialsList = (ListView) findViewById(R.id.materialsList);
        proceduresList = (ListView) findViewById(R.id.proceduresList);
        btnAddMaterial = (ImageButton) findViewById(R.id.btnMaterial);
        btnAddProcedure = (ImageButton) findViewById(R.id.btnProcedure);

        itemMaterial = new ArrayList<>();
        itemProcedure = new ArrayList<>();
        itemUnit = new ArrayList<>();
        itemMat = new ArrayList<>();
        itemQuantity = new ArrayList<>();
        dbMaterials = new ArrayList<>();

        mAdapter = new CommunityAdapter(getApplicationContext(), itemMaterial);
        pAdapter = new CommunityAdapter(getApplicationContext(), itemProcedure);

        materialsList.setAdapter(mAdapter);
        proceduresList.setAdapter(pAdapter);

        String[] values = new String[] { "Quantity" };

        getWordBank();
        prepareTags();

// ADD DIY TO THE COMMUNITY
        btnAddMaterial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(CaptureDIY.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.row_spinner);
                dialog.setCancelable(true);

                final Spinner spinner1 = (Spinner) dialog.findViewById(R.id.unitspinner);
                final Spinner spinner2 = (Spinner) dialog.findViewById(R.id.qtySpinner);
                Button okButton = (Button) dialog.findViewById(R.id.okaybtn);

                spinner1.setAdapter(umAdapter);
                spinner2.setAdapter(qAdapter);


                spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // TODO Auto-generated method stub
                        spinner_item_um = unitOfMeasurement[position];

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // TODO Auto-generated method stub

                    }
                });

                spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // TODO Auto-generated method stub
                        spinner_item_q = quantity[position];

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // TODO Auto-generated method stub

                    }
                });

                String inputMaterials = material.getText().toString();
                if (inputMaterials.isEmpty()) {
                    Toast.makeText(CaptureDIY.this, "Please enter materials", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CaptureDIY.this, spinner_item_q + spinner_item_um, Toast.LENGTH_SHORT).show();
                }

                dialog.show();

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String quantityMaterials = spinner_item_q + " " + spinner_item_um + " " + material.getText().toString();
//                        String qty_uni_mat = spinner_item_q + " " + spinner_item_um ;
                        String unit_material = spinner_item_um;
                        int quantity = Integer.parseInt(spinner_item_q);
                        String materials = material.getText().toString();

                        CommunityItem qm = new CommunityItem(quantityMaterials);
                        CommunityItem mat = new CommunityItem(materials);

                        QuantityItem qty_qty_mat = new QuantityItem(quantity);
                        QuantityItem _unit = new QuantityItem(unit_material);
                        itemMaterial.add(qm);

                        dbMaterials.add(new DBMaterial().setName(materials).setQuantity(quantity).setUnit(unit_material));

                        itemQuantity.add(qty_qty_mat);
                        itemUnit.add(_unit);

                        itemMat.add(mat);

                        mAdapter.notifyDataSetChanged();
                        material.setText("");

                        Toast.makeText(CaptureDIY.this, spinner_item_q + " " +spinner_item_um, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                    }
                });

            }
        });

        btnAddProcedure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputProcedure = procedure.getText().toString();
                if (inputProcedure.isEmpty()) {
                    Toast.makeText(CaptureDIY.this, "Please enter procedures", Toast.LENGTH_SHORT).show();
                } else {
                    CommunityItem md = new CommunityItem(inputProcedure);
                    itemProcedure.add(md);
                    pAdapter.notifyDataSetChanged();
                    procedure.setText(" ");

                }
            }
        });
        final ImageView addProductImagePlusIcon = (ImageView) findViewById(R.id.add_product_image_plus_icon);
        addProductImagePlusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        submitButton = (Button) findViewById(R.id.communityDiy);
        database = FirebaseDatabase.getInstance();
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                imageRef = storageReference.child(diyPictureUri.getLastPathSegment());

                //creating and showing progress dialog
                progressDialog = new ProgressDialog(CaptureDIY.this);
                progressDialog.setMax(100);
                progressDialog.setMessage("Adding DIY to the Community...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();
                progressDialog.setCancelable(false);

                //starting upload
                uploadTask = imageRef.putFile(diyPictureUri);
                // Observe state change events such as progress, pause, and resume
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        //sets and increments value of progressbar
                        progressDialog.incrementProgressBy((int) progress);
                    }
                });
                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(CaptureDIY.this, "Error in uploading!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Float float_this = Float.valueOf(0);

                        String upload = databaseReference.push().getKey();

                        Random random = new Random();
                        String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
                        String productID = String.valueOf(candidateChars.charAt(random.nextInt(candidateChars.length())));

                        //push data to Firebase Database
                        databaseReference = FirebaseDatabase.getInstance().getReference().child("diy_by_tags");

                        databaseReference.child(upload).setValue(new DIYnames(name.getText().toString(),
                                taskSnapshot.getDownloadUrl().toString(), userID, "prod_000"+ productID,
                                float_this, float_this));

//                        databaseReference.child(upload).child("materials").setValue(itemMat);
                        databaseReference.child(upload).child("materials").setValue(dbMaterials);
                        databaseReference.child(upload).child("quantity_unit").child("quantity").setValue(itemQuantity);
                        databaseReference.child(upload).child("quantity_unit").child("unit").setValue(itemUnit);
                        databaseReference.child(upload).child("procedures").setValue(itemProcedure);

                        Toast.makeText(CaptureDIY.this, "Upload successful", Toast.LENGTH_SHORT).show();

                        // Alert Dialog for finished uploaing DIYs
                        AlertDialog.Builder ab = new AlertDialog.Builder(CaptureDIY.this, R.style.MyAlertDialogStyle);
                        ab.setMessage("Thank you for contributing to the DIY Community!");
                        ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent in = new Intent(CaptureDIY.this, MainActivity.class);
                                startActivity(in);
                            }
                        });

                        ab.create().show();
                        progressDialog.dismiss();
                    }
                });

            }
        });


        //SELL DIY TO THE COMMUNITY
        sellButton = (Button) findViewById(R.id.sellDiy);
        sellButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(CaptureDIY.this, R.style.MyAlertDialogStyle);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.sell_diy_option);
                dialog.setCancelable(true);

                final EditText etPrice = (EditText) dialog.findViewById(R.id.etPrice);
                final EditText etQuantity = (EditText) dialog.findViewById(R.id.etQty);
                final EditText etDescription = (EditText) dialog.findViewById(R.id.etDescription);
                Button okButton = (Button) dialog.findViewById(R.id.okaybtn);

                dialog.show();

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(CaptureDIY.this, etPrice.getText() + "," + etQuantity.getText() + "," + etDescription.getText(),
                                Toast.LENGTH_SHORT).show();

                        imgRef = storageRef.child(diyPictureUri.getLastPathSegment());

                        //creating and showing progress dialog
                        progressDialog = new ProgressDialog(CaptureDIY.this);
                        progressDialog.setMax(100);
                        progressDialog.setMessage("Adding DIY to the Community...");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.show();
                        progressDialog.setCancelable(false);

                        //starting upload
                        uploadTask = imgRef.putFile(diyPictureUri);
                        // Observe state change events such as progress, pause, and resume
                        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                //sets and increments value of progressbar
                                progressDialog.incrementProgressBy((int) progress);
                            }
                        });
                        // Register observers to listen for when the download is done or if it fails
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Toast.makeText(CaptureDIY.this, "Error in uploading!", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                Float float_this = Float.valueOf(0);

                                String upload = dbRef.push().getKey();

                                Random random = new Random();
                                String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
                                String productID = String.valueOf(candidateChars.charAt(random.nextInt(candidateChars.length())));

                                //push data to Firebase Database
                                dbRef = FirebaseDatabase.getInstance().getReference().child("Sell DIY");

                                dbRef.child(upload).setValue(new DIYSell(name.getText().toString(),
                                        taskSnapshot.getDownloadUrl().toString(), userID, "prod_000"+ productID,
                                        float_this, float_this));

                                dbRef.child(upload).child("materials").setValue(itemMaterial);
                                dbRef.child(upload).child("procedures").setValue(itemProcedure);
                                dbRef.child(upload).child("DIY Price").setValue(etPrice.getText().toString());

                                dbRef.child(upload).child("Item Quantity").setValue(etQuantity.getText().toString());

                                dbRef.child(upload).child("Item Description").setValue(etDescription.getText().toString());

                                Toast.makeText(CaptureDIY.this, "Upload successful", Toast.LENGTH_SHORT).show();

                                // Alert Dialog for finished uploaing DIYs
                                AlertDialog.Builder ab = new AlertDialog.Builder(CaptureDIY.this, R.style.MyAlertDialogStyle);
                                ab.setMessage("Thank you for contributing to the DIY Community!");
                                ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent in = new Intent(CaptureDIY.this, MainActivity.class);
                                        startActivity(in);
                                    }
                                });

                                ab.create().show();
                                progressDialog.dismiss();
                            }
                        });

                        dialog.dismiss();

                    }
                });


            }
        });

    }

    public class SpinnerAdapter extends BaseAdapter {
        Context context;
        private LayoutInflater mInflater;

        public SpinnerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return unitOfMeasurement.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ListContent holder;
            View v = convertView;
            if (v == null) {
                mInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                v = mInflater.inflate(R.layout.row_textview, null);
                holder = new ListContent();
                holder.text = (TextView) v.findViewById(R.id.textView1);

                v.setTag(holder);
            } else {
                holder = (ListContent) v.getTag();
            }
            holder.text.setText(unitOfMeasurement[position]);
            return v;
        }
    }

    public class SpinnerAdapter1 extends BaseAdapter {
        Context context;
        private LayoutInflater mInflater;

        public SpinnerAdapter1(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return quantity.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ContentQty holder1;
            View vi = convertView;
            if (vi == null) {
                mInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                vi = mInflater.inflate(R.layout.row_textview, null);
                holder1 = new ContentQty();
                holder1.text1 = (TextView) vi.findViewById(R.id.textView1);

                vi.setTag(holder1);
            } else {
                holder1 = (ContentQty) vi.getTag();
            }
            holder1.text1.setText(quantity[position]);
            return vi;
        }
    }


    static class ListContent {
        TextView text;
    }

    static class ContentQty {
        TextView text1;
    }

    //Materials Tags
    private void prepareTags() {
        tagList = new ArrayList<>();
        JSONArray jsonArray;
        JSONObject temp;
        try {
            jsonArray = new JSONArray(Constants.MATERIALS);
            for (int i = 0; i < jsonArray.length(); i++) {
                temp = jsonArray.getJSONObject(i);
                tagList.add(new TagClass(temp.getString("code"), temp.getString("name")));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setTags(CharSequence cs) {
        /**
         * for empty edittext
         */
        if (cs.toString().equals("")) {
            tagGroup.addTags(new ArrayList<Tag>());
            return;
        }

        String text = cs.toString();
        ArrayList<Tag> tags = new ArrayList<>();
        Tag tag;

        for (int i = 0; i < tagList.size(); i++) {
            if (tagList.get(i).getName().toLowerCase().startsWith(text.toLowerCase())) {
                tag = new Tag(tagList.get(i).getName());
                tag.radius = 10f;
                tag.layoutColor = Color.parseColor(tagList.get(i).getColor());
                if (i % 2 == 0) // you can set deletable or not
                    tag.isDeletable = true;
                tags.add(tag);
            }
        }
        tagGroup.addTags(tags);

    }



    //Quantity Chooser
    private void AlertDialogView() {
        final CharSequence[] items = { "1pc", "2pcs", "3pcs", "4pcs", "5pcs", "6pcs", "7pcs",
                "8pcs", "9pcs", "10pcs"};

//        final CharSequence[] unitOfMeasurement = { "meters", "centimeters", "liters", "milligrams ", "grams", "kilograms", "inches ",
//                "feet", "square inches", "gallon"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CaptureDIY.this);
        builder.setTitle("Quantity of material: ");
        builder.setSingleChoiceItems(unitOfMeasurement, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String quantityMaterial = unitOfMeasurement[item] + " " + material.getText().toString();

                        CommunityItem qm = new CommunityItem(quantityMaterial);
                        itemMaterial.add(qm);
                        mAdapter.notifyDataSetChanged();
                        material.setText(" ");

                        Toast.makeText(getApplicationContext(), items[item] + " " + material.getText().toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(CaptureDIY.this, "Material added.", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(CaptureDIY.this, "Fail", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onClick(View v) {
        if(v==submitButton){
            registerForContextMenu(submitButton);
            openContextMenu(submitButton);
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v.getId()==R.id.add_submit_diy){
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }


    public void getWordBank(){
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("word_bank");
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                validWords.add(dataSnapshot.getValue().toString());
                Log.d("fsdfs", dataSnapshot.getValue().toString());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Toast.makeText(CaptureDIY.this, "Capture DIY!", Toast.LENGTH_SHORT).show();
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == MainActivity.RESULT_OK) {

                diyPictureUri = data.getData();
                if(diyPictureUri==null){
                    imgView.setImageURI(diyPictureUri);
                    Toast.makeText(this, "NULL PICTURE", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "NOT NULL BESH ", Toast.LENGTH_SHORT).show();
                }


                Bitmap bmp = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                // convert byte array to Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0,
                        byteArray.length);
                imgView.setImageBitmap(bitmap);

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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CaptureDIY.this, MainActivity.class);
        startActivity(intent);
    }

}
