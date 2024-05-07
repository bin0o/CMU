package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends ComponentActivity {

    private final String TAG = "SignUpActivity";
    private EditText editTextUsername, editTextPassword, editTextConfirmPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Go back to Welcome page
        Button back = findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Going back to Welcome Page");
                Intent intent = new Intent(SignUpActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        editTextUsername = findViewById(R.id.signup_username);
        editTextPassword = findViewById(R.id.signup_password);
        editTextConfirmPassword = findViewById(R.id.confirm_password);

        Button buttonSignup = findViewById(R.id.signup_button);

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username, password, confirmPassword;

                username = editTextUsername.getText().toString();
                password = editTextPassword.getText().toString();
                confirmPassword = editTextConfirmPassword.getText().toString();

                if(TextUtils.isEmpty(username)){
                    Toast.makeText(SignUpActivity.this, "Please enter username!",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    Toast.makeText(SignUpActivity.this, "Please enter password!",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!TextUtils.equals(password,confirmPassword)){
                    Toast.makeText(SignUpActivity.this, "Passwords don't match!",Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(username, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                try {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignUpActivity.this, "Your account has been created!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignUpActivity.this, HomePageActivity.class);
                                        startActivity(intent);
                                    } else {
                                        throw task.getException();
                                    }
                                } catch (Exception e) {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(SignUpActivity.this, "Failed to create account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });


            }
        });
    }
}
