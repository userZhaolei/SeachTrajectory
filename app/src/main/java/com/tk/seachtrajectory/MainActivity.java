package com.tk.seachtrajectory;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.clusterutil.clustering.Cluster;
import com.baidu.mapapi.clusterutil.clustering.ClusterItem;
import com.baidu.mapapi.clusterutil.clustering.ClusterManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.zhy.m.permission.MPermissions;
import com.zhy.m.permission.PermissionDenied;
import com.zhy.m.permission.PermissionGrant;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BaiduMap.OnMapLoadedCallback {
    private static final int REQUECT_CODE_SDCARD = 1001;
    private TextureMapView mMapView;
    private BaiduMap mBaiduMap;
    private ClusterManager<MyItem> mClusterManager;


    private final int MAP_STATUS_CHANGE = 100;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MAP_STATUS_CHANGE:
                    MapStatus mapStatus = (MapStatus) msg.obj;
                    if (mapStatus != null) {
                        Log.i("MarkerClusterDemo", "mapStatus=" + mapStatus.toString());
                        // to do :  判断地图状态，进行相应处理
                    }
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        MPermissions.requestPermissions(MainActivity.this, REQUECT_CODE_SDCARD, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        mMapView = (TextureMapView) findViewById(R.id.mTexturemap);

        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapLoadedCallback(this);

        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        // setPaint();
        setZoo();
        //setBitmap();


    }

    private void setBitmap() {

        LatLng point1 = new LatLng(39.965, 116.404);
        LatLng point2 = new LatLng(39.965, 116.604);
        List<OverlayOptions> options = new ArrayList<>();
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.mipmap.ic_launcher_round);
        //创建OverlayOptions属性
        OverlayOptions option1 = new MarkerOptions()
                .position(point1)
                .icon(bitmap)
                .title("开始");
        OverlayOptions option2 = new MarkerOptions()
                .position(point2)
                .icon(bitmap)
                .title("结束");
        ;
        //将OverlayOptions添加到list
        options.add(option1);
        options.add(option2);
        //在地图上批量添加
        mBaiduMap.addOverlays(options);
    }

    private void setZoo() {
        //设定中心点坐标
        LatLng cenpt = new LatLng(39.965, 116.404);
        //定义地图状态
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(cenpt)
                .zoom(5)
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        // 定义点聚合管理类ClusterManager
        mClusterManager = new ClusterManager<MyItem>(this, mBaiduMap);
        // 添加Marker点
        addMarkers();
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                Toast.makeText(MainActivity.this,
                        "有" + cluster.getSize() + "个点", Toast.LENGTH_SHORT).show();

                List<MyItem> items = (List<MyItem>) cluster.getItems();
                LatLngBounds.Builder builder2 = new LatLngBounds.Builder();
                int i = 0;
                for (MyItem myItem : items) {
                    builder2 = builder2.include(myItem.getPosition());
                    Log.i("map", "log: i=" + i++ + " pos=" + myItem.getPosition().toString());
                }

                LatLngBounds latlngBounds = builder2.build();
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(latlngBounds, mMapView.getWidth(), mMapView.getHeight());
                mBaiduMap.animateMapStatus(u);
                Log.i("map", "log: mBaiduMap.animateMapStatus(u)");

                return false;
            }
        });

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem item) {
                String showText = "点击单个Item";

                Toast.makeText(MainActivity.this,
                        showText, Toast.LENGTH_SHORT).show();

                return false;
            }
        });
        mClusterManager.setHandler(handler, MAP_STATUS_CHANGE); //设置handler
    }

    private void addMarkers() {
        List<MyItem> mMyItems = new ArrayList<MyItem>();
        List<LatLng> points = new ArrayList<LatLng>();
        points.add(new LatLng(39.965, 116.404));
        points.add(new LatLng(39.925, 116.454));
        points.add(new LatLng(39.955, 116.494));
        points.add(new LatLng(39.905, 116.554));
        points.add(new LatLng(39.965, 116.604));
        for (int i = 0; i < points.size(); i++) {
            LatLng latLng = points.get(i);
            mMyItems.add(new MyItem(latLng));
        }
        mClusterManager.addItems(mMyItems);

        // 设置地图监听，当地图状态发生改变时，进行点聚合运算
        mBaiduMap.setOnMapStatusChangeListener(mClusterManager);
        // 设置maker点击时的响应
        mBaiduMap.setOnMarkerClickListener(mClusterManager);
    }

    private void setPaint() {
        // 构造折线点坐标
        List<LatLng> points = new ArrayList<LatLng>();
        points.add(new LatLng(39.965, 116.404));
        points.add(new LatLng(39.925, 116.454));
        points.add(new LatLng(39.955, 116.494));
        points.add(new LatLng(39.905, 116.554));
        points.add(new LatLng(39.965, 116.604));

        //构建分段颜色索引数组
        List<Integer> colors = new ArrayList<>();
        colors.add(Integer.valueOf(Color.BLUE));
        colors.add(Integer.valueOf(Color.RED));
        colors.add(Integer.valueOf(Color.YELLOW));
        colors.add(Integer.valueOf(Color.GREEN));
        OverlayOptions ooPolyline = new PolylineOptions().width(10)
                .colorsValues(colors).points(points);
        Polyline mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @PermissionGrant(REQUECT_CODE_SDCARD)
    public void requestSdcardSuccess() {
        Toast.makeText(this, "GRANT ACCESS SDCARD!", Toast.LENGTH_SHORT).show();
    }

    @PermissionDenied(REQUECT_CODE_SDCARD)
    public void requestSdcardFailed() {
        Toast.makeText(this, "DENY ACCESS SDCARD!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapLoaded() {
        MapStatus ms = new MapStatus.Builder().zoom(9).build();
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    class MyItem implements ClusterItem {
        private LatLng latLng;

        public MyItem(LatLng latLng) {
            this.latLng = latLng;
        }

        @Override
        public LatLng getPosition() {
            return latLng;
        }

        @Override
        public BitmapDescriptor getBitmapDescriptor() {
            return BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_gcoding);//R.drawable.icon_gcoding);
        }

    }

}
