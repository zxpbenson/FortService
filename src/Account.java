import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import com.fortappend.fortservice.client.vo.BaseResourceVo;
import com.fortappend.fortservice.client.vo.FlagEnum;
import com.fortappend.fortservice.client.vo.ReadAuthorizationRequestVo;
import com.fortappend.fortservice.client.vo.ReadAuthorizationResponseVo;
import com.google.gson.Gson;

public class Account {

    public static void main(String[] args){
        System.out.println(listAllAuthorizationAccountForSocket("{\"personAccount\":\"zhangke\",\"operation\":\"readAuthorization\",\"fortEnv\":\"false\"}"));
        
        if(args.length == 1){
            System.out.println(listAllAuthorizationAccount(args[0]));
        }
        if(args.length == 2){
            System.out.println(listAllAuthorizationAccount(args[0], Boolean.parseBoolean(args[1])));
        }
        
    }
    
    public static String listAllAuthorizationAccountForSocket(String json){
        Gson gson = new Gson();
        ReadAuthorizationRequestVo request = gson.fromJson(json, ReadAuthorizationRequestVo.class);
        List<String[]> personAssetAccountList = listAllAuthorizationAccount(request.getPersonAccount(), Boolean.valueOf(request.getFortEnv()));
        ReadAuthorizationResponseVo response = new ReadAuthorizationResponseVo();
        if(personAssetAccountList == null){
            response.setFlag(FlagEnum.PERSON_NOT_EXISTS.getName());
        }else{
            if(personAssetAccountList.size() == 0){
                response.setFlag(FlagEnum.NO_AUTHORIZATION.getName());
            }else{
                response.setFlag(FlagEnum.SUCCESS.getName());
                BaseResourceVo[] resultSet = new BaseResourceVo[personAssetAccountList.size()];
                response.setResultSet(resultSet);
                for(int index = 0; index < resultSet.length; index++){
                    String[] pAA = personAssetAccountList.get(index);
                    BaseResourceVo result = new BaseResourceVo();
                    result.setPersonAccount(pAA[0]);
                    result.setResourceIp(pAA[1]);
                    result.setResourceAccount(pAA[2]);
                    resultSet[index] = result;
                }
            }
        }
        return gson.toJson(response, ReadAuthorizationResponseVo.class);
    }
    
    public static List<String[]> listAllAuthorizationAccount(String personCn){
        return listAllAuthorizationAccount(personCn, false);
    }
    
    public static List<String[]> listAllAuthorizationAccount(String personCn, boolean fortEnv){
        LDAPConnection conn = LDAPEnv.getLDAPConnection(fortEnv);
        List<String[]> resourceList = new ArrayList<String[]>();
        try{
            String personBaseDn = getPersonBaseDn(conn, personCn);
            if(personBaseDn != null){
                Map<String, Set<String>> authorizationAssetAndAccount = searchAuthorizationAssetAndAccount(conn, personBaseDn);
                for(String assetCn : authorizationAssetAndAccount.keySet()){
                    String assetId = assetCn;
                    if(assetCn.indexOf("=")>0){
                        assetId = assetCn.substring(assetCn.indexOf("=") + 1);
                    }
                    String[] assetInfo = Asset.getAssetBaseDnAndIpPort(conn, assetId);
                    if(assetInfo != null){
                        Set<String> accountSet = authorizationAssetAndAccount.get(assetCn);
                        for(String accountCn : accountSet){
                            String accountId = accountCn;
                            if(accountCn.indexOf("=")>0){
                                accountId = accountCn.substring(accountCn.indexOf("=") + 1);
                            }
                            String[] personAssetAccount = new String[3];
                            personAssetAccount[0] = personCn;
                            personAssetAccount[1] = assetInfo[0];
                            personAssetAccount[2] = accountId;
                            resourceList.add(personAssetAccount);
                        }
                    }else{
                        System.out.println("Assset not exists " + assetId);
                    }
                }
            }else{
                return null;
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            conn.close();
        }
        return resourceList;
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
    
    private static Map<String, Set<String>> searchAuthorizationAssetAndAccount(LDAPConnection conn, String personBaseDn){
        Map<String, Set<String>> assetMap = new HashMap<String, Set<String>>();
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
                        String simpAuthorizeAccountRdnStr = objectAuthorize.toString();
                        String accountCn = null;
                        String assetCn = null;
                        
                        int douIndex = simpAuthorizeAccountRdnStr.indexOf(",");
                        if(douIndex > 0){
                            accountCn = simpAuthorizeAccountRdnStr.substring(0, douIndex);
                            simpAuthorizeAccountRdnStr = simpAuthorizeAccountRdnStr.substring(douIndex + 1);
                            douIndex = simpAuthorizeAccountRdnStr.indexOf(",");
                            if(douIndex > 0){
                                assetCn = simpAuthorizeAccountRdnStr.substring(0, douIndex);
                            }
                        }
                        
                        if(accountCn != null && assetCn != null){
                            Set<String> accountSet = assetMap.get(assetCn);
                            if(accountSet == null){
                                accountSet = new HashSet<String>();
                                assetMap.put(assetCn, accountSet);
                            }
                            accountSet.add(accountCn);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assetMap;
    }
}
