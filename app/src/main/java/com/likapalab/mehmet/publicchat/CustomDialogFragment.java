package com.likapalab.mehmet.publicchat;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Mehmet on 1.03.2016.
 */

public class CustomDialogFragment extends DialogFragment{

    String usernameText,messageText,timeText;
    Boolean isReport;
    TextView username,message,time;
    int color;
    Button reportButton;
    DialogInterface dialogInterface;

    public CustomDialogFragment(){}
    @SuppressLint("ValidFragment")
    public CustomDialogFragment(String usernameText,String messageText,String timeText, int color, Boolean isReport){
        this.usernameText = usernameText;
        this.messageText = messageText;
        this.timeText = timeText;
        this.color = color;
        this.isReport = isReport;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,
                             Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.dialog_fragment,container);

        getDialog().getWindow().requestFeature(getActivity().getWindow().FEATURE_NO_TITLE);
        setCancelable(true);
        dialogInterface = (DialogInterface) getActivity();

        username = (TextView)view.findViewById(R.id.username_text);
        message = (TextView)view.findViewById(R.id.message_text);
        time = (TextView)view.findViewById(R.id.time);

        username.setTextColor(color);
        username.setText(usernameText);
        message.setText(messageText);
        time.setText(timeText);

        reportButton = (Button)view.findViewById(R.id.button);

        if(isReport) {
            reportButton.setEnabled(false);
            reportButton.setTextColor(getResources().getColor(R.color.softBlue));
        }
        else {
            reportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogInterface.buttonClick(1);
                    dismiss();
                }
            });
        }
        return view;
    }
}
