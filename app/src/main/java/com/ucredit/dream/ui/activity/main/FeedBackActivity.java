package com.ucredit.dream.ui.activity.main;

import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.ucredit.dream.R;
import com.ucredit.dream.utils.Utils;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.SyncListener;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Reply;

@SuppressLint("HandlerLeak")
public class FeedBackActivity extends BaseActivity {

    public static final int VIEW_TYPE_COUNT = 2;
    public static final int VIEW_TYPE_DEV = 0;
    public static final int VIEW_TYPE_USER = 1;
    @ViewInject(R.id.fb_reply_list)
    private ListView listView;
    @ViewInject(R.id.fb_send_btn)
    private Button button;
    @ViewInject(R.id.fb_send_content)
    private EditText editText;
    @ViewInject(R.id.fb_reply_refresh)
    private SwipeRefreshLayout refreshLayout;

    private Conversation mComversation;
    private ReplyAdapter adapter;
    private FeedbackAgent mAgent;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FeedBackActivity.this.adapter.notifyDataSetChanged();
        }
    };

    @OnClick(R.id.fb_send_btn)
    private void click(View view) {
        switch (view.getId()) {
            case R.id.fb_send_btn:
                String content = this.editText.getText().toString();
                
                if (Utils.isNotEmptyString(content)) {
                    this.mComversation.addUserReply(content);//添加到会话列表
                    this.mHandler.sendMessage(new Message());
                    this.sync();
                }else {
                    Utils.MakeToast(this, "输入内容不能为空");
                }
                this.editText.getEditableText().clear();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        this.refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (null == FeedBackActivity.this.mComversation.getReplyList()
                    || 0 == FeedBackActivity.this.mComversation.getReplyList()
                        .size()) {
                    FeedBackActivity.this.refreshLayout.setRefreshing(false);
                    return;
                }
                FeedBackActivity.this.sync();
            }
        });
        this.mAgent = new FeedbackAgent(this);
        this.mComversation = this.mAgent.getDefaultConversation();
        this.adapter = new ReplyAdapter();
        View view = LayoutInflater.from(FeedBackActivity.this).inflate(
            R.layout.custom_fb_dev_reply, null);
        TextView textView = (TextView) view.findViewById(R.id.fb_reply_content);
        textView.setText("你好！我是产品经理，欢迎您反馈使用产品的感受和建议");
        textView.setGravity(Gravity.LEFT);
        this.listView.addHeaderView(view);
        this.listView.setAdapter(this.adapter);
        this.sync();
    }

    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "意见反馈";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_feedback;
    }

    private void sync() {

        this.mComversation.sync(new SyncListener() {

            @Override
            public void onSendUserReply(List<Reply> replyList) {
            }

            @Override
            public void onReceiveDevReply(List<Reply> replyList) {
                FeedBackActivity.this.refreshLayout.setRefreshing(false);
                if (replyList == null || replyList.size() < 1) {
                    return;
                }
                FeedBackActivity.this.mHandler.sendMessage(new Message());
            }
        });
    }

    class ReplyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return FeedBackActivity.this.mComversation.getReplyList().size();
        }

        @Override
        public Object getItem(int arg0) {
            return FeedBackActivity.this.mComversation.getReplyList().get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public int getViewTypeCount() {
            // 两种不同的Item布局  
            return FeedBackActivity.VIEW_TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            // 获取单条回复  
            Reply reply = FeedBackActivity.this.mComversation.getReplyList()
                .get(position);
            if (Reply.TYPE_DEV_REPLY.equals(reply.type)) {
                // 开发者回复Item布局  
                return FeedBackActivity.VIEW_TYPE_DEV;
            } else {
                // 用户反馈、回复Item布局  
                return FeedBackActivity.VIEW_TYPE_USER;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            // 获取单条回复  
            Reply reply = FeedBackActivity.this.mComversation.getReplyList()
                .get(position);
            if (convertView == null) {
                // 根据Type的类型来加载不同的Item布局  
                if (Reply.TYPE_DEV_REPLY.equals(reply.type)) {
                    // 开发者的回复  
                    convertView = LayoutInflater.from(FeedBackActivity.this)
                        .inflate(R.layout.custom_fb_dev_reply, null);
                } else {
                    // 用户的反馈、回复  
                    convertView = LayoutInflater.from(FeedBackActivity.this)
                        .inflate(R.layout.custom_fb_user_reply, null);
                }
                holder = new ViewHolder();
                holder.replyContent = (TextView) convertView
                    .findViewById(R.id.fb_reply_content);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // 以下是填充数据  
            // 设置Reply的内容  
            holder.replyContent.setText(reply.content);
            return convertView;
        }

        class ViewHolder {
            TextView replyContent;
        }
    }

}
