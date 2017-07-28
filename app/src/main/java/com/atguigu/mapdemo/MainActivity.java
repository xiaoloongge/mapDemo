package com.atguigu.mapdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.atguigu.mapdemo.overlayutil.BikingRouteOverlay;
import com.atguigu.mapdemo.overlayutil.DrivingRouteOverlay;
import com.atguigu.mapdemo.overlayutil.PoiOverlay;
import com.atguigu.mapdemo.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.share.OnGetShareUrlResultListener;
import com.baidu.mapapi.search.share.PoiDetailShareURLOption;
import com.baidu.mapapi.search.share.ShareUrlResult;
import com.baidu.mapapi.search.share.ShareUrlSearch;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private BaiduMap map;
    private PoiSearch poiSearch;
    private ShareUrlSearch shareUrlSearch;
    private RoutePlanSearch routePlanSearch;
    private String TAG = "aaa";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化百度地图的SDK
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        //初始化百度地图控件
        mapView = (MapView) findViewById(R.id.mapview);
        //得到地图的视图层
        map = mapView.getMap();


        poiSearch = PoiSearch.newInstance();
//
//        shareUrlSearch = ShareUrlSearch.newInstance();
//线路规划
         routePlanSearch = RoutePlanSearch.newInstance();
//
        initListener();
    }

    private void initListener() {

        //地图监听事件
        map.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //删除覆盖物
                map.clear();
                //添加覆盖物
                //创建一个坐标
                BitmapDescriptor bitmapDescriptor =
                        BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
                //创建一个覆盖物
                OverlayOptions options =
                        new MarkerOptions().position(latLng).icon(bitmapDescriptor);
                //添加到地图中
                map.addOverlay(options);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });

        //marker监听事件
        map.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(MainActivity.this, marker.getPosition()+"", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        //检索监听
        poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {

                if (poiResult == null || poiResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Log.v("TAG", "没有找到相关搜索");
                    return;
                }

//                List<PoiInfo> list = poiResult.getAllPoi();
//                for (PoiInfo info : list){
//                    Log.i("TAG", "onGetPoiResult: "+info.name);
//                    //添加标注物
//                    BitmapDescriptor bitmapDescriptor
//                            = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
//
//                    OverlayOptions options = new MarkerOptions().position(info.location)
//                            .icon(bitmapDescriptor);
//
//                    map.addOverlay(options);
//
//               }
                    map.clear();
                    MyOverly myOverly = new MyOverly(map);
                    myOverly.setData(poiResult);
                    myOverly.addToMap();
                    myOverly.zoomToSpan();
                    map.setOnMarkerClickListener(myOverly);

            }


            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                shareUrlSearch.requestPoiDetailShareUrl(
                        new PoiDetailShareURLOption()
                                .poiUid(poiDetailResult.getUid()));
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
        });
