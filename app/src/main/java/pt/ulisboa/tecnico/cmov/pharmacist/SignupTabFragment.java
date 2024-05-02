package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupTabFragment extends Fragment {

    private EditText editTextUsername, editTextPassword, editTextConfirmPassword;

    private View view;

    private Button buttonSignup;

    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_signup_tab, container, false);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        editTextUsername = view.findViewById(R.id.signup_username);
        editTextPassword = view.findViewById(R.id.signup_password);
        editTextConfirmPassword = view.findViewById(R.id.confirm_password);

        buttonSignup = view.findViewById(R.id.signup_button);

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username, password, confirmPassword;

                username = editTextUsername.getText().toString();
                password = editTextPassword.getText().toString();
                confirmPassword = editTextConfirmPassword.getText().toString();

                if(TextUtils.isEmpty(username)){
                    Toast.makeText(getActivity(), "Please enter username!",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    Toast.makeText(getActivity(), "Please enter password!",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!TextUtils.equals(password,confirmPassword)){
                    Toast.makeText(getActivity(), "Password don't match!",Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(username, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                try {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getActivity(), "Your account has been created!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        throw task.getException();
                                    }
                                } catch (Exception e) {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(getActivity(), "Failed to create account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });


            }
        });

        return view;
    }
}