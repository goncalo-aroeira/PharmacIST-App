package pt.ulisboa.tecnico.cmov.pharmacist;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

// import Manifest.permission;
import android.Manifest;
import androidx.core.app.ActivityCompat;



import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetMenuFragment extends BottomSheetDialogFragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    public interface OnPhotoListener {
        void onOptionSelected(Bitmap image);
    }

    public OnPhotoListener listener;

    private ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null  && listener != null) {
                        Log.d("BottomSheetMenuFragment", "onActivityResult: Image captured");
                        listener.onOptionSelected(imageBitmap);
                    } else {
                        // add Log.d


                        Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
        }
    });

    public static BottomSheetMenuFragment newInstance() {
        return new BottomSheetMenuFragment();
    }

    public void setOnPhotoListener(OnPhotoListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bottom_sheet_menu, container, false);

        LinearLayout camera = view.findViewById(R.id.layout_camera);
        LinearLayout gallery = view.findViewById(R.id.layout_gallery);

        camera.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent();
                    Toast.makeText(requireContext(), "Camera", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        );

        //gallery.setOnClickListener(this);



        return view;
    }
    private void dispatchTakePictureIntent() {
        // Check if the camera permission is not granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request the camera permission
        ActivityCompat.requestPermissions((Activity) requireContext(), new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                cameraLauncher.launch(takePictureIntent);
                Log.d("BottomSheetMenuFragment", "dispatchTakePictureIntent: Camera app available");
            } else {
                Toast.makeText(requireContext(), "No camera app available", Toast.LENGTH_SHORT).show();
                Log.d("BottomSheetMenuFragment", "dispatchTakePictureIntent: No camera app available");
            }
        }
    }

}

