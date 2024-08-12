package za.co.sagoclubs;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

public class PlayerArrayAdapter extends ArrayAdapter<Player> implements SectionIndexer {

    private final String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    Context context;
    int layoutResourceId;
    Player[] data;

    public PlayerArrayAdapter(Context context, int layoutResourceId, Player[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public int getPositionForSection(int section) {
        // If there is no item for current section, previous section will be selected
        for (int i = section; i >= 0; i--) {
            for (int j = 0; j < getCount(); j++) {
                Player playerAtJ = getItem(j);
                if (playerAtJ == null) {
                    continue;
                }
                char playerFirstLetter = playerAtJ.getName().charAt(0);
                if (i == 0) {
                    // For numeric section
                    for (int k = 0; k <= 9; k++) {
                        if (StringMatcher.match(String.valueOf(playerFirstLetter), String.valueOf(k)))
                            return j;
                    }
                } else {
                    if (StringMatcher.match(String.valueOf(playerFirstLetter), String.valueOf(mSections.charAt(i))))
                        return j;
                }
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        String[] sections = new String[mSections.length()];
        for (int i = 0; i < mSections.length(); i++)
            sections[i] = String.valueOf(mSections.charAt(i));
        return sections;
    }
}
