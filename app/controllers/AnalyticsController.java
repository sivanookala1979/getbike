package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dataobject.RideStatus;
import models.Ride;
import org.json.simple.JSONObject;
import play.libs.Json;
import play.mvc.Result;
import utils.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wahid on 27/2/17.
 */
public class AnalyticsController extends BaseController {

    public Result analytics() {
        return ok(views.html.analytics.render());
    }

    public Result weeklyAnalytics() {
        double totalDistance = 0.0, totalAmount = 0.0;
        int noOfCompleted = 0, noOfPending = 0, noOfaccepted = 0, noOfCancel = 0;
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Double> totalAmountlist = new ArrayList<>(), totalDistancelist = new ArrayList<>();
        List<Integer> numberOfRideslist = new ArrayList<>(), noOfPendinglist = new ArrayList<>(), noOfacceptedlist = new ArrayList<>(), noOfCompletedlist = new ArrayList<>(), noOfCancellist = new ArrayList<>();
        List<String> formatedDatesList = new ArrayList<>();
        List<Ride> listOfRides = null;

        for (Date date : DateUtils.previousWeekDates()) {
            if (isNotNullAndEmpty(sdf.format(date))) {
                listOfRides = Ride.find.where().between("requested_at", DateUtils.getNewDate(sdf.format(date), 0, 0, 0), DateUtils.getNewDate(sdf.format(date), 23, 59, 59)).findList();
            }
            numberOfRideslist.add(listOfRides.size());

            for (Ride ride : listOfRides) {
                if (ride.getTotalBill() != null) {
                    totalAmount = totalAmount + ride.getTotalBill();
                }
                if (ride.getOrderDistance() != null) {
                    totalDistance = totalDistance + ride.getOrderDistance();
                }
                if (ride.getRideStatus().equals(RideStatus.RideRequested)) {
                    noOfPending++;
                }
                if (ride.getRideStatus().equals(RideStatus.RideCancelled)) {
                    noOfCancel++;
                }
                if (ride.getRideStatus().equals(RideStatus.RideAccepted)) {
                    noOfaccepted++;
                }
                if (ride.getRideStatus().equals(RideStatus.RideClosed)) {
                    noOfCompleted++;
                }

            }
            noOfacceptedlist.add(noOfaccepted);
            noOfPendinglist.add(noOfPending);
            noOfCompletedlist.add(noOfCompleted);
            noOfCancellist.add(noOfCancel);
            totalAmountlist.add(totalAmount);
            totalDistancelist.add((double) Math.round(totalDistance * 100) / 100);
            formatedDatesList.add(sdf.format(date));
            totalDistance = 0.0;
            totalAmount = 0.0;
            noOfPending = 0;
            noOfCompleted = 0;
            noOfaccepted = 0;
            noOfCancel = 0;
        }
        JSONObject obj = new JSONObject();
        obj.put("totalAmountForWeek", sum(totalAmountlist));
        obj.put("totalDistanceForWeek", (double) Math.round(sum(totalDistancelist) * 100) / 100);
        obj.put("numberOfRidesForWeek", count(numberOfRideslist));
        obj.put("pendingRidesForWeek", count(noOfPendinglist));
        obj.put("acceptedRidesForWeek", count(noOfacceptedlist));
        obj.put("completedRidesForWeek", count(noOfCompletedlist));
        obj.put("cancelRidesForWeek", count(noOfCancellist));
        obj.put("Dates", formatedDatesList);
        obj.put("numberOfRideslist", numberOfRideslist);
        obj.put("totalDistancelist", totalDistancelist);
        obj.put("totalAmountlist", totalAmountlist);
        obj.put("noOfPendinglist", noOfPendinglist);
        obj.put("noOfCompletedlist", noOfCompletedlist);
        obj.put("noOfacceptedlist", noOfacceptedlist);
        obj.put("noOfCancellist", noOfCancellist);
        ObjectNode objectNode = Json.newObject();
        setJson(objectNode, "rideSummary", obj);
        setResult(objectNode, objectNode);
        System.out.println("All json data is " + objectNode);
        return ok(Json.toJson(objectNode));

    }
    private double sum(List<Double> listData) {
        double sum = 0;
        for (Double d : listData)
            sum += d;
        return sum;
    }

    private int count(List<Integer> listData) {
        int sum = 0;
        for (Integer d : listData)
            sum += d;
        return sum;
    }

    public Result analyticsFilter() {
        return ok(views.html.analyticsBarcharts.render());
    }

    public Result reportData() {
        return ok(views.html.reports.render());
    }
}
