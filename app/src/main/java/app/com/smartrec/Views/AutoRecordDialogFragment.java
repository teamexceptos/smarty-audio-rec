package app.com.smartrec.Views;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import app.com.smartrec.Listeners.DialogFragmentInterface;
import app.com.smartrec.Utils.Utils;
import app.com.smartrec.R;

/**
 * Created by Big-Nosed Developer on the Edge of Infinity.
 */
public class AutoRecordDialogFragment extends DialogFragment implements View.OnClickListener{

    DialogFragmentInterface fragmentInterface;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.autorecord_dialogfrag, container, false);

        Button yes_button = view.findViewById(R.id.dialog_fragment_yes);
        Button no_button = view.findViewById(R.id.dialog_fragment_no);

        yes_button.setOnClickListener(this);
        no_button.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            fragmentInterface = (DialogFragmentInterface) context;
        }catch (Exception e){
            Log.d(AutoRecordDialogFragment.class.getSimpleName(), "Activity must implement DialogFragmentInterface");
        }


    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.dialog_fragment_no){
            fragmentInterface.isAutoRecordEnabled(Utils.autoRecordDisabled);
        }else if(v.getId() == R.id.dialog_fragment_yes){
            fragmentInterface.isAutoRecordEnabled(Utils.autoRecordEnabled);
        }

    }

}
