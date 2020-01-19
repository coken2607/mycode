package com.example.atn.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.atn.R;
import com.example.atn.curved.CurvedBottomNavigation;
import com.example.atn.fragment.ChatListFragment;
import com.example.atn.fragment.HomeFragment;
import com.example.atn.fragment.ProfileFragment;
import com.example.atn.fragment.UsersFragment;
import com.example.atn.main.MainActivity;
import com.example.atn.notifications.Token;
import com.example.atn.setting.SettingActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import java.util.Objects;

public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    Toolbar toolbar;
    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;
    private UsersFragment usersFragment;
    private ChatListFragment chatListFragment;
    String mUID;
    TextView title, subTitle;
    ImageView menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        title = findViewById(R.id.title);
        subTitle = findViewById(R.id.subTitle);
        menu = findViewById(R.id.setting);
        mAuth = FirebaseAuth.getInstance();
        homeFragment = new HomeFragment();
        profileFragment = new ProfileFragment();
        usersFragment = new UsersFragment();
        chatListFragment = new ChatListFragment();
        setFragment(homeFragment);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, SettingActivity.class));
            }
        });

        title.setText(getResources().getString(R.string.title));
        subTitle.setText(getResources().getString(R.string.subTitle));

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(" ");

        CurvedBottomNavigation bottomNavigationView =
                (CurvedBottomNavigation) findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(selectedListener);

        checkUserStatus();

//        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    private CurvedBottomNavigation.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            setFragment(homeFragment);
                            return true;
                        case R.id.nav_profile:
                            setFragment(profileFragment);
                            return true;
                        case R.id.nav_chat:
                            setFragment(chatListFragment);
                            return true;
                        case R.id.nav_users:
                            setFragment(usersFragment);
                            return true;
                        default:
                            return false;
                    }
                }
            };

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment);
        fragmentTransaction.commit();
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        reference.child(mUID).setValue(mToken);
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mUID = user.getUid();
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();
        } else {
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }
}
