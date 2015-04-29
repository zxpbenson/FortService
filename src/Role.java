import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;


/*
 * 前提:堡垒的限制逻辑保障用户是否能维护和登录该设备账号
 * accountLimited的语义定义为:列举改用户对该设备可维护的账号(授权登录的账号才可以维护)
 * 判定某个人对某个设备是否有限制角色范围内的授权账号，无非分下面四种情况
 * 1,没有限制角色 => 返回1
 * 2,有限制角色但是目标设备不在限制角色范围内 => 返回2
 * 3,有限制角色且目标设备也在限制角色范围内但是无相应授权账号 => 返回3
 * 4,有限制角色且目标设备也在限制角色范围内而且还有相应授权账号 => 返回授权账号(逗号分隔)
 */

public class Role {
    
    public static void main(String[] args){
        //System.out.println(accountLimited("zhangke","Asset_0031B20A8 "));
        if(args.length == 3){
            System.out.println(accountLimited(args[0],args[1],Boolean.parseBoolean(args[2])));
        }
        if(args.length == 2){
            System.out.println(accountLimited(args[0],args[1]));
        }
    }
    
    public static String accountLimited(String personCn, String assetCn){
    	if(personCn.startsWith("cn=")){
    		personCn = personCn.substring(3);
    	}
    	if(assetCn.startsWith("cn=")){
    		assetCn = assetCn.substring(3);
    	}
        return accountLimited(personCn, assetCn, false);
    }
    
    public static String accountLimited(String personCn, String assetCn, boolean fortEnv){
        LDAPConnection conn = LDAPEnv.getLDAPConnection(fortEnv);
        try{
            List<String> pseronCnInNameSpaceList = new ArrayList<String>();
        	List<String> roleList = searchRoleLimited(conn, personCn, pseronCnInNameSpaceList);
        	
        	String pseronCnInNameSpace = pseronCnInNameSpaceList.get(0);
            if(pseronCnInNameSpace == null){
                return "FAIL:1";
            }
            
        	if(roleList == null || roleList.size() < 1){
        	    return "SUCCESS:1";
        	}
        	
        	String assetCnInNamespace = getAssetCnInNamespace(conn, assetCn);
        	if(assetCnInNamespace == null){
        	    return "FAIL:2";
        	}
        	
        	boolean underRoleControl = false;
        	for(String roleCnInNamespace : roleList){
        	    //System.out.println(roleCnInNamespace);
        	    underRoleControl = underRoleControl(roleCnInNamespace, assetCnInNamespace);
        	    if(underRoleControl)break;
        	}
        	
        	if(!underRoleControl){
        	    return "SUCCESS:2";
        	}
        	
        	List<String> authorizationList = getAuthorization(conn, pseronCnInNameSpace);
        	List<String> underControlAuthorizationList = new ArrayList<String>();
        	for(String authorization : authorizationList){
        		if(underRoleControl(assetCnInNamespace, authorization)){
        			underControlAuthorizationList.add(authorization);
        		}
        	}
        	
        	if(underControlAuthorizationList.size() < 1){
        		return "SUCCESS:3";
        	}
        	
        	StringBuffer sb = new StringBuffer();
        	sb.append("SUCCESS:");
        	for(String underControlAuthorization : underControlAuthorizationList){
        		String account = underControlAuthorization.substring(underControlAuthorization.indexOf("=")+1, underControlAuthorization.indexOf(","));
        		sb.append(",");
        		sb.append(account);
        	}
        	return sb.toString().replaceAll(":,", ":");
        }catch(Exception e){
            e.printStackTrace();
            return "FAIL:X";
        }finally{
            conn.close();
        }
    }
    
