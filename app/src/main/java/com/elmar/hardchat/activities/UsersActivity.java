package com.elmar.hardchat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.elmar.hardchat.adapters.UsersAdapter;
import com.elmar.hardchat.databinding.ActivityUsersBinding;
import com.elmar.hardchat.models.User;
import com.elmar.hardchat.utilities.Constants;
import com.elmar.hardchat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            users.add(user);
                        }
                        if(users.size() > 0){
                            UsersAdapter usersAdapter = new UsersAdapter(users);
                            binding.usersRecyclyerView.setAdapter(usersAdapter);
                            binding.usersRecyclyerView.setVisibility(View.VISIBLE);
                        }else{
                            showErrorMessage();
                        }
                    }else{
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}