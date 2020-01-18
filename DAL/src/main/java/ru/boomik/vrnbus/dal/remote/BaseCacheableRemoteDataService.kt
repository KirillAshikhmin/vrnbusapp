package ru.boomik.vrnbus.dal.remote

import kotlin.reflect.KFunction1
import kotlin.reflect.KSuspendFunction0

open class BaseCacheableRemoteDataService<T : Any>(override val serviceClass: T, cachePath: String, private val cacheTime: Long) : BaseRemoteDataService<T>(serviceClass) {

    private val cache = LocalFileCache(cachePath)

    suspend fun <T, TR> makeRequestWithCacheAndConverter(loadingFun: KSuspendFunction0<T>, converterFun: KFunction1<T?, TR>, cacheName: String, cacheClass: Class<TR>, cacheTime: Long = 0L, invalidateCache: Boolean = false): RequestResultWithData<TR?> {

        var tmpCacheTime = cacheTime
        if (tmpCacheTime == 0L) tmpCacheTime = this.cacheTime
        var cachedResult: TR? = null
        if (!invalidateCache)
            cachedResult = cache.get(cacheName, cacheClass)

        if (!invalidateCache && cachedResult != null) {
            return RequestResultWithData(cachedResult, RequestStatus.Ok)
        }

        return try {
            val networkResult = invokeWithRetry(loadingFun)
            val data = converterFun(networkResult)

            if (data != null && tmpCacheTime > 0) cache.write(cacheName, data, tmpCacheTime)
            RequestResultWithData(data, RequestStatus.Ok)

        } catch (e: Throwable) {
            RequestResultWithData(null, statusFromException(e), e.localizedMessage)
        }

    }


    protected fun <TR> makeRequestFromCache(cacheName: String, cacheClass: Class<TR>): RequestResultWithData<TR?> {
        val cachedResult = cache.get(cacheName, cacheClass)
        return if (cachedResult != null)
            RequestResultWithData(cachedResult, RequestStatus.Ok)
        else RequestResultWithData(null, RequestStatus.NotFound)
    }
}