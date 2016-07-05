package complex;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yff.R;

import java.util.ArrayList;

import myinterface.OnDragVHListener;
import myinterface.OnItemMoveListener;

/**
 * 拖拽派讯+增删
 * Created by Administrator on 2016/6/29.
 */
public class DragComplexAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemMoveListener {


    // 我的频道 标题部分
    public static final int TYPE_MY_HEADER = 0;
    // 我的频道
    public static final int TYPE_MY = 1;
    // 其他频道 标题部分
    public static final int TYPE_OTHER_HEADER = 2;
    // 其他频道
    public static final int TYPE_OTHER = 3;

    // 我的频道之前的header数量  该demo中 即标题部分 为 1
    private static final int COUNT_PRE_MY_HEADER = 1;
    // 其他频道之前的header数量  该demo中 即标题部分 为 COUNT_PRE_MY_HEADER + 1
    private static final int COUNT_PRE_OTHER_HEADER = COUNT_PRE_MY_HEADER + 1;

    private static final long ANIM_TIME = 360L;

    // touch 点击开始时间
    private long startTime;
    // touch 间隔时间  用于分辨是否是 "点击"
    private static final long SPACE_TIME = 100;

    private LayoutInflater mInflater;
    private ItemTouchHelper mItemTouchHelper;

    // 是否为 编辑 模式
    private boolean isEditMode=false;


    private ArrayList<DragComplexEntity> mMyChannelItems, mOtherChannelItems;
    private LayoutInflater inflater;
    //设置监听事件
    private OnMyItemClickListener myItemClickListener;


