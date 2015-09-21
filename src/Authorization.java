
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;


public class Authorization {
    
    public static void main(String[] args){
        //System.out.println(authorizeValidate("zhangke","Asset_1351712111964296","support"));
        //System.out.println(authorizeValidate("zhangke","Asset_002A5D869","root"));
        //System.out.println(getAssetCnAndAccountByAuthorizationCn("zhangke","13165170732686"));
        //System.out.println(getAssetCnAndAccountByAuthorizationCn("zhangke","00521B692"));
        
        if(args.length == 6){
            System.out.println(getAssetCnAndAccountByAuthorizationCn(args[1],args[2],Boolean.parseBoolean(args[3])));
        }
        if(args.length == 5){
            System.out.println(getAssetCnAndAccountByAuthorizationCn(args[1],args[2]));
        }
        if(args.length == 4){
            System.out.println(authorizeValidate(args[0],args[1],args[2],Boolean.parseBoolean(args[3])));
        }
        if(args.length == 3){
            System.out.println(authorizeValidate(args[0],args[1],args[2]));
        }
    }
    
    public static String getAssetCnAndAccountByAuthorizationCn(String personCn, String authorizationCn){
        return getAssetCnAndAccountByAuthorizationCn(personCn, authorizationCn, false);
    }
    
    public static String getAssetCnAndAccountByAuthorizationCn(String personCn, String authorizationCn, boolean fortEnv){
    	LDAPConnection conn = LDAPEnv.getLDAPConnection(fortEnv);
        try{
            String accountRdnStr = getAccountRdnStr(conn, personCn, authorizationCn);
            if(accountRdnStr != null){
                String[] accountRdnArr = accountRdnStr.split(",");
                if(accountRdnArr != null && accountRdnArr.length >= 2){
                    String assetCn = accountRdnArr[1].replace("cn=", "");
                    String accountCn = accountRdnArr[0].replace("cn=", "");
                    return "ASSET_SEQ_NUM:"+assetCn+" ASSET_ACCOUNT:"+accountCn;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            conn.close();
        }
        return null;
    }
    
    public static boolean authorizeValidate(String personCn, String assetCn, String accountCn){
        return authorizeValidate(personCn, assetCn, accountCn, false);
    }
    
    private static String getAccountRdnStr(LDAPConnection conn, String personCn, String authorizationCn){
        try {
            LdapContext ctx = conn.getLdapContext();
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            
            String searchFilter = "(objectclass=simp-authorization)";
            String searchBase = "cn="+personCn+",ou=People,dc=simp,dc=com";
            
            String rs[] = {
                    "cn",
                    "name",
                    "simp-authorize-account-rdn"
            };
            
            sc.setReturningAttributes(rs);
            NamingEnumeration<SearchResult> anser = ctx.search(searchBase, searchFilter, sc);
            
            //for example : zhangke -> $00521B692$1351713120505256$
            String authorizationCn1 = "authorize_" + authorizationCn;
            String authorizationCn2 = "auth_" + authorizationCn;
            
            while(anser.hasMoreElements()){
                SearchResult sr = anser.next();
                Attributes attrs = sr.getAttributes();
                Attribute cn = attrs.get("cn");
                if(cn != null){
                    Object objectCn = cn.get();
                    if(objectCn != null){
                        String cnStr = objectCn.toString();
                        if(authorizationCn1.equals(cnStr) || authorizationCn2.equals(cnStr)){
                        	Attribute authorize = attrs.get("simp-authorize-account-rdn");
                        	if(authorize != null){
                        		Object objectAuthorize = authorize.get();
                        		if(objectAuthorize != null){
                        			return objectAuthorize.toString();
                        		}
                        	}
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static boolean authorizeValidate(String personCn, String assetCn, String accountCn, boolean fortEnv){
        LDAPConnection conn = LDAPEnv.getLDAPConnection(fortEnv);
//        if(fortEnv){
//            conn = new LDAPConnection(LDAPEnv.ladpURL_fort, LDAPEnv.adminName_fort, LDAPEnv.adminPassword_fort, LDAPEnv.authoenSchema);
//        }else{
//            conn = new LDAPConnection(LDAPEnv.ladpURL, LDAPEnv.adminName, LDAPEnv.adminPassword, LDAPEnv.authoenSchema);
//        }
        try{
            String personBaseDn = getPersonBaseDn(conn, personCn);
            if(personBaseDn != null){
                return searchAuthorization(conn, personBaseDn, assetCn, accountCn);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            conn.close();
        }
        return false;
    }
    
    private static String getPersonBaseDn(LDAPConnection conn, String personCn){
        try {
            LdapContext ctx = conn.getLdapContext();
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            
            String searchFilter = "(&(objectclass=simp-person)(cn="+ personCn +"))";
            String searchBase = "ou=People,dc=simp,dc=com";
            
            String rs[] = {
                    "cn",
                    "name",
                    "simp-pwd",
                    "userPassword"
            };
            
            sc.setReturningAttributes(rs);
            NamingEnumeration<SearchResult> anser = ctx.search(searchBase, searchFilter, sc);
            if(anser.hasMoreElements()){
                SearchResult sr = anser.next();
                return sr.getNameInNamespace();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    private static boolean searchAuthorization(LDAPConnection conn, String personBaseDn, String assetCn, String accountCn){
        try {
            LdapContext ctx = conn.getLdapContext();
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            
            String searchFilter = "(objectclass=simp-authorization)";
            String searchBase = personBaseDn;
            
            String rs[] = {
                    "cn",
                    "name",
                    "simp-authorize-account-rdn"
            };
            
            sc.setReturningAttributes(rs);
            NamingEnumeration<SearchResult> anser = ctx.search(searchBase, searchFilter, sc);
            while(anser.hasMoreElements()){
                SearchResult sr = anser.next();
                Attributes attrs = sr.getAttributes();
                Attribute authorize = attrs.get("simp-authorize-account-rdn");
                if(authorize != null){
                    Object objectAuthorize = authorize.get();
                    if(objectAuthorize != null){
                        if(objectAuthorize.toString().startsWith("cn="+accountCn+",cn="+assetCn)){
                            return true;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
}
