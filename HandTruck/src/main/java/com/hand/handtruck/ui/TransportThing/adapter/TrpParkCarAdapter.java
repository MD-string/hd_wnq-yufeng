package com.hand.handtruck.ui.TransportThing.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hand.handtruck.R;
import com.hand.handtruck.ui.TransportThing.bean.TransportBean;
import com.hand.handtruck.utils.Tools;

import java.util.List;

import static com.hand.handtruck.application.MyApplication.context;

/**
 * Created by wcf on 2018-03-08.
 * describe:公司及子条目车辆适配器
 * Company: Shen Zhen Hand Hitech(深圳汉德网络科技)
 */

public class TrpParkCarAdapter extends BaseAdapter {
    private Context mContext;
    private List<TransportBean> mList;

    public TrpParkCarAdapter(Context mContext, List<TransportBean> list) {
        this.mContext = mContext;
        this.mList = mList;
    }
    public void updateListView(List<TransportBean> list){
        this.mList = list;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        if (mList != null ){
            return mList.size();
        }else{
            return 0;
        }
    }


    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHold holder;
        if (convertView == null) {
            holder = new ViewHold();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_transport_errorpark, null);
            holder.rl_1=(RelativeLayout)convertView.findViewById(R.id.rl_1);
            holder.tv_order_number = (TextView) convertView.findViewById(R.id.tv_order_number);
            holder.tv_order_carnuber = (TextView) convertView.findViewById(R.id.tv_order_carnuber);
            holder.tv_company_name = (TextView) convertView.findViewById(R.id.tv_company_name);
            holder.tv_order_status = (TextView) convertView.findViewById(R.id.tv_order_status);
            holder.tv_ji_tuan = (TextView) convertView.findViewById(R.id.tv_ji_tuan);
            holder.tv_ouline_address=(TextView)convertView.findViewById(R.id.tv_ouline_address);
            holder.tv_out_date=(TextView)convertView.findViewById(R.id.tv_out_date);
            convertView.setTag(holder);
        } else {
            holder = (ViewHold) convertView.getTag();
        }
        final TransportBean cb = mList.get(position);
        holder.tv_order_number.setText(cb.getOrderCode());
        holder.tv_order_carnuber.setText(cb.getCarNumber());
        holder.tv_company_name.setText(cb.getCustName());
        String factoryName=cb.getFactoryName();
        if(Tools.isEmpty(factoryName)){
            factoryName=mContext.getResources().getString(R.string.cp_name1);
        }
        holder.tv_ji_tuan.setText(factoryName);

        String offDate=cb.getParkStartTime();

        holder.tv_out_date.setText(offDate+"");
        holder.tv_ouline_address.setText(cb.getAddress()+"");


        return convertView;
    }

    class ViewHold {
        RelativeLayout rl_1;
        TextView tv_order_number;
        TextView tv_order_carnuber;
        TextView tv_company_name;
        TextView tv_order_status;
        TextView tv_ji_tuan;

        TextView tv_ouline_address;
        TextView tv_out_date;
    }


}
