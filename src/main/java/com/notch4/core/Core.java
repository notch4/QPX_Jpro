package com.notch4.core;

import java.io.IOException;
import java.util.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ArrayMap;
import com.google.api.services.qpxExpress.QPXExpressRequestInitializer;
import com.google.api.services.qpxExpress.QPXExpress;
import com.google.api.services.qpxExpress.model.FlightInfo;
import com.google.api.services.qpxExpress.model.LegInfo;
import com.google.api.services.qpxExpress.model.PassengerCounts;
import com.google.api.services.qpxExpress.model.PricingInfo;
import com.google.api.services.qpxExpress.model.SegmentInfo;
import com.google.api.services.qpxExpress.model.SliceInfo;
import com.google.api.services.qpxExpress.model.TripOption;
import com.google.api.services.qpxExpress.model.TripOptionsRequest;
import com.google.api.services.qpxExpress.model.TripsSearchRequest;
import com.google.api.services.qpxExpress.model.SliceInput;
import com.google.api.services.qpxExpress.model.TripsSearchResponse;

public class Core {

    private static final String APPLICATION_NAME = "MyFlightApplication";
    private static final String API_KEY = "AIzaSyBJ8KyBsm_j2jRvWIm5PpO3Czctvblx8vE";
    /** Global instance of the HTTP transport. */
    private static HttpTransport httpTransport;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static void main(String[] args) {
        System.out.println(getData());
    }

    public static ArrayMap getData(){
        // TODO Auto-generated method stub
        ArrayMap<String, Object> data = new ArrayMap<String, Object>();

        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            PassengerCounts passengers= new PassengerCounts();
            passengers.setAdultCount(1);

            List<SliceInput> slices = new ArrayList<SliceInput>();

            SliceInput slice = new SliceInput();
            slice.setOrigin("NYC");
            slice.setDestination("LGA");
            slice.setDate("2016-04-29");
            slices.add(slice);


            TripOptionsRequest request= new TripOptionsRequest();
            request.setSolutions(10);
            request.setPassengers(passengers);
            request.setSlice(slices);

            TripsSearchRequest parameters = new TripsSearchRequest();
            parameters.setRequest(request);
            QPXExpress qpXExpress= new QPXExpress.Builder(httpTransport, JSON_FACTORY, null).setApplicationName(APPLICATION_NAME)
                    .setGoogleClientRequestInitializer(new QPXExpressRequestInitializer(API_KEY)).build();

            TripsSearchResponse list= qpXExpress.trips().search(parameters).execute();
            List<TripOption> tripResults=list.getTrips().getTripOption();

            String id;


            for(int i=0; i<tripResults.size(); i++){
                ArrayMap<String, Object> options = new ArrayMap<String, Object>();
                //Pricing
                List<PricingInfo> priceInfo= tripResults.get(i).getPricing();
                for(int p=0; p<priceInfo.size(); p++){
                    String price= priceInfo.get(p).getSaleTotal();
                    System.out.println("Price "+price);
                    options.put("price", price);
                }


                //Slice
                List<SliceInfo> sliceInfo= tripResults.get(i).getSlice();
                for(int j=0; j<sliceInfo.size(); j++){



                    List<SegmentInfo> segInfo= sliceInfo.get(j).getSegment();
                    ArrayMap<String, Object> segment = new ArrayMap<String, Object>();
                    for(int k=0; k<segInfo.size(); k++){



                        FlightInfo flightInfo=segInfo.get(k).getFlight();
                        String flightNum= flightInfo.getNumber();
                        String flightCarrier= flightInfo.getCarrier();
                        ArrayMap<String, String> legs = new ArrayMap<String, String>();
                        List<LegInfo> leg=segInfo.get(k).getLeg();
                        for(int l=0; l<leg.size(); l++){

                            String arrivalTime= leg.get(l).getArrivalTime();
                            String departTime=leg.get(l).getDepartureTime();
                            String dest=leg.get(l).getDestination();
                            String origin=leg.get(l).getOrigin();

                            legs.put("origin", origin);
                            legs.put("flightCarrier", flightCarrier);
                            legs.put("flightNum", flightNum);
                            legs.put("departTime", departTime);
                            legs.put("dest", dest);
                            legs.put("arrivalTime", arrivalTime );

                            System.out.println("\t"+
                                    origin + " "+
                                    flightCarrier + "["+
                                    flightNum+"] " +
                                    departTime + " "+
                                    dest + " "+
                                    arrivalTime
                            );

                        }
                        segment.add("legs", legs);
                    }
                    options.add("segments", segment);
                }
                data.add("data", options );
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return data;
    }
}

