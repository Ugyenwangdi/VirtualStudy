package com.boilerplate.firestore.virtualstudy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;

    private FloatingActionButton addPostBtn;

    private String current_user_id;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private BottomNavigationView mainBottomNav;
    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mainToolbar.setTitle("");
        mainToolbar.setSubtitle("");
       /* getSupportActionBar().setTitle("Photo Blog");*/

        mainBottomNav = findViewById(R.id.mainBottomNav);

        homeFragment = new HomeFragment();
        notificationFragment = new NotificationFragment();
        accountFragment = new AccountFragment();

        initializeFragment();

        if (mAuth.getCurrentUser() != null) {

            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_container);

                    switch (item.getItemId()) {

                        case R.id.bottom_action_home:
                            replaceFragment(homeFragment, currentFragment);
                            return true;
                        case R.id.bottom_action_notification:
                            replaceFragment(notificationFragment, currentFragment);
                            return true;
                        case R.id.bottom_action_account:
                            replaceFragment(accountFragment, currentFragment);
                            return true;
                        default:
                            return false;
                    }
                }
            });

            addPostBtn = findViewById(R.id.add_post_btn);
            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent postIntent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(postIntent);

                }
            });

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {

            sendToLogin();

        } else {

            current_user_id = mAuth.getCurrentUser().getUid();

            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {

                        if (!task.getResult().exists()) {

                            Intent setUpIntent = new Intent(MainActivity.this, SetupActivity.class);
                            setUpIntent.putExtra("Display_arrow", "no");
                            startActivity(setUpIntent);
                            finish();

                        }

                    } else {

                        Toasty.error(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG, true).show();
                    }

                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_logout_btn:
                logOut();
                return true;
            case R.id.action_settings_btn:

                Intent settingsIntent = new Intent(MainActivity.this, SetupActivity.class);
                settingsIntent.putExtra("Display_arrow", "yes");
                startActivity(settingsIntent);

                return true;
            case R.id.action_chnage_pass_btn:
                Intent change_passIntent = new Intent(MainActivity.this, ChangePasswordActivity.class);
                startActivity(change_passIntent);

                return true;

            case R.id.action_feedback:
                Intent Feedback = new Intent(MainActivity.this, Feedback.class);
                startActivity(Feedback);

                return true;
            case R.id.action_refresh:
                Intent Refresh = new Intent(MainActivity.this, MainActivity.class);
                startActivity(Refresh);

                return true;

            default:
                return false;
        }
    }

    private void searchData(String s) {

    }

    private void logOut() {

        mAuth.signOut();

        sendToLogin();

    }

    private void sendToLogin() {

        Intent intent = new Intent(MainActivity.this, loginActivity.class);
        startActivity(intent);
        finish();

    }

    private void initializeFragment() {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.main_container, homeFragment);
        fragmentTransaction.add(R.id.main_container, notificationFragment);
        fragmentTransaction.add(R.id.main_container, accountFragment);

        fragmentTransaction.hide(notificationFragment);
        fragmentTransaction.hide(accountFragment);

        fragmentTransaction.commit();

    }

    private void replaceFragment(Fragment fragment, Fragment currentFragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(fragment == homeFragment){

            fragmentTransaction.hide(accountFragment);
            fragmentTransaction.hide(notificationFragment);

        }

        if(fragment == accountFragment){

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(notificationFragment);

        }

        if(fragment == notificationFragment){

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(accountFragment);

        }
        fragmentTransaction.show(fragment);

        //fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

    }


}
