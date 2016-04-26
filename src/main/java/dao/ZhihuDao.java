package dao;

import entity.User;
import org.n3r.eql.Eql;

/**
 * Created by Cabeza on 2016/4/26.
 */
public class ZhihuDao {
    public  static void saveUserInfo(User user){
        new Eql().params(user).execute();
    }
    public static void saveUserRelations(){

    }
}
