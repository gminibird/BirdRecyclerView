package com.gminibird.birdrecyclerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import com.gminibird.birdrecyclerview.adapter.RecyclerAdapter;
import com.gminibird.birdrecyclerview.item.IRecyclerItem;
import com.gminibird.birdrecyclerview.item.SimpleItem;
import com.gminibird.birdrecyclerview.view.BirdRecyclerView;

public class MainActivity extends AppCompatActivity {

    private List<IRecyclerItem> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        itemList = createList();
        final BirdRecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final RecyclerAdapter adapter = new RecyclerAdapter(this, itemList);
        recyclerView.setAdapter(adapter);
        recyclerView.addDownPullRefresh(new BirdRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                itemList.add(0, new SimpleItem("刷新item"));
                                adapter.notifyItemInserted(0);
                                recyclerView.setDownRefreshing(false);
                            }
                        });
                    }
                }).start();
            }
        });
        recyclerView.addUpPullRefresh(new BirdRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                itemList.add(new SimpleItem("加载item"));
                                adapter.notifyItemInserted(itemList.size() - 1);
                                recyclerView.setUpRefreshing(false);
                            }
                        });
                    }
                }).start();
            }
        });

        List<IRecyclerItem> list = new ArrayList<>();
        list.add(new HeaderItem("header1"));
        list.add(new HeaderItem("header2"));
        recyclerView.addHeaders(list);
        recyclerView.addFooter(new HeaderItem("footer1"));
        recyclerView.addFooter(new HeaderItem("footer2"));
    }


    private List<IRecyclerItem> createList() {
        List<IRecyclerItem> items = new ArrayList<>();
        IRecyclerItem item;
        for (int i = 0; i < 10; i++) {
            item = new SimpleItem("simple_item : " + i);
            items.add(item);
        }
        return items;
    }
}
