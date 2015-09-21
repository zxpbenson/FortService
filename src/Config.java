import java.io.FileInputStream;
import java.util.Properties;

public class Config {
    private static Properties prop = null;
    private static String itilFilter = "0";
    private static String whiteList = "";
    
    private static void initProp(boolean fortEnv){
        prop = new Properties();
        FileInputStream fis = null;
        try{
            String filePath = "D://FortService//resources//itil.control.cnf";
            if(fortEnv){
                filePath = "/usr/local/fort_append/conf/itil.control.cnf";
            }
            fis = new FileInputStream(filePath); 
            prop.load(fis);
            fis.close();
            itilFilter = prop.getProperty("itil_filter");
            whiteList = prop.getProperty("white_list");
            itilFilter = "1".equals(itilFilter)?"1":"0";
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{fis.close();}catch(Exception e){}
        }         
    }
    
    private static void init(boolean fortEnv){
        if(prop == null){
            synchronized(Config.class){
                if(prop == null){
                    initProp(fortEnv);
                }
            }
        }        
    }
    
    public static String getWhiteList(boolean fortEnv){
        init(fortEnv);
        return whiteList;
    }
   
    public static String getItilFilter(boolean fortEnv){
        init(fortEnv);
        return itilFilter;
    }
}
