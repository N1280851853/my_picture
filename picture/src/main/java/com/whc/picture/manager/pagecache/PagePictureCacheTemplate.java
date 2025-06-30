package com.whc.picture.manager.pagecache;


public abstract class PagePictureCacheTemplate {

    public abstract String getCacheValueByKey(String catchKey);

    public abstract void setCache(String catchKey, String cacheValue);

}