//
//
//        shareUrlSearch.setOnGetShareUrlResultListener(new OnGetShareUrlResultListener() {
//            @Override
//            public void onGetPoiDetailShareUrlResult(final ShareUrlResult shareUrlResult) {
//
//                Log.i("aaa", "onGetPoiDetailShareUrlResult: "+"aaaa"+shareUrlResult.getUrl());
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.this,
//                                "aaaa"+shareUrlResult.getUrl(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//
//            }
//
//            @Override
//            public void onGetLocationShareUrlResult(ShareUrlResult shareUrlResult) {
//
//            }
//
//            @Override
//            public void onGetRouteShareUrlResult(ShareUrlResult shareUrlResult) {
//
//            }
//        });
//
//
        routePlanSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                //验证是否为空
                if (walkingRouteResult == null ||
                        walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Log.i(TAG, "onGetWalkingRouteResult: " + " 没找到结果");
                    return;
                }
                map.clear();//清除覆盖物
                WalkingRouteOverlay overly = new MyWalk(map);
                overly.setData(walkingRouteResult.getRouteLines().get(0));
                overly.addToMap();//添加地图
                overly.zoomToSpan();//放大地图

            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }


            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                //骑行路线回调
                if (drivingRouteResult == null ||
                        drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Log.i(TAG, "onGetWalkingRouteResult: " + " 没找到结果");
                    return;
                }
                map.clear();
                DrivingRouteOverlay overlay = new MyDriver(map);
                overlay.setData(drivingRouteResult.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }


            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

                //骑行路线回调
                if (bikingRouteResult == null ||
                        bikingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Log.i(TAG, "onGetWalkingRouteResult: " + " 没找到结果");
                    return;
                }
                map.clear();
                BikingRouteOverlay overlay = new MyBinking(map);
                overlay.setData(bikingRouteResult.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            }
        });

    }


    public void btn1(View v){

//        map.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
//
////普通地图
//        map.setMapType(BaiduMap.MAP_TYPE_NORMAL);
//
////卫星地图
//        map.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
//
////空白地图, 基础地图瓦片将不会被渲染。在地图类型中设置为NONE，将不会使用流量下载基础地图瓦片图层。使用场景：与瓦片图层一起使用，节省流量，提升自定义瓦片图下载速度。
//        map.setMapType(BaiduMap.MAP_TYPE_NONE);

//开启交通图   实时交通图
        //map.setTrafficEnabled(true);

////开启交通图   百度城市热力图
        map.setBaiduHeatMapEnabled(true);
    }


    public void btn2(View v){
        //添加marker覆盖物
       /* //经纬度
        LatLng l = new LatLng(39.963175,116.400244);

        //创建一个覆盖物的图标
        BitmapDescriptor icon = BitmapDescriptorFactory
                .fromResource(R.mipmap.ic_launcher);

        //创建一个覆盖物  position表示覆盖物的位置 icon是覆盖物的图片
        OverlayOptions options = new MarkerOptions().position(l).icon(icon);

        //将覆盖物添加到地图中
        map.addOverlay(options);*/

       //添加几何图形
        //添加一个几何图形
        LatLng l1 = new LatLng(39.93925,116.357428);
        LatLng l2 = new LatLng(39.90925,116.327428);
        LatLng l3 = new LatLng(39.88925,116.347428);
        //将三个坐标添加到集合中
        List<LatLng> pt = new ArrayList<>();
        pt.add(l1);
        pt.add(l2);
        pt.add(l3);
        //添加几何图形 points需要的坐标点
        OverlayOptions options = new PolygonOptions().points(pt)
                //2 边宽  和边的颜色  ， fillColor(填充的颜色)
                .stroke(new Stroke(2,0xAA00FF00)).fillColor(0xAA00FF00);
        //添加覆盖特
        map.addOverlay(options);
    }


    public void btn3(View v){

        LatLng l1 = new LatLng(39.93925, 116.357428);

        poiSearch.searchNearby(new PoiNearbySearchOption()
                .radius(10000)//半径
                .pageCapacity(10)//数量
                .pageNum(1)//页数
                .location(l1)//圆心（搜索的圆心）
                .keyword("ATM"));//搜索的物品名称

        //城市内检索
//        poiSearch.searchInCity(
//                new PoiCitySearchOption()
//                        .city("北京")//城市
//                        .keyword("商店")
//                        .pageNum(1).pageCapacity(10));
    }


    public void btn4(View v){
        //路线
        PlanNode stNode = PlanNode.withCityCodeAndPlaceName(132, "回龙观");
        PlanNode endNode = PlanNode.withCityCodeAndPlaceName(132, "西二旗");

//        routePlanSearch.transitSearch(new TransitRoutePlanOption()
//                .city("北京").from(stNode).to(endNode));
//        routePlanSearch.drivingSearch(new DrivingRoutePlanOption()
//                .from(stNode)
//                .to(endNode)
//        );
        routePlanSearch.bikingSearch(new BikingRoutePlanOption().from(stNode).to(endNode));
    }
    public void btn5(View v){
        //地理编码
        GeoCoder geoCoder = GeoCoder.newInstance();
//        监听
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

                Log.i(TAG, "onGetGeoCodeResult: "+geoCodeResult.getLocation().toString());
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                Log.i(TAG, "onGetGeoCodeResult: "+reverseGeoCodeResult.getAddress());
            }
        });



        //将经纬度转找成地址
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption()
                .location(new LatLng(39.946758,116.423134)));

        //将地址转换成经纬度
        //geoCoder.geocode(new GeoCodeOption().address("北京").city("北京"));

    }
    public void btn6(View v){

    }

    public void btn7(View v){

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁地图
        mapView.onDestroy();
        //销毁检索
        poiSearch.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }



    //自定义覆盖物的类
    class MyOverly extends PoiOverlay {
        /**
         * 构造函数
         *
         * @param baiduMap 该 PoiOverlay 引用的 BaiduMap 对象
         */
        public MyOverly(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int i) {

            List<PoiInfo> allPoi = getPoiResult().getAllPoi();

            Toast.makeText(MainActivity.this,
                    "" + allPoi.get(i).name, Toast.LENGTH_SHORT).show();

            if (allPoi.get(i).hasCaterDetails) {
                //如果是有美食才会调用详情
                poiSearch.searchPoiDetail(
                        new PoiDetailSearchOption().poiUid(allPoi.get(i).uid));
                return true;
            }

            return super.onPoiClick(i);
        }
    }


    //创建步行路线覆盖物
    class MyWalk extends WalkingRouteOverlay {

        public MyWalk(BaiduMap baiduMap) {
            super(baiduMap);
        }

//        @Override
//        public boolean onPolylineClick(Polyline polyline) {
//            return super.onPolylineClick(polyline);
//        }
    }



    //创建架车路线覆盖物
    class MyDriver extends DrivingRouteOverlay {

        public MyDriver(BaiduMap baiduMap) {
            super(baiduMap);
        }
    }



    //创建步行路线覆盖物
    class MyBinking extends BikingRouteOverlay {

        public MyBinking(BaiduMap baiduMap) {
            super(baiduMap);
        }
    }
}
