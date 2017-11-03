package cabiso.daphny.com.g_companion;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cabiso.daphny.com.g_companion.Model.CommunityItem;
import cabiso.daphny.com.g_companion.Model.DIYnames;
import cabiso.daphny.com.g_companion.Recommend.RecommendDIYAdapter;

/**
 * Created by Lenovo on 11/2/2017.
 */

public class DIYDetailViewActivity extends AppCompatActivity {

    private ArrayList<CommunityItem> infoList = new ArrayList<>();
    private ListView lv;
    private RecommendDIYAdapter adapter;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;

    private TextView diy_name, diy_materials, diy_procedures;
    private ImageView diy_image;
    private String userID;

    private DIYImagesViewPagerAdapter diyImagesViewPagerAdapter;
    private ViewPager diyImagesViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diy_data);

        String diyReferenceString = getIntent().getStringExtra("Community Ref");

        databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(diyReferenceString);


        progressDialog = new ProgressDialog(this);
        diy_name = (TextView) findViewById(R.id.diy_name);
        diy_image = (ImageView) findViewById(R.id.diy_image);
        diy_materials = (TextView) findViewById(R.id.diy_materials);
        diy_procedures = (TextView) findViewById(R.id.diy_procedures);


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DIYnames diyInfo = dataSnapshot.getValue(DIYnames.class);
                diy_name.setText(diyInfo.diyName);

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        CommunityItem communityItem = dataSnapshot.getValue(CommunityItem.class);
                        diy_materials.setText(communityItem.getVal());
                        diy_procedures.setText(communityItem.getVal());

                        Toast.makeText(DIYDetailViewActivity.this, communityItem.getVal() , Toast.LENGTH_SHORT).show();

                        if(communityItem != null){
                            byte[] dec = Base64.decode(String.valueOf(communityItem),Base64.DEFAULT);
                            Bitmap decodeByte = BitmapFactory.decodeByteArray(dec,0,dec.length);
                            diy_image.setImageBitmap(decodeByte);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                if (diyInfo.diyUrl != null) {
                    diyImagesViewPager = (ViewPager) findViewById(R.id.diyImagesViewPager);
                    diyImagesViewPagerAdapter = new DIYImagesViewPagerAdapter(getBaseContext(), diyInfo.diyUrl);
                    diyImagesViewPager.setAdapter(diyImagesViewPagerAdapter);

                Toast.makeText(DIYDetailViewActivity.this, diyInfo.diyUrl, Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }




    private class DIYImagesViewPagerAdapter extends PagerAdapter {

        private Context context;
        private String diyUrl;

        public DIYImagesViewPagerAdapter(Context context, String diyUrl) {
            this.context = context;
            this.diyUrl = diyUrl;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            View currentView = LayoutInflater.from(context).inflate(R.layout.viewpager_product_images, container, false);
            final ImageView productImageView = (ImageView) currentView.findViewById(R.id.viewpager_productImage);
            try {
                StorageReference diyImageStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://g-companion.appspot.com/").child("diy_by_tags");
                diyImageStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("Image Download URI", uri.toString());
                        Picasso.with(context).load(uri.getLastPathSegment()).resize(350,350).into(productImageView);
                        //Glide.with(context).load(uri.toString()).into(diy_image);

                    }
                });
            } catch (Exception e) {
                Log.d("Exception", "Getting diy image");
            }
            container.addView(currentView);
             return currentView;
        }


        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return diyUrl.length();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return false;
        }
    }

}