package net.nemur.phosom.auxiliary;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

public class CachePhosom {

	private static CachePhosom singleton;
	private static final Logger log = Logger.getLogger(CachePhosom.class.getName());
	private Cache cache;
	private final int TTL = 3600; // one hour
	
	public static CachePhosom getInstance() {
		
		if( null == singleton ) {
			singleton = new CachePhosom();
		}
		return singleton;
	}
	
	private CachePhosom() {
		Map<String, Integer> props = new HashMap<String, Integer>();
		props.put(GCacheFactory.EXPIRATION_DELTA, TTL);
		
		try {
			CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
			cache = cacheFactory.createCache(props);
		} catch( CacheException e) {
			log.severe("Error creating cache: " + e.getMessage());
		}
	}

	public Cache getCache() {
		return cache;
	}
}
