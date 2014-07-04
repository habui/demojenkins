package ads.common.geoip;

import ads.common.model.Config;
import com.maxmind.geoip.LookupService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GeoipService {
    private static LookupService service;
    private GeoipService() {}
    public static LookupService getGeoipClient() throws Exception{
        if(service != null) return service;
        service = new LookupService(Config.geopipDatabase().getValue(), LookupService.GEOIP_MEMORY_CACHE);
        return service;
    }
}
