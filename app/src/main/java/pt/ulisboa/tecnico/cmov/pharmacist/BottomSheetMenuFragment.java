package pt.ulisboa.tecnico.cmov.pharmacist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetMenuFragment extends BottomSheetDialogFragment {

    public interface OnButtonClickListener {
        void onCameraButtonClick();
        void onGalleryButtonClick();
    }

    private OnButtonClickListener mListener;


    private static final String TAG = "BottomSheetMenuFragment";

    public static BottomSheetMenuFragment newInstance() {
        return new BottomSheetMenuFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_menu, container, false);



        LinearLayout camera = view.findViewById(R.id.layout_camera);
        LinearLayout gallery = view.findViewById(R.id.layout_gallery);

        camera.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(requireContext(), "Camera", Toast.LENGTH_SHORT).show();
                    mListener.onCameraButtonClick();
                    dismiss();
                }
            }
        );

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onGalleryButtonClick();
                dismiss();
            }
        });


        return view;
    }


    public void setOnButtonClickListener(OnButtonClickListener listener) {
        mListener = listener;
    }


}

