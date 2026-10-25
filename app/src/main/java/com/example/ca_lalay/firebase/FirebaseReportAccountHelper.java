package com.example.ca_lalay.firebase;

import com.example.ca_lalay.firebase.callback.ReportAccountCallback;
import com.example.ca_lalay.model.ReportAccountModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseReportAccountHelper {
    private static DatabaseReference getDatabase() {
        return FirebaseDatabase.getInstance().getReference("accountReported");
    }
    public static void reportAccount(ReportAccountModel reportData, ReportAccountCallback callback) {
        String reportId = getDatabase().push().getKey();
        if (reportId == null) {
            callback.onFailure("Failed to generate report ID.");
            return;
        }

        reportData.setReportId(reportId);

        getDatabase().child(reportId).setValue(reportData).addOnSuccessListener(task -> {
            callback.onSuccess("Report submitted successfully.");
        }).addOnFailureListener(failed -> {
            callback.onFailure(failed.getMessage());
        });
    }
}
