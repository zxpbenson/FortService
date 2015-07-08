
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

public class Asset {
    
    public static void main(String[] args){
        //System.out.println(getAccountPassword("Asset_003B3A9AA","rtbridge"));
        //System.out.println(getAccountPassword("Asset_002E97B0C","$user"));
        //System.out.println(getAccountPassword("Asset_1316159995894567","root"));
        //System.out.println(getAccountPassword("Asset_1316159996475177","root"));
        //System.out.println(getAccountPassword("Asset_1351712111964296","rtbridge"));
        if(args.length == 3){
            System.out.println(getAccountPassword(args[0],args[1],Boolean.parseBoolean(args[2])));
        }
        if(args.length == 2){
            System.out.println(getAccountPassword(args[0],args[1]));
        }
    }
    
    public static String getAccountPassword(String assetCn, String assetAccount){
        return getAccountPassword(assetCn, assetAccount, false);
    }
    
    public static String getAccountPassword(String assetCn, String assetAccount, boolean fortEnv){
    	LDAPConnection conn = LDAPEnv.getLDAPConnection(fortEnv);
        try{
            String[] assetBaseDnAndIpPort = getAssetBaseDnAndIpPort(conn, assetCn);
            if(assetBaseDnAndIpPort != null){
                String assetPasswordInSecurityText = getAssetPasswordInSecurityText(conn, assetBaseDnAndIpPort[2], assetAccount);
                if(assetPasswordInSecurityText != null){
                    return "ASSET_IP:"+assetBaseDnAndIpPort[0]+" ASSET_PORT:"+assetBaseDnAndIpPort[1]+" DECRYPTION_PASSWORD:"+FortAppendDecryption.decrypPwdForAssetAccount(assetPasswordInSecurityText);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            conn.close();
        }
        return null;
    }
    
    private static String[] getAssetBaseDnAndIpPort(LDAPConnection conn, String assetCn){
        try {
            LdapContext ctx = conn.getLdapContext();
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            
            String searchFilter = "(&(objectclass=simp-asset)(cn="+ assetCn +"))";
            String searchBase = "ou=Groups,dc=simp,dc=com";
            
            String rs[] = {
                    "cn",
                    "name",
                    "simp-asset-type",
                    "simp-asset-ip",
                    "simp-asset-connector-ip",
                    "simp-asset-connector-os-port",
                    "simp-asset-connector-os-manager",
                    "simp-asset-connector-os-manager-pwd" 
            };
            sc.setReturningAttributes(rs);
            NamingEnumeration<SearchResult> anser = ctx.search(searchBase, searchFilter, sc);
            if(anser.hasMoreElements()){
                SearchResult sr = anser.next();
                Attributes attrs = sr.getAttributes();
                Attribute attributeAssetType = attrs.get("simp-asset-type");
                if(attributeAssetType != null){
                    Object objectAssetType = attributeAssetType.get();
                    if(objectAssetType != null){
                        String assetType = objectAssetType.toString();
                        if("linux".equals(assetType)||"unix".equals(assetType)||"aix".equals(assetType)){
                            String ip =  attrs.get("simp-asset-connector-ip").get().toString();
                            String port =  attrs.get("simp-asset-connector-os-port").get().toString();
                            return new String[]{ip, port, sr.getNameInNamespace()};
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    private static String getAssetPasswordInSecurityText(LDAPConnection conn, String assetBaseDn, String assetAccount){
        try {
            LdapContext ctx = conn.getLdapContext();
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            
            String searchFilter = "(&(objectclass=simp-account)(cn="+ assetAccount +"))";
            String searchBase = assetBaseDn;
            
            String rs[] = {
                    "cn",
                    "name",
                    "simp-account-home",
                    "simp-account-pwd"
            };
            sc.setReturningAttributes(rs);
            NamingEnumeration<SearchResult> anser = ctx.search(searchBase, searchFilter, sc);
            if(anser.hasMoreElements()){
                SearchResult sr = anser.next();
                Attributes attrs = sr.getAttributes();
                Attribute attributeAccountPwd = attrs.get("simp-account-pwd");
                if(attributeAccountPwd != null){
                    Object objectAccountPwd = attributeAccountPwd.get();
                    if(objectAccountPwd != null){
                        return objectAccountPwd.toString();
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
}
