package com.firebase.curd.app.support;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.curd.app.R;
import com.firebase.curd.app.main.MainActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class UserDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Object> dataList = new ArrayList();
    private Context context;

    // Constructor
    public UserDataAdapter(Context context){
        this.context=context;
    }

    public void setAdapterData(List<Object> adapterData){
        this.dataList = adapterData;
    }

    // We need to override this as we need to differentiate
    // which type viewHolder to be attached
    // This is being called from onBindViewHolder() method
    @Override
    public int getItemViewType(int position) {
        if (dataList.get(position) instanceof UserModel) {
            return MainActivity.TYPE_USER_DETAILS;
        }
        return -1;
    }

    // Invoked by layout manager to replace the contents of the views
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        int viewType=holder.getItemViewType();
        switch (viewType){
            case MainActivity.TYPE_USER_DETAILS:
                UserModel userDAO = (UserModel) dataList.get(position);
                ((UserViewHolder)holder).showNewsDetails(userDAO,position);
                break;
        }
    }

    @Override
    public int getItemCount(){return dataList.size();}

    // Invoked by layout manager to create new views
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = 0;
        RecyclerView.ViewHolder viewHolder;
        // Identify viewType returned by getItemViewType(...)
        // and return ViewHolder Accordingly
        switch (viewType){
            case MainActivity.TYPE_USER_DETAILS:
                layout= R.layout.list_item;
                View newsView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
                viewHolder=new UserViewHolder(newsView);
                break;
            default:
                viewHolder=null;
                break;
        }
        return viewHolder;
    }

    // Second ViewHolder of object type User
    public class UserViewHolder extends RecyclerView.ViewHolder {

        private TextView txtUserName,txtUserMobile,txtUserEmailAddress;
        private ImageView imgEdit,imgDelete;

        public UserViewHolder(View itemView) {
            super(itemView);
            // Initiate view
            txtUserName =(TextView)itemView.findViewById(R.id.txtUserName);
            txtUserMobile = (TextView)itemView.findViewById(R.id.txtUserMobile);
            txtUserEmailAddress = (TextView) itemView.findViewById(R.id.txtEmailAddress);
            imgEdit = (ImageView) itemView.findViewById(R.id.imgEdit);
            imgDelete  = (ImageView) itemView.findViewById(R.id.imgDelete);
        }

        public void showNewsDetails(final UserModel userDAO, final int position){
            // Attach values for each item
            txtUserName.setText("User Name : "+userDAO.getUserName());
            txtUserMobile.setText("Mobile No. : "+userDAO.getUserMobile());
            txtUserEmailAddress.setText("Email Address : "+userDAO.getUserEmail());
            imgEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.createUpdateUserDialog(context,false,position);
                }
            });
            imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage(context.getString(R.string.alertMsg));
                        builder.setCancelable(true);

                        builder.setPositiveButton(
                                context.getString(R.string.alertActionYes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Delete user object from Firebase
                                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.tagUserTable));
                                        mDatabase.child(userDAO.userID).removeValue();
                                        dataList.remove(userDAO);
                                        Toast.makeText(context,context.getString(R.string.tagUserDeleteMsg),Toast.LENGTH_SHORT).show();
                                        notifyDataSetChanged();
                                        MainActivity.getUserList(context);
                                        dialog.cancel();
                                    }
                                });

                        builder.setNegativeButton(context.getString(R.string.alertActionNo),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert11 = builder.create();
                        alert11.show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}

