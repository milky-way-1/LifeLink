package com.life.lifelink.util;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationUtils {

    /**
     * Calculate distance between two points in meters
     */
    public static double calculateDistance(LatLng from, LatLng to) {
        return SphericalUtil.computeDistanceBetween(from, to);
    }

    /**
     * Calculate bearing between two points in degrees
     */
    public static double calculateBearing(LatLng from, LatLng to) {
        return SphericalUtil.computeHeading(from, to);
    }

    /**
     * Calculate midpoint between two locations
     */
    public static LatLng calculateMidpoint(LatLng from, LatLng to) {
        return SphericalUtil.interpolate(from, to, 0.5);
    }

    /**
     * Calculate a point at a given distance and bearing from start
     */
    public static LatLng calculateOffset(LatLng start, double distance, double heading) {
        return SphericalUtil.computeOffset(start, distance, heading);
    }

    /**
     * Calculate area of a closed path in square meters
     */
    public static double calculateArea(List<LatLng> path) {
        return Math.abs(SphericalUtil.computeArea(path));
    }

    /**
     * Calculate if a point is inside a polygon
     */
    public static boolean isLocationInPath(LatLng point, List<LatLng> polygon) {
        return SphericalUtil.containsLocation(point, polygon, true);
    }

    /**
     * Format distance for display
     */
    public static String formatDistance(double meters) {
        if (meters < 1000) {
            return String.format(Locale.getDefault(), "%.0f m", meters);
        } else {
            return String.format(Locale.getDefault(), "%.1f km", meters/1000);
        }
    }

    /**
     * Calculate estimated time of arrival
     */
    public static String calculateETA(double distanceInMeters, double speedInMPS) {
        if (speedInMPS <= 0) return "N/A";

        int seconds = (int) (distanceInMeters / speedInMPS);
        int minutes = seconds / 60;

        if (minutes < 1) {
            return "< 1 min";
        } else if (minutes < 60) {
            return minutes + " min";
        } else {
            int hours = minutes / 60;
            minutes = minutes % 60;
            return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
        }
    }

    /**
     * Calculate bounds that include all points with padding
     */
    public static LatLngBounds calculateBounds(List<LatLng> points, double paddingMeters) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Points list cannot be null or empty");
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            // Add padding around each point
            double lat = point.latitude;
            double lng = point.longitude;

            // Convert padding from meters to degrees (approximate)
            double latPadding = paddingMeters / 111111;
            double lngPadding = paddingMeters / (111111 * Math.cos(Math.toRadians(lat)));

            builder.include(new LatLng(lat + latPadding, lng + lngPadding));
            builder.include(new LatLng(lat - latPadding, lng - lngPadding));
        }
        return builder.build();
    }

    /**
     * Calculate if driver has reached destination
     */
    public static boolean hasReachedDestination(LatLng driverLocation,
                                                LatLng destination,
                                                double thresholdMeters) {
        double distance = calculateDistance(driverLocation, destination);
        return distance <= thresholdMeters;
    }

    /**
     * Smooth a location path by removing noise
     */
    public static List<LatLng> smoothPath(List<LatLng> points, double tolerance) {
        if (points.size() <= 2) return points;

        List<LatLng> smoothed = new ArrayList<>();
        smoothed.add(points.get(0));

        for (int i = 1; i < points.size() - 1; i++) {
            LatLng prev = points.get(i - 1);
            LatLng curr = points.get(i);
            LatLng next = points.get(i + 1);

            // Calculate moving average
            double lat = (prev.latitude + curr.latitude + next.latitude) / 3;
            double lng = (prev.longitude + curr.longitude + next.longitude) / 3;

            // Only add point if it's moved more than tolerance
            if (calculateDistance(curr, new LatLng(lat, lng)) > tolerance) {
                smoothed.add(new LatLng(lat, lng));
            }
        }

        smoothed.add(points.get(points.size() - 1));
        return smoothed;
    }
}
