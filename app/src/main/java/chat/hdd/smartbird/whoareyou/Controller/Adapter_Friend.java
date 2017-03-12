package chat.hdd.smartbird.whoareyou.Controller;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import chat.hdd.smartbird.whoareyou.Model.Friend;
import chat.hdd.smartbird.whoareyou.R;

/**
 * Created by hiepdd on 11/03/2017.
 */

public class Adapter_Friend extends ArrayAdapter {
    private Context context;
    private ArrayList<Friend> list;

    public Adapter_Friend(Context context, int resource, ArrayList<Friend> list) {
        super(context, resource, list);
        this.context = context;
        this.list = new ArrayList<Friend>(list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),"miso-bold.otf");
        itemFriend item;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_friend_item, null);
            item = new itemFriend();
            item.tvName = (TextView)convertView.findViewById(R.id.textViewItemName);
            item.tvEmail = (TextView)convertView.findViewById(R.id.textViewItemEmail);
            item.tvName.setTypeface(typeface);
            item.tvEmail.setTypeface(typeface);
            item.imgPicture = (ImageView) convertView.findViewById(R.id.imageViewItemPicture);
            convertView.setTag(item);
        }else{
            item = (itemFriend) convertView.getTag();
        }
        Friend friend = list.get(position);
        item.tvName.setText(friend.getNameFriend());
        item.tvEmail.setText(friend.getEmailFriend());

        Random random = new Random();
        int number = random.nextInt(5);
        if(number == 0)item.imgPicture.setImageResource(R.drawable.ic_mask);
        else if(number == 1)item.imgPicture.setImageResource(R.drawable.ic_mask1);
        else if(number == 2)item.imgPicture.setImageResource(R.drawable.ic_mask2);
        else if(number == 3)item.imgPicture.setImageResource(R.drawable.ic_mask3);
        else if(number == 4)item.imgPicture.setImageResource(R.drawable.ic_mask4);

        return convertView;
    }

    private class itemFriend{
        private TextView tvEmail, tvName;
        private ImageView imgPicture;
    }
}