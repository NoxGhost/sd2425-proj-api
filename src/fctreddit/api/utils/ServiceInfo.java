package fctreddit.api.utils;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServiceInfo {

    //Using CopyOnWriteArrayList to ensure thread safety
    List<String> serviceUris = new CopyOnWriteArrayList<>();
    Instant serviceTimestamp;



}
