import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReadWriteLock;
//import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fortappend.fortservice.client.vo.FlagEnum;
import com.fortappend.fortservice.client.vo.ItilResourceVo;
import com.fortappend.fortservice.client.vo.ReadAuthorizationRequestVo;
import com.fortappend.fortservice.client.vo.WriteItilAuthorizationRequestVo;
import com.fortappend.fortservice.client.vo.WriteItilAuthorizationResponseVo;
import com.google.gson.Gson;

public class ItilLimit {
    
    //private static ReadWriteLock lock = new ReentrantReadWriteLock();
    //private static Lock readLock = lock.readLock();
    //private static Lock writeLock = lock.writeLock();
    
    private static int clean(boolean fortEnv){
        Connection conn = SQLITEConnection.getConnection(fortEnv);
        String sql = "DELETE FROM itil_limit WHERE end_datetime < ?";
        PreparedStatement ptmt = null;
        try {
            ptmt = conn.prepareStatement(sql);
            ptmt.setString(1, dateFormat(new Date()));
            //writeLock.lock();
            return ptmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            try {
                ptmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //writeLock.unlock();
        }
        return -1;
    }
    
    public static String insertForSocket(String json){
        Gson gson = new Gson();
        WriteItilAuthorizationRequestVo request = gson.fromJson(json, WriteItilAuthorizationRequestVo.class);
        if(request.getDataSet() != null){
            for(ItilResourceVo irv : request.getDataSet()){
                insert(Boolean.valueOf(request.getFortEnv()), irv);
            }
        }
        WriteItilAuthorizationResponseVo response = new WriteItilAuthorizationResponseVo();
        response.setFlag(FlagEnum.SUCCESS.getName());
        return gson.toJson(response, WriteItilAuthorizationResponseVo.class);
    }
    
    public static int insert(boolean fortEnv, ItilResourceVo irv){
        if(needClean()){
            clean(fortEnv);
        }
        Connection conn = SQLITEConnection.getConnection(fortEnv);
        String sql = "INSERT INTO itil_limit(person_account, resource_ip, resource_account, start_datetime, end_datetime)"
                + "VALUES(?,?,?,?,?)";
        PreparedStatement ptmt = null;
        try {
            ptmt = conn.prepareStatement(sql);
            ptmt.setString(1, irv.getPersonAccount());
            ptmt.setString(2, irv.getResourceIp());
            ptmt.setString(3, irv.getResourceAccount());
            ptmt.setString(4, irv.getStartDatetime());
            ptmt.setString(5, irv.getEndDatetime());
            //writeLock.lock();
            return ptmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            try {
                ptmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //writeLock.unlock();
        }
        return -1;
    }
    
    private static String cacheDateStr = "";
    private static boolean needClean(){
        //if(true)return true;
        try{
            Date date = new Date();
            String dateStr = dateFormat(date);
            if(cacheDateStr.equals(dateStr)){
                return false;
            }
            String dayOfMonthStr = dateStr.substring(8, 10);
            int dayOfMonth = Integer.valueOf(dayOfMonthStr);
            if(dayOfMonth % 7 == 0){
                cacheDateStr = dateStr;
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    private static String dateFormat(Date date){
        if(date == null)return null;
        String ret = DateConvertor.dateToString(date);
        System.out.println(date + " -> " + ret);
        return ret;
    }
    
    public static String getByPersonAccountForSocket(String json){
        Gson gson = new Gson();
        ReadAuthorizationRequestVo request = gson.fromJson(json, ReadAuthorizationRequestVo.class);
        List<ItilResourceVo> list = getByPersonAccount(Boolean.valueOf(request.getFortEnv()), request.getPersonAccount());
        StringBuffer sb = new StringBuffer();
        for(ItilResourceVo vo : list){
            sb.append(vo.getPersonAccount());
            sb.append(",");
            sb.append(vo.getResourceIp());
            sb.append(",");
            sb.append(vo.getResourceAccount());
            sb.append(",");
            sb.append(vo.getStartDatetime());
            sb.append(",");
            sb.append(vo.getEndDatetime());
            sb.append(";");
        }
        String ret = sb.toString();
        if(ret.length() >= 1){
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }
    
    public static List<ItilResourceVo> getByPersonAccount(boolean fortEnv, String personAccount){
        Connection conn = SQLITEConnection.getConnection(fortEnv);
        String sql = "SELECT person_account, resource_ip, resource_account, start_datetime, end_datetime FROM itil_limit WHERE person_account = ?";
        PreparedStatement ptmt = null;
        ResultSet rs = null;
        List<ItilResourceVo> list = new ArrayList<ItilResourceVo>();
        try {
            ptmt = conn.prepareStatement(sql);
            ptmt.setString(1, personAccount);
            rs = ptmt.executeQuery();
            //readLock.lock();
            while(rs.next()){
                ItilResourceVo vo = new ItilResourceVo();
                vo.setPersonAccount(rs.getString("person_account"));
                vo.setResourceIp(rs.getString("resource_ip"));
                vo.setResourceAccount(rs.getString("resource_account"));
                vo.setStartDatetime(rs.getString("start_datetime"));
                vo.setEndDatetime(rs.getString("end_datetime"));
                list.add(vo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                ptmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //readLock.unlock();
        }
        return list;
    }
    
    public static void main(String[] args) {
        ItilResourceVo voInsert = new ItilResourceVo();
        voInsert.setPersonAccount("zhangke");
        voInsert.setResourceIp("192.168.90.66"); //192.168.90.66,root;192.168.82.2,support
        voInsert.setResourceAccount("root");
        voInsert.setStartDatetime("2015-09-20 08:00:00");
        voInsert.setEndDatetime("2015-09-30 08:10:00");
        insert(false, voInsert);
        
        List<ItilResourceVo> list = getByPersonAccount(false, "zhangke");
        for(ItilResourceVo vo : list){
            System.out.println(vo.getResourceAccount());
            System.out.println(vo.getStartDatetime());
            System.out.println(vo.getEndDatetime());
        }
    }

}
