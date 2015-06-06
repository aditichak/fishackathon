package com.example.owner.fishyfish;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.MapView;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.PathOverlay;

/**
 * A placeholder fragment containing a simple view.
 */
public class MapActivityFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private ItemizedIconOverlay<OverlayItem> buildingOverlay;
    private OverlayItem selectedBuildingOnMap;
    private OverlayItem destination;

    /**
     * View that shows the map
     */
    private MapView mapView;

    /**
     * Map controller for zooming in/out, centering
     */
    private IMapController mapController;

    public static MapActivityFragment newInstance(int sectionNumber) {
        MapActivityFragment fragment = new MapActivityFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        System.out.println("gallery fragment started");
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }
}




    // ******************** Android methods for starting, resuming, ...

    // You should not need to touch this method
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentView(R.layout.activity_explore);
        // You are going to need an overlay to draw buildings and locations on the map
        buildingOverlay = createBuildingOverlay();
    }

    // You should not need to touch this method
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
    }

    // You should not need to touch this method
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mapView == null) {
            mapView = new MapView(getActivity(), null);

            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setClickable(true);
            mapView.setBuiltInZoomControls(true);
            mapView.setMultiTouchControls(true);

            mapController = mapView.getController();
            mapController.setZoom(mapView.getMaxZoomLevel() - 2);
        }

        return mapView;
    }

    // You should not need to touch this method
    @Override
    public void onDestroyView() {
        ((ViewGroup) mapView.getParent()).removeView(mapView);
        super.onDestroyView();
    }

    // You should not need to touch this method
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // You should not need to touch this method
    @Override
    public void onResume() {
        super.onResume();
    }

    // You should not need to touch this method
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Save map's zoom level and centre. You should not need to
     * touch this method
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mapView != null) {
            outState.putInt("zoomLevel", mapView.getZoomLevel());
            IGeoPoint cntr = mapView.getMapCenter();
            outState.putInt("latE6", cntr.getLatitudeE6());
            outState.putInt("lonE6", cntr.getLongitudeE6());
        }
    }

    // ****************** App Functionality

    /**
     * Show my schedule on the map. Every time "me"'s schedule shows, the map
     * should be cleared of all existing schedules, buildings, meetup locations, etc.
     */
    public void showMySchedule() {

        // CPSC 210 Students: You must complete the implementation of this method.
        // The very last part of the method should call the asynchronous
        // task (which you will also write the code for) to plot the route
        // for "me"'s schedule for the day of the week set in the Settings

        // Asynchronous tasks are a bit onerous to deal with. In order to provide
        // all information needed in one object to plot "me"'s route, we
        // create a SchedulePlot object and pass it to the asynchrous task.
        // See the project page for more details.

        clearSchedules();
        activeDay = sharedPreferences.getString("dayOfWeek", "Default");

        SchedulePlot mySchedulePlot = new SchedulePlot(me.getSchedule().getSections(activeDay), me.getFirstName(),
                "#FF0000", R.drawable.ic_action_place);


        // Get a routing between these points. This line of code creates and calls
        // an asynchronous task to do the calls to MapQuest to determine a route
        // and plots the route.
        // Assumes mySchedulePlot is a create and initialized SchedulePlot object

        // UNCOMMENT NEXT LINE ONCE YOU HAVE INSTANTIATED mySchedulePlot
        new GetRoutingForSchedule().execute(mySchedulePlot);
    }

    /**
     * Retrieve a random student's schedule from the Meetup web service and
     * plot a route for the schedule on the map. The plot should be for
     * the given day of the week as determined when "me"'s schedule
     * was plotted.
     */
    public void showRandomStudentsSchedule() {
        // To get a random student's schedule, we have to call the MeetUp web service.
        // Calling this web service requires a network access to we have to
        // do this in an asynchronous task. See below in this class for where
        // you need to implement methods for performing the network access
        // and plotting.
        new GetRandomSchedule().execute();
    }

    /**
     * Clear all schedules on the map
     */
    public void clearSchedules() {
        randomStudents = null;
        OverlayManager om = mapView.getOverlayManager();
        om.clear();
        scheduleOverlay.clear();
        myScheduleOverlay.clear();
        buildingOverlay.removeAllItems();
        om.addAll(scheduleOverlay);
        om.addAll(myScheduleOverlay);
        om.add(buildingOverlay);
        mapView.invalidate();
    }

    /**
     * Find all possible locations at which "me" and random student could meet
     * up for the set day of the week and the set time to meet and the set
     * distance either "me" or random is willing to travel to meet.
     * A meetup is only possible if both "me" and random are free at the
     * time specified in the settings and each of us must have at least an hour
     * (>= 60 minutes) free. You should display dialog boxes if there are
     * conditions under which no meetup can happen (e.g., me or random is
     * in class at the specified time)
     */


    private void plotSharedBuildings(Set<Place> closeToUs) {

        // CPSC 210 Students: Complete this method by plotting each building in the
        // schedulePlot with an appropriate message displayed
        // Map<Section, Building> buildings = new HashMap<Section, Building>();

        Iterator<Place> iterator = closeToUs.iterator();
        int drawableToUse = R.drawable.ic_action_event;
        Place place;

        while (iterator.hasNext()) {
            place = iterator.next();

            // DownloadImage dli = (DownloadImage) new DownloadImage((ImageView) mapView.getParent()).execute(place.getImg());
            String popupInfo = "What others are saying...\n" + place.getReview();
            plotASharedBuilding(place, place.getDisplayText(), popupInfo, drawableToUse);
        }


        // CPSC 210 Students: You will need to ensure the buildingOverlay is in
        // the overlayManager. The following code achieves this. You should not likely
        // need to touch it
        OverlayManager om = mapView.getOverlayManager();
        om.add(buildingOverlay);

    }

    private void plotASharedBuilding(Place place, String title, String msg, int drawableToUse) {
        // CPSC 210 Students: You should not need to touch this method
        OverlayItem placeItem = new OverlayItem(title, msg,
                new GeoPoint(place.getLatLon().getLatitude(), place.getLatLon().getLongitude()));

        //Create new marker
        Drawable icon = this.getResources().getDrawable(drawableToUse);

        //Set the bounding for the drawable
        icon.setBounds(
                0 - icon.getIntrinsicWidth() / 2, 0 - icon.getIntrinsicHeight(),
                icon.getIntrinsicWidth() / 2, 0);

        //Set the new marker to the overlay
        placeItem.setMarker(icon);
        buildingOverlay.addItem(placeItem);
    }

    /**
     * Plot all buildings referred to in the given information about plotting
     * a schedule.
     * @param schedulePlot All information about the schedule and route to plot.
     */
    private void plotBuildings(SchedulePlot schedulePlot) {

        // CPSC 210 Students: Complete this method by plotting each building in the
        // schedulePlot with an appropriate message displayed
        // Map<Section, Building> buildings = new HashMap<Section, Building>();
        SortedSet<Section> sections = schedulePlot.getSections();
        Iterator<Section> iterator = sections.iterator();
        int drawableToUse = schedulePlot.getIcon();
        Section section;
        List<Building> allBuildings = null;
        if (randomStudents != null) {
            allBuildings = getAllBuildings();
        }


        while (iterator.hasNext()) {
            double duplicateBuildings = 0.00000;
            section = iterator.next();
            // buildings.put(section, section.getBuilding());
            Building building = section.getBuilding();
            if (randomStudents != null) {
                duplicateBuildings = countDuplicateBuildings(building, allBuildings);
            }
            String popupInfo = schedulePlot.getName() + ": " + section.getCourseName() + " Section " + section.getName() +
                    "; " + section.getCourseTime().getStartTime() + " - " + section.getCourseTime().getEndTime();
            plotABuilding(building, building.getDisplayText(), popupInfo, drawableToUse, duplicateBuildings);
        }


        // CPSC 210 Students: You will need to ensure the buildingOverlay is in
        // the overlayManager. The following code achieves this. You should not likely
        // need to touch it
        OverlayManager om = mapView.getOverlayManager();
        om.add(buildingOverlay);

    }

    private List<Building> getAllBuildings() {
        List<Building> allBuildings = new ArrayList<Building>();
        for (Student student : randomStudents) {
            for (Section section : student.getSchedule().getSections(activeDay)) {
                allBuildings.add(section.getBuilding());
            }
        }
        for (Section section : me.getSchedule().getSections(activeDay)) {
            allBuildings.add(section.getBuilding());
        }
        return allBuildings;
    }

    private double countDuplicateBuildings(Building building, List<Building> allBuildings) {
        double count = 0.0000;
        for (Building b: allBuildings) {
            if (building.equals(b)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Plot a building onto the map
     * @param building The building to put on the map
     * @param title The title to put in the dialog box when the building is tapped on the map
     * @param msg The message to display when the building is tapped
     * @param drawableToUse The icon to use. Can be R.drawable.ic_action_place (or any icon in the res/drawable directory)
     */
    private void plotABuilding(Building building, String title, String msg, int drawableToUse, double duplicateBuildings) {
        // CPSC 210 Students: You should not need to touch this method
        OverlayItem buildingItem = new OverlayItem(title, msg,
                new GeoPoint(building.getLatLon().getLatitude() + duplicateBuildings*0.0001,
                        building.getLatLon().getLongitude()));

        //Create new marker
        Drawable icon = this.getResources().getDrawable(drawableToUse);

        //Set the bounding for the drawable
        icon.setBounds(
                0 - icon.getIntrinsicWidth() / 2, 0 - icon.getIntrinsicHeight(),
                icon.getIntrinsicWidth() / 2, 0);

        //Set the new marker to the overlay
        buildingItem.setMarker(icon);
        buildingOverlay.addItem(buildingItem);
    }

    /**
     * Initialize your schedule by coding it directly in. This is the schedule
     * that will appear on the map when you select "Show My Schedule".
     */


    /**
     * Helper to create simple alert dialog to display message
     *
     * @param msg message to display in alert dialog
     * @return the alert dialog
     */
    private AlertDialog createSimpleDialog(String msg) {
        // CPSC 210 Students; You should not need to modify this method
        AlertDialog.Builder dialogBldr = new AlertDialog.Builder(getActivity());
        dialogBldr.setMessage(msg);
        dialogBldr.setNeutralButton(R.string.ok, null);

        return dialogBldr.create();
    }

    /**
     * Create the overlay used for buildings. CPSC 210 students, you should not need to
     * touch this method.
     * @return An overlay
     */
    private ItemizedIconOverlay<OverlayItem> createBuildingOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(getActivity());

        ItemizedIconOverlay.OnItemGestureListener<OverlayItem> gestureListener =
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {

                    /**
                     * Display building description in dialog box when user taps stop.
                     *
                     * @param index
                     *            index of item tapped
                     * @param oi
                     *            the OverlayItem that was tapped
                     * @return true to indicate that tap event has been handled
                     */
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem oi) {

                        new AlertDialog.Builder(getActivity())
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        if (selectedBuildingOnMap != null) {
                                            mapView.invalidate();
                                        }
                                    }
                                }).setTitle(oi.getTitle()).setMessage(oi.getSnippet())
                                .show();

                        selectedBuildingOnMap = oi;
                        destination = oi;
                        mapView.invalidate();

                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem oi) {
                        // do nothing
                        return false;
                    }
                };

        return new ItemizedIconOverlay<OverlayItem>(
                new ArrayList<OverlayItem>(), getResources().getDrawable(
                R.drawable.ic_action_place), gestureListener, rp);
    }


    private void plotMeMoving() {
        OverlayManager om = mapView.getOverlayManager();
        om.clear();
        myScheduleOverlay.clear();
        om.addAll(scheduleOverlay);
        om.addAll(myScheduleOverlay);
        om.add(buildingOverlay);
        mapView.invalidate();

        String httpRequest = "http://open.mapquestapi.com/directions/v2/route?key=Fmjtd%7Cluu82luyl1%2Cax%3Do5-948g5u&ambiguities=ignore&from=" +
                me.getPoint().getLatitude() + "," + me.getPoint().getLongitude() + "&to=" +
                destination.getPoint().getLatitude() + ","  + destination.getPoint().getLongitude() +
                "&routeType=pedestrian&shapeFormat=raw&generalize=0";
        new GetRoutingForMe().execute(httpRequest);
    }


}
