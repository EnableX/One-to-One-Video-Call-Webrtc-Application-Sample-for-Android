package com.enablex.chatsample.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.enablex.chatsample.R;
import com.enablex.chatsample.model.MessageDetails;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    Context context;
    ArrayList<MessageDetails> list;

    public ChatAdapter(Context context, ArrayList<MessageDetails> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_item, null);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder chatViewHolder, int i) {
        if (list.get(i).isReceived() == true) {
            chatViewHolder.LL_Send.setVisibility(View.GONE);
            chatViewHolder.LL_receive.setVisibility(View.VISIBLE);
            chatViewHolder.txtSenderName.setText(list.get(i).getUserName().trim());
            chatViewHolder.txtBodyReceived.setText(list.get(i).getBody().trim());
            chatViewHolder.txtTimeReceived.setText(list.get(i).getReceiveTime().trim());
        } else {
            chatViewHolder.LL_Send.setVisibility(View.VISIBLE);
            chatViewHolder.LL_receive.setVisibility(View.GONE);
            chatViewHolder.txtTimeSent.setText(list.get(i).getReceiveTime());
            chatViewHolder.txtMe.setText("Me:");
            chatViewHolder.txtBodySend.setText(list.get(i).getBody().trim());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder{
        TextView txtSenderName;
        TextView txtBodyReceived;
        TextView txtTimeReceived;
        LinearLayout LL_receive;
        LinearLayout LL_Send;
        TextView txtMe;
        TextView txtBodySend;
        TextView txtTimeSent;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSenderName = (TextView) itemView.findViewById(R.id.txt_From);
            txtBodyReceived = (TextView) itemView.findViewById(R.id.txt_Body);
            txtTimeReceived = (TextView) itemView.findViewById(R.id.txt_time);

            txtMe = (TextView) itemView.findViewById(R.id.txt_me);
            txtBodySend = (TextView) itemView.findViewById(R.id.txt_Body_me);
            txtTimeSent = (TextView) itemView.findViewById(R.id.txt_time_me);

            LL_Send = (LinearLayout) itemView.findViewById(R.id.LL_Send);
            LL_receive = (LinearLayout)itemView. findViewById(R.id.LL_receive);
        }
    }
}
