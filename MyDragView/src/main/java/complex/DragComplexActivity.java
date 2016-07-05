package complex;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.yff.R;

import java.util.ArrayList;

import myinterface.ItemDragHelperCallback;

public class DragComplexActivity extends ActionBarActivity {

    private RecyclerView complexRecycleView;

    private ArrayList<DragComplexEntity> listMy,listOther;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_complex);
        assignViews();
        getDataMy();
        getDataOther();

        setContentData();

    }



    public void setContentData(){
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,4);
        complexRecycleView.setLayoutManager(gridLayoutManager);

        ItemDragHelperCallback callback=new ItemDragHelperCallback();
        ItemTouchHelper helper=new ItemTouchHelper(callback);
        helper.attachToRecyclerView(complexRecycleView);

        final DragComplexAdapter adapter=new DragComplexAdapter(this,helper,listMy,listOther);




        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType=adapter.getItemViewType(position);
                return viewType==DragComplexAdapter.TYPE_MY || viewType==DragComplexAdapter.TYPE_OTHER ? 1:4;
            }
        });

        adapter.setOnMyItemClickListener(new DragComplexAdapter.OnMyItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                Toast.makeText(DragComplexActivity.this,listMy.get(pos).getName()+"为什么！！！",Toast.LENGTH_SHORT).show();

            }
        });

        complexRecycleView.setAdapter(adapter);

    }

    public void getDataMy(){
        listMy=new ArrayList<>();
        for(int i=0;i<=15;i++){
            DragComplexEntity entity=new DragComplexEntity();
            entity.setName("频道"+i);
            listMy.add(entity);
        }
    }


    public void getDataOther(){
        listOther=new ArrayList<>();
        for(int i=0;i<=15;i++){
            DragComplexEntity entity=new DragComplexEntity();
            entity.setName("其他"+i);
            listOther.add(entity);
        }
    }
    private void assignViews() {
        complexRecycleView = (RecyclerView) findViewById(R.id.complexRecycleView);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Toast.makeText(this,"设置",Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
