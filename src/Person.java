
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;


public class Person {
    
    public static void main(String[] args){
        //System.out.println(authentication("zhangke","123"));
        if(args.length == 3){
            System.out.println(authentication(args[0],args[1],Boolean.parseBoolean(args[2])));
        }
        if(args.length == 2){
            System.out.println(authentication(args[0],args[1]));
        }
    }
    
    public static boolean authentication(String accountCn, String password){
        return authentication(accountCn, password, false);
    }
    
    public static boolean authentication(String personCn, String password, boolean fortEnv){
        LDAPConnection conn = LDAPEnv.getLDAPConnection(fortEnv);
        try{
            String personPasswordInSecurityText = getAssetPasswordInSecurityText(conn, personCn);
            if(personPasswordInSecurityText != null){
                String  personPassword = FortAppendDecryption.decrypPwdForAssetAccount(personPasswordInSecurityText);
                if(personPassword != null){
                    return personPassword.equals(password);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            conn.close();
        }
        return false;
    }
    
    private static String getAssetPasswordInSecurityText(LDAPConnection conn, String personCn){
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
                Attributes attrs = sr.getAttributes();
                Attribute passwordAttribute = attrs.get("simp-pwd");
                if(passwordAttribute != null){
                    Object passwordObj = passwordAttribute.get();
                    if(passwordObj != null){
                        return passwordObj.toString();
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
}
