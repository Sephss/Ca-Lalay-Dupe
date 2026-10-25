package com.example.ca_lalay.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.Session.SessionManager;
import com.example.ca_lalay.Session.ShopOwnerSession;
import com.example.ca_lalay.Session.UserSession;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.FirebaseStorageManager;
import com.example.ca_lalay.firebase.callback.ReuploadDocumentsCallback;
import com.example.ca_lalay.firebase.callback.ShopOwnerDataCallback;
import com.example.ca_lalay.firebase.callback.UpdateShopStatusResubmitDocuments;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.google.android.material.transition.platform.MaterialFadeThrough;

public class RejectedRegistration extends AppCompatActivity {
    private Button iUnderstandBtn, resubmitDocumentsBtn, resubmitTheDocumentsBtn;
    private SessionManager sessionManager;
    private ConstraintLayout root;
    private MaterialFadeThrough fadeThrough;
    private ImageView backBtn;
    private TextView tvRejectionReason;
    private TextView viewVerificationDocumentsLabel;

    private LinearLayout rejectedLayout, resubmitDocumentsLayout;
    private LinearLayout serviceAddPhotoLayout, serviceAddPhotoLayout2, serviceAddPhotoLayout3;
    private ImageView imgPhoto, imgPhoto2, imgPhoto3;
    private Uri imageUri, imageUri2, imageUri3;
    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int PICK_IMAGE_REQUEST2 = 1002;
    private static final int PICK_IMAGE_REQUEST3 = 1003;;
    private String currentUserLoggedinID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rejected_registration);

        iUnderstandBtn = findViewById(R.id.iUnderstandBtn);
        resubmitDocumentsBtn = findViewById(R.id.resubmitDocumentsBtn);
        resubmitTheDocumentsBtn = findViewById(R.id.resubmitTheDocumentsBtn);
        sessionManager = new SessionManager(this);

        root = findViewById(R.id.main);
        fadeThrough = new MaterialFadeThrough();
        fadeThrough.setDuration(300);
        rejectedLayout = findViewById(R.id.rejectedLayout);
        resubmitDocumentsLayout = findViewById(R.id.resubmitDocumentsLayout);
        viewVerificationDocumentsLabel = findViewById(R.id.viewVerificationDocumentsLabel);
        backBtn = findViewById(R.id.backBtn);
        serviceAddPhotoLayout = findViewById(R.id.serviceAddPhotoLayout);
        serviceAddPhotoLayout2 = findViewById(R.id.serviceAddPhotoLayout2);
        serviceAddPhotoLayout3 = findViewById(R.id.serviceAddPhotoLayout3);
        tvRejectionReason = findViewById(R.id.tvRejectionReason);
        imgPhoto = findViewById(R.id.imgPhoto);
        imgPhoto2 = findViewById(R.id.imgPhoto2);
        imgPhoto3 = findViewById(R.id.imgPhoto3);
        currentUserLoggedinID = FirebaseAuthManager.getCurrentUserUid();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getTheReason();

        viewVerificationDocumentsLabel.setPaintFlags(
                viewVerificationDocumentsLabel.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG
        );

        viewVerificationDocumentsLabel.setOnClickListener(s -> {
            viewApprovedDocuments();
        });



        iUnderstandBtn.setOnClickListener(s -> {
            FirebaseAuthManager.logout();
            UserSession.getInstance().clearSession();
            ShopOwnerSession.getInstance().clearSession();
            sessionManager.logout();

            finishAffinity();

        });

        resubmitDocumentsBtn.setOnClickListener(f -> {
            TransitionManager.beginDelayedTransition(root, fadeThrough);
            rejectedLayout.setVisibility(View.GONE);
            resubmitDocumentsLayout.setVisibility(View.VISIBLE);

        });

        backBtn.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition(root, fadeThrough);
            rejectedLayout.setVisibility(View.VISIBLE);
            resubmitDocumentsLayout.setVisibility(View.GONE);
        });

        serviceAddPhotoLayout.setOnClickListener(s -> {
            choosePhotoFromGallery();
        });

        imgPhoto.setOnClickListener(s -> {
            choosePhotoFromGallery();

        });


        serviceAddPhotoLayout2.setOnClickListener(s -> {
            choosePhotoFromGallery2();
        });

        imgPhoto2.setOnClickListener(s -> {
            choosePhotoFromGallery2();

        });


        serviceAddPhotoLayout3.setOnClickListener(s -> {
            choosePhotoFromGallery3();
        });

        imgPhoto3.setOnClickListener(s -> {
            choosePhotoFromGallery3();

        });

        resubmitTheDocumentsBtn.setOnClickListener(s -> {
            resubmitTheDocuments();
        });
    }
    private void viewApprovedDocuments() {

        StringBuilder message = new StringBuilder();

        // =========================
        // BUSINESS / SHOP OWNER
        // =========================
        message.append("SHOP OWNER\n\n");

        message.append("Required: 3 primary business verification documents\n\n");

        message.append("1. DTI Certificate of Business Name Registration ")
                .append("or SEC Certificate of Incorporation\n\n");

        message.append("2. Mayor's / Business Permit\n\n");

        message.append("3. BIR Certificate of Registration (Form 2303)\n\n");

        message.append("Supporting Documents:\n")
                .append("• Barangay Business Clearance\n")
                .append("• Fire Safety Inspection Certificate (FSIC)\n")
                .append("• Occupancy Permit\n")
                .append("• Other valid government-issued business document\n\n");

        // =========================
        // FREELANCER
        // =========================
        message.append("FREELANCER\n\n");

        message.append("Required: 3 verification documents\n\n");

        message.append("1. One (1) Valid Government-Issued ID\n\n");

        message.append("2. One (1) Valid Certificate or Registration Document\n")
                .append("   Examples: Barangay Certificate, Barangay Clearance, ")
                .append("DTI Certificate, or other applicable government-issued certificate\n\n");

        message.append("3. One (1) Proof of Work or Service Experience\n")
                .append("   Examples: Previous service records, client receipt, ")
                .append("service/work portfolio, certificate of employment, ")
                .append("training certificate, or other document demonstrating ")
                .append("relevant vehicle-service work\n\n");

        // =========================
        // SUPPORTING DOCUMENTS
        // =========================
        message.append("ADDITIONAL SUPPORTING DOCUMENTS\n\n");

        message.append("If a primary document is unavailable, you may provide ")
                .append("additional supporting documents for review. These may include:\n")
                .append("• Barangay Clearance or Certificate\n")
                .append("• Business-related certificates\n")
                .append("• Fire Safety Inspection Certificate (FSIC)\n")
                .append("• Occupancy Permit\n")
                .append("• Training or skills certificates\n")
                .append("• Service records or work receipts\n")
                .append("• Proof of previous vehicle-service work\n")
                .append("• Other valid government-issued documents\n\n");

        message.append("Note: All submitted documents must be valid, readable, ")
                .append("and consistent with the information provided during registration. ")
                .append("Supporting documents may be requested when additional verification ")
                .append("is needed.");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Accepted Verification Documents");

        builder.setMessage(message.toString());

        builder.setPositiveButton("Close", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void getTheReason() {
        FirebaseDatabaseManager.getShopOwnerData(currentUserLoggedinID, new ShopOwnerDataCallback() {
            @Override
            public void onSuccess(ShopOwnerModel data) {
                tvRejectionReason.setText(data.getRejectedReason());
            }

            @Override
            public void onFailure(String failed) {
                Toast.makeText(RejectedRegistration.this, "Failed to load the reason", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    public void choosePhotoFromGallery2() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST2);
    }
    public void choosePhotoFromGallery3() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST3);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            serviceAddPhotoLayout.setVisibility(View.GONE);
            imgPhoto.setVisibility(View.VISIBLE);
            imgPhoto.setImageURI(imageUri);
        }

        if (requestCode == PICK_IMAGE_REQUEST2 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri2 = data.getData();
            serviceAddPhotoLayout2.setVisibility(View.GONE);
            imgPhoto2.setVisibility(View.VISIBLE);
            imgPhoto2.setImageURI(imageUri2);
        }

        if (requestCode == PICK_IMAGE_REQUEST3 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri3 = data.getData();
            serviceAddPhotoLayout3.setVisibility(View.GONE);
            imgPhoto3.setVisibility(View.VISIBLE);
            imgPhoto3.setImageURI(imageUri3);
        }

    }
    private void resubmitTheDocuments() {
        if (imageUri == null || imageUri2 == null || imageUri3 == null) {
            Toast.makeText(this, "Please upload required images", Toast.LENGTH_SHORT).show();
            return;
        } else {
            setLoadingState();
            FirebaseStorageManager.reuploadDocumentsImage("UserProfile", imageUri, imageUri2, imageUri3, new ReuploadDocumentsCallback() {
                @Override
                public void onSuccess(String image1, String image2, String image3) {
                    updateUserStatus(image1, image2, image3);
                }

                @Override
                public void onFailure(String errorMessage) {
                    setNormalState();
                    Toast.makeText(RejectedRegistration.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void updateUserStatus(String image1, String image2, String image3) {
        FirebaseDatabaseManager.updateShopStatusReuploadDocuments(currentUserLoggedinID, "pending", image1, image2, image3, new UpdateShopStatusResubmitDocuments() {
            @Override
            public void onSuccess(String success) {
                setNormalState();
                FirebaseAuthManager.logout();
                UserSession.getInstance().clearSession();
                ShopOwnerSession.getInstance().clearSession();
                sessionManager.logout();

                displayConfirmation();
            }

            @Override
            public void onFailure(String failed) {
                displayResubmissionFailed();
                setNormalState();
            }
        });
    }
    private void displayConfirmation() {
        new AlertDialog.Builder(RejectedRegistration.this)
                .setTitle("Documents Resubmitted")
                .setMessage(
                        "Your documents have been successfully resubmitted. " +
                                "Your application is now pending review. " +
                                "Please wait while our administrator reviews your documents."
                )
                .setPositiveButton("Okay", (dialog, which) -> {
                    dialog.dismiss();


                    Intent intent = new Intent(
                            RejectedRegistration.this,
                            LoginActivity.class
                    );

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);
                })
                .setCancelable(false)
                .show();
    }
    private void displayResubmissionFailed() {
        new AlertDialog.Builder(RejectedRegistration.this)
                .setTitle("Resubmission Failed")
                .setMessage(
                        "We were unable to resubmit your documents. " +
                                "Please check your internet connection and try again."
                )
                .setPositiveButton("Okay", (dialog, which) -> {
                    dialog.dismiss();

                })

                .setCancelable(false)
                .show();
    }
    private void setLoadingState() {
        resubmitTheDocumentsBtn.setEnabled(false);
        resubmitTheDocumentsBtn.setAlpha(0.5f);
        resubmitTheDocumentsBtn.setText("SUBMITTING...");
    }

    private void setNormalState() {
        resubmitTheDocumentsBtn.setEnabled(true);
        resubmitTheDocumentsBtn.setAlpha(1f);
        resubmitTheDocumentsBtn.setText("RESUBMIT");
    }
}