package app.com.smartrec.Adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.com.smartrec.Models.cloudcontactModel;
import app.com.smartrec.R;

public class CloudContactRecAdapter extends RecyclerView.Adapter<CloudContactRecAdapter.CloudContactViewHolder> {

    private static List<cloudcontactModel> list = new ArrayList<>();
    private Callback callback;

    @Override
    public CloudContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cloudcontact_items, parent, false);
        return new CloudContactViewHolder(view, callback);
    }

    @Override
    public void onBindViewHolder(CloudContactViewHolder holder, int position) {
        holder.itemView.setLongClickable(true);
        holder.bindData(getItemByPosition(position));
    }

    public static cloudcontactModel getItemByPosition(int position) {
        return list.get(position);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setList(List<cloudcontactModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface Callback {
        void onItemClick(View view, cloudcontactModel cloudcontact);
    }

    public static class CloudContactViewHolder extends RecyclerView.ViewHolder {
        public static final String TAG = CloudContactViewHolder.class.getSimpleName();

        private TextView cloudName, cloudPhone;
        private ImageView menuimageView;
        private Callback callback;
        private Context context;

        public CloudContactViewHolder(View itemView, final Callback callback) {
            super(itemView);

            this.callback = callback;
            this.context = itemView.getContext();

            cloudName = itemView.findViewById(R.id.cloudcontact_name);
            cloudPhone = itemView.findViewById(R.id.cloudcontact_number);
            menuimageView = itemView.findViewById(R.id.cloudcontact_menu_icon);

            if (callback != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            callback.onItemClick(v, getItemByPosition(position));
                        }
                    }
                });
            }
        }

        public void bindData(final cloudcontactModel cloudcontactm) {

            cloudName.setText(cloudcontactm.getContact_name());
            cloudPhone.setText(cloudcontactm.getContact_number());

            menuimageView.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(context, itemView, Gravity.END, 0, R.style.PopupMenuMoreCentralized);
                    popup.getMenuInflater().inflate(R.menu.cloud_contacts, popup.getMenu());
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.call_contact:
                                    Toast.makeText(context, "Call", Toast.LENGTH_SHORT).show();
                                    break;

                                case R.id.remove_contact:
                                    Toast.makeText(context, "Remove", Toast.LENGTH_SHORT).show();
                                    break;

                            }

                            return false;
                        }
                    });
                }
            });

        }
    }
}
