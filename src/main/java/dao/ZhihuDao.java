package dao;

import entity.User;
import entity.UserRelation;
import org.n3r.eql.Eql;
import org.n3r.eql.EqlTran;
import org.n3r.eql.impl.EqlBatch;
import org.n3r.eql.util.Closes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Cabeza on 2016/4/26.
 */
public class ZhihuDao {
    private static Logger log= LoggerFactory.getLogger(ZhihuDao.class);
    public  static void saveUserInfo(User user){
        new Eql().insert("saveUserInfo").params(user).execute();
    }
    public static void saveUserRelations(List<UserRelation> list){
        Eql eql=new Eql();
        EqlTran eqlTran = eql.newTran();
        eqlTran.start();
        EqlBatch eqlBatch=new EqlBatch();
        for(UserRelation userRelation:list){
            eql.useBatch(eqlBatch).useTran(eqlTran).insert("saveUserRelations").params(userRelation).execute();
        }
        eqlBatch.executeBatch();
        eqlTran.commit();
        Closes.closeQuietly(eqlTran);
    }

    public static void clearDatabases(){
        new Eql().id("clearUser").execute();
        new Eql().id("clearRelation").execute();
    }
}
