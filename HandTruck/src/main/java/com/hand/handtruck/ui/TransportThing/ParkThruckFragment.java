package com.hand.handtruck.ui.TransportThing;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.hand.handtruck.R;
import com.hand.handtruck.Widget.listview.XListView;
import com.hand.handtruck.bean.PagerBean;
import com.hand.handtruck.constant.ConstantsCode;
import com.hand.handtruck.ui.TransportThing.adapter.TrpParkCarAdapter;
import com.hand.handtruck.ui.TransportThing.bean.TransportBean;
import com.hand.handtruck.ui.TransportThing.presenter.ParkCarAlarmTask;
import com.hand.handtruck.ui.form.FormInfoActivity;
import com.hand.handtruck.ui.form.bean.FormBean;
import com.hand.handtruck.ui.form.bean.SearchBean;
import com.hand.handtruck.ui.home.BaseFragment;
import com.hand.handtruck.utils.ToastUtil;
import com.hand.handtruck.utils.Tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 停车警报
 * @author hxz
 */

public class ParkThruckFragment extends BaseFragment implements OnClickListener, XListView.IXListViewListener {


	private XListView list_error;
	private Context mcontext;
	private TrpParkCarAdapter madapter;
	private List<TransportBean> list=new ArrayList<>();
	private ParkCarAlarmTask tranTask;
	private SharedPreferences sp;
	private HashMap<String, String> mapParams;
	private String token;
	private int currentPage;
	private List<TransportBean> tlist=new ArrayList<>();
	private TextView tv_current_page,tv_total_page;
	private BroadcastReceiver receiver;
	boolean isSearch;
	private String dateNow,dateYerterday;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = inflater.inflate(R.layout.frag_trp_error, container, false);
		Bundle bun= getArguments();
		dateNow=bun.getString("date_now");
		dateYerterday=bun.getString("date_yeterday10");
		initView(view);
		registerBrodcat();

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	private void initView(View view) {
		mcontext=getActivity();
		list_error=(XListView)view.findViewById(R.id.list_error);
		//		View headView = View.inflate(getActivity(), R.layout.frg_error_poweroff, null);
		//		list_error.addHeaderView(headView);
		list_error.setPullRefreshEnable(true);
		list_error.setPullLoadEnable(true);
		list_error.setXListViewListener(this);
		list_error.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				if(tlist !=null && tlist.size() >0 ){
					TransportBean bean=	tlist.get(position-1);
					String code=bean.getOrderCode()+"";
					if(Tools.isEmpty(code)){
						ToastUtil.getInstance().showCenterMessage(mcontext, "订单编号为空");
						return;
					}
					HashMap	mapParams1 = new HashMap<>();
					mapParams1.put("token", token);
					mapParams1.put("orderCode",code);
					tranTask.getOrderInfo(mapParams1);

				}else{
					ToastUtil.getInstance().showCenterMessage(mcontext, "数据为空");
				}

			}
		});

		madapter=new TrpParkCarAdapter(mcontext,list);
		list_error.setAdapter(madapter);

		tv_current_page=(TextView)view.findViewById(R.id.tv_current_page);//当前页面
		tv_total_page=(TextView)view.findViewById(R.id.tv_total_page); //总页码

		tranTask = ParkCarAlarmTask.getInstance(mcontext, mHandler);
		sp = mcontext.getSharedPreferences(ConstantsCode.FILE_NAME, 0);
		initData(0);
	}
	private void initData(int first){
		mapParams = new HashMap<>();
		token = (String) sp.getString("token", null);
		mapParams.put("token", token);
		mapParams.put("nowPage","1");
		mapParams.put("pageSize","10");
		mapParams.put("type","4");
		mapParams.put("startTime",dateYerterday);
		mapParams.put("endTime",dateNow);
		if(0 == first){
			tranTask.getTransportList(mapParams);
		}else{
			tranTask.pullListData(mapParams);
		}
	}


	@Override
	public void onClick(View v) {
	}



	//刷新
	@Override
	public void onRefresh() {  //方法无回调？
		//不延迟请求总报Token无效
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				//调网络接口 请求数据
				initData(1);
				onLoad();
				//				isRefresh=true;
			}
		}, 300);
	}

	//加载更多
	@Override
	public void onLoadMore() {
		//不延迟请求总报Token无效
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mapParams = new HashMap<>();
				token = (String) sp.getString("token", null);
				mapParams.put("token", token);
				mapParams.put("nowPage",String.valueOf(currentPage+1));
				mapParams.put("pageSize","10");
				mapParams.put("type","4");
				mapParams.put("startTime",dateYerterday);
				mapParams.put("endTime",dateNow);

				tranTask.loadMoreData(mapParams);
				onLoad();
			}
		}, 300);

	}

	private void onLoad() {
		list_error.stopRefresh();
		list_error.stopLoadMore();
	}
	@SuppressLint("HandlerLeak")
	Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case ConstantsCode.MSG_REQUEST_SUCCESS:
					tlist.clear();
					currentPage = 1;
					PagerBean transportbean = (PagerBean) msg.obj;
					tlist = transportbean.getContent();
					String totalPage=transportbean.getTotalPage();
					String totalElement=transportbean.getTotalElement();

					//					List<TransportBean> mlist =new ArrayList<>();
					//					if(tlist!=null && tlist.size() > 0){
					//						for(int i=0;i<tlist.size();i++){
					//							TransportBean bean =tlist.get(i);
					//							String status=bean.getStatus();
					//							if("-1".equals(status)){
					//								mlist.add(bean);
					//							}
					//
					//						}
					//					}
					if(tlist !=null && tlist.size() >0){

						madapter.updateListView(tlist);
						if(!Tools.isEmpty(totalPage)){
							int tPage=Integer.parseInt(totalPage);
							if(tPage <=1){
								list_error.setPullLoadEnable(false);
							}else{
								list_error.setPullLoadEnable(true);
							}
						}

						tv_current_page.setText(currentPage+"");
						tv_total_page.setText("/"+totalPage);

						Intent erIntent=new Intent(ConstantsCode.TRANSPORT_PARK_CAR);
						erIntent.putExtra("error_park_car",totalElement);
						mcontext.sendBroadcast(erIntent);

					}else{
						ToastUtil.getInstance().showCenterMessage(mcontext, "数据为空");
					}
					break;


				case ConstantsCode.MSG_REQUEST_FAIL:
					ToastUtil.getInstance().showCenterMessage(mcontext, "获取数据失败");
					break;
				case ConstantsCode.MSG_REQUEST_SUCCESS1:
					tlist.clear();
					currentPage = 1;
					PagerBean transportbean1 = (PagerBean) msg.obj;
					tlist= transportbean1.getContent();
					String totalPage1=transportbean1.getTotalPage();
					String totalElement1=transportbean1.getTotalElement();
					if(tlist !=null && tlist.size() >0){

						madapter.updateListView(tlist);
						if(!Tools.isEmpty(totalPage1)){
							int tPage1=Integer.parseInt(totalPage1);
							if(tPage1 <=1){
								list_error.setPullLoadEnable(false);
							}else{
								list_error.setPullLoadEnable(true);
							}
						}

						tv_current_page.setText(currentPage+"");
						tv_total_page.setText("/"+totalPage1);

						Intent erIntent=new Intent(ConstantsCode.TRANSPORT_PARK_CAR);
						erIntent.putExtra("error_park_car",totalElement1);
						mcontext.sendBroadcast(erIntent);
					}else{
						ToastUtil.getInstance().showCenterMessage(mcontext, "数据为空");
					}
					break;

				case ConstantsCode.MSG_REQUEST_FAIL1:
					ToastUtil.getInstance().showCenterMessage(mcontext, "数据刷新失败");
					break;
				case ConstantsCode.MSG_REQUEST_SUCCESS2:
					currentPage ++;
					PagerBean transportbean2 = (PagerBean) msg.obj;
					String tpage2=transportbean2.getTotalPage();
					String totalElement2=transportbean2.getTotalElement();
					List<TransportBean> tlist2= transportbean2.getContent();
					if(tlist2 !=null && tlist2.size() >0){

						for(int j=0;j<tlist2.size();j++){
							TransportBean bean=tlist2.get(j);
							tlist.add(bean);
						}

					}
					madapter.updateListView(tlist);
					if(!Tools.isEmpty(tpage2)){
						int tp2=Integer.parseInt(tpage2);
						if(currentPage <tp2){
							list_error.setPullLoadEnable(true);
						}else{
							list_error.setPullLoadEnable(false);
						}

					}

					tv_current_page.setText(currentPage+"");
					tv_total_page.setText("/"+tpage2);

					Intent erIntent=new Intent(ConstantsCode.TRANSPORT_PARK_CAR);
					erIntent.putExtra("error_park_car",totalElement2);
					mcontext.sendBroadcast(erIntent);
					break;

				case ConstantsCode.MSG_REQUEST_FAIL2:
					loadMoreError();
					ToastUtil.getInstance().showCenterMessage(mcontext, "数据加载失败");
					break;
				case ConstantsCode.MSG_REQUEST_EMPTY:
					ToastUtil.getInstance().showCenterMessage(mcontext, "数据为空");
					break;
				case ConstantsCode.MSG_REQUEST_SUCCESS3:
					FormBean formBean = (FormBean) msg.obj;
					FormInfoActivity.start(mcontext,formBean); //报警信息详情页面
					break;
				default:

					break;

			}
		}
	};



	/**
	 * 上拉加载异常
	 */
	private void loadMoreError() {
		list_error.showErrorTip();
	}

	/**
	 * 上拉加载异常
	 */
	private void loadMoreNotData() {
		list_error.disFooterView();
	}
	/**
	 * 下拉刷新异常
	 */
	private void loadRefreshError() {
		ToastUtil.getInstance().showCenterMessage(mcontext, "加载失败");
		onLoad();
	}

	/**
	 * 注册广播
	 */
	private void registerBrodcat() {
		receiver=new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action=intent.getAction();
				if(action.equals(ConstantsCode.BRO_INFORMATION_SEARCH)){
					SearchBean sbean=(SearchBean)intent.getSerializableExtra("search_bean");
					isSearch=true;
					//                    if(isHidden) {
					HashMap hap = new HashMap<>();
					hap.put("token", token);
					hap.put("nowPage", "1");
					hap.put("pageSize", "50");
					hap.put("type","4");
					hap.put("startTime", sbean.getStartTime());
					hap.put("endTime", sbean.getEndTime());
					hap.put("custName", sbean.getCustName());
					hap.put("address", sbean.getAddress());
					hap.put("carNumber", sbean.getCarNumber());
					tranTask.getTransportList(hap);
					//                    }
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConstantsCode.BRO_INFORMATION_SEARCH);
		getActivity().registerReceiver(receiver, filter);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (receiver != null) {
			getActivity().unregisterReceiver(receiver);
			receiver = null;
		}
	}
}
