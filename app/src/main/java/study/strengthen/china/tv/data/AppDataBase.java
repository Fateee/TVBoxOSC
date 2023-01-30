package study.strengthen.china.tv.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import study.strengthen.china.tv.cache.Cache;
import study.strengthen.china.tv.cache.CacheDao;
import study.strengthen.china.tv.cache.VodCollect;
import study.strengthen.china.tv.cache.VodCollectDao;
import study.strengthen.china.tv.cache.VodRecord;
import study.strengthen.china.tv.cache.VodRecordDao;


/**
 * 类描述:
 *
 * @author pj567
 * @since 2020/5/15
 */
@Database(entities = {Cache.class, VodRecord.class, VodCollect.class}, version = 1)
public abstract class AppDataBase extends RoomDatabase {
    public abstract CacheDao getCacheDao();

    public abstract VodRecordDao getVodRecordDao();

    public abstract VodCollectDao getVodCollectDao();
}
