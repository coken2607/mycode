package com.example.atn.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.atn.R;
import com.example.atn.adapter.AdapterPosts;
import com.example.atn.main.MainActivity;
import com.example.atn.model.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference databaseReference;
    private StorageReference mStorageReference;

    private ImageView coverIv;
    private CircleImageView avatarIv;
    private TextView nameTv, emailTv, phoneTv;
    private ProgressDialog pd;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    private static final int IMAGE_PICK_GALLEY_CODE = 300;

    private String[] cameraPermissions;
    private String[] storagePermissions;
    private Uri image_uri;
    private String profileOrCoverPhoto;
    private RecyclerView postRecyclerView;
    private List<ModelPost> postList;
    private AdapterPosts adapterPosts;
    private String uid;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        mStorageReference = getInstance().getReference();

        cameraPermissions = new String[] {
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE };
        storagePermissions = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };

        avatarIv = view.findViewById(R.id.avatarIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        coverIv = view.findViewById(R.id.coverIv);
        FloatingActionButton fab = view.findViewById(R.id.fab);
        postRecyclerView = view.findViewById(R.id.recyclerView_posts);

        pd = new ProgressDialog(getActivity());

        Query query = databaseReference.orderByChild("email").equalTo(mUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        Picasso.get().load(image).into(avatarIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.profile_image).into(avatarIv);
                    }

                    try {
                        Picasso.get().load(cover).into(coverIv);
                    } catch (Exception ignored) {
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        postList = new ArrayList<>();

        checkUserStatus();
        loadMyPosts();

        return view;
    }

    private void loadMyPosts() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        postRecyclerView.setLayoutManager(linearLayoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    postList.add(modelPost);
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    postRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] { Manifest.permission.CAMERA }, CAMERA_REQUEST_CODE);
        }
        return true;
    }

    private void requestCameraPermission() {
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        String[] options = {
                getResources().getString(R.string.edit_profile),
                getResources().getString(R.string.edit_cover),
                getResources().getString(R.string.edit_name),
                getResources().getString(R.string.edit_phone) };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.choice_action));
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    pd.setMessage(getResources().getString(R.string.update_profile));
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();
                } else if (which == 1) {
                    pd.setMessage(getResources().getString(R.string.update_cover));
                    profileOrCoverPhoto = "cover";
                    showImagePicDialog();
                } else if (which == 2) {
                    pd.setMessage(getResources().getString(R.string.update_name));
                    showNamePhoneUpdateDialog("name");
                } else if (which == 3) {
                    pd.setMessage(getResources().getString(R.string.update_phone));
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });

        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(final String key) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle("Cập nhật" + " " + key);
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        final EditText editText = new EditText(getActivity());
        editText.setHint("Nhập" + " " + key);
        linearLayout.addView(editText);
        builder.setView(linearLayout);

        builder.setMessage("")
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.update),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String value = editText.getText().toString().trim();
                                if (!TextUtils.isEmpty(value)) {
                                    pd.dismiss();
                                    HashMap<String, Object> result = new HashMap<>();
                                    result.put(key, value);
                                    databaseReference.child(mUser.getUid())
                                            .updateChildren(result)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    pd.dismiss();
                                                    Toast.makeText(getActivity(),
                                                            getResources().getString(
                                                                    R.string.update),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    pd.dismiss();
                                                    Toast.makeText(getActivity(),
                                                            "" + e.getMessage(), Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                            });
                                    if (key.equals("name")) {
                                        DatabaseReference ref = FirebaseDatabase.getInstance()
                                                .getReference("Posts");
                                        Query query = ref.orderByChild("uid").equalTo(uid);
                                        query.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(
                                                    @NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                    String child = ds.getKey();
                                                    dataSnapshot.getRef()
                                                            .child(Objects.requireNonNull(child))
                                                            .child("uName")
                                                            .setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(
                                                    @NonNull DatabaseError databaseError) {

                                            }
                                        });

                                        ref.addListenerForSingleValueEvent(
                                                new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(
                                                            @NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                            String child = ds.getKey();
                                                            if (dataSnapshot.child(
                                                                    Objects.requireNonNull(child))
                                                                    .hasChild("Comments")) {
                                                                String child1 =
                                                                        "" + dataSnapshot.child(
                                                                                child).getKey();
                                                                Query child2 =
                                                                        FirebaseDatabase.getInstance()
                                                                                .getReference(
                                                                                        "Posts")
                                                                                .child(child1)
                                                                                .child("Comments")
                                                                                .orderByChild("uid")
                                                                                .equalTo(uid);
                                                                child2.addValueEventListener(
                                                                        new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(
                                                                                    @NonNull
                                                                                            DataSnapshot dataSnapshot) {
                                                                                for (DataSnapshot ds : dataSnapshot
                                                                                        .getChildren()) {
                                                                                    String child =
                                                                                            ds.getKey();
                                                                                    dataSnapshot.getRef()
                                                                                            .child(Objects
                                                                                                    .requireNonNull(
                                                                                                            child))
                                                                                            .child("uName")
                                                                                            .setValue(
                                                                                                    value);
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(
                                                                                    @NonNull
                                                                                            DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(
                                                            @NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "Please enter" + key,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
        builder.create().show();
    }

    private void showImagePicDialog() {
        String[] options = {
                getResources().getString(R.string.camera),
                getResources().getString(R.string.album) };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.pick_image));
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                } else if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });

        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted =
                            grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.txt_camera),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted =
                            grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.txt_storage), Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLEY_CODE) {
                if (data != null) {
                    image_uri = data.getData();
                }
                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        String storagePath = "User_Profile_Cover_Imgs/";
        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + mUser.getUid();
        StorageReference storageReference2nd = mStorageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        final Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> result = new HashMap<>();
                            result.put(profileOrCoverPhoto,
                                    Objects.requireNonNull(downloadUri).toString());
                            databaseReference.child(mUser.getUid())
                                    .updateChildren(result)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), getResources().getString(
                                                    R.string.update_profile), Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), getResources().getString(
                                                    R.string.error_update_profile),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            if (profileOrCoverPhoto.equals("image")) {
                                DatabaseReference ref =
                                        FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            dataSnapshot.getRef()
                                                    .child(Objects.requireNonNull(child))
                                                    .child("uDp")
                                                    .setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            if (dataSnapshot.child(Objects.requireNonNull(child))
                                                    .hasChild("Comments")) {
                                                String child1 =
                                                        "" + dataSnapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance()
                                                        .getReference("Posts")
                                                        .child(child1)
                                                        .child("Comments")
                                                        .orderByChild("uid")
                                                        .equalTo(uid);
                                                child2.addValueEventListener(
                                                        new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull
                                                                    DataSnapshot dataSnapshot) {
                                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                                    String child = ds.getKey();
                                                                    dataSnapshot.getRef()
                                                                            .child(Objects.requireNonNull(
                                                                                    child))
                                                                            .child("uDp")
                                                                            .setValue(
                                                                                    downloadUri.toString());
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull
                                                                    DatabaseError databaseError) {

                                                            }
                                                        });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        } else {
                            pd.dismiss();
                            Toast.makeText(getActivity(),
                                    getResources().getString(R.string.some_error),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp description");
        image_uri = Objects.requireNonNull(getActivity())
                .getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLEY_CODE);
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        } else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            Objects.requireNonNull(getActivity()).finish();
        }
    }
}
