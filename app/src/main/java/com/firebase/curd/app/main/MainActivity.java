package com.firebase.curd.app.main;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.curd.app.R;
import com.firebase.curd.app.support.UserDataAdapter;
import com.firebase.curd.app.support.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static DatabaseReference databaseRefObject;

    public static final int TYPE_USER_DETAILS=1;
    private static ArrayList<UserModel> userData;
    private static UserDataAdapter userDataAdapter;
    private static List<Object> adapterList;
    private static String userId = "";
    private static String strMsg = "";

    private static RecyclerView recyclerView;
    private static TextView txtMessage;
    private static LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        databaseRefObject = FirebaseDatabase.getInstance().getReference(getString(R.string.tagUserTable));

        txtMessage = (TextView)findViewById(R.id.txtMessage);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        // Set Layout Manager
        linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        // Limiting the size
        recyclerView.setHasFixedSize(true);

        findViewById(R.id.btnAddUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUpdateUserDialog(MainActivity.this,true,0);
            }
        });
        getUserList(MainActivity.this);
    }

    //Used to create new user and update user in Firebase RealTime Database
    public static void createUpdateUserDialog(final Context context, boolean isAdd, int position){
        try{
            userId = "";
            strMsg = "";
            final Dialog dialog = new Dialog(context,R.style.full_screen_dialog);
            dialog.setContentView(R.layout.add_edit_dialog);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();

            wlp.gravity = Gravity.CENTER;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
            window.setAttributes(wlp);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

            // set values for custom dialog components
            TextView txtHeader = (TextView) dialog.findViewById(R.id.txtHeader);
            final EditText edtUserName = (EditText) dialog.findViewById(R.id.txtUserName);
            final EditText edtUserMobile = (EditText) dialog.findViewById(R.id.txtUserMobile);
            final EditText edtUserEmail = (EditText) dialog.findViewById(R.id.txtUserEmail);
            final EditText edtUserPassword = (EditText) dialog.findViewById(R.id.txtUserPassword);
            Button btnSave=(Button) dialog.findViewById(R.id.btnSave);
            if(isAdd){
                txtHeader.setText(context.getString(R.string.tagHeadingAdd));
                btnSave.setText(context.getString(R.string.action_save));
                userId  = databaseRefObject.push().getKey();
                strMsg = context.getString(R.string.tagUserAddedMsg);
            } else {
                txtHeader.setText(context.getString(R.string.tagHeadingUpdate));
                btnSave.setText(context.getString(R.string.action_update));
                UserModel userModel = userData.get(position);
                edtUserName.setText(userModel.getUserName());
                edtUserMobile.setText(userModel.getUserMobile());
                edtUserEmail.setText(userModel.getUserEmail());
                edtUserPassword.setText(userModel.getUserPassword());
                userId = userModel.getUserID();
                strMsg = context.getString(R.string.tagUserUpdateMsg);
            }
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{
                        boolean isFillAllDetails = false;
                        View focusView = null;

                        // Check for a valid Password
                        if (TextUtils.isEmpty(edtUserPassword.getText().toString().trim())) {
                            edtUserPassword.setError(context.getString(R.string.emptyString));
                            focusView = edtUserPassword;
                            isFillAllDetails = true;
                        } else if (edtUserPassword.getText().toString().trim().length()<6) {
                            edtUserPassword.setError(context.getString(R.string.errorPassLength));
                            focusView = edtUserPassword;
                            isFillAllDetails = true;
                        }

                        // Check for a valid email address.
                        if (TextUtils.isEmpty(edtUserEmail.getText().toString().trim())) {
                            edtUserEmail.setError(context.getString(R.string.emptyString));
                            focusView = edtUserEmail;
                            isFillAllDetails = true;
                        } else if (!isEmailValid(edtUserEmail.getText().toString().trim())) {
                            edtUserEmail.setError(context.getString(R.string.errorInvalidEmail));
                            focusView = edtUserEmail;
                            isFillAllDetails = true;
                        }

                        // Check for a valid Mobile
                        if (TextUtils.isEmpty(edtUserMobile.getText().toString().trim())) {
                            edtUserMobile.setError(context.getString(R.string.emptyString));
                            focusView = edtUserMobile;
                            isFillAllDetails = true;
                        } else if (!hasMobileText(edtUserMobile)) {
                            edtUserMobile.setError(context.getString(R.string.errorValidMobile));
                            focusView = edtUserMobile;
                            isFillAllDetails = true;
                        }

                        // Check for a valid UserName
                        if (TextUtils.isEmpty(edtUserName.getText().toString().trim())) {
                            edtUserName.setError(context.getString(R.string.emptyString));
                            focusView = edtUserName;
                            isFillAllDetails = true;
                        }


                        if (isFillAllDetails) {
                            focusView.requestFocus();
                        } else {
                            // creating user object
                            UserModel userData = new UserModel("",
                                    edtUserName.getText().toString().trim(),
                                    edtUserMobile.getText().toString().trim(),
                                    edtUserEmail.getText().toString().trim(),
                                    edtUserPassword.getText().toString().trim());

                            // pushing user to 'user_details' node using the userId
                            databaseRefObject.child(userId).setValue(userData);
                            getUserList(context);
                            dialog.dismiss();
                            Toast.makeText(context,strMsg,Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){

                    }
                }
            });

            dialog.show();

        }catch (Exception e){
        }
    }

    //Read all user list from FireBase database
    public static void getUserList(final Context context){
        try{
            databaseRefObject.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Get map of users in datasnapshot
                            extractUserData((Map<String,Object>) dataSnapshot.getValue(),context);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //handle databaseError
                        }
                    });
        }catch (Exception e){
        }
    }

    //Extract user data
    private static void extractUserData(Map<String,Object> users, Context context) {
        try{
            userData = new ArrayList<>();
            for (Map.Entry<String, Object> entry : users.entrySet()){
                Map singleUser = (Map) entry.getValue();
                userData.add(new UserModel(entry.getKey(),singleUser.get(context.getString(R.string.tagUserName)).toString(),
                        singleUser.get(context.getString(R.string.tagUserMobile)).toString(),
                        singleUser.get(context.getString(R.string.tagUserEmail)).toString(),
                        singleUser.get(context.getString(R.string.tagUserPassword)).toString()));
            }

            if(!userData.isEmpty()){
                recyclerView.setVisibility(View.VISIBLE);
                txtMessage.setVisibility(View.GONE);
                setAdapter(context);

            } else {
                recyclerView.setVisibility(View.GONE);
                txtMessage.setVisibility(View.VISIBLE);
                txtMessage.setText(context.getString(R.string.tagNoUserMsg));
            }
        } catch (Exception e){
            recyclerView.setVisibility(View.GONE);
            txtMessage.setVisibility(View.VISIBLE);
            txtMessage.setText(context.getString(R.string.tagNoUserMsg));
        }
   }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.menuLogout:
                finish();
                break;
        }
        return true;
    }

    // Initiating Adapter and set to recycleView
    private static void setAdapter(Context context){
        try {
            adapterList = new ArrayList<>();
            txtMessage.setVisibility(View.GONE);
            adapterList.addAll(userData);
            userDataAdapter = new UserDataAdapter(context);
            recyclerView.setAdapter(userDataAdapter);
            userDataAdapter.setAdapterData(adapterList);
            userDataAdapter.notifyDataSetChanged();
        } catch (Exception e){
        }
    }

    // Check valid email address
    private static boolean isEmailValid(String email) {
        return email.contains("@");
    }

    // Check valid mobile number
    public static boolean hasMobileText(EditText editText) {
        String text = editText.getText().toString().trim();
        editText.setError(null);
        if (text.length() == 0 || text.length()<10) {
            return false;
        }
        return true;
    }
}
