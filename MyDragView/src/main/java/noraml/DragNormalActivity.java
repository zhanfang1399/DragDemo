package noraml;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.yff.R;

import java.util.ArrayList;

import myinterface.ItemDragHelperCallback;

public class DragNormalActivity extends AppCompatActivity {

    private RecyclerView gridRecycleView;
    private ArrayList<String> list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_normal);



        assignViews();
        getData();

        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,3);
        gridRecycleView.setLayoutManager(gridLayoutManager);

        ItemDragHelperCallback callback = new ItemDragHelperCallback(){
            @Override
            public boolean isLongPressDragEnabled() {
                // 长按拖拽打开
                return true;
            }
        };

        ItemTouchHelper itemTouchHelper=new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(gridRecycleView);

        DragNormalAdapter adapter=new DragNormalAdapter(this,list);
        gridRecycleView.setAdapter(adapter);


    }


    public void getData(){
        list=new ArrayList<>();
        for(int i=0;i<30;i++){
            list.add("pos-"+i);
        }
    }

    private void assignViews() {
        gridRecycleView = (RecyclerView) findViewById(R.id.gridRecycleView);
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
