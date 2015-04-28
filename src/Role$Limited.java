import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;


/*
 * 判定某个人对某个设备是否有限制角色范围内的授权账号，无非分下面四种情况
 * 1,没有限制角色
 * 2,有限制角色但是目标设备不在限制角色范围内
 * 3,有限制角色且目标设备也在限制角色范围内但是无相应授权账号
 * 4,有限制角色且目标设备也在限制角色范围内而且还有相应授权账号
 */

public class Role$Limited {
    
    public static void main(String[] args){
        System.out.println(account$limited("zhangke","Asset_002A5D869 "));
//        if(args.length == 3){
//            System.out.println(account$limited(args[0],args[1],Boolean.parseBoolean(args[2])));
//        }
//        if(args.length == 2){
//            System.out.println(account$limited(args[0],args[1]));
//        }
    }
    
    public static String account$limited(String personCn, String assetCn){
    	if(personCn.startsWith("cn=")){
    		personCn = personCn.substring(3);
    	}
    	if(assetCn.startsWith("cn=")){
    		assetCn = assetCn.substring(3);
    	}
        return account$limited(personCn, assetCn, false);
    }
    
    public static String account$limited(String personCn, String assetCn, boolean fortEnv){
        LDAPConnection conn = LDAPEnv.getLDAPConnection(fortEnv);
        try{
        	List<String> roleList = searchLimitedlAssetAccount(conn, personCn);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            conn.close();
        }
        return null;
    }
    
    private static List<String> searchLimitedlAssetAccount(LDAPConnection conn, String personCn){
    	List<String> list = new ArrayList<String>();
        try {
        	
            LdapContext ctx = conn.getLdapContext();
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            
            String searchFilter = "(&(objectclass=simp-person)(cn="+ personCn +"))";
            String searchBase = "ou=People,dc=simp,dc=com";
            
            String rs[] = {
                    "cn",
                    "name",
                    "simp-roles"
            };
            sc.setReturningAttributes(rs);
            NamingEnumeration<SearchResult> anser = ctx.search(searchBase, searchFilter, sc);
            while(anser.hasMoreElements()){
                SearchResult sr = anser.next();
                Attributes attrs = sr.getAttributes();
                Attribute roleAttribute = attrs.get("simp-roles");
                if(roleAttribute != null){
                    Object roleObj = roleAttribute.get();
                    if(roleObj != null){
                    	System.out.println(roleObj.toString());
                    	list.add(roleObj.toString());
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
