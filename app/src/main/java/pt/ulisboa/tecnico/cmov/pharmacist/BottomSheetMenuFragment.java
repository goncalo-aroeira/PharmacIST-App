package pt.ulisboa.tecnico.cmov.pharmacist;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

public class BottomSheetMenuFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "BottomSheetMenu";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private TextView textViewCamera, textViewGallery;
    private OnPhotoSelectedListener listener;
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        assert result.getData() != null;
                        Uri photoUri = result.getData().getData();

                        if (listener != null) {
                            listener.onPhotoSelected(photoUri);
                            Toast.makeText(requireContext(), "Image captured successfully", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle the case where the image capture was not successful
                        Toast.makeText(requireContext(), "Image capture failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        assert result.getData() != null;
                        Uri imageUri = result.getData().getData();
                        if (listener != null) {
                            listener.onPhotoSelected(imageUri);
                            Toast.makeText(requireContext(), "Image selected successfully", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(requireContext(), "Failed to get image from gallery", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    public static BottomSheetMenuFragment newInstance() {
        return new BottomSheetMenuFragment();
    }

    public void setOnPhotoSelectedListener(OnPhotoSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_menu, container, false);

        textViewCamera = view.findViewById(R.id.text_view_camera);
        textViewGallery = view.findViewById(R.id.text_view_gallery);

        textViewCamera.setOnClickListener(this);
        textViewGallery.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        // Dismiss the bottom sheet menu
        if (R.id.text_view_camera == v.getId()) {
            handleCameraOption();
            Toast.makeText(getContext(), "Camera option selected", Toast.LENGTH_SHORT).show();
        } else if (R.id.text_view_gallery == v.getId()) {
            Toast.makeText(getContext(), "Gallery option selected", Toast.LENGTH_SHORT).show();
            handleGalleryOption();
        }
        dismiss();
    }

    private void handleCameraOption() {
        // request permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) != null) {
                cameraLauncher.launch(takePictureIntent);
            }

        }
    }

    private void handleGalleryOption() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(pickPhoto);
    }

    public interface OnPhotoSelectedListener {
        void onPhotoSelected(Uri photoUri);
    }
}
