package com.likapalab.mehmet.publicchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Mehmet on 26.09.2015.
 */
public class MessageAdapter extends ArrayAdapter<Message> {
    public MessageAdapter(Context context, int resource,List<Message> objects){
        super(context,resource,objects);
    }

    public View getView(int position,View convertView, ViewGroup parent ){
        View v = convertView;

        if(v == null){
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.messages_item_template,null);
        }

        Message message = getItem(position);

        LinearLayout a = (LinearLayout)v.findViewById(R.id.item_template_layout);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        if(message != null){
            TextView usernametext = (TextView)v.findViewById(R.id.username_text);
            TextView messagetext = (TextView)v.findViewById(R.id.message_text);
            TextView timetext = (TextView)v.findViewById(R.id.time);

            usernametext.setTextColor(message.getUserColor());

            messagetext.setText(""+message.getMessage());
            timetext.setText(""+message.getTime());

            if(message.getIsMe()){
                usernametext.setText("");
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                params.setMargins(150,3, 4, 0);
                a.setOrientation(LinearLayout.HORIZONTAL);
                a.setLayoutParams(params);
                a.setBackgroundResource(R.drawable.out_message_back);
            } else{
                usernametext.setText("" + message.getUsername());
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                params.setMargins(4, 3, 150,0);
                a.setOrientation(LinearLayout.VERTICAL);
                a.setLayoutParams(params);
                a.setBackgroundResource(R.drawable.in_message_back);
            }
        }
        return v;
    }
}
