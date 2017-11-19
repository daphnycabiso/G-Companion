package cabiso.daphny.com.g_companion.Recommend;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cabiso.daphny.com.g_companion.DIYDataActivity;
import cabiso.daphny.com.g_companion.DIYDetailViewActivity;
import cabiso.daphny.com.g_companion.MainActivity;
import cabiso.daphny.com.g_companion.Model.CommunityItem;
import cabiso.daphny.com.g_companion.Model.DIYnames;
import cabiso.daphny.com.g_companion.R;

/**
 * Created by Lenovo on 7/31/2017.
 */

public class Bottle_Recommend extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private DatabaseReference communityReference;

    private ArrayList<DIYnames> diyList = new ArrayList<>();
    private ArrayList<CommunityItem> infoList = new ArrayList<>();

    private ListView lv;
    private ImageView loadview;
    private RecommendDIYAdapter adapter;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;

   // ImageButton star;

    private FirebaseDatabase database;
    private String userID;
    private FirebaseUser mFirebaseUser;
    //ArrayList<CommunityItem> itemMaterial;
    private List<String> tags = new ArrayList<>();
    private Activity context;


    public Bottle_Recommend() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend_bottle);
        recyclerView = (RecyclerView) findViewById(R.id.list);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = mFirebaseUser.getUid();
        database = FirebaseDatabase.getInstance();
        lv = (ListView) findViewById(R.id.recommendLvView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait loading DIYs.....");
        progressDialog.show();

        final String intent = getIntent().getStringExtra("result_tag");

        final String data = getIntent().getStringExtra("result_tag");
        final String[] items = data.split(" ");
        for (final String item : items) {
            Log.e("itemsMASO: ",""+item);

            final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("diy_by_tags");
                myRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                    if (dataSnapshot.hasChildren()){
                        if (item != null) {
                            DIYnames diYnames = dataSnapshot.getValue(DIYnames.class);
                            DataSnapshot commu_snapshot = dataSnapshot.child("materials");


                            Log.e("item_materials: ",""+dataSnapshot.child("materials").getValue());
                            Log.e("commu_snapshot: ",""+commu_snapshot);


                            //    Toast.makeText(Bottle_Recommend.this, "item " + item + "\n" + "tagdb" + diYnames.getTag(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            if (item.equals(diYnames.getDiyName())) {
                                // if(diYnames.getBookmarks()!=null && diYnames.getLikes()!=null){
                                diyList.add(diYnames);
                                Collections.sort(diyList);
                                Collections.reverse(diyList);
                            }
                        }
//                    }

                        adapter = new RecommendDIYAdapter(Bottle_Recommend.this, R.layout.pending_layout, diyList);
                        lv.setAdapter(adapter);

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                Toast.makeText(getApplicationContext(), diyList.get(position).getDiyName(), Toast.LENGTH_SHORT).show();
                                DIYnames selectedItem = adapter.getItem(position);

                                Toast.makeText(getApplicationContext(), "chiii "+position, Toast.LENGTH_SHORT).show();


//                                //To-DO get you data from the ItemDetails Getter
//                                // selectedItem.getImage() or selectedItem.getName() .. etc
//                                // the  send the data using intent when opening another activity
                                Intent intent = new Intent(Bottle_Recommend.this, DIYDataActivity.class);
                                intent.putExtra("pos",position);
                                intent.putExtra("image", selectedItem.getDiyUrl().getBytes());
                                intent.putExtra("name", selectedItem.getDiyName());

                                Toast.makeText(getApplicationContext(), "piste "+selectedItem,Toast.LENGTH_SHORT).show();
                                Toast.makeText(getApplicationContext(), "piste@@@@ "+position,Toast.LENGTH_SHORT).show();
                                Log.e("communityItem: ", ""+position);

                                view.buildDrawingCache();
                                Bitmap image = view.getDrawingCache();
                                Bundle extras = new Bundle();
                                extras.putParcelable("imagebitmap", image);

                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                byte[] byteArray = stream.toByteArray();
                                intent.putExtra("image", byteArray);
                                startActivity(intent);
                            }
                        });


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

//        }
        }
    }



    public void sort_to_recommend(){
        DatabaseReference myRef = database.getReference("dy_by_tags");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Bottle_Recommend.this, MainActivity.class);
        startActivity(intent);
    }


    public void onListFragmentInteractionListener(DatabaseReference ref) {
        Intent intent = new Intent(this, DIYDetailViewActivity.class);
        intent.putExtra("Community Ref", ref.toString());
        startActivity(intent);
    }
}