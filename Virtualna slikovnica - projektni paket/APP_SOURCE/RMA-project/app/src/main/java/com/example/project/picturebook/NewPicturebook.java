package com.example.project.picturebook;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project.MainMenu;
import com.example.project.R;
import com.example.project.archive.EditPages;
import com.example.project.archive.SinglePicturebook;
import com.example.project.model.DatabasePage;
import com.example.project.model.Page;
import com.example.project.model.Picturebook;
import com.example.project.user_profile.Login;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


// Activity for creating and editing picture books
public class NewPicturebook extends AppCompatActivity {

    EditText title;
    EditText summary;
    Button savePicturebookBtn;
    Button discardPicturebookBtn;
    Button addPageBtn;
    Button editPagesBtn;
    String picturebookId;
    DatabasePage dbPage;
    ArrayList<DatabasePage> dbPages;

    FirebaseAuth auth;
    FirebaseUser loggedInUser;
    FirebaseDatabase databaseIns;
    DatabaseReference database;

    Uri filePath;
    FirebaseStorage storageIns;
    StorageReference storageRef;

    RecyclerView rv;
    ArrayList<Page> pages;
    PagesAdapter pAdapter;

    boolean editMode;

    Bitmap bm;
    String id;
    boolean addedPages;

    final long ONE_MEGABYTE = 1024 * 1024;
    final int MAX_PAGES = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Project);
        setContentView(R.layout.activity_new_picturebook);

        title = findViewById(R.id.editTextTitle);
        summary = findViewById(R.id.editTextSummary);
        savePicturebookBtn = findViewById(R.id.buttonSavePicturebook);
        discardPicturebookBtn = findViewById(R.id.buttonDiscardPicturebook);
        addPageBtn = findViewById(R.id.buttonAddPage);
        editPagesBtn = findViewById(R.id.buttonEditPages);
        rv = findViewById(R.id.recyclerViewPages);
        pages = new ArrayList<>();
        dbPages = new ArrayList<>();
        editMode = false;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        loggedInUser = auth.getCurrentUser();
        databaseIns = FirebaseDatabase.getInstance();
        storageIns = FirebaseStorage.getInstance();

        // if user is not logged in, redirect to login page
        if (loggedInUser == null) {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        }

        init();

        addPageBtn.setOnClickListener(view -> {
            if (pAdapter.getItemCount() == MAX_PAGES) {
                Toast.makeText(NewPicturebook.this, "You've reached maximum number of pages!", Toast.LENGTH_SHORT).show();
            } else {
                choosePages();
            }
        });

        savePicturebookBtn.setOnClickListener(view -> {
            if (checkData()) {
                savePicturebook();
            } else {
                Toast.makeText(NewPicturebook.this, "You have to provide title, summary and at least one page for picture book!", Toast.LENGTH_SHORT).show();
            }
        });

        // clear all entered data
        discardPicturebookBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.discard_picturebook_dialog)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            discardPicturebook();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // do nothing
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        editPagesBtn.setOnClickListener(view -> {
            Intent in = new Intent(this, EditPages.class);
            in.putExtra("picturebookId", picturebookId);
            startActivity(in);
        });

    }

    void init() {

        // if we are editing existing picture book, it means that we came from SinglePicturebook activity
        picturebookId = getIntent().getStringExtra("picturebookId");
        if (picturebookId != null) {
            editMode = true;
        }

        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        pAdapter = new PagesAdapter(this);
        rv.setAdapter(pAdapter);
        pAdapter.setImages(pages);

        // if we are editing existing picturebook, load it from database
        if (editMode) {
            loadData();
            discardPicturebookBtn.setVisibility(View.GONE);
        } else {
            // if we are creating new picture book, we can remove pages with long click from adapter
            // because they are not yet saved to database
            pAdapter.setNewPicturebook(true);
            editPagesBtn.setVisibility(View.GONE);
        }
    }

    void loadData() {

        database = databaseIns.getReference("/picturebooks");
        database.child(picturebookId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Picturebook picturebook = dataSnapshot.getValue(Picturebook.class);
                title.setText(picturebook.getTitle());
                summary.setText(picturebook.getSummary());
                database.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(NewPicturebook.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        database = databaseIns.getReference("/pages");
        database.orderByChild("picturebookId").equalTo(picturebookId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    pages.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        dbPage = ds.getValue(DatabasePage.class);
                        dbPage.setId(ds.getKey());
                        dbPages.add(dbPage);
                    }
                }
                getPages();
                database.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(NewPicturebook.this, "Failed to read value." + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    void getPages() {
        for (DatabasePage page : dbPages) {
            storageRef = storageIns.getReference().child("images/pages/" + picturebookId + "/" + page.getId());
            storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    if (page.getNum() != 0) {
                        pages.add(new Page(page.getId(), page.getNum(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length)));
                    } else {
                        pages.add(new Page(page.getId(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length)));
                    }
                    Collections.sort(pages);
                    pAdapter.notifyItemRangeChanged(0, pages.size() - 1);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(NewPicturebook.this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // reset input fields
    void discardPicturebook() {
        title.setText("");
        summary.setText("");
        filePath = null;
        pages.clear();
        pAdapter.notifyDataSetChanged();
    }

    // last image chosen from internal storage add to array of bitmaps
    public void AddImagesFromGalleryToRecyclerViewArrayList()
    {
        if (filePath != null) {
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(), filePath);
                pages.add(new Page(null, image));
            } catch (IOException e) {
                e.printStackTrace();
            }
            // refresh recyclerview
            pAdapter.notifyDataSetChanged();
            Toast.makeText(NewPicturebook.this, "You can remove page with long click!", Toast.LENGTH_SHORT).show();
        }
    }

    boolean checkData() {
        boolean valid = true;
        if (title == null || title.getText().toString().isEmpty()) {
            valid = false;
        } else if (summary == null || summary.getText().toString().isEmpty()) {
            valid = false;
        } else if (pages == null || pages.isEmpty()) {
            valid = false;
        }

        return valid;
    }

    // choose image from internal storage
    void choosePages() {
        Intent i = new Intent();
        i.setType("image/*");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        i.setAction(Intent.ACTION_GET_CONTENT);
        launchSomeActivity.launch(i);
    }

    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        filePath = data.getData();
                }
                }
                AddImagesFromGalleryToRecyclerViewArrayList();
            });


    void savePicturebook() {

        Picturebook picturebook = new Picturebook(loggedInUser.getUid(), title.getText().toString(), summary.getText().toString());
        database = databaseIns.getReference("/picturebooks");

        if (editMode) {
            // editing existing picture book
            database.child(picturebookId).setValue(picturebook);
        } else {
            // creating new picture book
            database.push().setValue(picturebook);
        }

        // wait for creating new picturebook in database and than save pages to storage
        database.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                picturebookId = dataSnapshot.getKey();
                if (picturebookId != null) {
                    // remove event listener so it wouldn't be called again after adding each page to storage
                    database.removeEventListener(this);
                    savePages();
                }
            }

            void savePages() {
                database = databaseIns.getReference("/pages");
                addedPages = false;

                for (Page currentPage : pages) {
                    // if id is not null, it was already saved to database (editMode)
                    if (currentPage.getId() != null) {
                        continue;
                    }

                    addedPages = true;

                    // create unique id for page
                    id = String.valueOf(System.currentTimeMillis()) + loggedInUser.getUid();
                    database.child(id).child("picturebookId").setValue(picturebookId);

                    storageRef = storageIns.getReference().child("images/pages/" + picturebookId + "/" + id);

                    // compress images before saving them to storage
                    bm = currentPage.getImage();
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 5, bytes);
                    byte[] reducedImage = bytes.toByteArray();

                    storageRef.putBytes(reducedImage)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // when all pages are stored, redirect to main menu
                                    if (currentPage.getImage().equals(pages.get(pages.size() - 1).getImage())) {
                                        Toast.makeText(NewPicturebook.this, "Picture Book successfully saved!", Toast.LENGTH_SHORT).show();
                                        if (editMode) {
                                            Intent in = new Intent(getApplicationContext(), SinglePicturebook.class);
                                            in.putExtra("picturebookId", picturebookId);
                                            startActivity(in);
                                        } else {
                                            Intent in = new Intent(getApplicationContext(), MainMenu.class);
                                            startActivity(in);
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(NewPicturebook.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                if (editMode && !addedPages) {
                    Toast.makeText(NewPicturebook.this, "Picture Book successfully saved!", Toast.LENGTH_SHORT).show();
                    Intent in = new Intent(getApplicationContext(), SinglePicturebook.class);
                    in.putExtra("picturebookId", picturebookId);
                    startActivity(in);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {            }

        });
    }
    
}