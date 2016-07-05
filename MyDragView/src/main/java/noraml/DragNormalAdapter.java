package noraml;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yff.R;

import java.util.ArrayList;

import myinterface.OnDragVHListener;
import myinterface.OnItemMoveListener;

/**
 * Created by Administrator on 2016/6/29.
 */
public class DragNormalAdapter  extends RecyclerView.Adapter<DragNormalAdapter.ItemViewHolder> implements OnItemMoveListener {

    private LayoutInflater inflater;
    private ArrayList<String> list;

    private Context context;
    public DragNormalAdapter(Context context,ArrayList<String> list) {
        this.context=context;
        this.list = list;
        inflater=LayoutInflater.from(context);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =inflater.inflate(R.layout.item_drag,parent,false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.textView.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onItemMove(int fromPos, int toPos) {
        String temp=list.get(fromPos);
        list.remove(fromPos);
        list.add(toPos,temp);
        notifyItemMoved(fromPos,toPos);
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener{

        private TextView textView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            textView=(TextView)itemView.findViewById(R.id.tv);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);

        }

        @Override
        public void onItemFinish() {
            itemView.setBackgroundColor(0);

        }


    }
}
