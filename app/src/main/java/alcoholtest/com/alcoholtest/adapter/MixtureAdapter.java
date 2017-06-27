package alcoholtest.com.alcoholtest.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;

import alcoholtest.com.alcoholtest.R;
import alcoholtest.com.alcoholtest.model.Mixture;

public class MixtureAdapter extends ArrayAdapter<Mixture> {
    private MixtureAdapter ma;
    private Mixture mixture;
    private Context mContext;

    public MixtureAdapter(Context context, ArrayList<Mixture> arrayList) {
        super(context, R.layout.items_mixture, arrayList);
        ma = this;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.items_mixture, parent, false);
        mixture = getItem(position);


        TextView name = (TextView) v.findViewById(R.id.name);
        TextView amount = (TextView) v.findViewById(R.id.amount);
        TextView percentage = (TextView) v.findViewById(R.id.percentage);
        ImageView image = (ImageView) v.findViewById(R.id.image);

        DecimalFormat format = new DecimalFormat();
        format.setDecimalSeparatorAlwaysShown(false);

        name.setText(mixture.getName());
        if (mixture.getAmount() < 100) {
            amount.setText(format.format(mixture.getAmount()) + " ml");
        } else {
            amount.setText(format.format(mixture.getAmount() / 1000) + " l");
        }
        percentage.setText(format.format(mixture.getPercentage() * 100) + " %");


        image.setImageResource(mContext.getResources().getIdentifier(mixture.getImage(), "drawable",
                mContext.getApplicationContext().getPackageName()));
        return v;
    }
}
