package nz.co.xtra.smith.mansill;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Sets up the on click method for the Add New USB button
        Button btnAddNewUSB= (Button) findViewById(R.id.btnAddNewUSB);
        btnAddNewUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditUSB();
            }
        });

    }

    //Opens the edit USB activity
    public void openEditUSB(){
        Intent intent = new Intent(this, EditUSB.class);
        startActivity(intent);
    }
}
