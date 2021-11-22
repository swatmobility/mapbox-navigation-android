package com.mapbox.navigation.ui.camera;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.api.directions.v5.DirectionsAdapterFactory;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.navigation.base.trip.model.RouteProgress;
import com.mapbox.navigation.ui.BaseTest;

import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DynamicCameraTest extends BaseTest {

  private static final String DIRECTIONS_PRECISION_6 = "directions_v5_precision_6.json";

  @Test
  public void sanity() {
    DynamicCamera cameraEngine = buildDynamicCamera();

    assertNotNull(cameraEngine);
  }

  @Test
  public void onInformationFromRoute_engineCreatesCorrectZoom() throws Exception {
    DynamicCamera cameraEngine = buildDynamicCamera();
    RouteInformation routeInformation = new RouteInformation(buildDirectionsRoute(), null, null);

    double zoom = cameraEngine.zoom(routeInformation);

    assertEquals(15d, zoom, 0.1);
  }

  @Test
  public void onCameraPositionNull_engineReturnsDefaultZoom() throws Exception {
    DynamicCamera theCameraEngine = buildDynamicCamera();
    RouteInformation anyRouteInformation = new RouteInformation(null,
      buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637), buildDefaultRouteProgress(1000d));

    double defaultZoom = theCameraEngine.zoom(anyRouteInformation);

    assertEquals(15d, defaultZoom, 0.1);
  }

  @Test
  public void onCameraPositionZoomGreaterThanMax_engineReturnsMaxCameraZoom() throws Exception {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    CameraPosition cameraPositionWithZoomGreaterThanMax = new CameraPosition.Builder()
      .zoom(20d)
      .build();
    when(mapboxMap.getCameraForLatLngBounds(any(LatLngBounds.class), any(int[].class))).thenReturn(cameraPositionWithZoomGreaterThanMax);
    DynamicCamera theCameraEngine = new DynamicCamera(mapboxMap);
    RouteInformation anyRouteInformation = new RouteInformation(null,
      buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637), buildDefaultRouteProgress(1000d));

    double maxCameraZoom = theCameraEngine.zoom(anyRouteInformation);

    assertEquals(DynamicCamera.MAX_CAMERA_ZOOM, maxCameraZoom, 0.1);
  }

  @Test
  public void onCameraPositionZoomLessThanMin_engineReturnsMinCameraZoom() throws Exception {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    CameraPosition cameraPositionWithZoomLessThanMin = new CameraPosition.Builder()
      .zoom(10d)
      .build();
    when(mapboxMap.getCameraForLatLngBounds(any(LatLngBounds.class), any(int[].class))).thenReturn(cameraPositionWithZoomLessThanMin);
    DynamicCamera theCameraEngine = new DynamicCamera(mapboxMap);
    RouteInformation anyRouteInformation = new RouteInformation(null,
      buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637), buildDefaultRouteProgress(1000d));

    double maxCameraZoom = theCameraEngine.zoom(anyRouteInformation);

    assertEquals(DynamicCamera.MIN_CAMERA_ZOOM, maxCameraZoom, 0.1);
  }

  @Test
  public void onCameraPositionZoomGreaterThanMinAndLessThanMax_engineReturnsCameraPositionZoom() throws Exception {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    CameraPosition cameraPositionWithZoomGreaterThanMinAndLessThanMax = new CameraPosition.Builder()
      .zoom(14d)
      .build();
    when(mapboxMap.getCameraForLatLngBounds(any(LatLngBounds.class), any(int[].class))).thenReturn(cameraPositionWithZoomGreaterThanMinAndLessThanMax);
    DynamicCamera theCameraEngine = new DynamicCamera(mapboxMap);
    RouteInformation anyRouteInformation = new RouteInformation(null,
      buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637), buildDefaultRouteProgress(1000d));

    double maxCameraZoom = theCameraEngine.zoom(anyRouteInformation);

    assertEquals(14d, maxCameraZoom, 0.1);
  }

  @Test
  public void onIsResetting_dynamicCameraReturnsDefault() throws Exception {
    RouteInformation routeInformation = new RouteInformation(buildDirectionsRoute(), null, null);
    DynamicCamera cameraEngine = buildDynamicCamera();
    cameraEngine.forceResetZoomLevel();

    double zoom = cameraEngine.zoom(routeInformation);

    assertEquals(15d, zoom, 0.1);
  }

  @Test
  public void onInformationFromRoute_engineCreatesCorrectTilt() throws Exception {
    DynamicCamera cameraEngine = buildDynamicCamera();
    RouteInformation routeInformation = new RouteInformation(buildDirectionsRoute(), null, null);

    double tilt = cameraEngine.tilt(routeInformation);

    assertEquals(DynamicCamera.DEFAULT_TILT, tilt, 0.1);
  }

  @Test
  public void onHighDistanceRemaining_engineCreatesCorrectTilt() throws Exception {
    DynamicCamera cameraEngine = buildDynamicCamera();
    RouteInformation routeInformation = new RouteInformation(null,
      buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637), buildDefaultRouteProgress(1000d));

    double tilt = cameraEngine.tilt(routeInformation);

    assertEquals(DynamicCamera.DEFAULT_TILT, tilt, 0.1);
  }

  @Test
  public void onMediumDistanceRemaining_engineCreatesCorrectTilt() throws Exception {
    DynamicCamera cameraEngine = buildDynamicCamera();
    RouteInformation routeInformation = new RouteInformation(null,
      buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637), buildDefaultRouteProgress(200d));

    double tilt = cameraEngine.tilt(routeInformation);

    assertEquals(DynamicCamera.DEFAULT_TILT, tilt, 0.1);
  }

  @Test
  public void onLowDistanceRemaining_engineCreatesCorrectTilt() throws Exception {
    DynamicCamera cameraEngine = buildDynamicCamera();
    RouteInformation routeInformation = new RouteInformation(null,
      buildDefaultLocationUpdate(-77.0339782574523, 38.89993519985637), buildDefaultRouteProgress(null));

    double tilt = cameraEngine.tilt(routeInformation);

    assertEquals(DynamicCamera.DEFAULT_TILT, tilt, 0.1);
  }

  @Test
  public void onInformationFromRoute_engineCreatesOverviewPointList() throws Exception {
    DynamicCamera cameraEngine = buildDynamicCamera();
    DirectionsRoute route = buildDirectionsRoute();
    List<Point> routePoints = generateRouteCoordinates(route);
    RouteInformation routeInformation = new RouteInformation(route, null, null);

    List<Point> overviewPoints = cameraEngine.overview(routeInformation);

    assertEquals(routePoints, overviewPoints);
  }

  @Test
  public void onInformationFromRouteProgress_engineCreatesOverviewPointList() throws Exception {
    DynamicCamera cameraEngine = buildDynamicCamera();
    RouteProgress routeProgress = buildDefaultRouteProgress(null);
    List<Point> routePoints = buildRouteCoordinatesFrom(routeProgress);
    RouteInformation routeInformation = new RouteInformation(null, null, routeProgress);

    List<Point> overviewPoints = cameraEngine.overview(routeInformation);

    assertEquals(routePoints, overviewPoints);
  }

  @Test
  public void noRouteInformation_engineCreatesEmptyOverviewPointList() {
    DynamicCamera cameraEngine = buildDynamicCamera();
    RouteInformation routeInformation = new RouteInformation(null, null, null);

    List<Point> overviewPoints = cameraEngine.overview(routeInformation);

    assertTrue(overviewPoints.isEmpty());
  }

  @Nullable
  private List<Point> buildRouteCoordinatesFrom(RouteProgress routeProgress) {
    return generateRouteCoordinates(routeProgress.getRoute());
  }

  @NonNull
  private DynamicCamera buildDynamicCamera() {
    MapboxMap mapboxMap = mock(MapboxMap.class);
    return new DynamicCamera(mapboxMap);
  }

  private Location buildDefaultLocationUpdate(double lng, double lat) {
    return buildLocationUpdate(lng, lat, System.currentTimeMillis());
  }

  private Location buildLocationUpdate(double lng, double lat, long time) {
    Location location = mock(Location.class);
    when(location.getLongitude()).thenReturn(lng);
    when(location.getLatitude()).thenReturn(lat);
    when(location.getSpeed()).thenReturn(30f);
    when(location.getBearing()).thenReturn(100f);
    when(location.getAccuracy()).thenReturn(10f);
    when(location.getTime()).thenReturn(time);
    return location;
  }

  private RouteProgress buildDefaultRouteProgress(@Nullable Double stepDistanceRemaining) throws Exception {
    DirectionsRoute aRoute = buildDirectionsRoute();
    double stepDistanceRemainingFinal = stepDistanceRemaining == null ? 100 : stepDistanceRemaining;
    return buildRouteProgress(aRoute, stepDistanceRemainingFinal, 0, 0, 0, 0);
  }

  private DirectionsRoute buildDirectionsRoute() throws IOException {
    Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(DirectionsAdapterFactory.create()).create();
    String body = loadJsonFixture(DIRECTIONS_PRECISION_6);
    DirectionsResponse response = gson.fromJson(body, DirectionsResponse.class);
    return response.routes().get(0);
  }

  private List<Point> generateRouteCoordinates(DirectionsRoute route) {
    if (route.geometry() == null) {
      return Collections.emptyList();
    }
    LineString lineString = LineString.fromPolyline(route.geometry(), Constants.PRECISION_6);
    return lineString.coordinates();
  }
}