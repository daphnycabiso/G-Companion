package cabiso.daphny.com.g_companion;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by Lenovo on 7/31/2017.
 */


public class Wishlists extends AppCompatActivity {

    private ImageButton imBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        imBack = (ImageButton) findViewById(R.id.ibBlack);

        imBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Wishlists.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
