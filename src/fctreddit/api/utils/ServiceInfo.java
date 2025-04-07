package fctreddit.api.utils;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServiceInfo {

    //Using CopyOnWriteArrayList to ensure thread safety
    List<URI> serviceUris = new CopyOnWriteArrayList<>();
    Instant serviceTimestamp;

    public List<URI> getList(){
        return serviceUris;
    }
}