    private static List<String> searchRoleLimited(LDAPConnection conn, String personCn, List<String> pseronCnInNameSpaceList){
    	List<String> list = new ArrayList<String>();
    	LdapContext ctx = conn.getLdapContext();
        //System.out.println("ctx=" + ctx);
    	SearchControls sc = new SearchControls();
    	sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
    	
    	String searchFilter = "(&(objectclass=simp-person)(cn="+ getCnValue(personCn) +"))";
    	String searchBase = "ou=People,dc=simp,dc=com";
    	String rs[] = {
    	        "cn",
    	        "name",
    	        "simp-roles"
    	};
    	sc.setReturningAttributes(rs);
        try {
        	
            NamingEnumeration<SearchResult> anser = ctx.search(searchBase, searchFilter, sc);
            while(anser.hasMoreElements()){
                SearchResult sr = anser.next();
                pseronCnInNameSpaceList.add(sr.getNameInNamespace());
                Attributes attrs = sr.getAttributes();
                Attribute roleAttribute = attrs.get("simp-roles");
                if(roleAttribute != null){
                    int size = roleAttribute.size();
                    for(int index = 0; index < size; index++){
                        Object roleObj = roleAttribute.get(index);
                        if(roleObj != null){
                            String roleCnInNamespace = roleObj.toString();
                            //System.out.println(roleObj.toString());
                            //list.add(nameInNamespace);
                            String roleNameAndCnInNamespace[] = getRoleNameAndCnInNamespace(conn, roleCnInNamespace);
                            if(roleNameAndCnInNamespace[0] != null && roleNameAndCnInNamespace[0].endsWith("@LIMITED")){
                                list.add(roleNameAndCnInNamespace[1]);
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            
        }
        return list;
    }
    
    private static String[] getRoleNameAndCnInNamespace(LDAPConnection conn, String roleCn){
        
        LdapContext ctx = conn.getLdapContext();
        //System.out.println("ctx=" + ctx);
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        
        String searchFilter = "(&(objectclass=simp-role)(cn="+ getCnValue(roleCn) +"))";
        String searchBase = "ou=Groups,dc=simp,dc=com";
        String rs[] = {
                "cn",
                "name"
        };
        sc.setReturningAttributes(rs);
        try {
            NamingEnumeration<SearchResult> anser = ctx.search(searchBase, searchFilter, sc);
            if(anser.hasMoreElements()){
                SearchResult sr = anser.next();
                String cnInNamespace = sr.getNameInNamespace();
                Attributes attrs = sr.getAttributes();
                //System.out.println("getNameInNamespace()="+sr.getNameInNamespace());//like : cn=Role_132645461724583,cn=Group_131607006073081,ou=Groups, dc=simp,dc=com
                Attribute roleAttribute = attrs.get("name");
                if(roleAttribute != null){
                    Object roleObj = roleAttribute.get();
                    if(roleObj != null) {
                        return new String[]{roleObj.toString(), cnInNamespace};
                    }
                }
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }finally{
            
        }
        return null;
    }
    
    private static String getAssetCnInNamespace(LDAPConnection conn, String assetCn){
        assetCn = getCnValue(assetCn);
        
        LdapContext ctx = conn.getLdapContext();
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        
        String searchFilter = "(&(objectclass=simp-asset)(cn="+ getCnValue(assetCn) +"))";
        String searchBase = "ou=Groups,dc=simp,dc=com";
        String rs[] = {
                "cn",
                "name"
        };
        sc.setReturningAttributes(rs);
        try {
            NamingEnumeration<SearchResult> anser = ctx.search(searchBase, searchFilter, sc);
            if(anser.hasMoreElements()){
                SearchResult sr = anser.next();
                return sr.getNameInNamespace();
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }finally{
            
        }
        return null; 
    }
    
    private static boolean underRoleControl(String roleCnInNamespace, String assetCnInNamespace) throws Exception{
        
        if(roleCnInNamespace == null)throw new Exception("illegal roleCnInNamespace:" + roleCnInNamespace);
        if(assetCnInNamespace == null)throw new Exception("illegal assetCnInNamespace:" + assetCnInNamespace);
        
        roleCnInNamespace = roleCnInNamespace.replaceAll(" ", "");
        assetCnInNamespace = assetCnInNamespace.replaceAll(" ", "");
        
        String scope = null;
        if(roleCnInNamespace.indexOf(",") > -1){
            scope = roleCnInNamespace.substring(roleCnInNamespace.indexOf(",") + 1);
        }else{
            throw new Exception("illegal roleCnInNamespace:" + roleCnInNamespace);
        }
        
        if(assetCnInNamespace.endsWith(scope))return true;
        
        return false;
    }
    
    private static List<String> getAuthorization(LDAPConnection conn, String personCnInNamespace){
        List<String> list = new ArrayList<String>();
        LdapContext ctx = conn.getLdapContext();
        //System.out.println("ctx=" + ctx);
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        
        String searchFilter = "(objectclass=simp-authorization)";
        String searchBase = personCnInNamespace;
        String rs[] = {
                "cn",
                "name",
                "simp-authorize-account-rdn"
        };
        sc.setReturningAttributes(rs);
        try {
            NamingEnumeration<SearchResult> anser = ctx.search(searchBase, searchFilter, sc);
            while(anser.hasMoreElements()){
                SearchResult sr = anser.next();
                Attributes attrs = sr.getAttributes();
                Attribute attribute = attrs.get("simp-authorize-account-rdn");
                if(attribute != null){
                    Object obj = attribute.get();
                    if(obj != null){
                        String cnInNamespace = obj.toString();
                        if(!cnInNamespace.endsWith(",ou=Groups,dc=simp,dc=com")){
                        	cnInNamespace = cnInNamespace + ",ou=Groups,dc=simp,dc=com";
                        }
                        //System.out.println(cnInNamespace);
                        list.add(cnInNamespace);
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            
        }
        return list;
    }
    
    private static String getCnValue(String cn){
        if(cn.indexOf(",") > -1){
            cn = cn.substring(0, cn.indexOf(","));
        }
        if(cn.indexOf("=") > -1){
            cn = cn.substring(cn.indexOf("=") + 1);
        }
        return cn;
    }
}
