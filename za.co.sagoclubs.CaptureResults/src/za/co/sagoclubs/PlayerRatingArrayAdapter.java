package za.co.sagoclubs;

/*
 * This is an array adapter used to render the items on the player rating list view
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class PlayerRatingArrayAdapter extends ArrayAdapter<PlayerRating> {

    private final Context context;
    private final int layoutResourceId;
    private final PlayerRating[] data;

    public PlayerRatingArrayAdapter(Context context, int layoutResourceId, PlayerRating[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        PlayerHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new PlayerHolder();
            holder.txtName = row.findViewById(R.id.txtName);
            holder.txtRank = row.findViewById(R.id.txtRank);

            row.setTag(holder);
        } else {
            holder = (PlayerHolder) row.getTag();
        }

        PlayerRating player = data[position];
        holder.txtName.setText(player.getName());
        holder.txtRank.setText(player.getRatingString());

        return row;
    }

    static class PlayerHolder {
        TextView txtName;
        TextView txtRank;
    }
}