    public DragComplexAdapter(Context context, ItemTouchHelper helper, ArrayList<DragComplexEntity> list, ArrayList<DragComplexEntity> listOther) {
        this.mItemTouchHelper = helper;
        this.mMyChannelItems = list;
        this.mOtherChannelItems = listOther;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_MY_HEADER;
        } else if (position > 0 && position < mMyChannelItems.size()+1) {
            return TYPE_MY;
        } else if (position == mMyChannelItems.size() + 1) {
            return TYPE_OTHER_HEADER;
        } else {
            return TYPE_OTHER;
        }
    }




    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case TYPE_MY_HEADER:
                view = inflater.inflate(R.layout.item_my_channel_header, parent, false);
                return typeMyHeader(view, parent);
            case TYPE_MY:
                view = inflater.inflate(R.layout.item_my, parent, false);
                return typeMyContent(view, parent);
            case TYPE_OTHER_HEADER:
                view = inflater.inflate(R.layout.item_other_channel_header, parent, false);
                return new RecyclerView.ViewHolder(view) {
                };
            case TYPE_OTHER:
                view = inflater.inflate(R.layout.item_other, parent, false);
                return typeOtherContent(view, parent);
        }
        return null;
    }


    /**
     * 我的head
     *
     * @param view   view
     * @param parent parent
     * @return holder
     */
    public MyChannelHeaderViewHolder typeMyHeader(View view, final ViewGroup parent) {
        final MyChannelHeaderViewHolder holder = new MyChannelHeaderViewHolder(view);
        holder.tvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEditMode) {
                    startEditMode((RecyclerView) parent);
                    holder.tvEdit.setText("完成");
                } else {
                    cancelEditMode((RecyclerView) parent);
                    holder.tvEdit.setText("编辑");
                }
            }
        });
        return holder;
    }


    /**
     * 我的频道
     *
     * @param view   view
     * @param parent panret
     */
    public MyViewHolder typeMyContent(View view, final ViewGroup parent) {
        final MyViewHolder holder = new MyViewHolder(view);
        holder.tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (isEditMode) {
                    RecyclerView recyclerView = (RecyclerView) parent;
                    View targetView = recyclerView.getLayoutManager()
                            .findViewByPosition(mMyChannelItems.size() + COUNT_PRE_OTHER_HEADER);
                    View currentView = recyclerView.getLayoutManager().findViewByPosition(position);
                    // 如果targetView不在屏幕内,则indexOfChild为-1  此时不需要添加动画,因为此时notifyItemMoved自带一个向目标移动的动画
                    // 如果在屏幕内,则添加一个位移动画
                    if (recyclerView.indexOfChild(targetView) >= 0) {
                        int targetX, targetY;
                        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                        int spanCount = ((GridLayoutManager) manager).getSpanCount();
                        // 移动后 高度将变化 (我的频道Grid 最后一个item在新的一行第一个)
                        if ((mMyChannelItems.size() - COUNT_PRE_MY_HEADER) % spanCount == 0) {
                            View preTargetView = recyclerView.getLayoutManager()
                                    .findViewByPosition(mMyChannelItems.size() + COUNT_PRE_OTHER_HEADER - 1);
                            targetX = preTargetView.getLeft();
                            targetY = preTargetView.getTop();
                        } else {
                            targetX = targetView.getLeft();
                            targetY = targetView.getTop();
                        }
                        moveMyToOther(holder);
                        Log.i("targetX-other:",""+targetX);
                        Log.i("targetY-other:",""+targetY);
//                        startAnimation(recyclerView, currentView, targetX, targetY);
                    } else {
                        moveMyToOther(holder);
                    }
                } else {
                    myItemClickListener.onItemClick(v, position - COUNT_PRE_MY_HEADER);
                }
            }
        });

        holder.tv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isEditMode) {
                    RecyclerView recyclerView = (RecyclerView) parent;
                    startEditMode(recyclerView);

                    View view1 = recyclerView.getChildAt(0);
                    if (view1 == recyclerView.getLayoutManager().findViewByPosition(0)) {
                        TextView textEdit = (TextView) view1.findViewById(R.id.tv_btn_edit);
                        textEdit.setText("完成");

                    }
                }
                mItemTouchHelper.startDrag(holder);
                return true;
            }
        });


        holder.tv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isEditMode) {
                    switch (MotionEventCompat.getActionMasked(event)) {
                        case MotionEvent.ACTION_DOWN:
                            startTime = System.currentTimeMillis();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (System.currentTimeMillis() - startTime > SPACE_TIME) {
                                mItemTouchHelper.startDrag(holder);
                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            startTime = 0;
                            break;
                    }
                }
                return false;
            }
        });

        return holder;

    }


    /**
     * myOtherContent
     */
    public RecyclerView.ViewHolder typeOtherContent(View view, final ViewGroup parent) {
        final OtherViewHolder holder = new OtherViewHolder(view);
        holder.otherTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView recyclerView = (RecyclerView) parent;
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                int currentPos = holder.getAdapterPosition();
                // 如果RecyclerView滑动到底部,移动的目标位置的y轴 - height
                View currentView = manager.findViewByPosition(currentPos);
                // 目标位置的前一个item  即当前MyChannel的最后一个
                View preTargetView = manager.findViewByPosition(mMyChannelItems.size() - 1 + COUNT_PRE_MY_HEADER);
                // 如果targetView不在屏幕内,则为-1  此时不需要添加动画,因为此时notifyItemMoved自带一个向目标移动的动画
                // 如果在屏幕内,则添加一个位移动画
                if (recyclerView.indexOfChild(preTargetView) >= 0) {
                    int targetX = preTargetView.getLeft();
                    int targetY = preTargetView.getTop();
                    int targetPos = mMyChannelItems.size() - 1 + COUNT_PRE_OTHER_HEADER;
                    GridLayoutManager gridLayoutManager = (GridLayoutManager) manager;
                    int spanCount = gridLayoutManager.getSpanCount();
                    // target 在最后一行第一个
                    if ((targetPos - COUNT_PRE_MY_HEADER) % spanCount == 0) {
                        View targetView = manager.findViewByPosition(targetPos);
                        targetX = targetView.getLeft();
                        targetY = targetView.getTop();
                    } else {
                        targetX += preTargetView.getWidth();
                        //最后一个item可见
                        if (gridLayoutManager.findLastVisibleItemPosition() == getItemCount() - 1) {
                            //最后的item在最后一行的第一个位置
                            if ((getItemCount() - 1 - mMyChannelItems.size() - COUNT_PRE_OTHER_HEADER) % spanCount == 0) {
                                // RecyclerView实际高度 > 屏幕高度 && RecyclerView实际高度 < 屏幕高度 + item.height
                                int firstVisiblePostion = gridLayoutManager.findFirstVisibleItemPosition();
                                if (firstVisiblePostion == 0) {
                                    // FirstCompletelyVisibleItemPosition == 0 即 内容不满一屏幕 , targetY值不需要变化
                                    // // FirstCompletelyVisibleItemPosition != 0 即 内容满一屏幕 并且 可滑动 , targetY值 + firstItem.getTop
                                    if (gridLayoutManager.findFirstCompletelyVisibleItemPosition() != 0) {
                                        int offset = (-recyclerView.getChildAt(0).getTop()) - recyclerView.getPaddingTop();
                                        targetY += offset;
                                    }
                                } else { // 在这种情况下 并且 RecyclerView高度变化时(即可见第一个item的 position != 0),
                                    // 移动后, targetY值  + 一个item的高度
                                    targetY += preTargetView.getHeight();
                                }
                            }
                        } else {
                            System.out.println("current-no");
                        }
                    }

                    // 如果当前位置是otherChannel可见的最后一个
                    // 并且 当前位置不在grid的第一个位置
                    // 并且 目标位置不在grid的第一个位置

                    // 则 需要延迟250秒 notifyItemMove , 这是因为这种情况 , 并不触发ItemAnimator , 会直接刷新界面
                    // 导致我们的位移动画刚开始,就已经notify完毕,引起不同步问题
                    if (currentPos == gridLayoutManager.findLastVisibleItemPosition()
                            && (currentPos - mMyChannelItems.size() - COUNT_PRE_OTHER_HEADER) % spanCount != 0
                            && (targetPos - COUNT_PRE_MY_HEADER) % spanCount != 0) {
                        moveOtherToMyWithDelay(holder);
                    } else {
                        moveOtherToMy(holder);
                    }
                    Log.i("targetX-other:",""+targetX);
                    Log.i("targetY-other:",""+targetY);
//                    startAnimation(recyclerView, currentView, targetX, targetY);
                } else {
                    moveOtherToMy(holder);
                }
            }
        });

        return holder;

    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            myViewHolder.tv.setText(mMyChannelItems.get(position - COUNT_PRE_MY_HEADER).getName());
            if (isEditMode) {
                myViewHolder.iv.setVisibility(View.VISIBLE);
            } else {
                myViewHolder.iv.setVisibility(View.INVISIBLE);
            }

        } else if (holder instanceof OtherViewHolder) {
            OtherViewHolder otherViewHolder = (OtherViewHolder) holder;

            Log.i("item个数：pos", ""+(position));
            Log.i("item个数：myList", ""+(mMyChannelItems.size()));
            otherViewHolder.otherTv.setText(mOtherChannelItems.get(position - mMyChannelItems.size() - COUNT_PRE_OTHER_HEADER).getName());

        } else if (holder instanceof MyChannelHeaderViewHolder) {
            MyChannelHeaderViewHolder myChannelHolder = (MyChannelHeaderViewHolder) holder;
            if (isEditMode) {
                myChannelHolder.tvEdit.setText("完成");
            } else {
                myChannelHolder.tvEdit.setText("编辑");
            }

        }


    }


    @Override
    public int getItemCount() {
        //我的频道size   +   其他频道 size+ 我的频道head size  +  其他频道 head size
        Log.i("item个数：", ""+(mMyChannelItems.size() + mOtherChannelItems.size() + COUNT_PRE_OTHER_HEADER));
        return mMyChannelItems.size() + mOtherChannelItems.size() + COUNT_PRE_OTHER_HEADER;
    }


    @Override
    public void onItemMove(int fromPos, int toPos) {
        DragComplexEntity entity = mMyChannelItems.get(fromPos - COUNT_PRE_MY_HEADER);
        mMyChannelItems.remove(fromPos - COUNT_PRE_MY_HEADER);
        mMyChannelItems.add(toPos - COUNT_PRE_MY_HEADER, entity);
        notifyItemMoved(fromPos, toPos);
    }


    /**
     * 开始增删动画
     *
     * @param recyclerView view
     * @param currentView  view
     * @param targetX      x
     * @param targetY      y
     */



    private void startAnimation(RecyclerView recyclerView, final View currentView, float targetX, float targetY) {
        final ViewGroup viewGroup = (ViewGroup) recyclerView.getParent();
        final ImageView mirrorView = addMirrorView(viewGroup, recyclerView, currentView);
        Animation animation = getTranslateAnimator(targetX - currentView.getLeft()
                , targetY - currentView.getTop());
        currentView.setVisibility(View.INVISIBLE);
        mirrorView.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewGroup.removeView(mirrorView);
                if (currentView.getVisibility() == View.INVISIBLE) {
                    currentView.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }


    /**
     * 我的频道移动到其他的频道
     */
    private void moveMyToOther(MyViewHolder myHolder) {
        int pos = myHolder.getAdapterPosition();
        int startPos = pos - COUNT_PRE_MY_HEADER;
        if (startPos > mMyChannelItems.size() - 1) {
            return;
        }

        DragComplexEntity item = mMyChannelItems.get(startPos);
        mMyChannelItems.remove(startPos);
        mOtherChannelItems.add(0, item);
        notifyItemMoved(pos, mMyChannelItems.size()  + COUNT_PRE_OTHER_HEADER);
    }


    /**
     * 其他频道移动到我的频道   无延迟
     */

    private void moveOtherToMy(OtherViewHolder otherHolder) {
        int pos = proccesItemRemoveAdd(otherHolder);
        if (pos == -1) {
            return;
        }
        notifyItemMoved(pos, mMyChannelItems.size() - 1 + COUNT_PRE_MY_HEADER);
    }


    /**
     * 其他频道到我的频道，伴随延迟
     *
     * @param otherHolder view
     */
    private void moveOtherToMyWithDelay(OtherViewHolder otherHolder) {
        final int pos = proccesItemRemoveAdd(otherHolder);
        if (pos == -1) {
            return;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyItemMoved(pos, mMyChannelItems.size() - 1 + COUNT_PRE_MY_HEADER);
            }
        }, ANIM_TIME);


    }

    private Handler handler = new Handler();

    /**
     * itemVIEW
     *
     * @param otherHolder OTHER
     * @return POS
     */
    private int proccesItemRemoveAdd(OtherViewHolder otherHolder) {
        int pos = otherHolder.getAdapterPosition();
        int startPos = pos - mMyChannelItems.size() - COUNT_PRE_OTHER_HEADER;
        if (startPos > mOtherChannelItems.size() - 1) {
            return -1;
        }

        DragComplexEntity item = mOtherChannelItems.get(startPos);
        mOtherChannelItems.remove(startPos);
        mMyChannelItems.add(item);
        return pos;
    }

    /**
     * 添加需要移动的 镜像View
     */
    private ImageView addMirrorView(ViewGroup parent, RecyclerView recyclerView, View view) {
        /**
         * 我们要获取cache首先要通过setDrawingCacheEnable方法开启cache，然后再调用getDrawingCache方法就可以获得view的cache图片了。
         buildDrawingCache方法可以不用调用，因为调用getDrawingCache方法时，若果cache没有建立，系统会自动调用buildDrawingCache方法生成cache。
         若想更新cache, 必须要调用destoryDrawingCache方法把旧的cache销毁，才能建立新的。
         当调用setDrawingCacheEnabled方法设置为false, 系统也会自动把原来的cache销毁。
         */
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        final ImageView mirrorView = new ImageView(recyclerView.getContext());
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        mirrorView.setImageBitmap(bitmap);
        view.setDrawingCacheEnabled(false);
        int[] locations = new int[2];
        view.getLocationOnScreen(locations);
        int[] parenLocations = new int[2];
        recyclerView.getLocationOnScreen(parenLocations);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        params.setMargins(locations[0], locations[1] - parenLocations[1], 0, 0);
        parent.addView(mirrorView, params);

        return mirrorView;
    }




    /**
     * 开启编辑模式
     *
     * @param parent
     */
    private void startEditMode(RecyclerView parent) {
        isEditMode = true;

        int visibleChildCount = parent.getChildCount();
        for (int i = 0; i < visibleChildCount; i++) {
            View view = parent.getChildAt(i);
            ImageView imgEdit = (ImageView) view.findViewById(R.id.img_edit);
            if (imgEdit != null) {
                imgEdit.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 完成编辑模式
     *
     * @param parent
     */
    private void cancelEditMode(RecyclerView parent) {
        isEditMode = false;

        int visibleChildCount = parent.getChildCount();
        for (int i = 0; i < visibleChildCount; i++) {
            View view = parent.getChildAt(i);
            ImageView imgEdit = (ImageView) view.findViewById(R.id.img_edit);
            if (imgEdit != null) {
                imgEdit.setVisibility(View.INVISIBLE);
            }
        }
    }


    /**
     * 获取位移动画
     */
    private TranslateAnimation getTranslateAnimator(float targetX, float targetY) {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetX,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetY);
        // RecyclerView默认移动动画250ms 这里设置360ms 是为了防止在位移动画结束后 remove(view)过早 导致闪烁
        translateAnimation.setDuration(ANIM_TIME);
        translateAnimation.setFillAfter(true);
        return translateAnimation;
    }

    /**
     * 点击的接口
     */
    interface OnMyItemClickListener {
        void onItemClick(View view, int pos);
    }

    /**
     * 8
     * 设置监听事件
     *
     * @param listener listener
     */
    public void setOnMyItemClickListener(OnMyItemClickListener listener) {
        this.myItemClickListener = listener;
    }


    /**
     * 我的list
     */
    public class MyViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener {

        private TextView tv;
        private ImageView iv;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv);
            iv = (ImageView) itemView.findViewById(R.id.img_edit);
        }

        /**
         * item被选中时
         */
        @Override
        public void onItemSelected() {
            tv.setBackgroundResource(R.drawable.bg_channel_p);
        }


        @Override
        public void onItemFinish() {
            tv.setBackgroundResource(R.drawable.bg_channel);

        }


    }


    /**
     * 我的HeadView   标题部分   相当于header
     */
    public class MyChannelHeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView tvEdit;

        public MyChannelHeaderViewHolder(View itemView) {
            super(itemView);
            tvEdit = (TextView) itemView.findViewById(R.id.tv_btn_edit);
        }
    }


    /**
     * 其他频道
     */
    public class OtherViewHolder extends RecyclerView.ViewHolder {
        private TextView otherTv;

        public OtherViewHolder(View itemView) {
            super(itemView);
            otherTv = (TextView) itemView.findViewById(R.id.tv);
        }
    }
}
